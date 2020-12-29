package zw494.Assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import zw494.AST.Factory;
import zw494.Assembly.BinOp.OpType;
import zw494.Assembly.UnOp.UnOpType;
import zw494.Assembly.utils.AddTranslation;
import zw494.Assembly.utils.CjumpTranslation;
import zw494.Assembly.utils.CmpTranslation;
import zw494.Assembly.utils.MovTranslation;
import zw494.Assembly.utils.MemTranslation;
import zw494.Assembly.Register.regNames;
import zw494.edu.cornell.cs.cs4120.xic.ir.*;

/**
 * This is an iterator over the IR tree that we have generated, and its output
 * would be the abstract assembly code that has infinite registers.
 */
public class AssemblyIterator {

	public static HashMap<IRNode, Tile> tileMap = new HashMap<>();
	public static int line_num_count = 0;
	public final regNames[] const_regs = { Register.regNames.rdi, Register.regNames.rsi, Register.regNames.rdx,
			Register.regNames.rcx, Register.regNames.r8, Register.regNames.r9 };
	public final regNames[] func_ret_regs = { Register.regNames.rax, Register.regNames.rdx };

	/**
	 * Matches the string given with binary OpType of our assembly
	 */
	public static OpType MatchOptype(String op) {
		OpType ret_op = null;
		switch (op) {
			case "ADD":
				ret_op = OpType.addq;
				break;
			case "SUB":
				ret_op = OpType.subq;
				break;
			case "MUL":
				ret_op = OpType.imulq;
				break;
			case "AND":
				ret_op = OpType.andq;
				break;

			case "OR":
				ret_op = OpType.orq;
				break;

			case "XOR":
				ret_op = OpType.xorq;
				break;

			case "LSHIFT":
				ret_op = OpType.shlq;
				break;

			case "RSHIFT":
				ret_op = OpType.shrq;
				break;

			case "ARSHIFT":
				ret_op = OpType.sarq;
				break;

			default:

		}

		return ret_op;
	}

	public AssemblyIterator() {

	}

