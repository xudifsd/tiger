package ast.method;

import ast.Visitor;

public class Method extends T {
	public ast.type.T retType;
	public String id;
	public java.util.LinkedList<ast.dec.T> formals;
	public java.util.LinkedList<ast.dec.T> locals;
	public java.util.LinkedList<ast.stm.T> stms;
	public ast.exp.T retExp;
	public int lineno;

	public Method(ast.type.T retType, String id,
			java.util.LinkedList<ast.dec.T> formals,
			java.util.LinkedList<ast.dec.T> locals,
			java.util.LinkedList<ast.stm.T> stms, ast.exp.T retExp,
			int lineno) {
		this.retType = retType;
		this.id = id;
		this.formals = formals;
		this.locals = locals;
		this.stms = stms;
		this.retExp = retExp;
		this.lineno = lineno;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
