package zw494.Assembly;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import zw494.Assembly.Register.regNames;
import zw494.Assembly.UnOp.UnOpType;

public class RegisterSpiller {

	List<LinkedList<Instr>> funcs = new LinkedList<LinkedList<Instr>>();
	List<LinkedList<Instr>> newfuncs = new LinkedList<LinkedList<Instr>>();
	List<LinkedList<Instr>> prologues = new LinkedList<LinkedList<Instr>>();

	public Tile spill(Tile t) {

		// deepclone all the instructions, because some of them
		// reuse register or arg objects that need to be renamed
		List<Instr> instrs = t.instrs;
		List<Instr> temp = new ArrayList<Instr>();
		for (Instr i : instrs) {
			temp.add(i.copy());
		}

		instrs = temp;

		// replace arguments/returns with built in SystemV registers
		replaceRegisters(instrs, "_ARG0", regNames.rdi);
		replaceRegisters(instrs, "_ARG1", regNames.rsi);
		replaceRegisters(instrs, "_ARG2", regNames.rdx);
		replaceRegisters(instrs, "_ARG3", regNames.rcx);
		replaceRegisters(instrs, "_ARG4", regNames.r8);
		replaceRegisters(instrs, "_ARG5", regNames.r9);

		replaceRegisters(instrs, "_RET0", regNames.rax);
		replaceRegisters(instrs, "_RET1", regNames.rdx);

		for (Instr i : instrs) {
			if (i.toString().length() > 0 && i.toString().charAt(0) != '#') {
				// System.out.println(i);
			}
		}

		// this loop does 2 things
		// 1. it separates the code into each individual fucntion so we can give them
		// prologues and epilogues
		// 2. it puts the barebones structure in place into finalInstrs so we can
		// replace the func lines
		// with the new generated funcs we create later
		List<Instr> finalInstrs = new LinkedList<Instr>();
		for (int i = 0; i < instrs.size(); i++) {
			if (instrs.get(i) instanceof Starter) {
				Starter s = (Starter) instrs.get(i);
				if (s.name.length() >= 5 && s.name.substring(0, 5).equals("FUNC(")) {
					LinkedList<Instr> func = new LinkedList<Instr>();
					func.add(instrs.get(i));
					while (true) {

						i++;
						if (i >= instrs.size()) {
							finalInstrs.add(s);
							funcs.add(func);
							break;
						}
						if (instrs.get(i) instanceof Starter) {
							Starter s2 = (Starter) instrs.get(i);
							if (s2.name.equals("\n")) {
								finalInstrs.add(s);
								finalInstrs.add(s2);
								funcs.add(func);
								break;
							}
						}
						func.add(instrs.get(i));
					}
				} else {
					finalInstrs.add(s);
				}

			}
		}

		// this loop iterates through the functions and does the register allocation
		for (int i = 0; i < funcs.size(); i++) {

			// this stack offset is so we know how much to offset fetches just in case of a
			// push statement or other
			// change to the stack register
			int stackOffset = 0;

			LinkedList<Instr> func = funcs.get(i);
			LinkedList<String> r = getRegisters(func);

			// this code separates all extra agrs registers 7 and above
			List<String> argRegs = new LinkedList<String>();
			for (String s : r) {
				if (s.contains("_ARG")) {
					argRegs.add(s);
				}
			}
			// this replaces those arg regs with the appropriate memory location on the
			// stack instead
			for (String s : argRegs) {
				int offset = (Integer.parseInt(s.replace("_ARG", "")) - 6) * 8 + 16;
				replaceRegistersWithMem(func, s, regNames.rbp, offset);
			}

			int extraArgsNum = argRegs.size();

			// same as args, just for the return reigsters
			List<String> retRegs = new LinkedList<String>();
			for (String s : r) {
				if (s.contains("_RET")) {
					retRegs.add(s);
				}
			}
			// System.out.println("EXTRA ARGS NUM: " + extraArgsNum);
			for (String s : retRegs) {
				int offset = (Integer.parseInt(s.replace("_RET", "")) - 2) * 8 + 16 + extraArgsNum * 8;
				replaceRegistersWithMem(func, s, regNames.rbp, offset);
			}

			// this creates the prologue for each function, which moves the stack pointer
			// down enough to
			// make room for all the temps
			LinkedList<Instr> prologue = new LinkedList<Instr>();
			// this is the amount of temp registrs there are that need to be allocated for
			int size = (r.size() + 8) * 8;
			if (size % 16 != 0) {
				size += 8;
			}
			prologue.add(new UnOp(UnOp.UnOpType.pushq, new Register(regNames.rbp)));
			prologue.add(new BinOp(BinOp.OpType.movq, new Register(regNames.rsp), new Register(regNames.rbp)));
			prologue.add(new BinOp(BinOp.OpType.subq, new Imm((long) size), new Register(regNames.rsp)));

			prologue.add(new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r15),
					new Mem(new Register(regNames.rbp), (long) -16)));
			// this is the new function that we iteratively build up. we start by adding the
			// decl and the prologue.
			LinkedList<Instr> newfunc = new LinkedList<Instr>();
			newfunc.add(func.get(0));
			newfunc.addAll(prologue);

