package cfg.stm;

import cfg.Visitor;

public class Move extends T {
	public String dst;
	public cfg.operand.T src;

	public Move(String dst, cfg.operand.T src) {
		this.dst = dst;
		this.src = src;
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
