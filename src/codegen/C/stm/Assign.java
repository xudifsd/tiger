package codegen.C.stm;

import codegen.C.Visitor;

public class Assign extends T {
	public String id;
	public codegen.C.exp.T exp;
	public boolean isField;
	public boolean isLocal;
	public ast.type.T type;

	public Assign(String id, codegen.C.exp.T exp, boolean isField, boolean isLocal, ast.type.T type) {
		this.id = id;
		this.exp = exp;
		this.isField = isField;
		this.isLocal = isLocal;
		this.type = type;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
