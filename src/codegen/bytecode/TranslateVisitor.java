package codegen.bytecode;

import java.util.Hashtable;
import java.util.LinkedList;

import util.Label;

// Given a Java ast, translate it into Java bytecode.

public class TranslateVisitor implements ast.Visitor {
	private String classId;
	private int index;
	private Hashtable<String, Integer> indexTable;
	private codegen.bytecode.type.T type; // type after translation
	private codegen.bytecode.dec.T dec;
	private LinkedList<codegen.bytecode.stm.T> stms;
	private codegen.bytecode.method.T method;
	private codegen.bytecode.classs.T classs;
	private codegen.bytecode.mainClass.T mainClass;
	private boolean inAssign;
	private LinkedList<codegen.bytecode.stm.T> assignList;
	public codegen.bytecode.program.T program;

	public TranslateVisitor() {
		this.classId = null;
		this.indexTable = null;
		this.type = null;
		this.dec = null;
		this.stms = new java.util.LinkedList<codegen.bytecode.stm.T>();
		this.method = null;
		this.classs = null;
		this.mainClass = null;
		this.program = null;
		this.inAssign = false;
		this.assignList = null;
	}

	private void emit(codegen.bytecode.stm.T s) {
		if (this.inAssign)
			this.assignList.add(s);
		else
			this.stms.add(s);
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		e.right.accept(this);
		emit(new codegen.bytecode.stm.Iadd());
	}

	@Override
	public void visit(ast.exp.And e) {
		Label left = new Label(), right = new Label(), out = new Label();
		e.left.accept(this);
		emit(new codegen.bytecode.stm.Ifne(left));
		emit(new codegen.bytecode.stm.Ldc(0));
		emit(new codegen.bytecode.stm.Goto(out));

		emit(new codegen.bytecode.stm.Label(left));
		e.right.accept(this);
		emit(new codegen.bytecode.stm.Ifne(right));
		emit(new codegen.bytecode.stm.Ldc(0));
		emit(new codegen.bytecode.stm.Goto(out));

		emit(new codegen.bytecode.stm.Label(right));
		emit(new codegen.bytecode.stm.Ldc(1));
		emit(new codegen.bytecode.stm.Label(out));
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		e.index.accept(this);
		emit(new codegen.bytecode.stm.Iaload());
	}

	@Override
	public void visit(ast.exp.Call e) {
		e.exp.accept(this);
		for (ast.exp.T x : e.args) {
			x.accept(this);
		}
		e.rt.accept(this);
		codegen.bytecode.type.T rt = this.type;
		java.util.LinkedList<codegen.bytecode.type.T> at = new java.util.LinkedList<codegen.bytecode.type.T>();
		for (ast.type.T t : e.at) {
			t.accept(this);
			at.add(this.type);
		}

		emit(new codegen.bytecode.stm.Invokevirtual(e.id, e.type, at, rt));
	}

	@Override
	public void visit(ast.exp.False e) {
		emit(new codegen.bytecode.stm.Ldc(0));
	}

	@Override
	public void visit(ast.exp.Id e) {
		if (e.isField) {
			e.type.accept(this);
			codegen.bytecode.type.T type = this.type;
			emit(new codegen.bytecode.stm.Getfield(this.classId, e.id, type));
		} else {
			int index = this.indexTable.get(e.id);
			ast.type.T type = e.type;
			if (type.getNum() > 0)// a reference
				emit(new codegen.bytecode.stm.Aload(index));
			else
				emit(new codegen.bytecode.stm.Iload(index));
		}
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		emit(new codegen.bytecode.stm.Arraylength());
	}

	@Override
	public void visit(ast.exp.Lt e) {
		Label tl = new Label(), fl = new Label(), el = new Label();
		e.left.accept(this);
		e.right.accept(this);
		emit(new codegen.bytecode.stm.Ificmplt(tl));
		emit(new codegen.bytecode.stm.Label(fl));
		emit(new codegen.bytecode.stm.Ldc(0));
		emit(new codegen.bytecode.stm.Goto(el));
		emit(new codegen.bytecode.stm.Label(tl));
		emit(new codegen.bytecode.stm.Ldc(1));
		emit(new codegen.bytecode.stm.Goto(el));
		emit(new codegen.bytecode.stm.Label(el));
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		emit(new codegen.bytecode.stm.NewarrayInt());
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		emit(new codegen.bytecode.stm.New(e.id));
	}

	@Override
	public void visit(ast.exp.Not e) {
		Label generate_0 = new Label(), out = new Label();
		e.exp.accept(this);
		emit(new codegen.bytecode.stm.Ifne(generate_0));
		emit(new codegen.bytecode.stm.Ldc(1));
		emit(new codegen.bytecode.stm.Goto(out));

		emit(new codegen.bytecode.stm.Label(generate_0));
		emit(new codegen.bytecode.stm.Ldc(0));
		emit(new codegen.bytecode.stm.Label(out));
	}

	@Override
	public void visit(ast.exp.Num e) {
		emit(new codegen.bytecode.stm.Ldc(e.num));
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		e.right.accept(this);
		emit(new codegen.bytecode.stm.Isub());
	}

	@Override
	public void visit(ast.exp.This e) {
		emit(new codegen.bytecode.stm.Aload(0));
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		e.right.accept(this);
		emit(new codegen.bytecode.stm.Imul());
	}

