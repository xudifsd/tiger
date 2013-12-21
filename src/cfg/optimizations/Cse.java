package cfg.optimizations;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;
import cfg.type.Boolean;

public class Cse implements cfg.Visitor {
	public cfg.program.T program;

	public Cse() {
		this.program = null;
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
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
	}

	@Override
	public void visit(cfg.stm.Lt s) {
	}

	@Override
	public void visit(cfg.stm.Move s) {
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
	}

	@Override
	public void visit(cfg.stm.Print s) {
	}

	@Override
	public void visit(cfg.stm.Sub s) {
	}

	@Override
	public void visit(cfg.stm.Times s) {
	}

	@Override
	public void visit(And and) {
	}

	@Override
	public void visit(ArraySelect arraySelect) {
	}

	@Override
	public void visit(Length length) {
	}

	@Override
	public void visit(NewIntArray newIntArray) {
	}

	@Override
	public void visit(Not not) {
	}

	@Override
	public void visit(AssignArray assignArray) {
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
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
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
		this.program = p;
	}
}
