#ifndef RUNTIME_H
#define RUNTIME_H

struct _runtime_int_array {
    int length;
    int *data;
};

void *xmalloc(int size);
void *Tiger_new(void *vtable, int size);
struct _runtime_int_array *Tiger_new_int_array(int size);
int System_out_println(int i);
#endif /* RUNTIME_H */
