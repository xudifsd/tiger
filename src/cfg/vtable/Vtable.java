package cfg.vtable;

import cfg.Visitor;

public class Vtable extends T {
	public String id; // name of the class
	public java.util.LinkedList<cfg.Ftuple> ms; // all methods
	public cfg.classs.T classs;

	public Vtable(String id, java.util.LinkedList<cfg.Ftuple> ms, cfg.classs.T classs) {
		this.id = id;
		this.ms = ms;
		this.classs = classs;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
