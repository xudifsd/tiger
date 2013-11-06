package ast.stm;

public class If extends T {
	public ast.exp.T condition;
	public T thenn;
	public T elsee;
	public int lineno;

	public If(ast.exp.T condition, T thenn, T elsee, int lineno) {
		this.condition = condition;
		this.thenn = thenn;
		this.elsee = elsee;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
