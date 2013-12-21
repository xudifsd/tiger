package cfg.stm;

import cfg.Visitor;

public class Sub extends T {
	public String dst;
	public cfg.operand.T left;
	public cfg.operand.T right;

	public Sub(String dst, cfg.operand.T left,
			cfg.operand.T right) {
		this.dst = dst;
		this.left = left;
		this.right = right;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return dst + " = " + left + " - " + right;
	}
}
