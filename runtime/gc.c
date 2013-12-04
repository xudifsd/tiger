#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/time.h>
#include <unistd.h>
#include <sys/mman.h>
#include "control.h"
#include "runtime.h"

#ifdef __APPLE__
#define GET_STACK_ARG_ADDRESS(base, index) (((void *)base)-(index)*sizeof(void *))
#else
#define GET_STACK_ARG_ADDRESS(base, index) (((void *)base)+(index)*sizeof(void *))
#endif

void log(const char *fmt, ...) {
    if (gcLog) {
        va_list args;
        va_start(args, fmt);
        vfprintf(gc_log_output, fmt, args);
        va_end(args);
        fputc('\n', gc_log_output);
        fflush(gc_log_output);
    }
}

void *xmalloc(int size) {
    void *result = malloc(size);
    if (result == NULL) {
        fprintf(stderr, "fatal: malloc returns NULL\n");
        exit(1);
    }
    return result;
}

struct young_gen {
    unsigned long size; // in bytes, both `from` and `to` heap have this size of space
    void *from;
    void *from_free;
    void *to;
    void *to_free;
    void *to_scaned;
};

#define PROMOTE_THRESHOLD 5
// when a obj has being scanned for PROMOTE_THRESHOLD times
// we promote it to old gen

struct old_gen {
    void *start;
    void *free;
    void *scaned; // used for move young_gen obj to old_gen
    unsigned long size; // heap size
    unsigned long available_size;
    // at first we will mmap(2) a lot of memory, but we won't use them all
    struct timeval last_major_collect_time; // record last time we did major collect
    unsigned int times_of_seeing_unused_last_two_page;
    // we keep track of times we seeing last two page unused, we this reach
    // threshold we will `free` last two page;
};

#define MAJOR_COLLECT_TIME_INTERVAL_SEC_THRESHOLD 2
// we won't do major collect too frequently

#define FREE_TAIL_PAGE_THRESHOLD 2
// when we see the last two page is unused for FREE_TAIL_PAGE_THRESHOLD times
// we `free` them, but don't `free` head OLD_GEN_SIZE_IN_PAGE*page_size
// in old heap

int page_size; // this is used to record page size of running machine
struct young_gen young_gen_heap;
struct old_gen old_gen_heap;

#define YOUNG_GEN_SIZE_IN_PAGE 2
#define OLD_GEN_SIZE_IN_PAGE 8

void Tiger_heap_init() {
    page_size = getpagesize();
    size_t young_gen_size = page_size*YOUNG_GEN_SIZE_IN_PAGE;
    void *from = mmap(NULL, young_gen_size, PROT_READ|PROT_WRITE,
                        MAP_ANON|MAP_PRIVATE, 0, 0);
    void *to = mmap(NULL, young_gen_size, PROT_READ|PROT_WRITE,
                        MAP_ANON|MAP_PRIVATE, 0, 0);
    if (from == MAP_FAILED || to == MAP_FAILED) {
        fprintf(stderr, "failed to initializing young gen heap\n");
        exit(2);
    }

    memset(&young_gen_heap, 0, sizeof(struct young_gen));
    young_gen_heap.size = young_gen_size;
    young_gen_heap.from = from;
    young_gen_heap.from_free = from;
    young_gen_heap.to = to;
    young_gen_heap.to_free = to;
    young_gen_heap.to_scaned = to;

    // initializing old gen heap
    void *old_gen_start = NULL;
    // get max size divisible by page_size
    size_t size = ((size_t)4*1024*1024*1024-1) & (~((size_t)(page_size-1)));

    /* *
     * We're tring to get as much space as possible.
     * Note, you're OS should support this kind of lazy
     * allocation of memory, or you should curse the author
     * of you're OS kernel.
     */
    for (; size > 0; size -= 2*page_size) {
        old_gen_start = mmap(NULL, size, PROT_READ|PROT_WRITE,
                            MAP_ANON|MAP_PRIVATE, 0, 0);
        if (old_gen_start != MAP_FAILED)
            break;
    }
    if (old_gen_start == MAP_FAILED ||
            size < OLD_GEN_SIZE_IN_PAGE*page_size) {
        fprintf(stderr, "failed to initializing old gen heap\n");
        exit(3);
    }
    memset(&old_gen_heap, 0, sizeof(struct old_gen));
    old_gen_heap.start = old_gen_start;
    old_gen_heap.free = old_gen_start;
    old_gen_heap.scaned = old_gen_start;
    old_gen_heap.size = size;
    size_t available_size = OLD_GEN_SIZE_IN_PAGE*page_size;
    old_gen_heap.available_size = available_size;
    int rv = madvise(((unsigned long)old_gen_start)+available_size,
                size-available_size,
                MADV_DONTNEED);
    if (rv) {
        perror("madvise");
        exit(4);
    }
    log("info: allocated %lu in old heap", (unsigned long)size);
}

void *gc_frame_prev = NULL;

