package ast.stm;

public class While extends T {
	public ast.exp.T condition;
	public T body;
	public int lineno;

	public While(ast.exp.T condition, T body, int lineno) {
		this.condition = condition;
		this.body = body;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
