package cfg.optimizations;

import java.util.LinkedList;

import cfg.block.T;
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
	public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
	public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

	// liveIn, liveOut for transfer
	public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveIn;
	public java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>> transferLiveOut;

	private boolean blockInChanged;
	private boolean blockOutChanged;

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

		this.stmLiveIn = new java.util.HashMap<cfg.stm.T, java.util.HashSet<String>>();
		this.stmLiveOut = new java.util.HashMap<cfg.stm.T, java.util.HashSet<String>>();

		this.transferLiveIn = new java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>>();
		this.transferLiveOut = new java.util.HashMap<cfg.transfer.T, java.util.HashSet<String>>();

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

		public Node(cfg.block.Block block) {
			this.block = block;
			in = new java.util.LinkedList<Node>();
			out = new java.util.LinkedList<Node>();
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
		}

		// we don't need anotherDic, but recursive function need empty HashMap
		java.util.HashMap<util.Label, Node> anotherDic;
		anotherDic = new java.util.HashMap<util.Label, Node>();
		// assume every blocks has at least one block
		Node root = generateGraph((cfg.block.Block) blocks.get(0), leaf,
				dictionary, anotherDic);

		// Now do the real work
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
		// Your code here:

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
		// Your code here:
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
