package cfg;

// Traverse the C AST, and generate
// a control-flow graph.
public class TranslateVisitor implements codegen.C.Visitor {
	private cfg.type.T type; // type after translation
	private cfg.operand.T operand;
	private cfg.dec.T dec;
	// A dirty hack. Can hold stm, transfer, or label.
	private java.util.ArrayList<Object> labelOrStmOrTransfer;
	private java.util.LinkedList<cfg.dec.T> newLocals;
	private cfg.method.T method;
	private cfg.classs.T classs;
	private cfg.vtable.T vtable;
	private cfg.mainMethod.T mainMethod;
	public cfg.program.T program;

	public TranslateVisitor() {
		this.type = null;
		this.dec = null;
		this.labelOrStmOrTransfer = new java.util.ArrayList<Object>();
		this.newLocals = new java.util.LinkedList<cfg.dec.T>();
		this.method = null;
		this.classs = null;
		this.vtable = null;
		this.mainMethod = null;
		this.program = null;
	}

	// /////////////////////////////////////////////////////
	// utility functions
	private java.util.LinkedList<cfg.block.T> cookBlocks() {
		java.util.LinkedList<cfg.block.T> blocks = new java.util.LinkedList<cfg.block.T>();

		int i = 0;
		int size = this.labelOrStmOrTransfer.size();
		while (i < size) {
			util.Label label;
			cfg.block.Block b;
			java.util.LinkedList<cfg.stm.T> stms = new java.util.LinkedList<cfg.stm.T>();
			cfg.transfer.T transfer;

			if (!(this.labelOrStmOrTransfer.get(i) instanceof util.Label)) {
				new util.Error();
			}
			label = (util.Label) this.labelOrStmOrTransfer.get(i++);
			while (i < size
					&& this.labelOrStmOrTransfer.get(i) instanceof cfg.stm.T) {
				stms.add((cfg.stm.T) this.labelOrStmOrTransfer.get(i++));
			}

			Object o = this.labelOrStmOrTransfer.get(i);
			if (o instanceof cfg.transfer.T) {
				transfer = (cfg.transfer.T) this.labelOrStmOrTransfer.get(i++);
				b = new cfg.block.Block(label, stms, transfer);
				blocks.add(b);
			} else if (o instanceof util.Label) {
				b = new cfg.block.Block(label, stms, new cfg.transfer.Goto(
						(util.Label) o));
				blocks.add(b);
			} else {
				throw new RuntimeException(
						"Unknow type in labelOrStmOrTransfer");
			}
		}
		this.labelOrStmOrTransfer = new java.util.ArrayList<Object>();
		return blocks;
	}

	private void emit(Object obj) {
		this.labelOrStmOrTransfer.add(obj);
	}

	private String genVar() {
		String fresh = util.Temp.next();
		cfg.dec.Dec dec = new cfg.dec.Dec(new cfg.type.Int(), fresh);
		this.newLocals.add(dec);
		return fresh;
	}

	private String genVar(cfg.type.T ty) {
		String fresh = util.Temp.next();
		cfg.dec.Dec dec = new cfg.dec.Dec(ty, fresh);
		this.newLocals.add(dec);
		return fresh;
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(codegen.C.exp.Add e) {
		String dst = genVar();
		e.left.accept(this);
		cfg.operand.T left = this.operand;
		e.right.accept(this);
		cfg.operand.T right = this.operand;
		emit(new cfg.stm.Add(dst, left, right));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Int(), false, true);
	}

