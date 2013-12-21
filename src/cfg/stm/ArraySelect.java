package cfg.stm;

import cfg.Visitor;

public class ArraySelect extends T {
	public String dst;
	public cfg.operand.T array;
	public cfg.operand.T index;

	public ArraySelect(String dst, cfg.operand.T array,
			cfg.operand.T index) {
		this.dst = dst;
		this.array = array;
		this.index = index;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return dst + " = " + array + "[" + index + "]";
	}
}
