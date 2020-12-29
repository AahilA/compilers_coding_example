package zw494.Assembly;

public class BinOp extends Instr {
    public enum OpType {
        addq, subq, andq, orq, xorq, shrq, shlq, sarq, testq, cmpq, movq, leaq, imulq, movzx, movabs
    }

    public OpType op;
    public Arg arg1;
    public Arg arg2;

    public BinOp() {

    }

    /**
     * Assembly binary instruction object.
     * 
     * @param op operand type
     * @param arg1 first arg
     * @param arg2 second arg
     */
    public BinOp(OpType op, Arg arg1, Arg arg2) {
        sanityCheck(op, arg1, arg2);
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    /**
     * Copy an assembly binary instruction.
     * @return the copied binop
     */
    @Override
    public BinOp copy() {
        // deep copy a bin op
        BinOp ret = new BinOp();
        ret.op = this.op;
        if (this.arg1 != null) {
            ret.arg1 = this.arg1.copy();
        }
        if (this.arg2 != null) {
            ret.arg2 = this.arg2.copy();
        }
        return ret;
    }

    /**
     * Check if the given binop is valid.
     */
    public void sanityCheck(OpType op, Arg arg1, Arg arg2) {
        // enforcing operand rules
        switch (op) {
            case addq:
            case subq:
            case andq:
            case imulq:
            case orq:
            case xorq:
                if (!(arg1 instanceof Register || arg2 instanceof Register))
                    throw new Error();
                if (arg2 instanceof Imm)
                    throw new Error();
                break;

            case movq:
                if (arg2 instanceof Imm)
                    throw new Error();
                else if (arg1 instanceof Mem && arg2 instanceof Mem)
                    throw new Error();
                break;

            case leaq:
                if (!(arg2 instanceof Register))
                    throw new Error();
                break;

            case shrq:
            case shlq:
            case sarq:
                if (!(arg1 instanceof Imm && arg2 instanceof Register))
                    throw new Error();
                break;

            case testq:
                if (arg1 instanceof Mem || arg2 instanceof Imm)
                    throw new Error();
                break;

            case cmpq:
                if (arg2 instanceof Imm)
                    throw new Error();
                // technically, arg1 and arg2 cannot be mem
                break;
        }
    }

    /**
     * Print the binop instruction object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        String result = "";
        result += this.op.toString() + "    " + arg1.toString() + ", " + arg2.toString();
        return result;
    }

}