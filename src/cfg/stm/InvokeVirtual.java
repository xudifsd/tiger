package cfg.stm;

import cfg.Visitor;

public class InvokeVirtual extends T {
	public String dst;
	public String obj;
	public String f;
	// type of the destination variable
	public java.util.LinkedList<cfg.operand.T> args;
	public cfg.type.T retType;
	public boolean isField;

	public InvokeVirtual(String dst, String obj, String f,
			java.util.LinkedList<cfg.operand.T> args, cfg.type.T retType, boolean isField) {
		this.dst = dst;
		this.obj = obj;
		this.f = f;
		this.args = args;
		this.retType = retType;
		this.isField = isField;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(dst + " = " + obj + "." + f);
		for (cfg.operand.T arg: args)
			sb.append(arg);
		return sb.toString();
	}
}