			// this loop goes through each instruction in the func and edits it to allocate
			// for the temo
			for (int j = 1; j < func.size(); j++) {

				Instr in = func.get(j);
				LinkedList<Instr> wrapper = new LinkedList<Instr>();
				wrapper.add(in);
				LinkedList<String> instregs = getRegisters(wrapper);

				// this handles the epilogue, which puts back the callee saved registers into
				// place
				if (instregs.size() == 0) {
					if (in instanceof MiscOp) {
						MiscOp m = (MiscOp) in;
						if (m.op == zw494.Assembly.MiscOp.OpType.ret) {
							newfunc.add(new BinOp(zw494.Assembly.BinOp.OpType.movq,
									new Mem(new Register(regNames.rbp), (long) -16), new Register(regNames.r15)));
							newfunc.add(new MiscOp(zw494.Assembly.MiscOp.OpType.leave));
						}
					}
					newfunc.add(in);
					// this checks if the %rsp was changed, and adjusts accordingly
					stackOffset += stackOffsetAddition(in);
				}

				// allocation for instructions that use 1 register
				if (instregs.size() == 1) {

					BinOp load1 = new BinOp(zw494.Assembly.BinOp.OpType.movq,
							new Mem(new Register(regNames.rsp), (long) getStackOffset(instregs.get(0), r, stackOffset)),
							new Register(regNames.r10));

					replaceRegisters(wrapper, instregs.get(0), regNames.r10);

					newfunc.add(load1);

					newfunc.add(in);
					stackOffset += stackOffsetAddition(in);

					BinOp putback1 = new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r10), new Mem(
							new Register(regNames.rsp), (long) getStackOffset(instregs.get(0), r, stackOffset)));

