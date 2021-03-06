package ast.optimizations;

// Constant folding optimizations on an AST.

public class ConstFold implements ast.Visitor {
	public ast.program.T program;

	public ConstFold() {
		this.program = null;
	}

	// expressions
	@Override
	public void visit(ast.exp.Add e) {
	}

	@Override
	public void visit(ast.exp.And e) {
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
	}

	@Override
	public void visit(ast.exp.Call e) {
		return;
	}

	@Override
	public void visit(ast.exp.False e) {
	}

	@Override
	public void visit(ast.exp.Id e) {
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {
	}

	@Override
	public void visit(ast.exp.Lt e) {
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
	}

	@Override
	public void visit(ast.exp.Num e) {
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {

		return;
	}

	@Override
	public void visit(ast.exp.True e) {
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {

		return;
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
	}

	@Override
	public void visit(ast.stm.Block s) {
	}

	@Override
	public void visit(ast.stm.If s) {

		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
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
		return;
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		return;
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {

		return;
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {

		return;
	}

	// program
	@Override
	public void visit(ast.program.Program p) {

		// NOTE we did Constant Folding in AlgSimp.java so we just skip this
		this.program = p;

		if (control.Control.isTracing("ast.ConstFold")) {
			System.out.println("before ast.ConstFold optimization:");
			ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
			p.accept(pp);
			System.out.println("after ast.ConstFold optimization: we did Constant Folding in AlgSimp.java, so this will looks same");
			this.program.accept(pp);
		}
		return;
	}
}
