// compile it using
// $ java Tiger -codegen C -auot ../test/Triger_gc2.java
// and run it using
// $ ./a.out @tiger -heapSize 1400 -gcLog true @
class T {
    public static void main(String[] args) {
        System.out.println(new Test().foo(3));
    }
}

class Test {
    public int foo(int j) {
        int i;
        int[] data;
        AllocateArray aa;
        i = 0;
        while (i < j) {
            data = new int[100];
            aa = new AllocateArray();
            i = i + aa.allocate(data);//trigger gc
        }
        return 1;
    }
}

class AllocateArray {
    int[] array;
    public int allocate(int[] data) {
        array = new int[100];
        array = new int[100];
        array = new int[100];
        array = new int[100];
        return 1;
    }
}
