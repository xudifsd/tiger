// test it we handle write_barrier right
class OldObjReferenceYoungObj {
    public static void main(String[] args) {
        System.out.println(new Test().doTest());
    }
}

class Node {
    Node next;

    public int setNext(Node n) {
        next = n;
        return 1;
    }
}

class Test {
    public int doTest() {
        int[] a;
        int result;
        int i;
        AllocateArray aa;
        Node old;
        Node young;

        aa = new AllocateArray();
        i = 0;

        old = new Node();
        // trying to promote old to old gen
        while (i < 6) {
            result = this.justAllocateArray(aa, 2020);// trigger gc
            i = i + 1;
        }
        young = new Node();
        result = old.setNext(young);// have old gen pointing to new gen
        a = new int[8000];// filled old gen, trigger minor collect
        result = this.justAllocateArray(aa, 3000);
        // trigger minor collect again and trigger major collect

        young = new Node();
        result = old.setNext(young);
        result = this.justAllocateArray(aa, 3000);

        return result;
    }

    public int justAllocateArray(AllocateArray aa, int size) {
        int[] tmp;
        tmp = aa.allocate(size);
        return 1;
    }
}

class AllocateArray {
    public int[] allocate(int len) {
        int[] result;
        result = new int[len];
        return result;
    }
}
