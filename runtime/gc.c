#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "runtime.h"

void *xmalloc(int size) {
    void *result = malloc(size);
    if (result == NULL) {
        fprintf(stderr, "fatal malloc returns NULL\n");
        exit(1);
    }
    return result;
}

// "new" a new object, do necessary initializations, and
// return the pointer (reference).
void *Tiger_new(void *vtable, int size) {
    void *result = xmalloc(size);
    void **pp = &vtable;
    memset(result, 0, size);
    memcpy(result, pp, sizeof(void *));
    return result;
}

struct _runtime_int_array *Tiger_new_int_array(int size) {
    struct _runtime_int_array *result = xmalloc(sizeof(struct _runtime_int_array));
    result->length = size;
    result->data = (int *)xmalloc(size * sizeof(int));
    memset(result->data, 0, size * sizeof(int));
    return result;
}
