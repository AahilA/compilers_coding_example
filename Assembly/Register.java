package zw494.Assembly;

import java.util.Arrays;
import java.util.List;

import zw494.Optimization.IntfGraph;
import zw494.Optimization.IntfGraphTest;
import zw494.Optimization.LiveVarsAnal;

public class Register extends Arg implements Comparable<Register> {
    // there are two types of registers.
    // regNames are concrete registers, whereas name are abstract registers
    public enum regNames {
        rax, rcx, rdx, rbx, rsi, rdi, rsp, rbp, r8, r9, r10, r11, r12, r13, r14, r15, al, dl
    }

    public static final List<regNames> all_regs = Arrays.asList(Register.regNames.rax, Register.regNames.rcx,
            Register.regNames.rdx, Register.regNames.rbx, Register.regNames.rsi, Register.regNames.rdi,
            Register.regNames.r8, Register.regNames.r9, Register.regNames.r10, Register.regNames.r11,
            Register.regNames.r12, Register.regNames.r13, Register.regNames.r14, Register.regNames.r15);
    public final regNames[] const_regs = { Register.regNames.rdi, Register.regNames.rsi, Register.regNames.rdx,
            Register.regNames.rcx, Register.regNames.r8, Register.regNames.r9 };
    public final regNames[] func_ret_regs = { Register.regNames.rax, Register.regNames.rdx };

    private regNames name;
    private String argName;

    public int degree = 0;

    // characterize spilling priority
    public int use_num = 0;

    public enum Type {
        precolored, initial, simplify, freeze, spilling, spilled, coalesced, colored, selected
    }

    public Register alias;

    public Type type;

    /**
     * Assembly register object. One of the concrete registers.
     * 
     * @param name the name of the register
     */
    public Register(regNames name) {
        this.name = name;
        this.argName = null;
        this.type = Type.precolored;
    }

    /**
     * Assembly register object. One of the abstract registers.
     * 
     * @param name the name of the register
     */
    public Register(String argName) {
        this.argName = argName;
        this.name = null;
        this.type = null;
    }

    public String getArgName() {
        return argName;
    }

    public regNames getRegNames() {
        return name;
    }

    void setRegName(regNames newRegNames) {
        this.name = newRegNames;
        this.argName = null;
        this.type = Type.precolored;
    }

    /**
     * Copy a register.
     * 
     * @return the copied register
     */
    @Override
    public Register copy() {
        Register neww = new Register("");
        neww.name = this.name;
        neww.argName = this.argName;
        neww.type = this.type;
        return neww;
    }

    /**
     * Print the register object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        // print the registers as is
        // for abstract registers, print their name directly

        if (this.name != null)
            return "%" + this.name.toString();

        else if (IntfGraphTest.curGlobalMap != null) {
            if (IntfGraphTest.curGlobalMap.get(this.argName) != null)
                return "%" + IntfGraphTest.curGlobalMap.get(this.argName).toString();
            // else
            // System.out.println("cannot get" + this.argName + "register from map" +
            // IntfGraphTest.curGlobalMap);
        }

        // special case for _ARG and _RET
        else if (this.argName.length() > 4) {
            if (this.argName.substring(0, 4).equals("_ARG")) {
                String w = this.argName.substring(4, 5);
                int q = Integer.parseInt(w);
                if (q < 6) {
                    return "%" + const_regs[q];
                } else {
                    // this would not happen after register allocation
                }
            } else if (this.argName.substring(0, 4).equals("_RET")) {
                String w = this.argName.substring(4, 5);
                int q = Integer.parseInt(w);
                if (q < 2) {
                    return "%" + func_ret_regs[q];
                } else {
                    // this would not happen after register allocation
                }
            }
        }

        // System.out.println("failed to get any global map" +
        // IntfGraphTest.curGlobalMap);
        // for concrete registers, print their real name
        return this.argName;

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Register))
            return false;
        // Register r = (Register) o;
        // if (this.name != null)
        // return this.name == r.name;
        // else if (r.name != null)
        // return false;
        // else
        // return this.argName.equals(r.argName);
        return this.toString().equals(o.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public double priority() {
        return use_num * 1.0 / degree;
    }

    @Override
    public int compareTo(Register r) {
        double val = (priority() - r.priority());
        if (val < -0.01)
            return -1;
        else if (val > 0.01)
            return 1;
        else
            return 0;
    }
}