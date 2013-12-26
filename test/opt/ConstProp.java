class ConstProp {
    public static void main(String[] a) {
        System.out.println(new Doit().doit());
    }
}

class Doit {
    public int doit() {
        int x;
        int y;

        x = 1;
        y = x;

        return y;
    }
}
