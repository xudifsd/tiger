package cfg.optimizations;

public class Main {
	public cfg.program.T program;

	public void accept(cfg.program.T cfg) {
		// liveness analysis
//		LivenessVisitor liveness = new LivenessVisitor();
//		control.CompilerPass livenessPass = new control.CompilerPass(
//				"Liveness analysis", cfg, liveness);
//		if (control.Control.skipPass("cfg.Linvess")) {
//		} else {
//			livenessPass.doit();
//			// we export it to later pass via public static field
//		}
//
//		// dead-code elimination
//		DeadCode deadCode = new DeadCode();
//		control.CompilerPass deadCodePass = new control.CompilerPass(
//				"Dead-code elimination", cfg, deadCode);
//		if (control.Control.skipPass("cfg.Linvess")
//				|| control.Control.skipPass("cfg.deadCode")) {
//			// deadCode needs livness
//		} else {
//			deadCodePass.doit();
//			cfg = deadCode.program;
//		}

		// reaching definition
		ReachingDefinition reachingDef = new ReachingDefinition();
		control.CompilerPass reachingDefPass = new control.CompilerPass(
				"Reaching definition", cfg, reachingDef);
		if (control.Control.skipPass("cfg.reaching")) {
		} else {
			reachingDefPass.doit();
			// Export necessary data structures
			// we export it to later pass via public static field
		}

		// constant propagation
		ConstProp constProp = new ConstProp();
		control.CompilerPass constPropPass = new control.CompilerPass(
				"Constant propagation", cfg, constProp);
		if (control.Control.skipPass("cfg.reaching")
				|| control.Control.skipPass("cfg.constProp")) {
			// constProp needs reach
		} else {
			constPropPass.doit();
			cfg = constProp.program;
		}

		// do reaching definition again for copyProp
		reachingDef = new ReachingDefinition();
		reachingDefPass = new control.CompilerPass(
				"Reaching definition for copyProp", cfg, reachingDef);
		if (control.Control.skipPass("cfg.reaching")) {
		} else {
			reachingDefPass.doit();
			// Export necessary data structures
			// we export it to later pass via public static field
		}

		// copy propagation
		CopyProp copyProp = new CopyProp();
		control.CompilerPass copyPropPass = new control.CompilerPass(
				"Copy propagation", cfg, copyProp);
		if (control.Control.skipPass("cfg.reaching")
				|| control.Control.skipPass("cfg.copyProp")) {
			// constProp needs reach
		} else {
			copyPropPass.doit();
			cfg = copyProp.program;
		}

		// available expression
		AvailExp availExp = new AvailExp();
		control.CompilerPass availExpPass = new control.CompilerPass(
				"Available expression", cfg, availExp);
		if (control.Control.skipPass("cfg.availExp")) {
		} else {
			availExpPass.doit();
			// Export necessary data structures
			// Your code here:
		}

		// CSE
		Cse cse = new Cse();
		control.CompilerPass csePass = new control.CompilerPass(
				"Common subexpression elimination", cfg, cse);
		if (control.Control.skipPass("cfg.cse")) {
		} else {
			csePass.doit();
			cfg = cse.program;
		}

		program = cfg;
	}
}
