package cfg.optimizations;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;
import cfg.type.Boolean;

public class DeadCode implements cfg.Visitor {
	public cfg.program.T program;

	private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

	private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveIn;
	private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveOut;

	private java.util.LinkedList<cfg.method.T> newMethods;
	private cfg.mainMethod.T mainMethod;
	private cfg.block.Block newBlock;

	public DeadCode() {
		this.program = null;
		this.newMethods = new java.util.LinkedList<cfg.method.T>();
		this.mainMethod = null;
	}

	// /////////////////////////////////////////////////////
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
	}

	@Override
	public void visit(cfg.operand.Var operand) {
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.Lt s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.Move s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.Print s) {
		newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.Sub s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(cfg.stm.Times s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(And s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(ArraySelect s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(Length s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(NewIntArray s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(Not s) {
		if (stmLiveOut.get(s).contains(s.dst))
			newBlock.stms.add(s);
	}

	@Override
	public void visit(AssignArray s) {
		newBlock.stms.add(s);
	}

	// transfer
	@Override
	public void visit(cfg.transfer.If s) {
	}

	@Override
	public void visit(cfg.transfer.Goto s) {
	}

	@Override
	public void visit(cfg.transfer.Return s) {
	}

	// type
	@Override
	public void visit(cfg.type.Class t) {
	}

	@Override
	public void visit(cfg.type.Int t) {
	}

	@Override
	public void visit(cfg.type.IntArray t) {
	}

	@Override
	public void visit(Boolean boolean1) {
	}

	// dec
	@Override
	public void visit(cfg.dec.Dec d) {
	}

	// block
	@Override
	public void visit(cfg.block.Block b) {
		/* *
		 * FIXME this will only delete one statement, won't delete cascadely, if
		 * we want to delete cascadely, we have to do some sort of fix-point
		 * calculation, and reverse topo-order traverse. but I have no time.
		 */
		java.util.LinkedList<cfg.stm.T> newStms;
		newStms = new java.util.LinkedList<cfg.stm.T>();
		newBlock = new cfg.block.Block(b.label, newStms, b.transfer);
		for (cfg.stm.T stm : b.stms)
			stm.accept(this);
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		java.util.LinkedList<cfg.block.T> newBlocks = new java.util.LinkedList<cfg.block.T>();
		cfg.method.Method newMethod = new cfg.method.Method(m.retType, m.id,
				m.classId, m.formals, m.locals, newBlocks, m.entry, m.exit,
				m.retValue);
		this.newMethods.add(newMethod);
		for (cfg.block.T b : m.blocks) {
			b.accept(this);
			newBlocks.add(newBlock);
		}
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		java.util.LinkedList<cfg.block.T> newBlocks = new java.util.LinkedList<cfg.block.T>();
		mainMethod = new cfg.mainMethod.MainMethod(m.locals, newBlocks);
		for (cfg.block.T b : m.blocks) {
			b.accept(this);
			newBlocks.add(newBlock);
		}
	}

	// vtables
	@Override
	public void visit(cfg.vtable.Vtable v) {
	}

	// class
	@Override
	public void visit(cfg.classs.Class c) {
	}

	// program
	@Override
	public void visit(cfg.program.Program p) {
		stmLiveIn = LivenessVisitor.stmLiveIn;
		stmLiveOut = LivenessVisitor.stmLiveOut;
		transferLiveIn = LivenessVisitor.transferLiveIn;
		transferLiveOut = LivenessVisitor.transferLiveOut;

		if (stmLiveIn == null || stmLiveOut == null || transferLiveIn == null
				|| transferLiveOut == null)
			throw new RuntimeException(
					"cdf.optimizations.DeadCode should be called after cdf.optimizations.LivenessVisitor");

		p.mainMethod.accept(this);
		for (cfg.method.T m : p.methods)
			m.accept(this);
		this.program = new cfg.program.Program(p.classes, p.vtables,
				newMethods, mainMethod);
		if (control.Control.isTracing("cfg.DeadCode")) {
			System.out.println("before cfg.DeadCode optimization:");
			cfg.PrettyPrintVisitor pp = new cfg.PrettyPrintVisitor(true);
			p.accept(pp);
			System.out.println("after cfg.DeadCode optimization:");
			this.program.accept(pp);
		}
	}
}