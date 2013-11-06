package ast.exp;

public class True extends T {
	public int lineno;

	public True(int lineno) {
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
