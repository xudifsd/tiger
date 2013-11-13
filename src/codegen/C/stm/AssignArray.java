package codegen.C.stm;

import codegen.C.Visitor;

public class AssignArray extends T {
	public String id;
	public codegen.C.exp.T index;
	public codegen.C.exp.T exp;
	public boolean isField;

	public AssignArray(String id, codegen.C.exp.T index, codegen.C.exp.T exp, boolean isField) {
		this.id = id;
		this.index = index;
		this.exp = exp;
		this.isField = isField;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
