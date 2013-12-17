package codegen.C.vtable;

import codegen.C.Visitor;

public class Vtable extends T {
	public String id; // name of the class
	public java.util.ArrayList<codegen.C.Ftuple> ms; // all methods
	public codegen.C.classs.T classs;

	public Vtable(String id, java.util.ArrayList<codegen.C.Ftuple> ms, codegen.C.classs.T classs) {
		this.id = id;
		this.ms = ms;
		this.classs = classs;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
