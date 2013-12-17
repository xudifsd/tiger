package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Iastore extends T {
	public Iastore() {
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}