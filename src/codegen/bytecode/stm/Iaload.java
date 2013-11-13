package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Iaload extends T {
	public Iaload() {
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}