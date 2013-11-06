package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Getfield extends T {
	public String classId;
	public String id;
	public codegen.bytecode.type.T type;

	public Getfield(String classId, String id, codegen.bytecode.type.T type) {
		this.classId = classId;
		this.id = id;
		this.type = type;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}