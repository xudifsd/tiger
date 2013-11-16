package codegen.C.exp;

import codegen.C.Visitor;

public class Id extends T {
	public String id;
	public boolean isField;
	public boolean isLocal;

	public Id(String id, boolean isField, boolean isLocal) {
		this.id = id;
		this.isField = isField;
		this.isLocal = isLocal;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
		return;
	}
}
