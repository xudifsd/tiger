package codegen.C;

import codegen.C.stm.T;
import control.Control;

public class PrettyPrintVisitor implements Visitor {
	private int indentLevel;
	private java.io.BufferedWriter writer;

	public PrettyPrintVisitor() {
		this.indentLevel = 0;
	}

	private void indent() {
		this.indentLevel += 4;
	}

	private void unIndent() {
		this.indentLevel -= 4;
	}

	private void printSpaces() {
		int i = this.indentLevel;
		while (i-- != 0)
			this.say(" ");
	}

	private void sayln(String s) {
		say(s);
		try {
			this.writer.write("\n");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void say(String s) {
		try {
			this.writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(codegen.C.exp.Add e) {
		e.left.accept(this);
		this.say(" + ");
		e.right.accept(this);
	}

	@Override
	public void visit(codegen.C.exp.And e) {
		e.left.accept(this);
		this.say(" && ");
		e.right.accept(this);
	}

	@Override
	public void visit(codegen.C.exp.ArraySelect e) {
		e.array.accept(this);
		this.say("->data[");
		e.index.accept(this);
		this.say("]");
	}

	@Override
	public void visit(codegen.C.exp.Call e) {
		this.say("(" + e.assign + "=");
		e.exp.accept(this);
		this.say(", ");
		this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
		int size = e.args.size();
		if (size == 0) {
			this.say("))");
			return;
		}
		for (codegen.C.exp.T x : e.args) {
			this.say(", ");
			x.accept(this);
		}
		this.say("))");
	}

	@Override
	public void visit(codegen.C.exp.Id e) {
		this.say(e.id);
	}

	@Override
	public void visit(codegen.C.exp.Length e) {
		this.say("(");
		e.array.accept(this);
		this.say("->length)");
	}

	@Override
	public void visit(codegen.C.exp.Lt e) {
		e.left.accept(this);
		this.say(" < ");
		e.right.accept(this);
	}

	@Override
	public void visit(codegen.C.exp.NewIntArray e) {
		this.say("Tiger_new_int_array(");
		e.exp.accept(this);
		this.say(")");
	}

	@Override
	public void visit(codegen.C.exp.NewObject e) {
		this.say("((struct " + e.id + "*)(Tiger_new(&" + e.id
				+ "_vtable_, sizeof(struct " + e.id + "))))");
	}

	@Override
	public void visit(codegen.C.exp.Not e) {
		this.say("!");
		e.exp.accept(this);
	}

	@Override
	public void visit(codegen.C.exp.Num e) {
		this.say(Integer.toString(e.num));
	}

	@Override
	public void visit(codegen.C.exp.Sub e) {
		e.left.accept(this);
		this.say(" - ");
		e.right.accept(this);
	}

	@Override
	public void visit(codegen.C.exp.This e) {
		this.say("this");
	}

	@Override
	public void visit(codegen.C.exp.Times e) {
		e.left.accept(this);
		this.say(" * ");
		e.right.accept(this);
	}

	// statements
	@Override
	public void visit(codegen.C.stm.Assign s) {
		this.say(s.id + " = ");
		s.exp.accept(this);
		this.sayln(";");
	}

	@Override
	public void visit(codegen.C.stm.AssignArray s) {
		this.say(s.id + "->data[");
		s.index.accept(this);
		this.say("] = ");
		s.exp.accept(this);
		this.sayln(";");
	}

	@Override
	public void visit(codegen.C.stm.Block s) {
		this.sayln("{");
		this.indent();
		for (T stm : s.stms) {
			this.printSpaces();
			stm.accept(this);
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("}");
	}

	@Override
	public void visit(codegen.C.stm.If s) {
		this.say("if (");
		s.condition.accept(this);
		this.sayln(")");
		this.indent();
		this.printSpaces();
		s.thenn.accept(this);
		this.unIndent();
		this.printSpaces();
		this.sayln("else");
		this.indent();
		this.printSpaces();
		s.elsee.accept(this);
		this.sayln("");
		this.unIndent();
	}

	@Override
	public void visit(codegen.C.stm.Print s) {
		this.say("System_out_println(");
		s.exp.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(codegen.C.stm.While s) {
		this.say("while (");
		s.condition.accept(this);
		this.sayln(")");
		this.indent();
		this.printSpaces();
		s.body.accept(this);
		this.unIndent();
		this.sayln("");
	}

	// type
	@Override
	public void visit(codegen.C.type.Class t) {
		this.say("struct " + t.id + " *");
	}

	@Override
	public void visit(codegen.C.type.Int t) {
		this.say("int");
	}

	@Override
	public void visit(codegen.C.type.IntArray t) {
		this.say("struct _runtime_int_array *");
	}

	// dec
	@Override
	public void visit(codegen.C.dec.Dec d) {
		d.type.accept(this);
		this.say(" ");
		this.sayln(d.id + ";");
	}

	// method
	@Override
	public void visit(codegen.C.method.Method m) {
		m.retType.accept(this);
		this.say(" " + m.classId + "_" + m.id + "(");
		int size = m.formals.size();
		for (codegen.C.dec.T d : m.formals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			size--;
			dec.type.accept(this);
			this.say(" " + dec.id);
			if (size > 0)
				this.say(", ");
		}
		this.sayln(") {");
		this.indent();

		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			this.printSpaces();
			dec.type.accept(this);
			this.say(" " + dec.id + ";\n");
		}
		this.sayln("");
		for (codegen.C.stm.T s : m.stms) {
			this.printSpaces();
			s.accept(this);
		}
		this.printSpaces();
		this.say("return ");
		m.retExp.accept(this);
		this.sayln(";");
		this.unIndent();
		this.printSpaces();
		this.sayln("}");
	}

	@Override
	public void visit(codegen.C.mainMethod.MainMethod m) {
		this.sayln("void Tiger_main () {");
		this.indent();
		for (codegen.C.dec.T dec : m.locals) {
			this.printSpaces();
			codegen.C.dec.Dec d = (codegen.C.dec.Dec) dec;
			d.type.accept(this);
			this.say(" ");
			this.sayln(d.id + ";");
		}
		this.printSpaces();
		m.stm.accept(this);
		this.unIndent();
		this.printSpaces();
		this.sayln("}");
	}

	// vtables
	@Override
	public void visit(codegen.C.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable {");
		this.indent();
		for (codegen.C.Ftuple t : v.ms) {
			this.printSpaces();
			t.ret.accept(this);
			this.sayln(" (*" + t.id + ")();");
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("};\n");
	}

	private void outputVtable(codegen.C.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = {");
		this.indent();
		for (codegen.C.Ftuple t : v.ms) {
			this.printSpaces();
			this.sayln(t.classs + "_" + t.id + ",");
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("};\n");
	}

	// class
	@Override
	public void visit(codegen.C.classs.Class c) {
		this.sayln("struct " + c.id + " {");
		this.indent();
		this.printSpaces();
		this.sayln("struct " + c.id + "_vtable *vptr;");
		for (codegen.C.Tuple t : c.decs) {
			this.printSpaces();
			t.type.accept(this);
			this.say(" ");
			this.sayln(t.id + ";");
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("};");
	}

	// program
	@Override
	public void visit(codegen.C.program.Program p) {
		// we'd like to output to a file, rather than the "stdout".
		try {
			String outputName = null;
			if (Control.outputName != null)
				outputName = Control.outputName;
			else if (Control.fileName != null) {
				int index = Control.fileName.indexOf("/");
				String tmp = Control.fileName;
				while (index != -1) {
					tmp = tmp.substring(index + 1);
					index = tmp.indexOf("/");
				}
				Control.outputName = outputName = "/tmp/" + tmp + ".c";
			} else
				Control.outputName = outputName = "/tmp/" + "a.c";

			System.out.format("write output file to %s\n", Control.outputName);
			this.writer = new java.io.BufferedWriter(
					new java.io.OutputStreamWriter(
							new java.io.FileOutputStream(outputName)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		this.sayln("// This is automatically generated by the Tiger compiler.");
		this.sayln("// Do NOT modify!\n");
		this.sayln("#include \"runtime.h\"\n");

		this.sayln("\n// structures");
		for (codegen.C.classs.T c : p.classes) {
			c.accept(this);
		}

		this.sayln("\n// vtables structures");
		for (codegen.C.vtable.T v : p.vtables) {
			v.accept(this);
		}
		this.sayln("");

		this.sayln("\n// methods");
		for (codegen.C.method.T m : p.methods) {
			m.accept(this);
		}
		this.sayln("");

		this.sayln("\n// vtables");
		for (codegen.C.vtable.T v : p.vtables) {
			outputVtable((codegen.C.vtable.Vtable) v);
		}
		this.sayln("");

		this.sayln("\n// main method");
		p.mainMethod.accept(this);

		try {
			this.writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
