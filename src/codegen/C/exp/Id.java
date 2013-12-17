package codegen.C.exp;

import codegen.C.Visitor;

public class Id extends T {
	public String id;
	public boolean isField;
	public boolean isLocal;
	public ast.type.T type;

	public Id(String id, boolean isField, boolean isLocal, ast.type.T type) {
		this.id = id;
		this.isField = isField;
		this.isLocal = isLocal;
		this.type = type;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
		return;
	}
}
