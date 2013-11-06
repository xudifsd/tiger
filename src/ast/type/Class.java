package ast.type;

import ast.Visitor;

public class Class extends T {
	public String id;
	public int lineno;

	public Class(String id, int lineno) {
		this.id = id;
		this.lineno = lineno;
	}

	@Override
	public String toString() {
		return this.id;
	}

	@Override
	public int getNum() {
		return 2;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