// following is unmodified
void forward(struct node **head, struct node **tail, void **p) {
    struct __tiger_obj_header **root;
    root = (struct __tiger_obj_header **)p;
    struct __tiger_obj_header *to_be_process = *root;

    if (to_be_process == NULL) {
        log("fatal: 0x%lx to_be_process is NULL, this couldn't happen");
        return;
    }
    if (to_be_process->__forwarding >= heap.to &&
            to_be_process->__forwarding < heap.toNext) {
        /* already being processed */
        log("debug: 0x%lx 's content 0x%lx already being processed", (unsigned long)root, (unsigned long)to_be_process);
        *root = to_be_process->__forwarding;
        return;
    }
    log("debug: processing 0x%lx have content 0x%lx", (unsigned long)root, (unsigned long)to_be_process);
    long size = sizeof(struct __tiger_obj_header);
    struct vtable_header *vtable = (struct vtable_header *)to_be_process->__u.vptr;
    switch (to_be_process->__obj_or_array) {
        case 0:
            /* obj */
            size += strlen(vtable->__class_gc_map) * sizeof(void *); // TODO makes compiler to generate this info
            break;
        case 1:
            /* array */
            size += to_be_process->__u.length * sizeof(int);
            break;
        default:
            fprintf(stderr, "fatal, unknow type of tiger obj\n");
            exit(4);
    }
    memcpy(heap.toNext, to_be_process, size);
    *root = to_be_process->__forwarding = heap.toNext;
    heap.toNext += size;
    if (to_be_process->__obj_or_array == 0) {
        /* obj */
        void **next = (void *)to_be_process + sizeof(struct __tiger_obj_header);
        const char *c;
        int index;
        for (index = 0, c = vtable->__class_gc_map;
                *c != '\0';
                c++, index++) {
            if (*c == '1') {
                log("debug: add 0x%lx to to-do list in the body of 0x%lx",
                        (unsigned long)next + index*sizeof(void *),
                        (unsigned long)to_be_process);
                append(head, tail, (void *)next + index*sizeof(void *));
            }
        }
    }
}

void process_list(struct node **head, struct node **tail) {
    while (*head != NULL ||
            *tail != NULL) {
        void **to_be_process = pop(head, tail);
        forward(head, tail, to_be_process);
    }
}

double get_time_diff(struct timeval end, struct timeval start) {
    return end.tv_sec - start.tv_sec + (end.tv_usec/1000000.0 - start.tv_usec/1000000.0);
}

void Tiger_gc() {
    static int round = 0;
    struct timeval start, end;
    long size_before_gc;
    gettimeofday(&start, NULL);
    size_before_gc = heap.fromFree - heap.from;

    struct node *head = NULL;
    struct node *tail = NULL;

    if (!gc_frame_prev) {
        fprintf(stderr, "fatal no stack but Tiger_gc was called\n");
        exit(5);
    }

    struct gc_frame_header *stack_top = gc_frame_prev;
    for (; stack_top;
            stack_top = stack_top->__prev) {
        if (stack_top->__arguments_gc_map != NULL) {
            char *p = stack_top->__arguments_gc_map;
            int index = 0;
            log("debug: deallocating arguments, __arguments_gc_map is '%s'", stack_top->__arguments_gc_map);
            for (; *p != '\0'; p++, index++) {
                if (*p == '1') {
                    log("debug: using GET_STACK_ARG_ADDRESS of index %d", index);
                    append(&head, &tail, GET_STACK_ARG_ADDRESS(stack_top->__arguments_base_address, index));
                }
            }
        }
        if (stack_top->__locals_gc_number != 0) {
            log("debug: deallocating local, __locals_gc_number is %d", stack_top->__locals_gc_number);
            void **base = (void *)stack_top + sizeof(struct gc_frame_header);
            unsigned long index = 0;
            for (; index < stack_top->__locals_gc_number;
                    index++) {
                log("debug: add 0x%lx to to-do list in local of index %ld",
                        (unsigned long)base+index*sizeof(void *),
                        index);
                append(&head, &tail, (void *)base+index*sizeof(void *));
            }
        }
        process_list(&head, &tail);
    }
    void *tmp;
    tmp = heap.to;
    heap.to = heap.from;
    heap.from = tmp;

    tmp = heap.toNext;
    heap.toNext = heap.fromFree;
    heap.fromFree = tmp;
    heap.toStart = heap.toNext = heap.to;

    long size_after_gc = heap.fromFree - heap.from;
    gettimeofday(&end, NULL);
    log("info: %d round of GC: %.5fs, collected %ld bytes",
                    round,
                    get_time_diff(end, start),
                    size_before_gc - size_after_gc);
}

void *Tiger_new(void *vtable, int size) {
    int times = 0;
    for (;; times++) {
        if (heap.fromFree + size > heap.from + heap.size) {
            // no space left
            if (times == 1)
                break;
            else {
                log("debug: having only %ld bytes remains when allocating %d bytes of obj", heap.fromFree - heap.from, size);
                Tiger_gc();
                continue;
            }
        } else {
            struct __tiger_obj_header *result;
            result = (struct __tiger_obj_header *)heap.fromFree;
            /* TODO we should align it */
            heap.fromFree += size;
            memset(result, 0, size);
            result->__u.vptr = vtable;
            result->__obj_or_array = 0;//obj
            log("debug: allocated 0x%lx obj of %d bytes", (unsigned long)result, size);
            return result;
        }
    }
    fprintf(stderr, "fatal: OutOfMemory\n");
    exit(7);
}

struct __tiger_obj_header *Tiger_new_array(int length) {
    int times = 0;
    int size = length * sizeof(int) + sizeof(struct __tiger_obj_header);
    for (;; times++) {
        if (heap.fromFree + size > heap.from + heap.size) {
            // no space left
            if (times == 1)
                break;// out of memory
            else {
                log("debug: having only %ld bytes remains when allocating %d bytes of array", heap.fromFree - heap.from, size);
                Tiger_gc();
                continue;
            }
        } else {
            struct __tiger_obj_header *result;
            result = (struct __tiger_obj_header *)heap.fromFree;
            heap.fromFree += size;
            memset(result, 0, size);
            result->__u.length = length;
            result->__obj_or_array = 1;//array
            log("debug: allocated 0x%lx array of %d bytes", (unsigned long)result, size);
            return result;
        }
    }
    fprintf(stderr, "fatal: OutOfMemory\n");
    exit(7);
}
