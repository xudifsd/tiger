package cfg.optimizations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;
import cfg.stm.T;
import cfg.type.Boolean;

public class ReachingDefinition implements cfg.Visitor {
	private java.util.HashMap<String, java.util.HashSet<cfg.stm.T>> oneMethodDef;
	private java.util.HashMap<cfg.method.T, java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>> methodsDef;
	private java.util.HashMap<String, java.util.HashSet<cfg.stm.T>> mainMehtodDef;
	private int step;

	// gen, kill for statements
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmGen;
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmKill;

	// gen, kill for blocks
	private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockGen;
	private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockKill;

	// in, out for blocks
	private java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockIn;
	public static java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>> blockOut;

	public static java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmIn;
	public static java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmOut;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void safeAddAll(java.util.HashSet from, java.util.HashSet to) {
		if (to == null)
			return;
		else
			from.addAll(to);
	}

	public ReachingDefinition() {
		methodsDef = new java.util.HashMap<cfg.method.T, java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>>();

		this.stmGen = new java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>>();
		this.stmKill = new java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>>();

		this.blockGen = new java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>>();
		this.blockKill = new java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>>();

		blockIn = new java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>>();
		blockOut = new java.util.HashMap<cfg.block.T, java.util.HashSet<cfg.stm.T>>();

		stmIn = new java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>>();
		stmOut = new java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>>();
	}

	// /////////////////////////////////////////////////////
	// utilities

