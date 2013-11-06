package ast.exp;

public class Num extends T {
	public int num;
	public int lineno;

	public Num(int num, int lineno) {
		this.num = num;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
