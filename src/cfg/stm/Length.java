package cfg.stm;

import cfg.Visitor;

public class Length extends T {
	public String dst;
	public cfg.operand.T array;

	public Length(String dst, cfg.operand.T array) {
		this.dst = dst;
		this.array = array;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return dst + " = " + array + ".length";
	}
}
