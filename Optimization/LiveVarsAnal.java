package zw494.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import zw494.Assembly.Arg;
import zw494.Assembly.BinOp;
import zw494.Assembly.Instr;
import zw494.Assembly.Mem;
import zw494.Assembly.MiscOp;
import zw494.Assembly.Register;
import zw494.Assembly.UnOp;
import zw494.Assembly.BinOp.OpType;
import zw494.CFG.ControlFlowGraphMaster;

public class LiveVarsAnal extends DataFlowAnal<Instr, Set<Register>> {

    List<Instr> instrs;

    boolean checked = false;
    boolean containsRax = false;
    boolean containsRdx = false;

    public LiveVarsAnal(ControlFlowGraphMaster<Instr> cfg, List<Instr> w) {
        this.cfg = cfg;
        this.instrs = w;
        this.w = new LinkedList<>();
        this.w.addAll(w);
        this.in = new HashMap<>();
        this.out = new HashMap<>();

        for (Instr ins : w) {
            in.put(ins, new HashSet<>());
            out.put(ins, new HashSet<>());
        }
    }

    public Set<Register> meet(List<Set<Register>> varslist) {
        Set<Register> result = new HashSet<>();

        for (Set<Register> s : varslist) {
            result.addAll(s);
        }

        return result;

    }

    public Set<Register> transfer(Instr i, Set<Register> vars) {
        Set<Register> u = use(i);
        Set<Register> d = def(i);

        Set<Register> temp = new HashSet<>();
        temp.addAll(vars);
        temp.removeAll(d);
        temp.addAll(u);

        return temp;
    }

    public boolean hasSameElt(Set<Register> s1, Set<Register> s2) {
        Set<Register> temp = new HashSet<>();
        temp.addAll(s1);
        temp.removeAll(s2);
        if (temp.size() != 0)
            return false;
        temp.addAll(s2);
        temp.removeAll(s1);
        if (temp.size() != 0)
            return false;
        return true;
    }

    public Set<Register> use(Instr i) {
        Set<Register> result = new HashSet<>();

        if (i instanceof BinOp) {
            BinOp j = (BinOp) i;
            addVar(result, j.arg1, true);
            if (j.op != BinOp.OpType.movq && j.op != BinOp.OpType.leaq)
                addVar(result, j.arg2, true);
            else if (j.arg2 instanceof Mem) {
                addVar(result, j.arg2, true);
            }
        } else if (i instanceof UnOp) {
            UnOp j = (UnOp) i;
            addVar(result, j.arg1, true);
            switch (j.op) {
                case mulq:
                case imulq:
                    addVar(result, new Register(Register.regNames.rax), false);
                    break;
                case idivq:
                    addVar(result, new Register(Register.regNames.rdx), false);
                    addVar(result, new Register(Register.regNames.rax), false);
                    break;
                default:

            }
        } else if (i instanceof MiscOp) {
            MiscOp j = (MiscOp) i;
            if (j.op == MiscOp.OpType.ret) {
                // uses all callee saved registers
                addVar(result, new Register(Register.regNames.rbx), false);
                // addVar(result, new Register(Register.regNames.rbp), false);
                addVar(result, new Register(Register.regNames.r12), false);
                addVar(result, new Register(Register.regNames.r13), false);
                addVar(result, new Register(Register.regNames.r14), false);
                addVar(result, new Register(Register.regNames.r15), false);

                if (!checked) {
                    checked = true;
                    // uses rax only if someone defs it
                    for (Instr k : instrs) {
                        if (def(k).contains(new Register(Register.regNames.rax))) {
                            containsRax = true;
                            break;
                        }
                    }
                    // uses rdx only if someone defs it

                    for (Instr k : instrs) {
                        if (def(k).contains(new Register(Register.regNames.rdx))) {
                            containsRdx = true;
                            break;
                        }
                    }
                }

                if (containsRax) {
                    addVar(result, new Register(Register.regNames.rax), false);
                }

                if (containsRdx) {
                    addVar(result, new Register(Register.regNames.rdx), false);
                }

            }
        }

        return result;
    }

