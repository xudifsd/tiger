package cfg.stm;

import cfg.Visitor;

public class Move extends T {
	public String dst;
	public cfg.operand.T src;
	public boolean isField; //is dst field?
	public boolean isLocal; //is dst local?
	public ast.type.T type;

	public Move(String dst, cfg.operand.T src, boolean isField, boolean isLocal, ast.type.T type) {
		this.dst = dst;
		this.src = src;
		this.isField = isField;
		this.isLocal = isLocal;
		this.type = type;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return dst + " = " + src;
	}
}
