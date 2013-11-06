package ast.stm;

public class Block extends T {
	public java.util.LinkedList<T> stms;
	public int lineno;

	public Block(java.util.LinkedList<T> stms, int lineno) {
		this.stms = stms;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
	}
}