	// /////////////////////////////////////////////////////
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
	}

	@Override
	public void visit(cfg.operand.Var operand) {
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
		if (step == 0) {
			if (s.isField)
				this.oneMethodDef
						.put(s.dst, new java.util.HashSet<cfg.stm.T>());
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.Lt s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.Move s) {
		if (step == 0) {
			if (s.isField)
				this.oneMethodDef
						.put(s.dst, new java.util.HashSet<cfg.stm.T>());
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.Print s) {
		if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.Sub s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(cfg.stm.Times s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(And s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(ArraySelect s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(Length s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(NewIntArray s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(Not s) {
		if (step == 0) {
			this.oneMethodDef.get(s.dst).add(s);
		} else if (step == 1) {
			java.util.HashSet<cfg.stm.T> gen = new java.util.HashSet<cfg.stm.T>();
			java.util.HashSet<cfg.stm.T> kill = new java.util.HashSet<cfg.stm.T>();
			kill.addAll(oneMethodDef.get(s.dst));
			kill.remove(s);
			gen.add(s);
			this.stmKill.put(s, kill);
			this.stmGen.put(s, gen);
		}
	}

	@Override
	public void visit(AssignArray s) {
		if (s.isField)
			this.oneMethodDef.put(s.id, new java.util.HashSet<cfg.stm.T>());
	}

	// transfer
	@Override
	public void visit(cfg.transfer.If s) {
	}

	@Override
	public void visit(cfg.transfer.Goto s) {
	}

	@Override
	public void visit(cfg.transfer.Return s) {
	}

	// type
	@Override
	public void visit(cfg.type.Class t) {
	}

	@Override
	public void visit(cfg.type.Int t) {
	}

	@Override
	public void visit(cfg.type.IntArray t) {
	}

	@Override
	public void visit(Boolean boolean1) {
	}

	// dec
	@Override
	public void visit(cfg.dec.Dec d) {
	}

	private void calculateBlockGenKill(cfg.block.Block b) {
		if (control.Control.isTracing("reach.step2"))
			System.out.println("calculating BlockGenKill for block " + b.label);
		java.util.HashSet<cfg.stm.T> currentBlockGen = new java.util.HashSet<cfg.stm.T>();
		java.util.HashSet<cfg.stm.T> currentBlockKill = new java.util.HashSet<cfg.stm.T>();

		for (int i = 0; i < b.stms.size(); i++) {
			cfg.stm.T stm = b.stms.get(i);
			java.util.HashSet<cfg.stm.T> oneStmGen = this.stmGen.get(stm);
			java.util.HashSet<cfg.stm.T> oneStmKill = this.stmKill.get(stm);

			safeAddAll(currentBlockGen, oneStmGen);
			safeAddAll(currentBlockKill, oneStmKill);
		}
		if (control.Control.isTracing("reach.step2")) {
			System.out.println("    block gen is: " + currentBlockGen);
			System.out.println("    block kill is: " + currentBlockKill);
		}
		this.blockGen.put(b, currentBlockGen);
		this.blockKill.put(b, currentBlockKill);
	}

	/* *
	 * This class is use to create graph of blocks, and then use this to do
	 * topo-order traverse
	 */
	class Node {
		public java.util.LinkedList<Node> in;
		public java.util.LinkedList<Node> out;
		public cfg.block.Block block;
		/* *
		 * This just like visited bit for traverse, but if we just use one bit,
		 * we need to clear all bit after one pass, so we could use long to
		 * represent the times we visited this node, if this field has value
		 * equals the times we traverse, we skip it.
		 */
		public long visiedTimes;

		public Node(cfg.block.Block block) {
			this.block = block;
			in = new java.util.LinkedList<Node>();
			out = new java.util.LinkedList<Node>();
			visiedTimes = 0;
		}
	}

	/* *
	 * This is a mutual recursive function
	 */
	private Node generateGraph(cfg.block.Block b,
			java.util.LinkedList<Node> leaf,
			java.util.HashMap<util.Label, cfg.block.Block> dic,
			java.util.HashMap<util.Label, Node> anotherDic) {

		Node result = new Node(b);
		anotherDic.put(b.label, result);
		if (b.transfer instanceof cfg.transfer.If) {
			cfg.transfer.If iff = (cfg.transfer.If) b.transfer;
			Node node = getNode(iff.truee, leaf, dic, anotherDic);
			result.out.add(node);
			node.in.add(result);

			node = getNode(iff.falsee, leaf, dic, anotherDic);
			result.out.add(node);
			node.in.add(result);
		} else if (b.transfer instanceof cfg.transfer.Goto) {
			cfg.transfer.Goto gotoo = (cfg.transfer.Goto) b.transfer;
			Node node = getNode(gotoo.label, leaf, dic, anotherDic);
			result.out.add(node);
			node.in.add(result);
		} else if (b.transfer instanceof cfg.transfer.Return) {
			leaf.add(result);
		} else {
			throw new RuntimeException("unknow type of cfg.transfer.T "
					+ b.transfer);
		}
		return result;
	}

	// util for generateGraph, is mutual recursive
	private Node getNode(util.Label label, java.util.LinkedList<Node> leaf,
			java.util.HashMap<util.Label, cfg.block.Block> dic,
			java.util.HashMap<util.Label, Node> anotherDic) {
		Node result;
		result = anotherDic.get(label);
		if (result == null) {
			cfg.block.Block b = dic.get(label);
			if (b == null)
				throw new RuntimeException(
						"couldn't find block in dictionary, maybe it's bug of calculateBlockInOut");
			result = generateGraph(b, leaf, dic, anotherDic);
			anotherDic.put(label, result);
			return result;
		} else
			return result;
	}

	private void calculateBlockInOut(java.util.LinkedList<cfg.block.T> blocks) {
		// record the leaf of graph
		java.util.LinkedList<Node> leaf = new java.util.LinkedList<Node>();
		java.util.HashMap<util.Label, cfg.block.Block> dictionary;
		dictionary = new java.util.HashMap<util.Label, cfg.block.Block>();

		for (cfg.block.T block : blocks) {
			cfg.block.Block b = (cfg.block.Block) block;
			dictionary.put(b.label, b);
			blockIn.put(b, new java.util.HashSet<cfg.stm.T>());
			blockOut.put(b, new java.util.HashSet<cfg.stm.T>());
		}

		// we don't need anotherDic, but recursive function need empty HashMap
		java.util.HashMap<util.Label, Node> anotherDic;
		anotherDic = new java.util.HashMap<util.Label, Node>();

		// assume every blocks has at least one block
		// we don't need to hold the root, because we have leaf to do this

		/* *
		 * NOTE: we need to regenerate Graph again, because LivenessVisitor's
		 * graph will be changed after DeadCode elimiation.
		 */
		Node root = generateGraph((cfg.block.Block) blocks.get(0), leaf,
				dictionary, anotherDic);

		anotherDic = null;// allow it to GC

		// Now do the real work
		boolean changed;
		long times = 0;
		java.util.LinkedList<Node> workList = new java.util.LinkedList<Node>();
		do {
			changed = false;
			times++;
			workList.add(root);
			while (workList.size() > 0) {
				Node currentNode = workList.removeFirst();
				if (currentNode.visiedTimes == times)
					continue; // skip visited node

				workList.addAll(currentNode.out);
				currentNode.visiedTimes++;
				java.util.HashSet<cfg.stm.T> curBlockIn;
				curBlockIn = blockIn.get(currentNode.block);
				java.util.HashSet<cfg.stm.T> curBlockOut;
				curBlockOut = blockOut.get(currentNode.block);

				changed |= curBlockOut.addAll(blockGen.get(currentNode.block));
				for (Node prev : currentNode.in) {
					java.util.HashSet<cfg.stm.T> prevOut;
					prevOut = blockOut.get(prev.block);
					changed |= curBlockIn.addAll(prevOut);

					java.util.HashSet<cfg.stm.T> diff;
					diff = new java.util.HashSet<cfg.stm.T>();
					diff.addAll(curBlockIn);
					diff.removeAll(this.blockKill.get(currentNode.block));
					changed |= curBlockOut.addAll(diff);
				}
			}
		} while (changed);

		if (control.Control.isTracing("reach.step3")) {
			for (cfg.block.T block : blocks) {
				cfg.block.Block b = (cfg.block.Block) block;
				System.out.println("in block " + b.label);
				System.out.format("    in is %s\n", blockIn.get(b));
				System.out.format("    out is %s\n", blockOut.get(b));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void calculateStmInOut(cfg.block.Block b) {
		java.util.HashSet<cfg.stm.T> aboveIn;
		// java.util.HashSet<cfg.stm.T> aboveOut;
		aboveIn = new java.util.HashSet<cfg.stm.T>();
		// aboveOut = new java.util.HashSet<cfg.stm.T>();

		aboveIn.addAll(this.blockIn.get(b));
		for (cfg.stm.T stm : b.stms) {
			java.util.HashSet<cfg.stm.T> curIn;
			curIn = (HashSet<T>) aboveIn.clone();
			this.stmIn.put(stm, curIn);
			// curIn.removeAll(c);
			java.util.HashSet<cfg.stm.T> curGen = this.stmGen.get(stm);
			java.util.HashSet<cfg.stm.T> curKill = this.stmKill.get(stm);
		}
		// b.transfer.accept(this);
	}

	// block
	@Override
	public void visit(cfg.block.Block b) {
		switch (step) {
		case 0:
		case 1:
			for (cfg.stm.T s : b.stms)
				s.accept(this);
			break;
		case 2:
			calculateBlockGenKill(b);
			break;
		case 4:
			calculateStmInOut(b);
			break;
		default:
			throw new RuntimeException("Unknow step in ReachingDefinition");
		}
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		// Five steps:
		// Step 0: for each argument or local variable "x" in the
		// method m, calculate x's definition site set def(x).
		step = 0; // calculate def
		oneMethodDef = new java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>();
		for (cfg.dec.T d : m.formals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			oneMethodDef.put(dec.id, new java.util.HashSet<cfg.stm.T>());
		}
		for (cfg.dec.T d : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			oneMethodDef.put(dec.id, new java.util.HashSet<cfg.stm.T>());
		}
		for (cfg.block.T b : m.blocks)
			b.accept(this);

		if (control.Control.isTracing("reach.step0")) {
			System.out.println("methodDef for mehthod " + m.id + " is :");
			Iterator<Entry<String, HashSet<T>>> it = oneMethodDef.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, HashSet<T>> entry = it.next();
				System.out.format("    %s: %s\n", entry.getKey(),
						entry.getValue());
			}
		}
		methodsDef.put(m, oneMethodDef);

		// Step 1: calculate the "gen" and "kill" sets for each
		// statement and transfer
		step = 1;
		for (cfg.block.T b : m.blocks)
			b.accept(this);

		if (control.Control.isTracing("reach.step1")) {
			System.out.println("in method " + m.id);
			for (cfg.block.T block : m.blocks) {
				cfg.block.Block b = (cfg.block.Block) block;
				for (cfg.stm.T stm : b.stms) {
					System.out.format("    gen for stm '%s' is %s\n", stm,
							stmGen.get(stm));
					System.out.format("    kill for stm '%s' is %s\n", stm,
							stmKill.get(stm));
				}
			}
		}

		// Step 2: calculate the "gen" and "kill" sets for each block.
		// For this, you should visit statements and transfers in a
		// block sequentially.
		step = 2;
		for (cfg.block.T block : m.blocks)
			block.accept(this);

		// Step 3: calculate the "in" and "out" sets for each block
		// Note that to speed up the calculation, you should use
		// a topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		step = 3;
		calculateBlockInOut(m.blocks);

		// Step 4: calculate the "in" and "out" sets for each
		// statement and transfer
		step = 4;
		oneMethodDef = methodsDef.get(m);
		for (cfg.block.T b : m.blocks)
			b.accept(this);
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		// Five steps:
		// Step 0: for each argument or local variable "x" in the
		// method m, calculate x's definition site set def(x).
		step = 0;
		oneMethodDef = new java.util.HashMap<String, java.util.HashSet<cfg.stm.T>>();
		for (cfg.dec.T d : m.locals) {
			cfg.dec.Dec dec = (cfg.dec.Dec) d;
			oneMethodDef.put(dec.id, new java.util.HashSet<cfg.stm.T>());
		}
		for (cfg.block.T b : m.blocks)
			b.accept(this);
		if (control.Control.isTracing("reach.step0")) {
			System.out.println("methodDef for main mehthod is :");
			Iterator<Entry<String, HashSet<T>>> it = oneMethodDef.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, HashSet<T>> entry = it.next();
				System.out.format("    %s: %s\n", entry.getKey(),
						entry.getValue());
			}
		}
		mainMehtodDef = oneMethodDef;

		// Step 1: calculate the "gen" and "kill" sets for each
		// statement and transfer
		step = 1;
		for (cfg.block.T b : m.blocks)
			b.accept(this);

		if (control.Control.isTracing("reach.step1")) {
			System.out.println("in main method");
			for (cfg.block.T block : m.blocks) {
				cfg.block.Block b = (cfg.block.Block) block;
				for (cfg.stm.T stm : b.stms) {
					System.out.format("    gen for stm '%s' is %s\n", stm,
							stmGen.get(stm));
					System.out.format("    kill for stm '%s' is %s\n", stm,
							stmKill.get(stm));
				}
			}
		}

		// Step 2: calculate the "gen" and "kill" sets for each block.
		// For this, you should visit statements and transfers in a
		// block sequentially.
		step = 2;
		for (cfg.block.T block : m.blocks)
			block.accept(this);

		// Step 3: calculate the "in" and "out" sets for each block
		// Note that to speed up the calculation, you should use
		// a topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		step = 3;
		calculateBlockInOut(m.blocks);

		// Step 4: calculate the "in" and "out" sets for each
		// statement and transfer
		step = 4;
		oneMethodDef = mainMehtodDef;
		for (cfg.block.T b : m.blocks)
			b.accept(this);
	}

	// vtables
	@Override
	public void visit(cfg.vtable.Vtable v) {
	}

	// class
	@Override
	public void visit(cfg.classs.Class c) {
	}

	// program
	@Override
	public void visit(cfg.program.Program p) {
		p.mainMethod.accept(this);
		for (cfg.classs.T classs : p.classes)
			classs.accept(this);
		for (cfg.method.T m : p.methods)
			m.accept(this);
	}
}
