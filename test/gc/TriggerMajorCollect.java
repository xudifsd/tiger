// This test case tests the normal case that trigger major
// collect by minor collect
class TriggerMajorCollect {
    public static void main(String[] args) {
        System.out.println(new Test().doTest());
    }
}

class Test {
    public int doTest() {
        int[] huge;
        int[] medium;
        int[] tiny;
        AllocateArray aa;

        aa = new AllocateArray();

        huge = aa.allocate(8000);
        medium = aa.allocate(2000);
        tiny = aa.allocate(1000);// trigger major gc

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
