class AlgSimp {
    public static void main(String[] a) {
        System.out.println(new Doit().doit());
    }
}

class Doit {
    public int doit() {
        int x;
        int[] arr;
        x = 999;
        arr = new int[20+1];

        if ((x*0)<2)
            System.out.println(1);
        else
            System.out.println(0);

        if (false && x < 10)
            System.out.println(0);
        else
            System.out.println(1);

        x = 1+3-4*2;
        x = x + 3;
        x = x + 0;
        x = this.call(1+4-2, 1<2);
        x = arr[1+2];
        return 1+4;
    }
    public int call(int num, boolean b) {
        return 1;
    }
}
