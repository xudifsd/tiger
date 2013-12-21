package cfg.stm;

import cfg.Visitor;

public class AssignArray extends T {
	public String id;
	public cfg.operand.T index;
	public cfg.operand.T exp;
	public boolean isField;
	public boolean isLocal;

	public AssignArray(String id, cfg.operand.T index, cfg.operand.T exp, boolean isField, boolean isLocal) {
		this.id = id;
		this.index = index;
		this.exp = exp;
		this.isField = isField;
		this.isLocal = isLocal;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return id + "[" + index + "] = " + exp;
	}
}