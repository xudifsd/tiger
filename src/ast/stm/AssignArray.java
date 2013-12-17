package ast.stm;

public class AssignArray extends T {
	public String id;
	public ast.exp.T index;
	public ast.exp.T exp;
	public boolean isField;
	public boolean isLocal;
	public int lineno;

	public AssignArray(String id, ast.exp.T index, ast.exp.T exp, int lineno) {
		this.id = id;
		this.index = index;
		this.exp = exp;
		this.isField = false;
		this.isLocal = false;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