	@Override
	public void visit(ast.exp.True e) {
		emit(new codegen.bytecode.stm.Ldc(1));
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		if (s.isField) {
			this.inAssign = true;//capture all emitted stm
			this.assignList = new LinkedList<codegen.bytecode.stm.T>();
			s.exp.accept(this);
			s.type.accept(this);
			codegen.bytecode.type.T type = this.type;
			this.inAssign = false;
			emit(new codegen.bytecode.stm.Putfield(this.classId, s.id, type, this.assignList));
			this.assignList = null;
		} else {
			s.exp.accept(this);
			int index = this.indexTable.get(s.id);
			ast.type.T type = s.type;
			if (type.getNum() > 0)// a reference
				emit(new codegen.bytecode.stm.Astore(index));
			else
				emit(new codegen.bytecode.stm.Istore(index));
		}
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		if (s.isField) {
			emit(new codegen.bytecode.stm.Getfield(this.classId, s.id, new codegen.bytecode.type.IntArray()));
		} else {
			int index = this.indexTable.get(s.id);
			emit(new codegen.bytecode.stm.Aload(index));
		}
		s.index.accept(this);
		s.exp.accept(this);
		emit(new codegen.bytecode.stm.Iastore());
	}

	@Override
	public void visit(ast.stm.Block s) {
		for (ast.stm.T stm: s.stms)
			stm.accept(this);
	}

	@Override
	public void visit(ast.stm.If s) {
		Label tl = new Label(), fl = new Label(), el = new Label();
		s.condition.accept(this);
		emit(new codegen.bytecode.stm.Ifne(tl));
		emit(new codegen.bytecode.stm.Label(fl));
		s.elsee.accept(this);
		emit(new codegen.bytecode.stm.Goto(el));
		emit(new codegen.bytecode.stm.Label(tl));
		s.thenn.accept(this);
		emit(new codegen.bytecode.stm.Goto(el));
		emit(new codegen.bytecode.stm.Label(el));
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
		emit(new codegen.bytecode.stm.Print());
	}

	@Override
	public void visit(ast.stm.While s) {
		Label body = new Label(), out = new Label(), start = new Label();
		emit(new codegen.bytecode.stm.Label(start));
		s.condition.accept(this);
		emit(new codegen.bytecode.stm.Ifne(body));
		emit(new codegen.bytecode.stm.Goto(out));
		emit(new codegen.bytecode.stm.Label(body));
		s.body.accept(this);
		emit(new codegen.bytecode.stm.Goto(start));
		emit(new codegen.bytecode.stm.Label(out));
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
		this.type = new codegen.bytecode.type.Int();
	}

	@Override
	public void visit(ast.type.Class t) {
		this.type = new codegen.bytecode.type.Class(t.id);
	}

	@Override
	public void visit(ast.type.Int t) {
		this.type = new codegen.bytecode.type.Int();
	}

	@Override
	public void visit(ast.type.IntArray t) {
		this.type = new codegen.bytecode.type.IntArray();
	}

	// dec
	@Override
	public void visit(ast.dec.Dec d) {
		d.type.accept(this);
		this.dec = new codegen.bytecode.dec.Dec(this.type, d.id);
		if (this.indexTable != null)
			this.indexTable.put(d.id, index++);
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		// record, in a hash table, each var's index
		// this index will be used in the load store operation
		this.index = 1;
		this.indexTable = new java.util.Hashtable<String, Integer>();

		m.retType.accept(this);
		codegen.bytecode.type.T newRetType = this.type;
		java.util.LinkedList<codegen.bytecode.dec.T> newFormals = new java.util.LinkedList<codegen.bytecode.dec.T>();
		for (ast.dec.T d : m.formals) {
			d.accept(this);
			newFormals.add(this.dec);
		}
		java.util.LinkedList<codegen.bytecode.dec.T> locals = new java.util.LinkedList<codegen.bytecode.dec.T>();
		for (ast.dec.T d : m.locals) {
			d.accept(this);
			locals.add(this.dec);
		}
		this.stms = new java.util.LinkedList<codegen.bytecode.stm.T>();
		for (ast.stm.T s : m.stms) {
			s.accept(this);
		}

		// return statement is specially treated
		m.retExp.accept(this);

		if (m.retType.getNum() > 0)
			emit(new codegen.bytecode.stm.Areturn());
		else
			emit(new codegen.bytecode.stm.Ireturn());

		this.method = new codegen.bytecode.method.Method(newRetType, m.id,
				this.classId, newFormals, locals, this.stms, 0, this.index);
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		this.classId = c.id;
		java.util.LinkedList<codegen.bytecode.dec.T> newDecs = new java.util.LinkedList<codegen.bytecode.dec.T>();
		this.indexTable = null;
		for (ast.dec.T dec : c.decs) {
			dec.accept(this);
			newDecs.add(this.dec);
		}
		java.util.LinkedList<codegen.bytecode.method.T> newMethods = new java.util.LinkedList<codegen.bytecode.method.T>();
		for (ast.method.T m : c.methods) {
			m.accept(this);
			newMethods.add(this.method);
		}
		this.classs = new codegen.bytecode.classs.Class(c.id, c.extendss,
				newDecs, newMethods);
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		c.stm.accept(this);
		this.mainClass = new codegen.bytecode.mainClass.MainClass(c.id, c.arg,
				this.stms);
		this.stms = new java.util.LinkedList<codegen.bytecode.stm.T>();
	}

	// program
	@Override
	public void visit(ast.program.Program p) {
		// do translations
		p.mainClass.accept(this);

		java.util.LinkedList<codegen.bytecode.classs.T> newClasses = new java.util.LinkedList<codegen.bytecode.classs.T>();
		for (ast.classs.T classs : p.classes) {
			classs.accept(this);
			newClasses.add(this.classs);
		}
		this.program = new codegen.bytecode.program.Program(this.mainClass,
				newClasses);
	}
}
