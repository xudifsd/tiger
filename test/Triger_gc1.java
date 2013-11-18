// compile it using
// $ java Tiger -codegen C -auot ../test/Triger_gc1.java
// and run it using
// $ ./a.out @tiger -heapSize 600 -gcLog true @
class T {
    public static void main(String[] args) {
        System.out.println(new Test().foo(3));
    }
}

class Test {
    public int foo(int j) {
        int i;
        AllocateArray aa;
        i = 0;
        while (i < j) {
            aa = new AllocateArray();
            i = i + aa.allocate();//trigger gc
        }
        return 1;
    }
}

class AllocateArray {
    int[] array;
    public int allocate() {
        array = new int[100];
        return 1;
    }
}
