package ast.type;

import ast.Visitor;

public class Int extends T {
	public int lineno;

	public Int(int lineno) {
		this.lineno = lineno;
	}

	@Override
	public String toString() {
		return "@int";
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public int getNum() {
		return 0;
	}
}
