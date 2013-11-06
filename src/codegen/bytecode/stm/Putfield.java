package codegen.bytecode.stm;

import java.util.LinkedList;

import codegen.bytecode.Visitor;

public class Putfield extends T {
	public String classId;
	public String id;
	public codegen.bytecode.type.T type;
	public LinkedList<T> assignList;

	public Putfield(String classId, String id, codegen.bytecode.type.T type, LinkedList<T> assignList) {
		this.classId = classId;
		this.id = id;
		this.type = type;
		this.assignList = assignList;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}