	/**
	 * The major translation function. Its output is a Tile, which is a collection
	 * of codes, costs, and returning registers. It uses the dynamic programming
	 * algorithm to find the optimal tiling with lowest costs.
	 */
	public Tile assemblyTranslate(IRNode x) {

		int cost = 0;
		ArrayList<Instr> instrs = new ArrayList<>();
		Arg ret_reg = null;
		Tile retval = null;
		ArrayList<Tile> options = new ArrayList<>();

		// dynamic programming!
		if (tileMap.get(x) != null)
			return tileMap.get(x);

		if (x instanceof IRConst) {

			IRConst y = (IRConst) x;
			ret_reg = new Imm(y.value());

			options.add(new Tile(cost, instrs, ret_reg));
		}

		// most binop translations are done separately in other functions, and
		// are optimized
		if (x instanceof IRBinOp) {

			IRBinOp y = (IRBinOp) x;
			IRExpr left = y.left();
			IRExpr right = y.right();
			Tile left_t = assemblyTranslate(left);
			Tile right_t = assemblyTranslate(right);
			Arg left_r = left_t.reg;
			Arg right_r = right_t.reg;

			

			instrs.addAll(left_t.instrs);
			instrs.addAll(right_t.instrs);

			// System.out.println("\n\n");
			// System.out.println("binop instrs");
			// for (Instr i : instrs) {
			// 	System.out.println(i.toString());
			// }
			// System.out.println("\n\n");

			cost += left_t.cost + right_t.cost;

			// if(left_r instanceof Imm){

			// 	if(((Imm)left_r).value > 32766 || ((Imm)left_r).value < -32767){
			// 		Register temp_r = new Register(Factory.tempFactory());
			// 		BinOp bigMove = new BinOp(OpType.movabs, left_r, temp_r);
			// 		System.out.println("big move left \n" + bigMove.toString());
			// 		// instrs.add(bigMove);
			// 		boolean bigmove = instrs.add(bigMove);
			// 		System.out.println("big move get added \n\n\n" + bigmove);
					
					
			// 		for (Instr i : instrs) {
			// 			System.out.println("\n\n");
			// 			System.out.println("move instrs");
			// 			System.out.println(i.toString());
			// 			System.out.println("\n\n");
			// 		}

			// 		System.out.println("\n\n\n");

			// 		left_t.reg = temp_r;
			// 		System.out.println("left reg \n" + left_t.reg.toString());
			// 		cost++;
			// 	}

			// }

			// if(right_r instanceof Imm){

			// 	if(((Imm)right_r).value > 32766 || ((Imm)right_r).value < -32767){
			// 		Register temp_r = new Register(Factory.tempFactory());
			// 		BinOp bigMove = new BinOp(OpType.movabs, right_r, temp_r);
			// 		System.out.println("big move right \n" + bigMove.toString());
			// 		// instrs.add(bigMove);
			// 		boolean bigmove = instrs.add(bigMove);
			// 		System.out.println("big move get added \n" + bigmove);


			// 		System.out.println("\n\n");
			// 		System.out.println("move instrs");
			// 		for (Instr i : instrs) {
			// 			System.out.println(i.toString());
			// 		}
			// 		System.out.println("\n\n");

			// 		System.out.println("\n\n\n");

			// 		right_t.reg = temp_r;
			// 		System.out.println("right reg \n" + right_t.reg.toString());
			// 		cost++;
			// 		System.out.println(x.label());
			// 	}

			// }

			switch (x.label()) {
				case "ADD":
					options.addAll(AddTranslation.AddTranslationAll(y));
					break;
				case "SUB":
					options.add(AddTranslation.AddTranslationOne(y, OpType.subq));
					break;
				case "MUL":
					options.add(AddTranslation.AddTranslationOne(y, OpType.imulq));
					break;

				case "HMUL":
					// SPECIAL CASE FOR REGISTER ALLOCATION
					// Before executing "hmul", content in rdx and rax should be removed
					// after executing "hmul", return register must be rdx
					// special attention to sign
					BinOp move1 = new BinOp(OpType.movq, left_r, new Register(regNames.rax));
					instrs.add(move1);
					cost++;

					Register temp_r = new Register(Factory.tempFactory());
					BinOp r_to_reg = new BinOp(OpType.movq, right_r, temp_r);
					instrs.add(r_to_reg);
					right_r = temp_r;
					cost++;

					UnOp mulq1 = new UnOp(UnOpType.imulq, right_r);
					ret_reg = new Register(regNames.rdx);
					cost += 2;
					instrs.add(mulq1);

					options.add(new Tile(cost, instrs, ret_reg));
					break;

				case "DIV":
					// SPECIAL CASE FOR REGISTER ALLOCATION
					// Before executing "div", content in rdx and rax should be removed
					// after executing "div", return register must be rax
					Arg mov = new Imm(0L);
					if (left_r instanceof Imm) {
						Imm w = (Imm) left_r;
						if (w.value < 0)
							mov = new Imm(-1L);
					} else {
						Register r = new Register(Factory.tempFactory());
						instrs.add(new BinOp(OpType.movq, left_r, r));
						instrs.add(new BinOp(OpType.sarq, new Imm(63L), r));
						mov = r;
						cost += 2; // special attention to sign extend
					}

					BinOp move2 = new BinOp(OpType.movq, mov, new Register(regNames.rdx));
					BinOp move3 = new BinOp(OpType.movq, left_r, new Register(regNames.rax));
					instrs.add(move2);
					instrs.add(move3);
					cost += 2;

					temp_r = new Register(Factory.tempFactory());
					r_to_reg = new BinOp(OpType.movq, right_r, temp_r);
					instrs.add(r_to_reg);
					right_r = temp_r;
					cost++;

					UnOp div1 = new UnOp(UnOpType.idivq, right_r);
					ret_reg = new Register(regNames.rax);
					cost += 2;
					instrs.add(div1);

					options.add(new Tile(cost, instrs, ret_reg));
					break;

				case "MOD":
					// SPECIAL CASE FOR REGISTER ALLOCATION
					// Before executing "mod", content in rdx and rax should be removed
					// after executing "mod", return register must be rdx
					mov = new Imm(0L);
					if (left_r instanceof Imm) {
						Imm w = (Imm) left_r;
						if (w.value < 0)
							mov = new Imm(-1L);
					} else {
						Register r = new Register(Factory.tempFactory());
						instrs.add(new BinOp(OpType.movq, left_r, r));
						instrs.add(new BinOp(OpType.sarq, new Imm(63L), r));
						mov = r;
						cost += 2; // special attention to sign extend
					}

					BinOp move4 = new BinOp(OpType.movq, mov, new Register(regNames.rdx));
					BinOp move5 = new BinOp(OpType.movq, left_r, new Register(regNames.rax));
					instrs.add(move4);
					instrs.add(move5);
					cost += 2;

					temp_r = new Register(Factory.tempFactory());
					r_to_reg = new BinOp(OpType.movq, right_r, temp_r);
					instrs.add(r_to_reg);
					right_r = temp_r;
					cost++;

					UnOp div2 = new UnOp(UnOpType.idivq, right_r);
					ret_reg = new Register(regNames.rdx);
					cost += 2;

					instrs.add(div2);
					options.add(new Tile(cost, instrs, ret_reg));
					break;

				case "AND":
					options.add(AddTranslation.AddTranslationOne(y, OpType.andq));
					break;

				case "OR":
					options.add(AddTranslation.AddTranslationOne(y, OpType.orq));
					break;

				case "XOR":
					options.add(AddTranslation.AddTranslationOne(y, OpType.xorq));
					break;

				// we do not use shifts in our IR translations, but they might
				// be useful in the future
				case "LSHIFT":
					temp_r = new Register(Factory.tempFactory());
					r_to_reg = new BinOp(OpType.movq, left_r, temp_r);
					instrs.add(r_to_reg);
					left_r = temp_r;
					cost++;

					BinOp lshift1 = new BinOp(OpType.shlq, right_r, left_r);
					ret_reg = left_r; // ??
					cost += 1;
					instrs.add(lshift1);

					options.add(new Tile(cost, instrs, ret_reg));
					break;

				case "RSHIFT":
					temp_r = new Register(Factory.tempFactory());
					r_to_reg = new BinOp(OpType.movq, left_r, temp_r);
					instrs.add(r_to_reg);
					left_r = temp_r;
					cost++;
					BinOp rshift1 = new BinOp(OpType.shrq, right_r, left_r);
					ret_reg = left_r; // ??
					cost += 1;
					instrs.add(rshift1);

					options.add(new Tile(cost, instrs, ret_reg));
					break;

				case "ARSHIFT":
					temp_r = new Register(Factory.tempFactory());
					r_to_reg = new BinOp(OpType.movq, left_r, temp_r);
					instrs.add(r_to_reg);
					left_r = temp_r;
					cost++;
					BinOp rashift1 = new BinOp(OpType.sarq, right_r, left_r);
					ret_reg = left_r; // ??
					cost += 1;
					instrs.add(rashift1);

					options.add(new Tile(cost, instrs, ret_reg));
					break;

				case "EQ":
					options.add(CmpTranslation.CmpTranslationOne(y, UnOpType.je));
					break;

				case "NEQ":
					options.add(CmpTranslation.CmpTranslationOne(y, UnOpType.jne));
					break;

				case "LT":
					options.add(CmpTranslation.CmpTranslationOne(y, UnOpType.jl));
					break;

				case "GT":
					options.add(CmpTranslation.CmpTranslationOne(y, UnOpType.jg));
					break;

				case "LEQ":
					options.add(CmpTranslation.CmpTranslationOne(y, UnOpType.jle));
					break;

				case "GEQ":
					options.add(CmpTranslation.CmpTranslationOne(y, UnOpType.jge));
					break;

				default:
			}
		}
		// mem, move are translated separately
		if (x instanceof IRMem) {
			IRMem y = (IRMem) x;

			options.addAll(MemTranslation.MemTranslationAll(y));

		}

		if (x instanceof IRMove) {
			IRMove y = (IRMove) x;
			options.addAll(MovTranslation.MovTranslationAll(y));
		}

		if (x instanceof IRTemp) {
			IRTemp y = (IRTemp) x;
			Register target_r = new Register(y.name());
			ret_reg = target_r;

			options.add(new Tile(cost, instrs, ret_reg));
		}

		if (x instanceof IRLabel) {
			IRLabel y = (IRLabel) x;
			instrs.add(new Label(y.name()));
			cost++;
			ret_reg = null;

			options.add(new Tile(cost, instrs, ret_reg));
		}
		if (x instanceof IRName) {
			IRName y = (IRName) x;
			ret_reg = new Name(y.name());

			options.add(new Tile(cost, instrs, ret_reg));
		}
		if (x instanceof IRJump) {

			IRJump y = (IRJump) x;
			Tile label = assemblyTranslate(y.target());
			instrs.addAll(label.instrs);
			cost += label.cost;

			assert (label.reg instanceof Name);

			Name l_instr = (Name) label.reg;
			String str = l_instr.name;

			instrs.add(new UnOp(UnOpType.jmp, str));
			cost++;

			ret_reg = null; // should be null also

			options.add(new Tile(cost, instrs, ret_reg));
		}
		// cjump are done separately
		if (x instanceof IRCJump) {
			IRCJump y = (IRCJump) x;
			options.add(CjumpTranslation.CjumpTranslationOne(y));
			Tile t = CjumpTranslation.CjumpTranslationTwo(y);
			if (t != null)
				options.add(t);
		}
		// Function call processing.
		if (x instanceof IRCallStmt) {
			IRCallStmt y = (IRCallStmt) x;
			LinkedList<Instr> extraArgs = new LinkedList<Instr>();
			// extra moving statements
			for (int i = 0; i < y.args().size(); i++) {
				IRExpr temp = y.args().get(i);
				Tile temp_ret = assemblyTranslate(temp);
				instrs.addAll(temp_ret.instrs);
				cost += temp_ret.cost;
				if (i < 6)
					instrs.add(new BinOp(OpType.movq, temp_ret.reg, new Register(const_regs[i])));
				else {
					int argnum = i;
					// when more than 6 arguments, push them to stack
					// using a dynamic stack
					// the push is done in reverse order
					extraArgs.add(0, new UnOp(UnOpType.pushq, temp_ret.reg));
					// System.out.println(extraArgs.get(0));
				}
				cost++;
			}
			int extraArgsNum = extraArgs.size();
			if (y.collectors().size() > 2) {
				for (int i = 2; i < y.collectors().size(); i++) {
					// push space for more than 2 return values to stack
					extraArgs.add(0, new UnOp(UnOpType.pushq, new Imm((long) 0)));
					cost++;
				}
			}
			int retOffsetInt = 0;
			if (extraArgs.size() % 2 == 1) {
				// 16 byte call alignment enforced
				extraArgs.add(0, new UnOp(UnOpType.pushq, new Imm((long) 0)));
				cost++;
				retOffsetInt = 1;
			}
			instrs.addAll(extraArgs);

			// translating the target
			Tile call_tile = assemblyTranslate(y.target());
			cost += call_tile.cost;

			assert (call_tile.reg instanceof Name);

			Name funcName = (Name) call_tile.reg;
			funcName.is_func = true;

			// call!
			instrs.add(new UnOp(UnOpType.callq, funcName));
			cost++;

			if (extraArgsNum > 0) {
				instrs.add(new BinOp(OpType.addq, new Imm((long) extraArgsNum * 8), new Register(regNames.rsp)));
				cost++;
			} // may be security vulnerability here: memory leak!
			int totalRegs = y.collectors().size();
			for (int i = 0; i < y.collectors().size(); i++) {
				// retrieving the return values
				if (i < 2) {
					// fit in two return registers
					instrs.add(new BinOp(OpType.movq, new Register(func_ret_regs[i]),
							new Register(y.collectors().get(i))));
					cost++;

					// System.out.println(instrs.get(instrs.size() - 1));
				} else {
					// get them from predetermined places on stack
					// int offset = (i-2)*8+extraArgsNum*8;
					int offset = (extraArgs.size() - extraArgsNum) * 8 + ((totalRegs) - i) * -8 - 8 * retOffsetInt;
					// r3
					// r2
					instrs.add(new BinOp(OpType.movq, new Mem(new Register(regNames.rsp), (long) offset),
							new Register(y.collectors().get(i))));
					cost++;
					// System.out.println(instrs.get(instrs.size() - 1));
				}
			}
			int w = extraArgs.size() - extraArgsNum;
			if (w > 0) {
				instrs.add(new BinOp(OpType.addq, new Imm((long) w * 8), new Register(regNames.rsp)));
				cost++;
			} // security vulnerability again

			ret_reg = null;

			options.add(new Tile(cost, instrs, ret_reg));
		}
		if (x instanceof IRReturn) {
			instrs.add(new MiscOp(MiscOp.OpType.ret));
			cost++;
			ret_reg = null;

			options.add(new Tile(cost, instrs, ret_reg));
		}

		if (x instanceof IRCompUnit) {
			IRCompUnit y = (IRCompUnit) x;
			Map<String, IRFuncDecl> funcs = y.functions();

			instrs.add(new Starter("	.text"));
			cost++;

			for (IRFuncDecl func : funcs.values()) {
				Tile func_tile = assemblyTranslate(func);
				instrs.addAll(func_tile.instrs);
				cost += func_tile.cost;
			}
			ret_reg = null;

			options.add(new Tile(cost, instrs, ret_reg));
		}

		if (x instanceof IRFuncDecl) {
			IRFuncDecl y = (IRFuncDecl) x;
			String func_name = y.name();

			instrs.add(new Starter("\n"));
			instrs.add(new Starter("	.globl	FUNC(" + func_name + ")"));
			instrs.add(new Starter("    .align  4"));
			instrs.add(new Starter("FUNC(" + func_name + "):"));
			cost += 4;

			Tile stmt_tile = assemblyTranslate(y.body());
			instrs.addAll(stmt_tile.instrs);
			cost += stmt_tile.cost;
			ret_reg = null;
			options.add(new Tile(cost, instrs, ret_reg));
		}

		if (x instanceof IRSeq) {

			IRSeq y = (IRSeq) x;

			for (IRStmt stmt : y.stmts()) {
				Tile stmt_tile = assemblyTranslate(stmt);
				instrs.addAll(stmt_tile.instrs);
				cost += stmt_tile.cost;
			}

			ret_reg = null;

			options.add(new Tile(cost, instrs, ret_reg));
		}

		// dynamic programming enforcement

		int max = Integer.MAX_VALUE;
		for (int i = 0; i < options.size(); i++) {
			Tile t = options.get(i);
			if (t.cost < max) {
				max = t.cost;
				retval = t;
			}
		}

		// notes that might be useful for debugging

		String[] info = x.toString().split("\\r?\\n");
		// retval.instrs.add(0, new Starter("#    " + line_num_count + "    test01.S"));
		retval.cost++;

		// // for (int i = 1; i < info.length + 1; ++i) {
		// // retval.instrs.add(i, new Starter("# " + info[i - 1]));
		// // }

		line_num_count++;

		// put dynamic programming optimal result into the tile map!

		tileMap.put(x, retval);

		return retval;
	}

}