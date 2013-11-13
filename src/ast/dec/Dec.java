package ast.dec;

import ast.Visitor;

public class Dec extends T {
	public ast.type.T type;
	public String id;
	public int lineno;

	public Dec(ast.type.T type, String id, int lineno) {
		this.type = type;
		this.id = id;
		this.lineno = lineno;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
