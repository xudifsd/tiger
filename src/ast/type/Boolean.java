package ast.type;

import ast.Visitor;

public class Boolean extends T {
	public int lineno;

	public Boolean(int lineno) {
		this.lineno = lineno;
	}

	@Override
	public String toString() {
		return "@boolean";
	}

	@Override
	public int getNum() {
		return -1;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
