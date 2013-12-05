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

void write_log(const char *fmt, ...) {
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

void die(const char *fmt, ...) {
    fprintf(stderr, "fatal: ");
    va_list args;
    va_start(args, fmt);
    vfprintf(stderr, fmt, args);
    va_end(args);
    fputc('\n', stderr);
    fflush(stderr);
    exit(1);
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

static unsigned long round_down_to_page_boundary(unsigned long size) {
    return size & (~((unsigned long)(page_size-1)));
}

void Tiger_heap_init() {
    page_size = getpagesize();
    size_t young_gen_size = page_size*YOUNG_GEN_SIZE_IN_PAGE;
    void *from = mmap(NULL, young_gen_size, PROT_READ|PROT_WRITE,
                        MAP_ANON|MAP_PRIVATE, 0, 0);
    void *to = mmap(NULL, young_gen_size, PROT_READ|PROT_WRITE,
                        MAP_ANON|MAP_PRIVATE, 0, 0);
    if (from == MAP_FAILED || to == MAP_FAILED)
        die("failed to initializing young gen heap");

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
    size_t size = round_down_to_page_boundary((size_t)4*1024*1024*1024-1);

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
            size < OLD_GEN_SIZE_IN_PAGE*page_size)
        die("failed to initializing old gen heap");

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
    write_log("info: allocated %lu in old heap", (unsigned long)size);
}

static int in_range(void *target, void *start, unsigned long size) {
    return (start < target && target < ((char *)start) + size)? 1: 0;
}

struct gc_frame_header  *gc_frame_prev = NULL;

struct node {
    void *old_obj;
    void *young_obj;
    struct node *next;
};

/* *
 * This is a linked list, it's sorted by old_obj ascending.
 * This is required by minor collect, because we need the
 * root info from old generation, why we use linked list to
 * store this info? Because there few old object have reference
 * to new object, and linked list is the most liable data structure
 * to implement.
 * */
struct node *root_from_old_gen = NULL;

struct node *new_node(void *old_obj, void *young_obj, struct node *next) {
    struct node *result;
    result = malloc(sizeof(struct node));
    result->old_obj = old_obj;
    result->young_obj = young_obj;
    result->next = next;
    return result;
}

void add_to_barrier(void *old_obj, void *young_obj) {
    struct node **p;
    struct node *node;
    node = root_from_old_gen;
    p = &root_from_old_gen;
    for (; node; p = &node->next, node = *p) {
        if (old_obj < node->old_obj)
            break;
        /* *
         * We can't simply discard node if old_obj is already existed,
         * because there may be two field in old obj pointing to new_obj.
         * we could ask runtime system to provide more info to let us
         * detective this sitution, but this add too much overhead, so we
         * simple let it have duplicate node in barrier. because this sitution
         * is rare, and clean duplicate node during major collection
         * */
    }
    *p = new_node(old_obj, young_obj, node);
}

void write_barrier(void *old_obj, void *young_obj) {
    if (in_range(old_obj, old_gen_heap.start, old_gen_heap.available_size) &&
            in_range(young_obj, young_gen_heap.from, young_gen_heap.size)) {
        write_log("debug: write_barrier seen assigning reference of young gen 0x%lx to old gen 0x%lx", young_obj, old_obj);
        add_to_barrier(old_obj, young_obj);
    }
}

/* *
 * Return 1 for did major collect, 0 for didn't, because of time interval
 * is too short since last time we did it.
 * */
int major_collect() {
    return 0;
}

/* *
 * Get some free space in old gen heap, if there are enough space, do nothing.
 * If there're not, we trying to do major collect to get space, if it failed
 * we then try to `allocate` some space, if this also failed, we are out of
 * memory.
 */
void prepare_free_memory(unsigned long size) {
    int tried_times;
    for (tried_times = 0; tried_times < 2; tried_times++) {
        unsigned long used;
        unsigned long remaining;

        used = (unsigned long)old_gen_heap.free - (unsigned long)old_gen_heap.start;
        remaining = old_gen_heap.available_size - used;

        if (remaining > size) {
            /* there are enough memory in old_gen_heap */
            return;
        } else {
            if (major_collect())
                continue;
            else if (old_gen_heap.available_size == old_gen_heap.size ||
                    used + size > old_gen_heap.size)
                break;
            else {
                unsigned long to_alloc;
                to_alloc = round_down_to_page_boundary(size);
                if (to_alloc != size)
                    to_alloc += page_size;
                if (madvise((char *)old_gen_heap.start + old_gen_heap.available_size,
                            to_alloc, MADV_WILLNEED)) {
                    die("failed in madvise when trying to allocate %ld bytes, start is 0x%lx, available_size is %ld, obj we trying to fit in old gen has %ld bytes",
                            to_alloc,
                            (unsigned long)old_gen_heap.start,
                            old_gen_heap.available_size,
                            size);
                }
                old_gen_heap.available_size += to_alloc;
            }
        }
    }
    die("OutOfMemory");
}

void *promote(void *addr, unsigned long size) {
    write_log("debug: tring to promote 0x%lx obj of %d bytes to old gen", (unsigned long)addr, size);
    prepare_free_memory(size);
    //TODO we should do some align here
    memcpy(old_gen_heap.free, addr, size);
    void *result = old_gen_heap.free;
    old_gen_heap.free = (char *)old_gen_heap.free + size;
    return result;
}

struct __tiger_obj_header *alloc_obj_in_old_gen_heap(void *vtable, int size) {
    prepare_free_memory(size);
    struct __tiger_obj_header *result;
    //TODO we should do some align here
    result = (struct __tiger_obj_header *)old_gen_heap.free;
    memset(result, 0, size);

    result->__u.vptr = vtable;
    old_gen_heap.free = (char *)old_gen_heap.free + size;

    write_log("debug: allocated 0x%lx obj of %d bytes directly in old gen", (unsigned long)result, size);
    return result;
}

struct __tiger_obj_header *alloc_array_in_old_gen_heap(int length) {
    unsigned long size = length * sizeof(int) + sizeof(struct __tiger_obj_header);
    prepare_free_memory(size);
    struct __tiger_obj_header *result;
    //TODO we should do some align here
    result = (struct __tiger_obj_header *)old_gen_heap.free;
    memset(result, 0, size);

    result->__u.length = length;
    SET_ARRAY_TYPE(result);
    old_gen_heap.free = (char *)old_gen_heap.free + size;

    write_log("debug: allocated 0x%lx array of %d bytes directly in old gen", (unsigned long)result, size);
    return result;
}

// following is unmodified
void forward(struct node **head, struct node **tail, void **p) {
    struct __tiger_obj_header **root;
    root = (struct __tiger_obj_header **)p;
    struct __tiger_obj_header *to_be_process = *root;

    if (to_be_process == NULL) {
        write_log("fatal: 0x%lx to_be_process is NULL, this couldn't happen");
        return;
    }
    if (to_be_process->__forwarding >= heap.to &&
            to_be_process->__forwarding < heap.toNext) {
        /* already being processed */
        write_log("debug: 0x%lx 's content 0x%lx already being processed", (unsigned long)root, (unsigned long)to_be_process);
        *root = to_be_process->__forwarding;
        return;
    }
    write_log("debug: processing 0x%lx have content 0x%lx", (unsigned long)root, (unsigned long)to_be_process);
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
                write_log("debug: add 0x%lx to to-do list in the body of 0x%lx",
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

void minor_collect() {
    static int round = 0;
    struct timeval start, end;
    long size_before_gc;
    gettimeofday(&start, NULL);
    size_before_gc = heap.fromFree - heap.from;

    struct node *head = NULL;
    struct node *tail = NULL;

    if (!gc_frame_prev) {
        fprintf(stderr, "fatal no stack but minor_collect was called\n");
        exit(5);
    }

    struct gc_frame_header *stack_top = gc_frame_prev;
    for (; stack_top;
            stack_top = stack_top->__prev) {
        if (stack_top->__arguments_gc_map != NULL) {
            char *p = stack_top->__arguments_gc_map;
            int index = 0;
            write_log("debug: deallocating arguments, __arguments_gc_map is '%s'", stack_top->__arguments_gc_map);
            for (; *p != '\0'; p++, index++) {
                if (*p == '1') {
                    write_log("debug: using GET_STACK_ARG_ADDRESS of index %d", index);
                    append(&head, &tail, GET_STACK_ARG_ADDRESS(stack_top->__arguments_base_address, index));
                }
            }
        }
        if (stack_top->__locals_gc_number != 0) {
            write_log("debug: deallocating local, __locals_gc_number is %d", stack_top->__locals_gc_number);
            void **base = (void *)stack_top + sizeof(struct gc_frame_header);
            unsigned long index = 0;
            for (; index < stack_top->__locals_gc_number;
                    index++) {
                write_log("debug: add 0x%lx to to-do list in local of index %ld",
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
    write_log("info: %d round of GC: %.5fs, collected %ld bytes",
                    round,
                    get_time_diff(end, start),
                    size_before_gc - size_after_gc);
}

struct __tiger_obj_header *Tiger_new(void *vtable, int size) {
    int times = 0;
    for (;; times++) {
        if (heap.fromFree + size > heap.from + heap.size) {
            // no space left
            if (times == 1)
                break;
            else {
                write_log("debug: having only %ld bytes remains when allocating %d bytes of obj", heap.fromFree - heap.from, size);
                minor_collect();
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
            write_log("debug: allocated 0x%lx obj of %d bytes", (unsigned long)result, size);
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
                write_log("debug: having only %ld bytes remains when allocating %d bytes of array", heap.fromFree - heap.from, size);
                minor_collect();
                continue;
            }
        } else {
            struct __tiger_obj_header *result;
            result = (struct __tiger_obj_header *)heap.fromFree;
            heap.fromFree += size;
            memset(result, 0, size);
            result->__u.length = length;
            result->__obj_or_array = 1;//array
            write_log("debug: allocated 0x%lx array of %d bytes", (unsigned long)result, size);
            return result;
        }
    }
    fprintf(stderr, "fatal: OutOfMemory\n");
    exit(7);
}
