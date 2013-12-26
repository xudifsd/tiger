class CopyProp {
    public static void main(String[] a) {
        System.out.println(new Doit().doit(10));
    }
}

class Doit {
    public int doit(int a) {
        int x;
        int y;

        x = a;
        y = x + 2;

        return y;
    }
}
