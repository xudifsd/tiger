package cfg.optimizations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import cfg.stm.And;
import cfg.stm.ArraySelect;
import cfg.stm.AssignArray;
import cfg.stm.Length;
import cfg.stm.NewIntArray;
import cfg.stm.Not;
import cfg.type.Boolean;

public class LivenessVisitor implements cfg.Visitor {
	// gen, kill for one statement
	private java.util.HashSet<String> oneStmGen;
	private java.util.HashSet<String> oneStmKill;

	// gen, kill for statements
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmGen;
	private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmKill;

	// gen, kill for transfers
	private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferGen;
	private java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferKill;

	// gen, kill for blocks
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockGen;
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockKill;

	// liveIn, liveOut for blocks
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockLiveIn;
	private java.util.HashMap<cfg.block.T, java.util.HashSet<String>> blockLiveOut;

	// liveIn, liveOut for statements
	public static java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
	public static java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

	// liveIn, liveOut for transfer
	public static java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveIn;
	public static java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveOut;

	// As you will walk the tree for many times, so
	// it will be useful to recored which is which:
	enum Liveness_Kind_t {
		None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
	}

	private Liveness_Kind_t kind = Liveness_Kind_t.None;

	public LivenessVisitor() {
		this.oneStmGen = null;
		this.oneStmKill = null;

		this.stmGen = new java.util.HashMap<cfg.stm.T, java.util.HashSet<String>>();
		this.stmKill = new java.util.HashMap<cfg.stm.T, java.util.HashSet<String>>();

		this.transferGen = new java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>>();
		this.transferKill = new java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>>();

		this.blockGen = new java.util.HashMap<cfg.block.T, java.util.HashSet<String>>();
		this.blockKill = new java.util.HashMap<cfg.block.T, java.util.HashSet<String>>();

		this.blockLiveIn = new java.util.HashMap<cfg.block.T, java.util.HashSet<String>>();
		this.blockLiveOut = new java.util.HashMap<cfg.block.T, java.util.HashSet<String>>();

		stmLiveIn = new java.util.HashMap<cfg.stm.T, java.util.HashSet<String>>();
		stmLiveOut = new java.util.HashMap<cfg.stm.T, java.util.HashSet<String>>();

		transferLiveIn = new java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>>();
		transferLiveOut = new java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>>();

		this.kind = Liveness_Kind_t.None;
	}

	// /////////////////////////////////////////////////////
	// operand
	@Override
	public void visit(cfg.operand.Int operand) {
	}

	@Override
	public void visit(cfg.operand.Var operand) {
		this.oneStmGen.add(operand.id);
	}

	// statements
	@Override
	public void visit(cfg.stm.Add s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
	}

	@Override
	public void visit(cfg.stm.InvokeVirtual s) {
		this.oneStmKill.add(s.dst);
		this.oneStmGen.add(s.obj);
		for (cfg.operand.T arg : s.args) {
			arg.accept(this);
		}
	}

	@Override
	public void visit(cfg.stm.Lt s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
	}

	@Override
	public void visit(cfg.stm.Move s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.src.accept(this);
	}

	@Override
	public void visit(cfg.stm.NewObject s) {
		this.oneStmKill.add(s.dst);
	}

	@Override
	public void visit(cfg.stm.Print s) {
		s.arg.accept(this);
	}

	@Override
	public void visit(cfg.stm.Sub s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
	}

	@Override
	public void visit(cfg.stm.Times s) {
		this.oneStmKill.add(s.dst);
		// Invariant: accept() of operand modifies "gen"
		s.left.accept(this);
		s.right.accept(this);
	}

	@Override
	public void visit(And s) {
		this.oneStmKill.add(s.dst);
		s.left.accept(this);
		s.right.accept(this);
	}

	@Override
	public void visit(ArraySelect s) {
		this.oneStmKill.add(s.dst);
		s.array.accept(this);
		s.index.accept(this);
	}

	@Override
	public void visit(Length s) {
		this.oneStmKill.add(s.dst);
		s.array.accept(this);
	}

	@Override
	public void visit(NewIntArray s) {
		this.oneStmKill.add(s.dst);
		s.exp.accept(this);
	}

	@Override
	public void visit(Not s) {
		this.oneStmKill.add(s.dst);
		s.exp.accept(this);
	}

