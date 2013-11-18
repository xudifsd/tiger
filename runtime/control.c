#include "control.h"
#include <stdio.h>

int Control_heapSize = 1024;
int gcLog = 0;
FILE *gc_log_output = NULL;
