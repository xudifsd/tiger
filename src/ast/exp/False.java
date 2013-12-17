package ast.exp;

public class False extends T {
	public int lineno;
	public False(int lineno) {
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
