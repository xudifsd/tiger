package elaborator;

public class ElaboratorVisitor implements ast.Visitor {
	public ClassTable classTable; // symbol table for class
	public MethodTable methodTable; // symbol table for each method
	public String currentClass; // the class name being elaborated
	public ast.type.T type; // type of the expression being elaborated

	public ElaboratorVisitor() {
		this.classTable = new ClassTable();
		this.methodTable = new MethodTable();
		this.currentClass = null;
		this.type = null;
	}

	private void error(String hint) {
		System.out.println(hint);
		System.exit(1);
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		if (!this.type.toString().equals("@int"))
			error("in Add, type of left hand is not int");
		e.right.accept(this);
		if (!this.type.toString().equals("@int"))
			error("in Add, type of right hand is not int");
		this.type = new ast.type.Int(-1);
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("in And, type of left hand is not boolean");
		e.right.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("in And, type of right hand is not boolean");
		this.type = new ast.type.Boolean(-1);
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.array.accept(this);
		if (!this.type.toString().equals("@int[]"))
			error("in ArraySelect, type of array is not int[]");
		e.index.accept(this);
		if (!this.type.toString().equals("@int"))
			error("in ArraySelect, type of index is not int");
		this.type = new ast.type.Int(-1);
	}

	@Override
	public void visit(ast.exp.Call e) {
		ast.type.T leftty;
		ast.type.Class ty = null;

		e.exp.accept(this);
		leftty = this.type;
		if (leftty instanceof ast.type.Class) {
			ty = (ast.type.Class) leftty;
			e.type = ty.id;
		} else
			error(leftty.toString() + " is not class");
		MethodType mty = this.classTable.getm(ty.id, e.id);
		java.util.LinkedList<ast.type.T> declaredArgTypes = new java.util.LinkedList<ast.type.T>();
		for (ast.dec.T dec: mty.argsType) {
			declaredArgTypes.add(((ast.dec.Dec)dec).type);
		}
		java.util.LinkedList<ast.type.T> argsty = new java.util.LinkedList<ast.type.T>();
		for (ast.exp.T a : e.args) {
			a.accept(this);
			argsty.addLast(this.type);
		}

		if (declaredArgTypes.size() != argsty.size())
			error("in Call, argument list length is not match parpameter list length");

		for (int i = 0; i < argsty.size(); i++) {
			ast.dec.Dec dec = (ast.dec.Dec) mty.argsType.get(i);
			if (dec.type.toString().equals(argsty.get(i).toString()))
				;
			else {
				String ancestor = argsty.get(i).toString();
				for (;;) {
					// find if dec.type.toString() is ancestor of argsty.get(i).toString()
					ClassBinding cb = this.classTable.get(ancestor);
					if (cb.extendss == null)
						error("in Call, dec type is " + dec.type.toString() +
								", real is " + argsty.get(i).toString());
					else {
						if (cb.extendss.equals(dec.type.toString()))
							;//detected extends
						else {
							ancestor = cb.extendss;
							continue;
						}
					}
					break;
				}
			}
		}
		this.type = mty.retType;
		// the following two types should be the declared types.
		e.at = declaredArgTypes;
		e.rt = this.type;
	}

	@Override
	public void visit(ast.exp.False e) {
		this.type = new ast.type.Boolean(-1);
	}

