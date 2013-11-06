package ast.exp;

public class Times extends T {
	public T left;
	public T right;
	public int lineno;

	public Times(T left, T right, int lineno) {
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
