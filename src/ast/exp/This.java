package ast.exp;

public class This extends T {
	public int lineno;

	public This(int lineno) {
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
