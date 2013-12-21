#!/bin/bash

for i in `find test -type f -regex ".*\.java"`
do
    filename=`basename $i`
    classname="${filename%.*}"
    dir=`dirname $i`
    java -cp bin Tiger $i
    gcc /tmp/$filename.c runtime/runtime.c -I runtime
    ./a.out > /tmp/a

    javac $i
    java -cp $dir $classname > /tmp/b
    diff /tmp/a /tmp/b
    echo
done
