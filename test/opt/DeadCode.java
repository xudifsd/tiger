class DeadCode {
    public static void main(String[] a) {
        System.out.println(new Doit().doit());
    }
}

class Doit {
    public int doit() {
        if (true)
            System.out.println(1);
        else
            System.out.println(0);

        if (false)
            System.out.println(0);
        else
            System.out.println(1);

        if (false && true)
            System.out.println(0);
        else
            System.out.println(1);

        while (false)
            System.out.println(0);
        return 0;
    }
}
