// This test case test that when user asks space more than
// free spaces in young_gen_heap.to, so after 1 times of
// minor collect we need to allocate it directly in old gen.
class NoEnoughSpaceAfterMinorCollectSoToPromote {
    public static void main(String[] args) {
        System.out.println(new Test().doTest());
    }
}

class Test {
    public int doTest() {
        int i;
        i = this.test1();
        i = this.test2();
        return i;
    }

    // ask for more space than young gen
    public int test1() {
        int[] a;
        int[] b;
        int[] c;
        AllocateArray aa;
        aa = new AllocateArray();
        a = aa.allocate(1000);
        b = aa.allocate(1000);
        c = aa.allocate(1000);// trigger minor collect
        return 1;
    }

    public int test2() {
        int[] a;
        a = new int[3000];// trigger minor collect
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
