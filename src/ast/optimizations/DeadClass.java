package ast.optimizations;

// Dead class elimination optimizations on an AST.

public class DeadClass implements ast.Visitor {
	private java.util.HashSet<String> set;
	private java.util.LinkedList<String> worklist;
	public ast.program.T program;

	public DeadClass() {
		this.set = new java.util.HashSet<String>();
		this.worklist = new java.util.LinkedList<String>();
		this.program = null;
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		e.right.accept(this);
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		e.right.accept(this);
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		e.index.accept(this);
	}

	@Override
	public void visit(ast.exp.Call e) {
		e.exp.accept(this);
		for (ast.exp.T arg : e.args) {
			arg.accept(this);
		}
	}

	@Override
	public void visit(ast.exp.False e) {
	}

	@Override
	public void visit(ast.exp.Id e) {
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		e.right.accept(this);
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		if (this.set.contains(e.id))
			return;
		this.worklist.add(e.id);
		this.set.add(e.id);
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
	}

	@Override
	public void visit(ast.exp.Num e) {
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		e.right.accept(this);
	}

	@Override
	public void visit(ast.exp.This e) {
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		e.right.accept(this);
	}

	@Override
	public void visit(ast.exp.True e) {
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		s.exp.accept(this);
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		s.index.accept(this);
		s.exp.accept(this);
	}

	@Override
	public void visit(ast.stm.Block s) {
		for (ast.stm.T x : s.stms)
			x.accept(this);
	}

	@Override
	public void visit(ast.stm.If s) {
		s.condition.accept(this);
		s.thenn.accept(this);
		s.elsee.accept(this);
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
	}

	@Override
	public void visit(ast.stm.While s) {
		s.condition.accept(this);
		s.body.accept(this);
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
	}

	@Override
	public void visit(ast.type.Class t) {
		/* *
		 * There will be code that have declaration but without assign, so
		 * we may need to do DeadClass elimination again after DeadCode
		 * elimination.
		 * */
		if (t.id != null) {
			if (this.set.contains(t.id))
				return;
			this.worklist.add(t.id);
			this.set.add(t.id);
		}
	}

	@Override
	public void visit(ast.type.Int t) {
	}

	@Override
	public void visit(ast.type.IntArray t) {
	}

	// dec
	@Override
	public void visit(ast.dec.Dec d) {
		d.type.accept(this);
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		for (ast.dec.T formal: m.formals)
			formal.accept(this);
		m.retType.accept(this);
		for (ast.dec.T dec: m.locals)
			dec.accept(this);
		for (ast.stm.T s : m.stms)
			s.accept(this);
		m.retExp.accept(this);
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		if (c.extendss != null) {
			if (this.set.contains(c.extendss))
				return;
			this.worklist.add(c.extendss);
			this.set.add(c.extendss);
		}
		for (ast.dec.T dec: c.decs)
			dec.accept(this);
		for (ast.method.T m: c.methods)
			m.accept(this);
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		c.stm.accept(this);
	}

	// program
	@Override
	public void visit(ast.program.Program p) {
		// we push the class name for mainClass onto the worklist
		ast.mainClass.MainClass mainclass = (ast.mainClass.MainClass) p.mainClass;
		this.set.add(mainclass.id);

		p.mainClass.accept(this);

		while (!this.worklist.isEmpty()) {
			String cid = this.worklist.removeFirst();

			for (ast.classs.T c : p.classes) {
				ast.classs.Class current = (ast.classs.Class) c;

				if (current.id.equals(cid)) {
					c.accept(this);
					break;
				}
			}
		}

		java.util.LinkedList<ast.classs.T> newClasses = new java.util.LinkedList<ast.classs.T>();
		for (ast.classs.T classs : p.classes) {
			ast.classs.Class c = (ast.classs.Class) classs;
			if (this.set.contains(c.id))
				newClasses.add(c);
		}

		this.program = new ast.program.Program(p.mainClass, newClasses);

		if (control.Control.isTracing("ast.DeadClass")) {
			System.out.println("before optimization:");
			ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
			p.accept(pp);
			System.out.println("after optimization:");
			this.program.accept(pp);
		}
	}
}
