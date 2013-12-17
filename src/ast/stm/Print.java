package ast.stm;

public class Print extends T {
	public ast.exp.T exp;
	public int lineno;

	public Print(ast.exp.T exp, int lineno) {
		this.exp = exp;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
