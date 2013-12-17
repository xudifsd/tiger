#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "runtime.h"

extern void Tiger_heap_init();

int main (int argc, char **argv) {
  // Lab 4, exercise 13:
  // You should add some command arguments to the generated executable
  // to control the behaviour of your Gimple garbage collector.
  // You can use the offered function in file "control.c"
  // and "command-line.c"  
  // Your code here:
  CommandLine_doarg(argc, argv);

  // initialize the Java heap
  Tiger_heap_init();

  // enter Java code...
  Tiger_main(1);
}
