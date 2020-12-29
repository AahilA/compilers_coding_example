package zw494.Assembly;


public class UnOp extends Instr {

    public enum UnOpType {
        negq, notq, incq, decq, je, jmp, jne, jg, jge, jl, jle, callq, pushq, popq, mulq, setz, setnz, setl, setg,
        setle, setge, idivq, imulq
    }

    public UnOpType op;
    public Arg arg1;
    public String argString;

    public UnOp() {

    }

     /**
     * Assembly unary instruction object.
     * 
     * @param op operand type
     * @param arg1 arg, either a register or imm or mem
     */
    public UnOp(UnOpType op, Arg arg1) {
        // System.out.println(op);
        // System.out.println(arg1);

        sanityCheck(op, arg1);
        this.op = op;
        this.arg1 = arg1;
    }

    /**
     * Assembly unary instruction object.
     * 
     * @param op operand type
     * @param arg1 arg, a string
     */
    public UnOp(UnOpType op, String arg1) {
        // System.out.println(op);
        // System.out.println(arg1);
        this.op = op;
        this.argString = arg1;
    }

    /**
     * Copy an assembly unop.
     * @return the copied unop
     */
    @Override
    public Instr copy() {
        // deep copy a unop
        UnOp neww = new UnOp();
        neww.op = this.op;
        if (arg1 != null) {
            neww.arg1 = this.arg1.copy();
        }
        neww.argString = this.argString;
        return neww;
    }

    public void sanityCheck(UnOpType op, Arg arg1) {
        // enforce sanity check of unary operators
        switch (op) {
            case negq:
            case notq:
            case incq:
            case mulq:
            case imulq:
            case decq:
            case idivq:
                if (!(arg1 instanceof Register || arg1 instanceof Mem))
                    throw new Error();
                break;

            case popq:
            case pushq:
                break;
            case setz:
            case setnz:
            case setg:
            case setl:
            case setle:
            case setge:
                if (!(arg1 instanceof Register))
                    throw new Error();
                break;
            case callq:
                if (!(arg1 instanceof Name))
                    throw new Error();
                break;

            default:
                // System.out.println(op);
                throw new Error();
        }
    }

    /**
     * Print the unop instruction object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        String result = "";
        UnOpType[] arr = { UnOpType.je, UnOpType.jne, UnOpType.jmp, UnOpType.jg, UnOpType.jge, UnOpType.jl,
                UnOpType.jle };
        for (int i = 0; i < arr.length; i++) {
            if (this.op == arr[i]) {
                result += this.op.toString() + "    " + argString.toString();
                return result;
            }
        }

        result += this.op.toString() + "    " + arg1.toString();
        return result;
    }

}