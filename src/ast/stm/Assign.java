package ast.stm;

public class Assign extends T {
	public String id;
	public ast.exp.T exp;
	public ast.type.T type; // type of the id
	public boolean isField;
	public boolean isLocal;
	public int lineno;

	public Assign(String id, ast.exp.T exp, int lineno) {
		this.id = id;
		this.exp = exp;
		this.type = null;
		this.isField = false;
		this.isLocal = false;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
