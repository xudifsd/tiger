package ast.exp;

public class Length extends T {
	public T array;
	public int lineno;

	public Length(T array, int lineno) {
		this.array = array;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
