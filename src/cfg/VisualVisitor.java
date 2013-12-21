package cfg;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;
import cfg.type.Boolean;

public class VisualVisitor implements Visitor {
	/* *
	 * We've implemented all visualize functionality in cfg.stm.*'s toString(),
	 * so this Visitor is just a kicker to start the visualization.
	 * I know this is very dirty, but I'm too lazy to copy all the toString to
	 * this class.
	 * */
	public VisualVisitor() {
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

	// dec
	@Override
	public void visit(cfg.dec.Dec d) {
	}

	// dec
	@Override
	public void visit(cfg.block.Block b) {
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		java.util.HashMap<util.Label, cfg.block.T> map = new java.util.HashMap<util.Label, cfg.block.T>();
		for (cfg.block.T block : m.blocks) {
			cfg.block.Block b = (cfg.block.Block) block;
			util.Label label = b.label;
			map.put(label, b);
		}

		util.Graph<cfg.block.T> graph = new util.Graph<cfg.block.T>(m.classId
				+ "_" + m.id);

		for (cfg.block.T block : m.blocks) {
			graph.addNode(block);
		}
		for (cfg.block.T block : m.blocks) {
			cfg.block.Block b = (cfg.block.Block) block;
			cfg.transfer.T transfer = b.transfer;
			if (transfer instanceof cfg.transfer.Goto) {
				cfg.transfer.Goto gotoo = (cfg.transfer.Goto) transfer;
				cfg.block.T to = map.get(gotoo.label);
				graph.addEdge(block, to);
			} else if (transfer instanceof cfg.transfer.If) {
				cfg.transfer.If iff = (cfg.transfer.If) transfer;
				cfg.block.T truee = map.get(iff.truee);
				graph.addEdge(block, truee);
				cfg.block.T falsee = map.get(iff.falsee);
				graph.addEdge(block, falsee);
			}
		}
		graph.visualize();
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		/* *
		 * This method is useless, because main method has only one statement,
		 * it can't generate any output to dot
		 * */
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
		/* *
		 * Kick start the visualization.
		 * */
		for (cfg.method.T m : p.methods) {
			m.accept(this);
		}
	}

	@Override
	public void visit(Boolean boolean1) {
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
}
