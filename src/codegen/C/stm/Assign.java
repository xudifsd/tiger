package codegen.C.stm;

import codegen.C.Visitor;

public class Assign extends T {
	public String id;
	public codegen.C.exp.T exp;
	public boolean isField;
	public boolean isLocal;

	public Assign(String id, codegen.C.exp.T exp, boolean isField, boolean isLocal) {
		this.id = id;
		this.exp = exp;
		this.isField = isField;
		this.isLocal = isLocal;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
