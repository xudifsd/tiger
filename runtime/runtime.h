#include <stdio.h>
#include <string.h>
#ifndef RUNTIME_H
#define RUNTIME_H

struct __tiger_obj_header {
    union {
        void *vptr;
        unsigned long length;
    } __u;
    int __obj_or_array;//0 for obj 1 for array
    void *__forwarding;
    int __data[0];
    // array field is unused if it's obj
    // about zero length array see
    // http://gcc.gnu.org/onlinedocs/gcc/Zero-Length.html
    /* other object field */
};

struct vtable_header {
    const char *__class_gc_map;
    /* remaining function pointer */
};

// all methods' gc-frame contains this header's field
struct gc_frame_header {
    void *__prev;
    char *__arguments_gc_map;
    void *__arguments_base_address;
    unsigned long __locals_gc_number;
    /* specified fields of method, contains only reference */
};

extern void *gc_frame_prev;

extern void Tiger_main(long dummy);
extern void *Tiger_new(void *vtable, int size);
extern struct __tiger_obj_header *Tiger_new_array(int size);
extern int System_out_println(int i);
#endif /* RUNTIME_H */
