package ast.exp;

public class Add extends T {
	public T left;
	public T right;
	public int lineno;

	public Add(T left, T right, int lineno) {
		this.left = left;
		this.right = right;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
