// This test case tests the normal case that obj being promote
// to old gen
class NormalPromote {
    public static void main(String[] args) {
        System.out.println(new Test().doTest());
    }
}

class Test {
    public int doTest() {
        int[] a;
        int[] b;
        int[] c;
        int i;
        int result;
        AllocateArray aa;
        aa = new AllocateArray();
        i = 0;
        a = aa.allocate(1000);
        b = aa.allocate(1000);
        result = 0;

        // increase times of a and b
        while (i < 6) {
            result = this.justAllocateArray(aa, 1000);// trigger gc
            i = i + 1;
        }

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
