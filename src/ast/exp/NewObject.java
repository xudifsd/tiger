package ast.exp;

public class NewObject extends T {
	public String id;
	public int lineno;

	public NewObject(String id, int lineno) {
		this.id = id;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