	@Override
	public void visit(codegen.C.exp.And e) {
		String dst = genVar(new cfg.type.Boolean());
		e.left.accept(this);
		cfg.operand.T left = this.operand;
		e.right.accept(this);
		cfg.operand.T right = this.operand;
		emit(new cfg.stm.And(dst, left, right));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Boolean(), false,
				true);
	}

	@Override
	public void visit(codegen.C.exp.ArraySelect e) {
		String dst = genVar();
		e.array.accept(this);
		cfg.operand.T array = this.operand;
		e.index.accept(this);
		cfg.operand.T index = this.operand;
		emit(new cfg.stm.ArraySelect(dst, array, index));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Int(), false, true);
	}

	@Override
	public void visit(codegen.C.exp.Call e) {
		// we ignore e.assign
		e.retType.accept(this);
		cfg.type.T retType = this.type;
		String dst = genVar(retType);
		String obj = null;
		e.exp.accept(this);
		cfg.operand.T objOp = this.operand;
		if (objOp instanceof cfg.operand.Var) {
			cfg.operand.Var var = (cfg.operand.Var) objOp;
			obj = var.id;
		} else {
			new util.Error();
		}

		java.util.LinkedList<cfg.operand.T> newArgs = new java.util.LinkedList<cfg.operand.T>();
		for (codegen.C.exp.T x : e.args) {
			x.accept(this);
			newArgs.add(this.operand);
		}
		emit(new cfg.stm.InvokeVirtual(dst, obj, e.id, newArgs, retType,
				((cfg.operand.Var) objOp).isField,
				((cfg.operand.Var) objOp).isLocal));
		this.operand = new cfg.operand.Var(dst, retType, false, true);
	}

	@Override
	public void visit(codegen.C.exp.Id e) {
		if (e.type instanceof ast.type.Boolean) {
			this.type = new cfg.type.Boolean();
		} else if (e.type instanceof ast.type.Int) {
			this.type = new cfg.type.Int();
		} else if (e.type instanceof ast.type.IntArray) {
			this.type = new cfg.type.IntArray();
		} else if (e.type instanceof ast.type.Class) {
			this.type = new cfg.type.Class(((ast.type.Class) e.type).id);
		}
		this.operand = new cfg.operand.Var(e.id, this.type, e.isField,
				e.isLocal);
	}

	@Override
	public void visit(codegen.C.exp.Length e) {
		String dst = genVar();
		e.array.accept(this);
		cfg.operand.T array = this.operand;

		emit(new cfg.stm.Length(dst, array));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Int(), false, true);
	}

	@Override
	public void visit(codegen.C.exp.Lt e) {
		String dst = genVar(new cfg.type.Boolean());
		e.left.accept(this);
		cfg.operand.T left = this.operand;
		e.right.accept(this);
		emit(new cfg.stm.Lt(dst, left, this.operand));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Boolean(), false,
				true);
	}

	@Override
	public void visit(codegen.C.exp.NewIntArray e) {
		String dst = genVar(new cfg.type.IntArray());
		e.exp.accept(this);
		cfg.operand.T exp = this.operand;
		emit(new cfg.stm.NewIntArray(dst, exp));
		this.operand = new cfg.operand.Var(dst, new cfg.type.IntArray(), false,
				true);
	}

	@Override
	public void visit(codegen.C.exp.NewObject e) {
		cfg.type.Class classs = new cfg.type.Class(e.id);
		String dst = genVar(classs);
		emit(new cfg.stm.NewObject(dst, e.id));
		this.operand = new cfg.operand.Var(dst, classs, false, true);
	}

	@Override
	public void visit(codegen.C.exp.Not e) {
		String dst = genVar(new cfg.type.Boolean());
		e.exp.accept(this);
		cfg.operand.T exp = this.operand;
		emit(new cfg.stm.Not(dst, exp));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Boolean(), false,
				true);
	}

	@Override
	public void visit(codegen.C.exp.Num e) {
		this.operand = new cfg.operand.Int(e.num);
	}

	@Override
	public void visit(codegen.C.exp.Sub e) {
		String dst = genVar();
		e.left.accept(this);
		cfg.operand.T left = this.operand;
		e.right.accept(this);
		emit(new cfg.stm.Sub(dst, left, this.operand));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Int(), false, true);
	}

	@Override
	public void visit(codegen.C.exp.This e) {
		this.operand = new cfg.operand.Var("this", null, false, false);
	}

	@Override
	public void visit(codegen.C.exp.Times e) {
		String dst = genVar();
		e.left.accept(this);
		cfg.operand.T left = this.operand;
		e.right.accept(this);
		emit(new cfg.stm.Times(dst, left, this.operand));
		this.operand = new cfg.operand.Var(dst, new cfg.type.Int(), false, true);
	}

	// statements
	@Override
	public void visit(codegen.C.stm.Assign s) {
		s.exp.accept(this);
		emit(new cfg.stm.Move(s.id, this.operand, s.isField, s.isLocal, s.type));
	}

	@Override
	public void visit(codegen.C.stm.AssignArray s) {
		s.index.accept(this);
		cfg.operand.T index = this.operand;
		s.exp.accept(this);
		cfg.operand.T exp = this.operand;
		emit(new cfg.stm.AssignArray(s.id, index, exp, s.isField, s.isLocal));
	}

	@Override
	public void visit(codegen.C.stm.Block s) {
		for (codegen.C.stm.T stm : s.stms) {
			stm.accept(this);
		}
	}

	@Override
	public void visit(codegen.C.stm.If s) {
		util.Label tl = new util.Label(), fl = new util.Label(), el = new util.Label();
		s.condition.accept(this);
		emit(new cfg.transfer.If(this.operand, tl, fl));
		emit(fl);
		s.elsee.accept(this);
		emit(new cfg.transfer.Goto(el));
		emit(tl);
		s.thenn.accept(this);
		emit(new cfg.transfer.Goto(el));
		emit(el);
	}

	@Override
	public void visit(codegen.C.stm.Print s) {
		s.exp.accept(this);
		emit(new cfg.stm.Print(this.operand));
	}

	@Override
	public void visit(codegen.C.stm.While s) {
		util.Label start = new util.Label();
		util.Label end = new util.Label();
		util.Label bodyStart = new util.Label();
		emit(start);
		s.condition.accept(this);
		emit(new cfg.transfer.If(this.operand, bodyStart, end));
		emit(bodyStart);
		s.body.accept(this);
		emit(new cfg.transfer.Goto(start));
		emit(end);
	}

	// type
	@Override
	public void visit(codegen.C.type.Class t) {
		this.type = new cfg.type.Class(t.id);
	}

	@Override
	public void visit(codegen.C.type.Int t) {
		this.type = new cfg.type.Int();
	}

	@Override
	public void visit(codegen.C.type.IntArray t) {
		this.type = new cfg.type.IntArray();
	}

	// dec
	@Override
	public void visit(codegen.C.dec.Dec d) {
		d.type.accept(this);
		this.dec = new cfg.dec.Dec(this.type, d.id);
	}

	// vtable
	@Override
	public void visit(codegen.C.vtable.Vtable v) {
		java.util.LinkedList<cfg.Ftuple> newTuples = new java.util.LinkedList<cfg.Ftuple>();
		for (codegen.C.Ftuple t : v.ms) {
			t.ret.accept(this);
			cfg.type.T ret = this.type;
			java.util.LinkedList<cfg.dec.T> args = new java.util.LinkedList<cfg.dec.T>();
			for (codegen.C.dec.T dec : t.args) {
				dec.accept(this);
				args.add(this.dec);
			}
			newTuples.add(new cfg.Ftuple(t.classs, ret, args, t.id));
		}
		java.util.LinkedList<cfg.Tuple> tuples = new java.util.LinkedList<cfg.Tuple>();
		for (codegen.C.Tuple tuple : ((codegen.C.classs.Class) v.classs).decs) {
			if (tuple.type instanceof codegen.C.type.IntArray)
				tuples.add(new cfg.Tuple(v.id, new cfg.type.IntArray(),
						tuple.id));
			else if (tuple.type instanceof codegen.C.type.Int)
				tuples.add(new cfg.Tuple(v.id, new cfg.type.Int(), tuple.id));
			else if (tuple.type instanceof codegen.C.type.Class)
				tuples.add(new cfg.Tuple(v.id, new cfg.type.Class(
						((codegen.C.type.Class) tuple.type).id), tuple.id));
			else
				throw new RuntimeException(
						"unknow codegen.C.type.* when translate codegen.C to cfg");
		}
		cfg.classs.T classs = new cfg.classs.Class(v.id, tuples);
		this.vtable = new cfg.vtable.Vtable(v.id, newTuples, classs);
	}

	// class
	@Override
	public void visit(codegen.C.classs.Class c) {
		java.util.LinkedList<cfg.Tuple> newTuples = new java.util.LinkedList<cfg.Tuple>();
		for (codegen.C.Tuple t : c.decs) {
			t.type.accept(this);
			newTuples.add(new cfg.Tuple(t.classs, this.type, t.id));
		}
		this.classs = new cfg.classs.Class(c.id, newTuples);
	}

	// method
	@Override
	public void visit(codegen.C.method.Method m) {
		this.newLocals = new java.util.LinkedList<cfg.dec.T>();

		m.retType.accept(this);
		cfg.type.T retType = this.type;

		java.util.LinkedList<cfg.dec.T> newFormals = new java.util.LinkedList<cfg.dec.T>();
		for (codegen.C.dec.T c : m.formals) {
			c.accept(this);
			newFormals.add(this.dec);
		}

		java.util.LinkedList<cfg.dec.T> locals = new java.util.LinkedList<cfg.dec.T>();
		for (codegen.C.dec.T c : m.locals) {
			c.accept(this);
			locals.add(this.dec);
		}

		// a junk label
		util.Label entry = new util.Label();
		emit(entry);

		for (codegen.C.stm.T s : m.stms)
			s.accept(this);

		m.retExp.accept(this);
		emit(new cfg.transfer.Return(this.operand));

		//
		java.util.LinkedList<cfg.block.T> blocks = cookBlocks();

		for (cfg.dec.T d : this.newLocals)
			locals.add(d);

		this.method = new cfg.method.Method(retType, m.id, m.classId,
				newFormals, locals, blocks, entry, null, null);
	}

	// main method
	@Override
	public void visit(codegen.C.mainMethod.MainMethod m) {
		this.newLocals = new java.util.LinkedList<cfg.dec.T>();

		java.util.LinkedList<cfg.dec.T> locals = new java.util.LinkedList<cfg.dec.T>();
		for (codegen.C.dec.T c : m.locals) {
			c.accept(this);
			locals.add(this.dec);
		}

		util.Label entry = new util.Label();
		emit(entry);

		m.stm.accept(this);

		emit(new cfg.transfer.Return(new cfg.operand.Int(0)));

		java.util.LinkedList<cfg.block.T> blocks = cookBlocks();
		for (cfg.dec.T d : this.newLocals)
			locals.add(d);
		this.mainMethod = new cfg.mainMethod.MainMethod(locals, blocks);
	}

	// program
	@Override
	public void visit(codegen.C.program.Program p) {
		java.util.LinkedList<cfg.classs.T> newClasses = new java.util.LinkedList<cfg.classs.T>();
		for (codegen.C.classs.T c : p.classes) {
			c.accept(this);
			newClasses.add(this.classs);
		}

		java.util.LinkedList<cfg.vtable.T> newVtable = new java.util.LinkedList<cfg.vtable.T>();
		for (codegen.C.vtable.T v : p.vtables) {
			v.accept(this);
			newVtable.add(this.vtable);
		}

		java.util.LinkedList<cfg.method.T> newMethods = new java.util.LinkedList<cfg.method.T>();
		for (codegen.C.method.T m : p.methods) {
			m.accept(this);
			newMethods.add(this.method);
		}

		p.mainMethod.accept(this);
		cfg.mainMethod.T newMainMethod = this.mainMethod;

		this.program = new cfg.program.Program(newClasses, newVtable,
				newMethods, newMainMethod);
	}
}
