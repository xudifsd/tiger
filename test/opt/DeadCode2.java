class DeadCode2 {
    public static void main(String[] a) {
        System.out.println(new Doit().doit());
    }
}

class Doit {
    public int doit() {
        int s;
        s = 100 + 20;
        return 0;
    }
}
