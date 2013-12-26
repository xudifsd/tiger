package cfg.optimizations;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;
import cfg.type.Boolean;

public class ConstProp implements cfg.Visitor {
	public cfg.program.T program;

	private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockOut;

	private cfg.stm.T currentStm;
	private boolean inspect;
	private String dst;
	private boolean isConst;
	private int constV;

	private java.util.HashSet<cfg.stm.T> curBlockOut;

	private java.util.LinkedList<cfg.method.T> newMethods;
	private cfg.mainMethod.T mainMethod;
	private cfg.block.Block newBlock;
	private cfg.operand.T operand;

	public ConstProp() {
		this.program = null;
		this.inspect = false;
		this.isConst = false;
		this.newMethods = new java.util.LinkedList<cfg.method.T>();
	}

	// /////////////////////////////////////////////////////
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
		if (inspect) {
			isConst = true;
			constV = operand.i;
		} else {
			this.operand = operand;
		}
	}

	@Override
	public void visit(cfg.operand.Var operand) {
		if (inspect) {
			isConst = false;
			dst = operand.id;
		} else {
			int times = 0;
			boolean isConst = false;
			int constV = 0;
			for (cfg.stm.T stm : curBlockOut) {
				if (stm.equals(currentStm))
					continue;
				inspect = true;
				this.dst = null;
				this.isConst = false;
				stm.accept(this);
				inspect = false;
				if (this.dst != null && operand.id.equals(this.dst)) {
					times++;
					if (times == 2)
						break;
					if (this.isConst) {
						isConst = this.isConst;
						constV = this.constV;
					}
				}
			}
			if (times == 1 && isConst)
				this.operand = new cfg.operand.Int(constV);
			else
				this.operand = operand;
		}
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.left.accept(this);
			cfg.operand.T left = operand;
			s.right.accept(this);
			cfg.operand.T right = operand;
			this.newBlock.stms.add(new cfg.stm.Add(s.dst, left, right));
		}
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			java.util.LinkedList<cfg.operand.T> newArgList = new java.util.LinkedList<cfg.operand.T>();
			for (cfg.operand.T arg : s.args) {
				arg.accept(this);
				newArgList.add(operand);
			}
			this.newBlock.stms.add(new cfg.stm.InvokeVirtual(s.dst, s.obj, s.f,
					newArgList, s.retType, s.isField, s.isLocal));
		}
	}

	@Override
	public void visit(cfg.stm.Lt s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.left.accept(this);
			cfg.operand.T left = operand;
			s.right.accept(this);
			cfg.operand.T right = operand;
			this.newBlock.stms.add(new cfg.stm.Lt(s.dst, left, right));
		}
	}

	@Override
	public void visit(cfg.stm.Move s) {
		if (inspect) {
			this.dst = s.dst;
			s.src.accept(this);
		} else {
			currentStm = s;
			s.src.accept(this);
			this.newBlock.stms.add(new cfg.stm.Move(s.dst, operand, s.isField,
					s.isLocal, s.type));
		}
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			this.newBlock.stms.add(s);
		}
	}

	@Override
	public void visit(cfg.stm.Print s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.arg.accept(this);
			this.newBlock.stms.add(new cfg.stm.Print(operand));
		}
	}

	@Override
	public void visit(cfg.stm.Sub s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.left.accept(this);
			cfg.operand.T left = operand;
			s.right.accept(this);
			cfg.operand.T right = operand;
			this.newBlock.stms.add(new cfg.stm.Sub(s.dst, left, right));
		}
	}

	@Override
	public void visit(cfg.stm.Times s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.left.accept(this);
			cfg.operand.T left = operand;
			s.right.accept(this);
			cfg.operand.T right = operand;
			this.newBlock.stms.add(new cfg.stm.Times(s.dst, left, right));
		}
	}

	@Override
	public void visit(And s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.left.accept(this);
			cfg.operand.T left = operand;
			s.right.accept(this);
			cfg.operand.T right = operand;
			this.newBlock.stms.add(new cfg.stm.And(s.dst, left, right));
		}
	}

	@Override
	public void visit(ArraySelect s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.index.accept(this);
			this.newBlock.stms.add(new cfg.stm.ArraySelect(s.dst, s.array,
					operand));
		}
	}

	@Override
	public void visit(Length s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			this.newBlock.stms.add(s);
		}
	}

	@Override
	public void visit(NewIntArray s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			this.newBlock.stms.add(s);
		}
	}

	@Override
	public void visit(Not s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.exp.accept(this);
			this.newBlock.stms.add(new cfg.stm.Not(s.dst, operand));
		}
	}

	@Override
	public void visit(AssignArray s) {
		if (inspect) {
			return;
		} else {
			currentStm = s;
			s.index.accept(this);
			cfg.operand.T index = operand;
			s.exp.accept(this);
			cfg.operand.T exp = operand;
			this.newBlock.stms.add(new cfg.stm.AssignArray(s.id, index, exp,
					s.isField, s.isLocal));
		}
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
		curBlockOut = blockOut.get(b);
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
		blockOut = cfg.optimizations.ReachingDefinition.blockOut;

		if (blockOut == null)
			throw new RuntimeException(
					"cdf.optimizations.ConstProp should be called after cdf.optimizations.ReachingDefinition");
		p.mainMethod.accept(this);
		for (cfg.method.T m : p.methods)
			m.accept(this);
		this.program = new cfg.program.Program(p.classes, p.vtables,
				newMethods, mainMethod);
		if (control.Control.isTracing("cfg.ConstProp")) {
			System.out.println("before cfg.ConstProp optimization:");
			cfg.PrettyPrintVisitor pp = new cfg.PrettyPrintVisitor(true);
			p.accept(pp);
			System.out.println("after cfg.ConstProp optimization:");
			this.program.accept(pp);
		}
	}
}