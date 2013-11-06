package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class NewarrayInt extends T {
	public NewarrayInt() {
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
