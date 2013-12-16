// test if move obj in old gen is right
class HoleInTheOldGen {
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
        result = this.justAllocateArray(aa, 3000);// will become a hole
        a = new int[3000];// allocate directly in old gen
        b = new int[2000];//allocate in young gen

        c = new int[3000];//trigger minor and major

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
