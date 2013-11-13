package ast;

public class Fac {
	// Lab2, exercise 2: read the following code and make
	// sure you understand how the sample program "test/Fac.java" is
	// represented.

	// /////////////////////////////////////////////////////
	// To represent the "Fac.java" program in memory manually
	// this is for demonstration purpose only, and
	// no one would want to do this in reality (boring and error-prone).
	/*
	 * class Factorial { public static void main(String[] a) {
	 * System.out.println(new Fac().ComputeFac(10)); } } class Fac { public int
	 * ComputeFac(int num) { int num_aux; if (num < 1) num_aux = 1; else num_aux
	 * = num * (this.ComputeFac(num-1)); return num_aux; } }
	 */

	// // main class: "Factorial"
	static ast.mainClass.MainClass factorial = new ast.mainClass.MainClass(
			"Factorial", "a", new ast.stm.Print(new ast.exp.Call(
					new ast.exp.NewObject("Fac", 2), "ComputeFac",
					new util.Flist<ast.exp.T>().addAll(new ast.exp.Num(10, 3)), 3), 2), 1);

	// // class "Fac"
	static ast.classs.Class fac = new ast.classs.Class(
			"Fac",
			null,
			new util.Flist<ast.dec.T>().addAll(),
			new util.Flist<ast.method.T>().addAll(new ast.method.Method(
					new ast.type.Int(2),
					"ComputeFac",
					new util.Flist<ast.dec.T>().addAll(new ast.dec.Dec(
							new ast.type.Int(5), "num", 3)),
					new util.Flist<ast.dec.T>().addAll(new ast.dec.Dec(
							new ast.type.Int(5), "num_aux", 3)),
					new util.Flist<ast.stm.T>().addAll(new ast.stm.If(
							new ast.exp.Lt(new ast.exp.Id("num", 5),
									new ast.exp.Num(1, 2), 3),
							new ast.stm.Assign("num_aux", new ast.exp.Num(1, 1), 3),
							/*
							 * class Factorial { public static void
							 * main(String[] a) { System.out.println(new
							 * Fac().ComputeFac(10)); } } class Fac { public int
							 * ComputeFac(int num) { int num_aux; if (num < 1)
							 * num_aux = 1; else num_aux = num *
							 * (this.ComputeFac(num-1)); return num_aux; } }
							 */
							new ast.stm.Assign(
									"num_aux",
									new ast.exp.Times(
											new ast.exp.Id("num", 2),
											new ast.exp.Call(
													new ast.exp.This(3),
													"ComputeFac",
													new util.Flist<ast.exp.T>()
															.addAll(new ast.exp.Sub(
																	new ast.exp.Id(
																			"num", 2),
																	new ast.exp.Num(
																			1, 2), 3)), 3), 3), 4), 4)),
					new ast.exp.Id("num_aux", 5), 3)), 1);

	// program
	public static ast.program.Program prog = new ast.program.Program(factorial,
			new util.Flist<ast.classs.T>().addAll(fac));

	// Lab2, exercise 2: you should write some code to
	// represent the program "test/Sum.java".
	// Your code here:

	// // main class: "Sum"
	static ast.mainClass.MainClass sum = new ast.mainClass.MainClass("Sum",
			"a", new ast.stm.Print(new ast.exp.Call(new ast.exp.NewObject(
					"Doit", 2), "doit",
					new util.Flist<ast.exp.T>().addAll(new ast.exp.Num(101, 4)), 3), 2), 1);

	// // class "Doit"
	static ast.classs.Class doit = new ast.classs.Class("Doit", null,
			new util.Flist<ast.dec.T>().addAll(),
			new util.Flist<ast.method.T>().addAll(new ast.method.Method(
					new ast.type.Int(1), "doit", new util.Flist<ast.dec.T>()
							.addAll(new ast.dec.Dec(new ast.type.Int(1), "n", 1)),
					new util.Flist<ast.dec.T>().addAll(new ast.dec.Dec(
							new ast.type.Int(1), "sum", 1), new ast.dec.Dec(
							new ast.type.Int(1), "i", 1)),
					new util.Flist<ast.stm.T>().addAll(
							new ast.stm.Assign("i", new ast.exp.Num(0, 1), 1),
							new ast.stm.Assign("sum", new ast.exp.Num(0, 1), 1),
							new ast.stm.While(
									new ast.exp.Lt(
											new ast.exp.Id("i", 1), new ast.exp.Id("n", 1), 1),
									new ast.stm.Assign("sum",
											new ast.exp.Add(new ast.exp.Id("sum", 1),
											new ast.exp.Id("i", 1), 1), 1), 1)),
					new ast.exp.Id("sum", 1), 1)), 1);

	// program
	public static ast.program.Program prog1 = new ast.program.Program(sum,
			new util.Flist<ast.classs.T>().addAll(doit));
}
