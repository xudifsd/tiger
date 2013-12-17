#ifndef RUNTIME_H
#define RUNTIME_H
#include <stdio.h>
#include <string.h>
#include "command-line.h"
#include "control.h"

struct vtable_header {
    const char *__class_gc_map;
    /* remaining function pointer */
};

struct __tiger_obj_header {
    union {
        struct vtable_header *vptr;
        unsigned long length;
    } __u;
    unsigned long times;
    // times's top-most bit is used to indicate it's obj or array,
    // 0 for obj 1 for array, second-most bit is used to indicate
    // it's live obj or not
    void *__forwarding;
    int __data[0];
    // array field is unused if it's obj
    // about zero length array see
    // http://gcc.gnu.org/onlinedocs/gcc/Zero-Length.html
    /* other object field */
};

#define __TOPMOST_BIT_OF_UL (1ul<<(sizeof(unsigned long)*8-1))
#define __SECONDMOST_BIT_OF_UL (1ul<<(sizeof(unsigned long)*8-2))
#define __TOPTWO_BITS_OF_UL ((__TOPMOST_BIT_OF_UL)|__SECONDMOST_BIT_OF_UL)

#define SET_ARRAY_TYPE(addr) \
    (((struct __tiger_obj_header *)(addr))->times |= __TOPMOST_BIT_OF_UL)
#define GET_TYPE(addr) \
    ((((struct __tiger_obj_header *)(addr))->times & __TOPMOST_BIT_OF_UL)?1ul:0ul)
//GET_TYPE return 0 for obj, 1 for array
#define GET_TIMES(addr) \
    (((struct __tiger_obj_header *)(addr))->times & ~(__TOPTWO_BITS_OF_UL))
#define MARK(addr) \
    (((struct __tiger_obj_header *)(addr))->times |= __SECONDMOST_BIT_OF_UL)
#define UNMARK(addr) \
    (((struct __tiger_obj_header *)(addr))->times &= ~__SECONDMOST_BIT_OF_UL)
#define IS_MARKED(addr) \
    ((((struct __tiger_obj_header *)(addr))->times & __SECONDMOST_BIT_OF_UL)?1ul:0ul)

// all methods' gc-frame contains this header's field
struct gc_frame_header {
    void *__prev;
    char *__arguments_gc_map;
    void *__arguments_base_address;
    unsigned long __locals_gc_number;
    /* specified fields of method, contains only reference */
};

extern struct gc_frame_header *gc_frame_prev;

extern void write_barrier(void *old_obj, void *new_obj);
extern void Tiger_main(long dummy);
extern struct __tiger_obj_header *Tiger_new(void *vtable, int size);
extern struct __tiger_obj_header *Tiger_new_array(int size);
extern int System_out_println(int i);
#endif /* RUNTIME_H */
