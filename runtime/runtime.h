#include <stdio.h>
#ifndef RUNTIME_H
#define RUNTIME_H

struct _runtime_int_array {
    int length;
    int *data;
};

// all methods' gc-frame contains this header's field
struct gc_frame_header {
    void *__prev;
    char *__arguments_gc_map;
    void *__arguments_base_address;
    unsigned int __locals_gc_number;
    /* specified fields of method */
};

extern void *gc_frame_prev;

extern void Tiger_main(int dummy);
extern void *xmalloc(int size);
extern void *Tiger_new(void *vtable, int size);
extern struct _runtime_int_array *Tiger_new_int_array(int size);
extern int System_out_println(int i);
#endif /* RUNTIME_H */
