package ast.stm;

public class AssignArray extends T {
	public String id;
	public ast.exp.T index;
	public ast.exp.T exp;
	public boolean isField;

	public AssignArray(String id, ast.exp.T index, ast.exp.T exp) {
		this.id = id;
		this.index = index;
		this.exp = exp;
		this.isField = false;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
