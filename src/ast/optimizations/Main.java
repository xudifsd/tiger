package ast.optimizations;

public class Main {
	public ast.program.T program;

	public void optimize(ast.program.T ast) {
		DeadClass dceVisitor = new DeadClass();
		control.CompilerPass deadClassPass = new control.CompilerPass(
				"Dead class elimination", ast, dceVisitor);
		if (control.Control.skipPass("ast.DeadClass")) {
		} else {
			deadClassPass.doit();
			ast = dceVisitor.program;
		}

		DeadCode dcodeVisitor = new DeadCode();
		control.CompilerPass deadCodePass = new control.CompilerPass(
				"Dead code elimination", ast, dcodeVisitor);
		if (control.Control.skipPass("ast.DeadCode")) {
		} else {
			deadCodePass.doit();
			ast = dcodeVisitor.program;
		}

		AlgSimp algVisitor = new AlgSimp();
		control.CompilerPass algPass = new control.CompilerPass(
				"Algebraic simplification", ast, algVisitor);
		if (control.Control.skipPass("ast.AlgSimp")) {
		} else {
			algPass.doit();
			ast = algVisitor.program;
		}

		ConstFold cfVisitor = new ConstFold();
		control.CompilerPass constFoldPass = new control.CompilerPass(
				"Const folding", ast, cfVisitor);
		if (control.Control.skipPass("ast.ConstFold")) {
		} else {
			// we already did constant folding in AlgSimp, this is just a nop
			constFoldPass.doit();
			ast = cfVisitor.program;
		}

		// because dead code elimination could generate some dead class
		DeadClass dceVisitor2 = new DeadClass();
		control.CompilerPass deadClassPass2 = new control.CompilerPass(
				"Dead class elimination", ast, dceVisitor2);
		if (control.Control.skipPass("ast.DeadClass")) {
		} else {
			deadClassPass2.doit();
			ast = dceVisitor.program;
		}

		program = ast;
	}
}