					newfunc.add(putback1);

				}

				// allocation for instructions that use 2 registers
				if (instregs.size() == 2) {

					BinOp load1 = new BinOp(zw494.Assembly.BinOp.OpType.movq,
							new Mem(new Register(regNames.rsp), (long) getStackOffset(instregs.get(0), r, stackOffset)),
							new Register(regNames.r10));
					BinOp load2 = new BinOp(zw494.Assembly.BinOp.OpType.movq,
							new Mem(new Register(regNames.rsp), (long) getStackOffset(instregs.get(1), r, stackOffset)),
							new Register(regNames.r11));
					replaceRegisters(wrapper, instregs.get(0), regNames.r10);
					replaceRegisters(wrapper, instregs.get(1), regNames.r11);

					newfunc.add(load1);
					newfunc.add(load2);

					newfunc.add(in);
					stackOffset += stackOffsetAddition(in);

					BinOp putback1 = new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r10), new Mem(
							new Register(regNames.rsp), (long) getStackOffset(instregs.get(0), r, stackOffset)));
					BinOp putback2 = new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r11), new Mem(
							new Register(regNames.rsp), (long) getStackOffset(instregs.get(1), r, stackOffset)));

					newfunc.add(putback1);
					newfunc.add(putback2);

				}
				// allocation for instructions that use 3 registers, the maximum possible
				if (instregs.size() == 3) {
					BinOp load1 = new BinOp(zw494.Assembly.BinOp.OpType.movq,
							new Mem(new Register(regNames.rsp), (long) getStackOffset(instregs.get(0), r, stackOffset)),
							new Register(regNames.r10));

					BinOp load2 = new BinOp(zw494.Assembly.BinOp.OpType.movq,
							new Mem(new Register(regNames.rsp), (long) getStackOffset(instregs.get(1), r, stackOffset)),
							new Register(regNames.r11));

					BinOp load3 = new BinOp(zw494.Assembly.BinOp.OpType.movq,
							new Mem(new Register(regNames.rsp), (long) getStackOffset(instregs.get(2), r, stackOffset)),
							new Register(regNames.r15));

					replaceRegisters(wrapper, instregs.get(0), regNames.r10);
					replaceRegisters(wrapper, instregs.get(1), regNames.r11);
					replaceRegisters(wrapper, instregs.get(2), regNames.r15);

					newfunc.add(load1);
					newfunc.add(load2);
					newfunc.add(load3);

					newfunc.add(in);
					stackOffset += stackOffsetAddition(in);

					BinOp putback1 = new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r10), new Mem(
							new Register(regNames.rsp), (long) getStackOffset(instregs.get(0), r, stackOffset)));
					BinOp putback2 = new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r11), new Mem(
							new Register(regNames.rsp), (long) getStackOffset(instregs.get(1), r, stackOffset)));
					BinOp putback3 = new BinOp(zw494.Assembly.BinOp.OpType.movq, new Register(regNames.r15), new Mem(
							new Register(regNames.rsp), (long) getStackOffset(instregs.get(2), r, stackOffset)));

					newfunc.add(putback1);
					newfunc.add(putback2);
					newfunc.add(putback3);
				}
			}

			// add the newly generated func to the list of all newly allocated functions
			newfuncs.add(newfunc);
			// System.out.println("\n\n");
		}

		// this code takes the final istructions template we made before, and creates a
		// new list
		// repalcing the skeleton with the new functions we just made
		ArrayList<Instr> replacedFinalInstrs = new ArrayList<Instr>();
		for (int i = 0; i < finalInstrs.size(); i++) {
			if (finalInstrs.get(i) instanceof Starter) {
				Starter s = (Starter) finalInstrs.get(i);
				if (s.name.length() >= 5 && s.name.substring(0, 5).equals("FUNC(")) {
					replacedFinalInstrs.addAll(newfuncs.remove(0));
				} else {
					replacedFinalInstrs.add(finalInstrs.get(i));
				}
			} else {
				replacedFinalInstrs.add(finalInstrs.get(i));
			}
		}

		t.instrs = replacedFinalInstrs;
		return t;

	}

	// this function simply calculates the offset on the stack for a given temp and
	// a given list of registers
	public int getStackOffset(String register, LinkedList<String> regs, int stackOffset) {
		return regs.indexOf(register) * 8 + 8 + stackOffset;
	}

	// this function checks if a given instruction edits the stack pointer, and
	// calcualates the offset accordingly
	// as to adjust future fetches from the stack using the stack pointer
	public int stackOffsetAddition(Instr in) {
		if (in instanceof UnOp) {
			UnOp u = (UnOp) in;
			if (u.op == UnOpType.pushq) {
				return 8;
			}
		}
		// TODO: add more checks for stack pointer editing
		if (in instanceof BinOp) {
			BinOp b = (BinOp) in;
			if (b.arg2 instanceof Register) {
				Register reg = (Register) b.arg2;
				if (reg.getRegNames() == regNames.rsp) {
					if (b.arg1 instanceof Imm) {
						Imm im = (Imm) b.arg1;
						return (int) (0 - (im.value));
					} else {
						System.out.println("FICK, NON IMM");
					}
				}

			}

		}
		return 0;
	}

	// this function pulls out the list of all registers used from a given list of
	// instructions
	// the list can be a function, or simple one instruction wrapped in a list
	public LinkedList<String> getRegisters(List<Instr> instrs) {
		LinkedList<String> regs = new LinkedList<String>();
		for (int i = 0; i < instrs.size(); i++) {
			Instr instr = instrs.get(i);
			if (instr instanceof BinOp) {
				BinOp b = (BinOp) instr;
				addRegsFromArg(regs, b.arg1);
				addRegsFromArg(regs, b.arg2);
			} else if (instr instanceof Label) {

			} else if (instr instanceof MiscOp) {

			} else if (instr instanceof Starter) {

			} else if (instr instanceof UnOp) {
				UnOp u = (UnOp) instr;
				addRegsFromArg(regs, u.arg1);

			} else {
				System.out.println("FICK!" + instr.getClass());
			}
		}
		return regs;

	}

	// helper function for get registers to pull out registers from args
	public void addRegsFromArg(LinkedList<String> regs, Arg a) {
		if (a instanceof Register) {
			Register r = (Register) a;
			if (r.getRegNames() == null) {
				if (!regs.contains(r.getArgName())) {
					regs.add(r.getArgName());
				}
			}
		} else if (a instanceof Mem) {
			Mem m = (Mem) a;
			if (m.r1 != null) {
				addRegsFromArg(regs, m.r1);
			}
			if (m.r2 != null) {
				addRegsFromArg(regs, m.r2);
			}

		} else if (a instanceof Imm) {

		} else if (a instanceof Name) {

		} else {
			if (a != null) {
				System.out.println("FICK!" + a.getClass());
			} else {
				// System.out.println("FICK!null");
			}
		}
	}

	// similar to get registers, except it replaces temp registers with real x86
	// registers deisgnated by
	// original and repl
	public LinkedList<String> replaceRegisters(List<Instr> instrs, String original, regNames repl) {
		LinkedList<String> regs = new LinkedList<String>();
		for (int i = 0; i < instrs.size(); i++) {
			Instr instr = instrs.get(i);
			if (instr instanceof BinOp) {
				BinOp b = (BinOp) instr;
				replaceRegsFromArg(regs, b.arg1, original, repl);
				replaceRegsFromArg(regs, b.arg2, original, repl);
			} else if (instr instanceof Label) {

			} else if (instr instanceof MiscOp) {

			} else if (instr instanceof Starter) {

			} else if (instr instanceof UnOp) {
				UnOp u = (UnOp) instr;
				replaceRegsFromArg(regs, u.arg1, original, repl);

			} else {
				System.out.println("FICK!" + instr.getClass());
			}
		}
		return regs;

	}

	// helper function for replaceregisters
	public void replaceRegsFromArg(LinkedList<String> regs, Arg a, String original, regNames repl) {
		if (a instanceof Register) {
			Register r = (Register) a;
			if (r.getRegNames() == null) {
				/*
				 * if (!regs.contains(r.argName)) { regs.add(r.argName); }
				 */
				if (r.getArgName().equals(original)) {
					// System.out.println("repl1");
					r.setRegName(repl);
				}
			}
		} else if (a instanceof Mem) {
			Mem m = (Mem) a;
			if (m.r1 != null) {
				replaceRegsFromArg(regs, m.r1, original, repl);
			}
			if (m.r2 != null) {
				replaceRegsFromArg(regs, m.r2, original, repl);
			}

		} else if (a instanceof Imm) {

		} else if (a instanceof Name) {

		} else {
			if (a != null) {
				System.out.println("FICK!" + a.getClass());
			} else {
				// System.out.println("FICK!null");
			}
		}
	}

	// this function takes a string temp register, and replaces it with a memory
	// offset
	// used for replacing args and rets with their stack locations
	public LinkedList<String> replaceRegistersWithMem(List<Instr> instrs, String original, regNames repl, long offset) {
		LinkedList<String> regs = new LinkedList<String>();
		for (int i = 0; i < instrs.size(); i++) {
			Instr instr = instrs.get(i);
			if (instr instanceof BinOp) {
				BinOp b = (BinOp) instr;
				if (b.arg1 instanceof Register) {
					Register r = (Register) b.arg1;
					if (r.getArgName() != null && r.getArgName().equals(original)) {
						b.arg1 = new Mem(new Register(repl), offset);
					}

				}
				if (b.arg2 instanceof Register) {
					Register r = (Register) b.arg2;
					if (r.getArgName() != null && r.getArgName().equals(original)) {
						b.arg2 = new Mem(new Register(repl), offset);
					}

				}
			} else if (instr instanceof Label) {

			} else if (instr instanceof MiscOp) {

			} else if (instr instanceof Starter) {

			} else if (instr instanceof UnOp) {
				UnOp u = (UnOp) instr;
				if (u.arg1 instanceof Register) {
					Register r = (Register) u.arg1;
					if (r.getArgName() != null && r.getArgName().equals(original)) {
						u.arg1 = new Mem(new Register(repl), offset);
					}

				}

			} else {
				System.out.println("FICK!" + instr.getClass());
			}
		}
		return regs;

	}

}
