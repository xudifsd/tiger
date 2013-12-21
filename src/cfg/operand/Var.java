package cfg.operand;

import cfg.Visitor;

public class Var extends T {
	public String id;
	public cfg.type.T type;
	public boolean isField;
	public boolean isLocal;

	public Var(String id, cfg.type.T type, boolean isField, boolean isLocal) {
		this.id = id;
		this.type = type;
		this.isField = isField;
		this.isLocal = isLocal;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return id;
	}
}