    public Set<Register> def(Instr i) {
        Set<Register> result = new HashSet<>();

        if (i instanceof BinOp) {
            BinOp j = (BinOp) i;
            if (j.op != OpType.cmpq && j.op != OpType.testq)
                addVar(result, j.arg2, false);
        } else if (i instanceof UnOp) {
            UnOp j = (UnOp) i;
            switch (j.op) {
                case negq:
                case notq:
                case incq:
                case decq:
                    addVar(result, j.arg1, false);
                    break;
                case mulq:
                case imulq:
                case idivq:
                    addVar(result, new Register(Register.regNames.rax), false);
                    addVar(result, new Register(Register.regNames.rdx), false);
                    break;
                case callq:
                    // defines all caller saved registers
                    addVar(result, new Register(Register.regNames.rdi), false);
                    addVar(result, new Register(Register.regNames.rsi), false);
                    addVar(result, new Register(Register.regNames.rdx), false);
                    addVar(result, new Register(Register.regNames.rcx), false);
                    addVar(result, new Register(Register.regNames.rax), false);
                    addVar(result, new Register(Register.regNames.r8), false);
                    addVar(result, new Register(Register.regNames.r9), false);
                    addVar(result, new Register(Register.regNames.r10), false);
                    addVar(result, new Register(Register.regNames.r11), false);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    public void addVar(Set<Register> result, Arg a, Boolean is_use) {
        if (a instanceof Register) {
            Register b = (Register) a;
            b.use_num++;
            result.add(b);

        } else if (a instanceof Mem && is_use) {
            Mem b = (Mem) a;
            b.r1.use_num++;
            result.add(b.r1);
            if (b.r2 != null) {
                b.r2.use_num++;
                result.add(b.r2);
            }
        }
    }

    public void useReplace(Instr i, Register r, Register rn) {

        if (i instanceof BinOp) {
            BinOp j = (BinOp) i;
            if (r.equals(j.arg1)) {
                j.arg1 = rn;
            }
            if (j.arg1 instanceof Mem) {
                Mem k = (Mem) j.arg1;
                if (r.equals(k.r1))
                    k.r1 = rn;
                if (k.r2 != null && r.equals(k.r2))
                    k.r2 = rn;
            }
            if (j.op != BinOp.OpType.movq && j.op != BinOp.OpType.leaq || j.arg2 instanceof Mem) {
                if (r.equals(j.arg2)) {
                    j.arg2 = rn;
                }
                if (j.arg2 instanceof Mem) {
                    Mem k = (Mem) j.arg2;
                    if (r.equals(k.r1))
                        k.r1 = rn;
                    if (k.r2 != null && r.equals(k.r2))
                        k.r2 = rn;
                }
            }
        } else if (i instanceof UnOp) {
            UnOp j = (UnOp) i;
            if (r.equals(j.arg1)) {
                j.arg1 = rn;
            }
            if (j.arg1 instanceof Mem) {
                Mem k = (Mem) j.arg1;
                if (r.equals(k.r1))
                    k.r1 = rn;
                if (k.r2 != null && r.equals(k.r2))
                    k.r2 = rn;
            }
        }
    }

    public void defReplace(Instr i, Register r, Register rn) {

        if (i instanceof BinOp) {
            BinOp j = (BinOp) i;
            if (j.op != OpType.cmpq && j.op != OpType.testq) {
                if (r.equals(j.arg2)) {
                    j.arg2 = rn;
                }
                if (j.arg2 instanceof Mem) {
                    Mem k = (Mem) j.arg2;
                    if (r.equals(k.r1))
                        k.r1 = rn;
                    if (k.r2 != null && r.equals(k.r2))
                        k.r2 = rn;
                }
            }
        } else if (i instanceof UnOp) {
            UnOp j = (UnOp) i;
            switch (j.op) {
                case negq:
                case notq:
                case incq:
                case decq:
                    if (r.equals(j.arg1)) {
                        j.arg1 = rn;
                    }
                    if (j.arg1 instanceof Mem) {
                        Mem k = (Mem) j.arg1;
                        if (r.equals(k.r1))
                            k.r1 = rn;
                        if (k.r2 != null && r.equals(k.r2))
                            k.r2 = rn;
                    }
                    break;
                default:
                    break;
            }
        }

        return;
    }

    @Override
    public void cond(List<Instr> lst) {
        return;
    }



}