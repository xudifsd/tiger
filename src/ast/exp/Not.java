package ast.exp;

public class Not extends T {
	public T exp;
	public int lineno;

	public Not(T exp, int lineno) {
		this.exp = exp;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
