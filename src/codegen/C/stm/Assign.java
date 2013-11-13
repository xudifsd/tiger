package codegen.C.stm;

import codegen.C.Visitor;

public class Assign extends T {
	public String id;
	public codegen.C.exp.T exp;
	public boolean isField;

	public Assign(String id, codegen.C.exp.T exp, boolean isField) {
		this.id = id;
		this.exp = exp;
		this.isField = isField;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
