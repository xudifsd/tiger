package cfg.stm;

import cfg.Visitor;

public class NewIntArray extends T {
	public String dst;
	public cfg.operand.T exp;

	public NewIntArray(String dst, cfg.operand.T exp) {
		this.dst = dst;
		this.exp = exp;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return dst + " = new int[" + exp + "]";
	}
}
