package zw494.Assembly;

public class MiscOp extends Instr {

    public enum OpType {
        ret, enter, leave
    }

    public OpType op;

    int value = 0;

    public MiscOp() {

    }

    /**
     * Some special assembly operand object.
     * 
     * @param op the operand type, including ret and leave
     */
    public MiscOp(OpType op) {
        this.op = op;
    }

    /**
     * Some special assembly operand object.
     * 
     * @param op    the operand type, including enter
     * @param value the argument of the op
     */
    public MiscOp(OpType op, int value) {
        this.op = op;
        this.value = value;
    }

    /**
     * Copy an assembly miscop.
     * 
     * @return the copied miscop
     */
    @Override
    public Instr copy() {
        MiscOp ret = new MiscOp();
        ret.op = this.op;
        ret.value = this.value;
        return ret;
    }

    /**
     * Print the miscop object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        String result = "";
        result += this.op.toString();
        if (op == OpType.enter) {
            result += " $" + value + ", $0";
        }
        if (this.op == OpType.ret) {
            result += "q";
        }
        return result;
    }
}