	@Override
	public void visit(AssignArray s) {
		this.oneStmGen.add(s.id);
		s.index.accept(this);
		s.exp.accept(this);
	}

	// transfer
	@Override
	public void visit(cfg.transfer.If s) {
		s.operand.accept(this);
	}

	@Override
	public void visit(cfg.transfer.Goto s) {
	}

	@Override
	public void visit(cfg.transfer.Return s) {
		s.operand.accept(this);
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

	// dec
	@Override
	public void visit(cfg.dec.Dec d) {
	}

	@Override
	public void visit(Boolean b) {
	}

	// utility functions:
	private void calculateStmTransferGenKill(cfg.block.Block b) {
		for (cfg.stm.T s : b.stms) {
			this.oneStmGen = new java.util.HashSet<String>();
			this.oneStmKill = new java.util.HashSet<String>();
			s.accept(this);
			this.stmGen.put(s, this.oneStmGen);
			this.stmKill.put(s, this.oneStmKill);
			if (control.Control.isTracing("liveness.step1")) {
				System.out.print("gen, kill for statement: '");
				System.out.println(s.toString() + "'");
				System.out.print("gen is: {");
				for (String str : this.oneStmGen) {
					System.out.print(str + ", ");
				}
				System.out.print("}\nkill is: {");
				for (String str : this.oneStmKill) {
					System.out.print(str + ", ");
				}
				System.out.println("}\n");
			}
		}
		this.oneStmGen = new java.util.HashSet<String>();
		this.oneStmKill = new java.util.HashSet<String>();
		b.transfer.accept(this);
		this.transferGen.put(b.transfer, this.oneStmGen);
		this.transferKill.put(b.transfer, this.oneStmKill);
		if (control.Control.isTracing("liveness.step1")) {
			System.out.print("gen, kill for transfer: '");
			System.out.println(b.transfer.toString() + "'");
			System.out.print("gen is: {");
			for (String str : this.oneStmGen) {
				System.out.print(str + ", ");
			}
			System.out.print("}\nkill is: {");
			for (String str : this.oneStmKill) {
				System.out.print(str + ", ");
			}
			System.out.println("}\n");
		}
	}

	private void calculateBlockGenKill(cfg.block.Block b) {
		if (control.Control.isTracing("liveness.step2"))
			System.out.println("calculating BlockGenKill for block " + b.label);
		java.util.HashSet<String> currentBlockGen = new java.util.HashSet<String>();
		java.util.HashSet<String> currentBlockKill = new java.util.HashSet<String>();

		java.util.HashSet<String> oneTransferGen = this.transferGen
				.get(b.transfer);
		java.util.HashSet<String> oneTransferKill = this.transferKill
				.get(b.transfer);

		// init with transfer's gen and kill
		currentBlockGen.addAll(oneTransferGen);
		currentBlockKill.addAll(oneTransferKill);

		for (int i = b.stms.size() - 1; i >= 0; i--) {
			// reverse order
			cfg.stm.T stm = b.stms.get(i);
			java.util.HashSet<String> oneStmGen = this.stmGen.get(stm);
			java.util.HashSet<String> oneStmKill = this.stmKill.get(stm);

			currentBlockGen.removeAll(oneStmKill);
			currentBlockGen.addAll(oneStmGen);

			currentBlockKill.addAll(oneStmKill);
		}
		if (control.Control.isTracing("liveness.step2")) {
			System.out.print("    block gen is: {");
			for (String s : currentBlockGen)
				System.out.print(s + ", ");
			System.out.println("}");

			System.out.print("    block kill is: {");
			for (String s : currentBlockKill)
				System.out.print(s + ", ");
			System.out.println("}");
		}
		this.blockGen.put(b, currentBlockGen);
		this.blockKill.put(b, currentBlockKill);
	}

	/* *
	 * This class is use to create graph of blocks, and then use this to do
	 * reverse topo-order traverse
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

	private void calculateBlockInOut(LinkedList<cfg.block.T> blocks) {
		// record the leaf of graph
		java.util.LinkedList<Node> leaf = new java.util.LinkedList<Node>();
		java.util.HashMap<util.Label, cfg.block.Block> dictionary;
		dictionary = new java.util.HashMap<util.Label, cfg.block.Block>();

		for (cfg.block.T block : blocks) {
			cfg.block.Block b = (cfg.block.Block) block;
			dictionary.put(b.label, b);
			blockLiveIn.put(b, new java.util.HashSet<String>());
			blockLiveOut.put(b, new java.util.HashSet<String>());
		}

		// we don't need anotherDic, but recursive function need empty HashMap
		java.util.HashMap<util.Label, Node> anotherDic;
		anotherDic = new java.util.HashMap<util.Label, Node>();

		// assume every blocks has at least one block
		// we don't need to hold the root, because we have leaf to do this
		generateGraph((cfg.block.Block) blocks.get(0), leaf, dictionary,
				anotherDic);

		anotherDic = null;// allow it to GC

		// Now do the real work
		boolean changed;
		long times = 0;
		java.util.LinkedList<Node> workList; // for BFS
		workList = new java.util.LinkedList<Node>();
		do {
			changed = false;
			times++;
			workList.addAll(leaf);
			while (workList.size() > 0) {
				Node node = workList.removeFirst();
				if (node.visiedTimes == times)
					continue; // skip visited node

				// visit this node
				node.visiedTimes++;
				java.util.HashSet<String> currentLiveIn;
				currentLiveIn = blockLiveIn.get(node.block);

				java.util.HashSet<String> currentLiveOut;
				currentLiveOut = blockLiveOut.get(node.block);

				java.util.HashSet<String> currentGen;
				currentGen = blockGen.get(node.block);

				java.util.HashSet<String> currentKill;
				currentKill = blockGen.get(node.block);

				// start equation
				changed |= currentLiveIn.addAll(currentGen);

				@SuppressWarnings("unchecked")
				java.util.HashSet<String> out = (HashSet<String>) currentLiveOut
						.clone();
				out.removeAll(currentKill);

				changed |= currentLiveIn.addAll(out);

				// get currentLiveOut
				for (Node outNode : node.out) {
					cfg.block.Block outBlock = outNode.block;
					changed |= currentLiveOut.addAll(blockLiveIn.get(outBlock));
				}
				for (Node inNode : node.in)
					workList.addLast(inNode);
			}
		} while (changed);
		if (control.Control.isTracing("liveness.step3")) {
			Iterator<Entry<cfg.block.T, HashSet<String>>> it = blockLiveIn
					.entrySet().iterator();
			while (it.hasNext()) {
				Entry<cfg.block.T, HashSet<String>> entry = it.next();
				System.out.format("liveIn for block %s is %s\n",
						((cfg.block.Block) entry.getKey()).label,
						entry.getValue());
			}
			System.out.println();
			it = blockLiveOut.entrySet().iterator();
			while (it.hasNext()) {
				Entry<cfg.block.T, HashSet<String>> entry = it.next();
				System.out.format("liveOut for block %s is %s\n",
						((cfg.block.Block) entry.getKey()).label,
						entry.getValue());
			}
			System.out.println();
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

	@SuppressWarnings("unchecked")
	private void calculateStmInOut(cfg.block.Block b) {
		java.util.HashSet<String> currentBlockIn = blockLiveIn.get(b);
		java.util.HashSet<String> currentBlockOut = blockLiveOut.get(b);
		java.util.HashSet<String> belowOut = (HashSet<String>) currentBlockOut
				.clone();
		java.util.HashSet<String> belowIn = new java.util.HashSet<String>();

		if (b.transfer instanceof cfg.transfer.If) {
			cfg.transfer.If iff = (cfg.transfer.If) b.transfer;
			java.util.HashSet<String> currentTransferGen = transferGen.get(iff);

			transferLiveOut.put(iff, (HashSet<String>) belowOut.clone());
			belowOut.addAll(currentTransferGen);
			belowIn.addAll(currentTransferGen); // TODO what's the use of
												// belowIn?
			transferLiveIn.put(iff, (HashSet<String>) belowIn.clone());
		} else if (b.transfer instanceof cfg.transfer.Return) {
			cfg.transfer.Return returnn = (cfg.transfer.Return) b.transfer;
			java.util.HashSet<String> currentTransferGen = transferGen
					.get(returnn);

			transferLiveOut.put(returnn, (HashSet<String>) belowOut.clone());
			belowOut.addAll(currentTransferGen);
			belowIn.addAll(currentTransferGen); // TODO what's the use of
												// belowIn?
			transferLiveIn.put(returnn, (HashSet<String>) belowIn.clone());
		}

		java.util.HashSet<String> currentTransferLiveIn = transferLiveIn
				.get(b.transfer);
		java.util.HashSet<String> currentTransferLiveOut = transferLiveOut
				.get(b.transfer);
		if (control.Control.isTracing("liveness.step4")
				&& currentTransferLiveIn != null) {
			System.out.format("transfer '%s' liveIn in block %s is %s\n",
					b.transfer, b.label, currentTransferLiveIn);
			System.out.format("transfer '%s' liveOut in block %s is %s\n",
					b.transfer, b.label, currentTransferLiveOut);
		}

		// backwards visit
		for (int i = b.stms.size() - 1; i >= 0; i--) {
			cfg.stm.T stm = b.stms.get(i);
			java.util.HashSet<String> currentStmGen = stmGen.get(stm);
			java.util.HashSet<String> currentStmKill = stmKill.get(stm);

			stmLiveOut.put(stm, (HashSet<String>) belowOut.clone());
			belowOut.removeAll(currentStmKill);
			belowOut.addAll(currentStmGen);
			belowIn.addAll(currentStmGen); // TODO what's the use of belowIn?
			stmLiveIn.put(stm, (HashSet<String>) belowIn.clone());

			if (control.Control.isTracing("liveness.step4")) {
				System.out.format("stm liveIn in block %s of stm '%s' is %s\n",
						b.label, stm, stmLiveIn.get(stm));
				System.out.format(
						"stm liveOut in block %s of stm '%s' is %s\n\n",
						b.label, stm, stmLiveOut.get(stm));
			}
		}
	}

	// block
	@Override
	public void visit(cfg.block.Block b) {
		switch (this.kind) {
		case StmGenKill:
			calculateStmTransferGenKill(b);
			break;
		case BlockGenKill:
			calculateBlockGenKill(b);
			break;
		case StmInOut:
			calculateStmInOut(b);
			break;
		default:
			throw new RuntimeException("unknow Liveness_Kind_t: " + this.kind);
		}
	}

	// method
	@Override
	public void visit(cfg.method.Method m) {
		// Four steps:
		// Step 1: calculate the "gen" and "kill" sets for each
		// statement and transfer
		this.kind = Liveness_Kind_t.StmGenKill;
		for (cfg.block.T block : m.blocks)
			block.accept(this);

		// Step 2: calculate the "gen" and "kill" sets for each block.
		// For this, you should visit statements and transfers in a
		// block in a reverse order.
		this.kind = Liveness_Kind_t.BlockGenKill;
		for (cfg.block.T block : m.blocks)
			block.accept(this);

		// Step 3: calculate the "liveIn" and "liveOut" sets for each block
		// Note that to speed up the calculation, you should first
		// calculate a reverse topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		this.kind = Liveness_Kind_t.BlockInOut;
		calculateBlockInOut(m.blocks);

		// Step 4: calculate the "liveIn" and "liveOut" sets for each
		// statement and transfer
		this.kind = Liveness_Kind_t.StmInOut;
		for (cfg.block.T block : m.blocks)
			block.accept(this);
	}

	@Override
	public void visit(cfg.mainMethod.MainMethod m) {
		// Four steps:
		// Step 1: calculate the "gen" and "kill" sets for each
		// statement and transfer
		this.kind = Liveness_Kind_t.StmGenKill;
		for (cfg.block.T block : m.blocks) {
			block.accept(this);
		}

		// Step 2: calculate the "gen" and "kill" sets for each block.
		// For this, you should visit statements and transfers in a
		// block in a reverse order.
		this.kind = Liveness_Kind_t.BlockGenKill;
		for (cfg.block.T block : m.blocks)
			block.accept(this);

		// Step 3: calculate the "liveIn" and "liveOut" sets for each block
		// Note that to speed up the calculation, you should first
		// calculate a reverse topo-sort order of the CFG blocks, and
		// crawl through the blocks in that order.
		// And also you should loop until a fix-point is reached.
		this.kind = Liveness_Kind_t.BlockInOut;
		calculateBlockInOut(m.blocks);

		// Step 4: calculate the "liveIn" and "liveOut" sets for each
		// statement and transfer
		this.kind = Liveness_Kind_t.StmInOut;
		for (cfg.block.T block : m.blocks)
			block.accept(this);
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
		for (cfg.method.T mth : p.methods) {
			mth.accept(this);
		}
	}
}
