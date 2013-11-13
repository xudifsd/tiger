package ast.exp;

public class NewIntArray extends T {
	public T exp;
	public int lineno;

	public NewIntArray(T exp, int lineno) {
		this.exp = exp;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
