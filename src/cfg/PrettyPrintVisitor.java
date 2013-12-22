package cfg;

import cfg.method.T;
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
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
		this.say(new Integer(operand.i).toString());
	}

	@Override
	public void visit(cfg.operand.Var operand) {
		if (operand.isField)
			this.say("this->");
		else if (operand.type instanceof cfg.type.Class
				|| operand.type instanceof cfg.type.IntArray) {
			this.say("__gc_frame.");
		}
		this.say(operand.id);
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
		this.say(s.dst + " = (");
		s.left.accept(this);
		this.say(" + ");
		s.right.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
		if (s.retType instanceof cfg.type.Class
				|| s.retType instanceof cfg.type.IntArray)
			this.say("__gc_frame.");
		this.say(s.dst + " = ");

		if (!s.obj.equals("this") && !s.isField)
			this.say("__gc_frame.");
		else if (s.isField)
			this.say("this->");

		this.say(s.obj);
		this.say("->vptr->" + s.f + "(");

		if (!s.obj.equals("this") && !s.isField)
			this.say("__gc_frame.");
		else if (s.isField)
			this.say("this->");

		this.say(s.obj);
		for (cfg.operand.T x : s.args) {
			this.say(", ");
			x.accept(this);
		}
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.Lt s) {
		this.say(s.dst + " = ");
		s.left.accept(this);
		this.say(" < ");
		s.right.accept(this);
		this.sayln(";");
	}

	@Override
	public void visit(cfg.stm.Move s) {
		if (s.isField)
			this.say("this->");
		else if ((s.type instanceof ast.type.IntArray || s.type instanceof ast.type.Class))
			this.say("__gc_frame.");
		this.say(s.dst + " = ");
		s.src.accept(this);
		this.sayln(";");
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
		this.sayln("__gc_frame." + s.dst + " = ((struct " + s.c
				+ "*)(Tiger_new (&" + s.c + "_vtable_, sizeof(struct " + s.c
				+ "))));");
	}

	@Override
	public void visit(cfg.stm.Print s) {
		this.say("System_out_println (");
		s.arg.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.Sub s) {
		this.say(s.dst + " = (");
		s.left.accept(this);
		this.say(" - ");
		s.right.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.Times s) {
		this.say(s.dst + " = (");
		s.left.accept(this);
		this.say(" * ");
		s.right.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.And s) {
		this.say(s.dst + " = (");
		s.left.accept(this);
		this.say(" && ");
		s.right.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.ArraySelect s) {
		this.say(s.dst + " = ");
		s.array.accept(this);
		this.say("->__data[");
		s.index.accept(this);
		this.sayln("];");
	}

	@Override
	public void visit(cfg.stm.Length s) {
		this.say(s.dst + " = ");
		s.array.accept(this);
		this.sayln("->__u.length;");
	}

	@Override
	public void visit(cfg.stm.NewIntArray s) {
		this.say("__gc_frame." + s.dst + " = Tiger_new_array(");
		s.exp.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.Not s) {
		this.say(s.dst + " = !(");
		s.exp.accept(this);
		this.sayln(");");
	}

	@Override
	public void visit(cfg.stm.AssignArray s) {
		if (s.isField)
			this.say("this->");
		else
			this.say("__gc_frame.");
		this.say(s.id + "->__data" + "[");
		s.index.accept(this);
		this.say("] = ");
		s.exp.accept(this);
		this.sayln(";");
	}

	// transfer
	@Override
	public void visit(cfg.transfer.If s) {
		this.say("if (");
		s.operand.accept(this);
		this.sayln(")");
		this.indent();
		this.printSpaces();
		this.sayln("goto " + s.truee.toString() + ";");
		this.unIndent();
		this.printSpaces();
		this.sayln("else");
		this.indent();
		this.printSpaces();
		this.sayln("goto " + s.falsee.toString() + ";");
		this.unIndent();
	}

	@Override
	public void visit(cfg.transfer.Goto s) {
		this.sayln("goto " + s.label.toString() + ";");
	}

	@Override
	public void visit(cfg.transfer.Return s) {
		// generate code to pop gc-frame
		this.sayln("gc_frame_prev = __gc_frame.__prev; // pop gc-frame");

		this.printSpaces();
		this.say("return ");
		s.operand.accept(this);
		this.sayln(";");
	}

	// type
	@Override
	public void visit(cfg.type.Class t) {
		this.say("struct " + t.id + " *");
	}

	@Override
	public void visit(cfg.type.Int t) {
		this.say("int");
	}

	@Override
	public void visit(cfg.type.IntArray t) {
		this.say("struct __tiger_obj_header *");
	}

	@Override
	public void visit(cfg.type.Boolean boolean1) {
		this.say("long");// if we use int here, the stack address is hard to
							// calculate
	}

	// dec
	@Override
	public void visit(cfg.dec.Dec d) {
		d.type.accept(this);
		this.say(" " + d.id);
	}

	// block
	@Override
	public void visit(cfg.block.Block b) {
		this.sayln(b.label.toString() + ":");
		for (cfg.stm.T s : b.stms) {
			printSpaces();
			s.accept(this);
		}
		this.printSpaces();
		b.transfer.accept(this);
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		m.retType.accept(this);
		this.say(" " + m.classId + "_" + m.id + "(");
		int size = m.formals.size();
		for (cfg.dec.T d : m.formals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			size--;
			dec.type.accept(this);
			this.say(" " + dec.id);
			if (size > 0)
				this.say(", ");
		}
		this.sayln(") {");
		this.indent();

		// generate gc-map for formals
		this.printSpaces();
		this.say("char *__arguments_gc_map = \"");
		for (cfg.dec.T f : m.formals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) f;
			if (dec.type instanceof cfg.type.Class
					|| dec.type instanceof cfg.type.IntArray)
				this.say("1");
			else
				this.say("0");
		}
		this.sayln("\"; // generate gc-map for formals");

		// generate gc-frame
		this.sayln("");
		this.printSpaces();
		this.sayln("// START generate gc-frame");
		this.printSpaces();
		this.sayln("struct {");
		this.indent();
		this.printSpaces();
		this.sayln("void *__prev;");
		this.printSpaces();
		this.sayln("char *__arguments_gc_map;");
		this.printSpaces();
		this.sayln("void *__arguments_base_address;");
		this.printSpaces();
		this.sayln("unsigned long __locals_gc_number;");
		// method specified fields(locals) of reference type
		for (cfg.dec.T d : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (dec.type instanceof cfg.type.IntArray
					|| dec.type instanceof cfg.type.Class) {
				this.printSpaces();
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
			}
		}
		/* *
		 * Make formals also looks like local.
		 * This is very ugly, but I'm too lazy to figure out how to
		 * pass enough information to let PrettyPrint to know how to
		 * generate right statement, because the framework is fixed,
		 * I just don't want to change the framework, cause I've spent
		 * 2 day in part A and part B in Lab5!!
		 */
		this.printSpaces();
		this.sayln("//make formals also looks like local");
		for (cfg.dec.T d : m.formals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (dec.type instanceof cfg.type.IntArray
					|| dec.type instanceof cfg.type.Class) {
				this.printSpaces();
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
			}
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("} __gc_frame;");
		this.printSpaces();
		this.sayln("// END generate gc-frame\n");

		// generate code to push gc-frame
		this.printSpaces();
		this.sayln("// START generate code to push gc-frame");
		this.printSpaces();
		this.sayln("memset(&__gc_frame, 0, sizeof(__gc_frame));//make sure reference init with NULL");
		this.printSpaces();
		this.sayln("__gc_frame.__prev = gc_frame_prev;");
		this.printSpaces();
		this.sayln("__gc_frame.__arguments_gc_map = __arguments_gc_map;");
		this.printSpaces();
		this.sayln("__gc_frame.__arguments_base_address = &this;");
		this.printSpaces();
		this.say("__gc_frame.__locals_gc_number = ");
		int __locals_gc_number = 0;
		for (cfg.dec.T f : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) f;
			if (dec.type instanceof cfg.type.Class
					|| dec.type instanceof cfg.type.IntArray)
				__locals_gc_number++;
		}
		for (cfg.dec.T d : m.formals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (dec.type instanceof cfg.type.IntArray
					|| dec.type instanceof cfg.type.Class) {
				__locals_gc_number++;
			}
		}
		this.say(new Integer(__locals_gc_number).toString());
		this.sayln(";");

		// make formals also looks like local
		this.printSpaces();
		this.sayln("//make formals also looks like local");
		for (cfg.dec.T d : m.formals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (dec.type instanceof cfg.type.IntArray
					|| dec.type instanceof cfg.type.Class) {
				this.printSpaces();
				this.sayln("__gc_frame." + dec.id + " = " + dec.id + ";");
			}
		}

		this.printSpaces();
		this.sayln("gc_frame_prev = (struct gc_frame_header *)&__gc_frame;");
		this.printSpaces();
		this.sayln("// END generate code to push gc-frame\n");

		// method specified fields(locals) of non-reference type
		this.printSpaces();
		this.sayln("// START method specified fields(locals) of non-reference type");
		for (cfg.dec.T d : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (!(dec.type instanceof cfg.type.Class || dec.type instanceof cfg.type.IntArray)) {
				this.printSpaces();
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
			}
		}
		this.printSpaces();
		this.sayln("// END method specified fields(locals) of non-reference type\n");

		// method body
		this.printSpaces();
		this.sayln("// START real body");
		for (cfg.block.T block : m.blocks) {
			block.accept(this);
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("}");
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		this.sayln("int Tiger_main(long __dummy) {");
		this.sayln("//'__dummy' is just a dummy argument to get base address of argument in main");
		this.indent();

		// generate gc-frame
		this.printSpaces();
		this.sayln("// START generate gc-frame");
		this.printSpaces();
		this.sayln("struct {");
		this.indent();
		this.printSpaces();
		this.sayln("void *__prev;");
		this.printSpaces();
		this.sayln("char *__arguments_gc_map;");
		this.printSpaces();
		this.sayln("void *__arguments_base_address;");
		this.printSpaces();
		this.sayln("unsigned long __locals_gc_number;");
		// method specified fields(locals) of reference type
		for (cfg.dec.T d : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (dec.type instanceof cfg.type.IntArray
					|| dec.type instanceof cfg.type.Class) {
				this.printSpaces();
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
			}
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("} __gc_frame;");
		this.printSpaces();
		this.sayln("// END generate gc-frame\n");

		// generate code to push gc-frame
		this.printSpaces();
		this.sayln("// START generate code to push gc-frame");
		this.printSpaces();
		this.sayln("memset(&__gc_frame, 0, sizeof(__gc_frame));//make sure reference init with NULL");
		this.printSpaces();
		this.sayln("__gc_frame.__prev = gc_frame_prev;");
		this.printSpaces();
		this.sayln("__gc_frame.__arguments_gc_map = NULL;");
		this.printSpaces();
		this.sayln("__gc_frame.__arguments_base_address = &__dummy;");
		this.printSpaces();
		this.say("__gc_frame.__locals_gc_number = ");
		int __locals_gc_number = 0;
		for (cfg.dec.T f : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) f;
			if (dec.type instanceof cfg.type.Class
					|| dec.type instanceof cfg.type.IntArray)
				__locals_gc_number++;
		}
		this.say(new Integer(__locals_gc_number).toString());
		this.sayln(";");
		this.printSpaces();
		this.sayln("gc_frame_prev = (struct gc_frame_header *)&__gc_frame;");
		this.printSpaces();
		this.sayln("// END generate code to push gc-frame\n");

		// method specified fields(locals) of non-reference type
		this.printSpaces();
		this.sayln("// START method specified fields(locals) of non-reference type");
		for (cfg.dec.T d : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			if (dec.type instanceof cfg.type.Int) {
				this.printSpaces();
				dec.type.accept(this);
				this.sayln(" " + dec.id + ";");
			}
		}
		this.printSpaces();
		this.sayln("// END method specified fields(locals) of non-reference type\n");

		this.printSpaces();
		this.sayln("// START real body");
		for (cfg.block.T block : m.blocks) {
			cfg.block.Block b = (cfg.block.Block) block;
			b.accept(this);
		}
		this.sayln("// END real body");

		this.unIndent();
		this.printSpaces();
		this.sayln("}");
	}

	// vtables
	@Override
	public void visit(cfg.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable {");
		this.indent();
		this.printSpaces();
		this.sayln("const char *__class_gc_map;");
		for (cfg.Ftuple t : v.ms) {
			this.printSpaces();
			t.ret.accept(this);
			this.sayln(" (*" + t.id + ")();");
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("};\n");
	}

	private void outputVtable(cfg.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = {");
		this.indent();

		// generate class gc map
		this.printSpaces();
		this.say("\"");
		cfg.classs.Class classs = (cfg.classs.Class) v.classs;
		for (cfg.Tuple dec : classs.decs) {
			if (dec.type instanceof cfg.type.IntArray
					|| dec.type instanceof cfg.type.Class)
				this.say("1");
			else
				this.say("0");
		}
		this.sayln("\",");

		for (cfg.Ftuple t : v.ms) {
			this.printSpaces();
			this.sayln(t.classs + "_" + t.id + ",");
		}
		this.unIndent();
		this.printSpaces();
		this.sayln("};\n");
	}

	// class
	@Override
	public void visit(cfg.classs.Class c) {
		this.sayln("struct " + c.id + " {");
		this.indent();
		this.printSpaces();
		this.sayln("struct " + c.id + "_vtable *vptr;");
		this.printSpaces();
		this.sayln("unsigned long times;");
		this.printSpaces();
		this.sayln("void *__forwarding;//used for gc");
		for (cfg.Tuple t : c.decs) {
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
	public void visit(cfg.program.Program p) {
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
		this.sayln("// Do NOT modify!");
		this.sayln("// Control-flow Graph\n");
		this.sayln("#include \"runtime.h\"\n");

		this.sayln("// structures");
		for (cfg.classs.T c : p.classes) {
			c.accept(this);
		}

		this.sayln("// vtables structures");
		for (cfg.vtable.T v : p.vtables) {
			v.accept(this);
		}
		this.sayln("");

		this.sayln("\n// declarations");
		for (T generalM : p.methods) {
			if (generalM instanceof cfg.method.Method) {
				cfg.method.Method m = (cfg.method.Method) generalM;
				m.retType.accept(this);
				this.say(" " + m.classId + "_" + m.id + "(");
				int size = m.formals.size();
				for (cfg.dec.T d : m.formals) {
					cfg.dec.Dec dec = (cfg.dec.Dec) d;
					size--;
					dec.type.accept(this);
					this.say(" " + dec.id);
					if (size > 0)
						this.say(", ");
				}
				this.sayln(");");
			} else {
				/* couldn't happen */
				System.err
						.println("fatal error, method is not of codegen.C.method.Method class");
				System.exit(3);
			}
		}
		this.sayln("");

		this.sayln("// vtables");
		for (cfg.vtable.T v : p.vtables) {
			outputVtable((cfg.vtable.Vtable) v);
		}
		this.sayln("");

		this.sayln("// methods");
		for (cfg.method.T m : p.methods) {
			m.accept(this);
		}
		this.sayln("");

		this.sayln("// main method");
		p.mainMethod.accept(this);
		this.sayln("");

		try {
			this.writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
