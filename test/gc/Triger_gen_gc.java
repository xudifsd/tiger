class Triger_gen_gc {
    public static void main(String[] args) {
        System.out.println(new Starter().start());
    }
}

class Starter {
    public int start() {
        Node node;
        int dummy;
        node = new Node();
        dummy = node.setLeft(new Node());
        dummy = node.setRight(new Node());
        return 1;
    }
}

class Node {
    Node left;
    Node right;
    public int setLeft(Node node) {
        left = node;
        return 1;
    }
    public int setRight(Node node) {
        right = node;
        return 1;
    }
}