	@Override
	public void visit(ast.exp.Id e) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(e.id);
		// if search failed, then s.id must be a class field.
		if (type == null) {
			type = this.classTable.get(this.currentClass, e.id);
			// mark this id as a field id, this fact will be
			// useful in later phase.
			e.isField = true;
		}
		if (this.methodTable.locals.get(e.id) != null)
			e.isLocal = true;
		if (type == null)
			error(e.id + " is not defined");
		this.type = type;
		// record this type on this node for future use.
		e.type = type;
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		if (!this.type.toString().equals("@int[]"))
			error("access length property when exp is not int[]");
		this.type = new ast.type.Int(-1);
	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		ast.type.T ty = this.type;
		e.right.accept(this);
		if (!(this.type.toString().equals(ty.toString()) &&
				this.type.toString().equals("@int")))
			error("left and right of < should both be of type int");
		this.type = new ast.type.Boolean(-1);
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error("line " + e.lineno + " sematic error, size of new array is not int");
		this.type = new ast.type.IntArray(-1);
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		this.type = new ast.type.Class(e.id, -1);
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("! operator only accept boolean");
		this.type = new ast.type.Boolean(-1);
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.type = new ast.type.Int(-1);
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!(this.type.toString().equals(leftty.toString()) &&
				this.type.toString().equals("@int")))
			error("left and right of - should both be of type int");
		this.type = new ast.type.Int(-1);
	}

	@Override
	public void visit(ast.exp.This e) {
		this.type = new ast.type.Class(this.currentClass, -1);
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!(this.type.toString().equals(leftty.toString()) &&
				this.type.toString().equals("@int")))
			error("left and right of * should both be of type int");
		this.type = new ast.type.Int(-1);
	}

	@Override
	public void visit(ast.exp.True e) {
		this.type = new ast.type.Boolean(-1);
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(s.id);
		// if search failed, then s.id must a class field.
		if (type == null) {
			type = this.classTable.get(this.currentClass, s.id);
			s.isField = true;
		}

		if (this.methodTable.locals.get(s.id) != null)
			s.isLocal = true;

		if (type == null)
			error("in Assign, unknow " + s.id);
		s.type = type;
		s.exp.accept(this);
		// FIXME couldn't handle extends right now
		if (!this.type.toString().equals(s.type.toString()))
			error("trying to assign " + this.type + " to " + s.type);
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		ast.type.T type = this.methodTable.get(s.id);
		if (type == null) {
			type = this.classTable.get(this.currentClass, s.id);
			s.isField = true;
		}

		if (this.methodTable.locals.get(s.id) != null)
			s.isLocal = true;

		if (type == null)
			error("in AssignArray, unknow int array");
		if (!type.toString().equals("@int[]"))
			error("in AssignArray, " + s.id + " is not int[]");
		s.index.accept(this);
		if (!this.type.toString().equals("@int"))
			error("in AssignArray, index is not int");
		s.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error("in AssignArray, couldn't assign non-int to int array");
	}

	@Override
	public void visit(ast.stm.Block s) {
		for (ast.stm.T stm : s.stms)
			stm.accept(this);
	}

	@Override
	public void visit(ast.stm.If s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("condition of if is not boolean type");
		s.thenn.accept(this);
		s.elsee.accept(this);
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error("in Print");
	}

	@Override
	public void visit(ast.stm.While s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error("condition of while is not boolean type");
		s.body.accept(this);
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
	}

	@Override
	public void visit(ast.type.Class t) {
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
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		this.methodTable = new MethodTable(); // we should use new MethodTable each time construct the method table
		this.methodTable.put(m.formals, m.locals);

		if (control.Control.elabMethodTable)
			this.methodTable.dump(m.id);

		for (ast.stm.T s : m.stms)
			s.accept(this);
		m.retExp.accept(this);
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		this.currentClass = c.id;

		for (ast.method.T m : c.methods)
			m.accept(this);
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		this.currentClass = c.id;
		// "main" has an argument "arg" of type "String[]", but
		// one has no chance to use it. So it's safe to skip it...

		c.stm.accept(this);
	}

	// ////////////////////////////////////////////////////////
	// step 1: build class table
	// class table for Main class
	private void buildMainClass(ast.mainClass.MainClass main) {
		this.classTable.put(main.id, new ClassBinding(null));
	}

	// class table for normal classes
	private void buildClass(ast.classs.Class c) {
		this.classTable.put(c.id, new ClassBinding(c.extendss));
		for (ast.dec.T dec : c.decs) {
			ast.dec.Dec d = (ast.dec.Dec) dec;
			this.classTable.put(c.id, d.id, d.type);
		}
		for (ast.method.T method : c.methods) {
			ast.method.Method m = (ast.method.Method) method;
			this.classTable.put(c.id, m.id,
					new MethodType(m.retType, m.formals));
		}
	}

	// step 1: end
	// ///////////////////////////////////////////////////

	// program
	@Override
	public void visit(ast.program.Program p) {
		// ////////////////////////////////////////////////
		// step 1: build a symbol table for class (the class table)
		// a class table is a mapping from class names to class bindings
		// classTable: className -> ClassBinding{extends, fields, methods}
		buildMainClass((ast.mainClass.MainClass) p.mainClass);
		for (ast.classs.T c : p.classes) {
			buildClass((ast.classs.Class) c);
		}

		// we can double check that the class table is OK!
		if (control.Control.elabClassTable) {
			this.classTable.dump();
		}

		// ////////////////////////////////////////////////
		// step 2: elaborate each class in turn, under the class table
		// built above.
		p.mainClass.accept(this);
		for (ast.classs.T c : p.classes) {
			c.accept(this);
		}
	}
}
