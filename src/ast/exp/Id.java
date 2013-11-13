package ast.exp;

public class Id extends T {
	public String id; // name of the id
	public ast.type.T type; // type of the id
	public boolean isField; // whether or not this is a class field
	public int lineno;

	public Id(String id, int lineno) {
		this.id = id;
		this.type = null;
		this.isField = false;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
