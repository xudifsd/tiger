#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <stdarg.h>
#include "control.h"
#include "runtime.h"

#define MAC
// NOTE if you're using linux other mac you should delet previous line
#ifdef MAC
#define GET_STACK_ARG_ADDRESS(base, index) ((base)-(index)*sizeof(long))
#else
#define GET_STACK_ARG_ADDRESS(base, index) ((base)+(index)*sizeof(long))
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
        fprintf(stderr, "fatal malloc returns NULL\n");
        exit(1);
    }
    return result;
}

// The Gimple Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap {
    int size;         // in bytes, note that this if for semi-heap size
    void *from;       // the "from" space pointer
    void *fromFree;   // the next "free" space in the from space
    void *to;         // the "to" space pointer
    void *toStart;    // "start" address in the "to" space
    void *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize) {
    void *m1 = xmalloc(heapSize);
    void *m2 = xmalloc(heapSize);
    heap.size = heapSize;
    heap.from = m1;
    heap.fromFree = m1;
    heap.to = m2;
    heap.toStart = m2;//TODO what this field is for?
    heap.toNext = m2;
}

void *gc_frame_prev = NULL;

//===============================================================//
// The Gimple Garbage Collector

/**
 * this struct is a litte heavy weight, but it's easy to implement
 * becuase we need to do BFS, so we will use a list to store the info
 * */
struct node {
    void **to_be_process;
    struct node *next;
};

void append(struct node **head, struct node **tail, void **to_be_process) {
    if (!(*((struct __tiger_obj_header **)to_be_process)))
        return;
    log("debug: add 0x%lx to to-do list, have content 0x%lx",
            (unsigned long)to_be_process,
            (unsigned long)(*((struct __tiger_obj_header **)to_be_process)));
    struct node *new = (struct node *)xmalloc(sizeof(struct node));
    new->to_be_process = to_be_process;
    new->next = NULL;
    if (!*head) {
        *head = *tail = new;
    } else {
        (*tail)->next = new;
        *tail = new;
    }
}

void **pop(struct node **head, struct node **tail) {
    if (!*head) {
        fprintf(stderr, "fatal pop a NULL\n");
        exit(2);
    }
    void **result = (*head)->to_be_process;
    if (*head == *tail) {
        free(*head);
        *head = *tail = NULL;
    } else {
        struct node *p = *head;
        *head = p->next;
        free(p);
    }
    return result;
}

void swap(void **p1, void **p2) {
    void *tmp;
    memcpy(&tmp, p1, sizeof(void *));
    memcpy(p1, p2, sizeof(void *));
    memcpy(p2, &tmp, sizeof(void *));
}

/**
 * this function do the actual gc, it pop a pointer from list
 * and copy it from 'from' space to 'to' space, and change the
 * root pointer.
 * */
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
    log("debug: processing ox%lx have content 0x%lx", (unsigned long)root, (unsigned long)to_be_process);
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
        void **next = to_be_process + sizeof(struct __tiger_obj_header);
        const char *c;
        int index;
        for (index = 0, c = vtable->__class_gc_map;
                *c;
                c++, index++) {
            if (*c == '1')
                append(head, tail, next + index*sizeof(void *));
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

void Tiger_gc() {
    static int round = 0;
    time_t start;
    long size_before_gc;
    start = time(NULL);
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
            for (; *p; p++, index++) {
                if (*p == '1') {
                    log("debug: deallocating arguments");
                    append(&head, &tail, GET_STACK_ARG_ADDRESS(stack_top->__arguments_base_address, index));
                }
            }
        }
        if (stack_top->__locals_gc_number != 0) {
            void *base = &stack_top->__locals_gc_number;
            //makes sure __locals_gc_number is the last know element of struct gc_frame_header
            base += sizeof(unsigned long);
            unsigned long index = 0;
            for (; index < stack_top->__locals_gc_number;
                    index++) {
                    append(&head, &tail, base+index*sizeof(void *));
            }
        }
        process_list(&head, &tail);
    }
    swap(&heap.to, &heap.from);
    swap(&heap.toNext, &heap.fromFree);
    heap.toStart = heap.toNext = heap.to;

    long size_after_gc = heap.fromFree - heap.from;
    time_t end = time(NULL);
    log("info: %d round of GC: %2fs, collected %ld bytes",
                    round,
                    difftime(end, start),
                    size_before_gc - size_after_gc);
}

//===============================================================//
// Object Model And allocation


// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   |
      |--------------|\
p---->| v_0          | \
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1;
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
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
            log("debug: allocated 0x%lx obj", (unsigned long)result);
            return result;
        }
    }
    fprintf(stderr, "fatal: OutOfMemory\n");
    exit(7);
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   |
      |--------------|\
p---->| e_0          | \
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
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
            log("debug: allocated 0x%lx array", (unsigned long)result);
            return result;
        }
    }
    fprintf(stderr, "fatal: OutOfMemory\n");
    exit(7);
}
