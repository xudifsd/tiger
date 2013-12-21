package cfg.block;

import cfg.Visitor;

public class Block extends T {
	public util.Label label;
	public java.util.LinkedList<cfg.stm.T> stms;
	public cfg.transfer.T transfer;

	public Block(util.Label label, java.util.LinkedList<cfg.stm.T> stms,
			cfg.transfer.T transfer) {
		this.label = label;
		this.stms = stms;
		this.transfer = transfer;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (!(o instanceof Block))
			return false;

		Block ob = (Block) o;
		return this.label.equals(ob.label);
	}

	@Override
	public String toString() {
		StringBuffer strb = new StringBuffer();
		strb.append(this.label.toString() + ":\\n");
		for (cfg.stm.T stm: stms) {
			strb.append(stm);
			strb.append("\\n");
		}
		if (transfer instanceof cfg.transfer.Goto) {
			strb.append("Goto ");
			strb.append(((cfg.transfer.Goto) transfer).label);
		} else if (transfer instanceof cfg.transfer.If) {
			strb.append("if ");
			strb.append(((cfg.transfer.If) transfer).operand);
			strb.append(" goto ");
			strb.append(((cfg.transfer.If) transfer).truee);
			strb.append(" else goto ");
			strb.append(((cfg.transfer.If) transfer).falsee);
		} else if (transfer instanceof cfg.transfer.Return) {
			strb.append("return ");
			strb.append(((cfg.transfer.Return) transfer).operand);
		} else {
			throw new RuntimeException("unknow transfer type " + transfer);
		}
		strb.append("\\n");
		return strb.toString();
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
