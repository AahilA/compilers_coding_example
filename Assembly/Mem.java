package zw494.Assembly;

public class Mem extends Arg {
    public Register r1 = null;
    public Register r2 = null;
    Long w = null;
    Long o = null;
    
    /**
     * Assembly memory object.
     * 
     * @param r1 register in the form (r)
     */
    public Mem(Register r1) {
        this.r1 = r1;
    }

    /**
     * Assembly memory object in the form (r1, r2).
     * 
     * @param r1 first register of (r1, r2)
     * @param r2 second register of (r1, r2)
     */
    public Mem(Register r1, Register r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    /**
     * Assembly memory object in the form o(r1).
     * 
     * @param r1 register of o(r1)
     * @param o immediate of o(r1)
     */
    public Mem(Register r1, Long o) {
        this.r1 = r1;
        this.o = o;
    }

    /**
     * Assembly memory object in the form (r1, r2, w).
     * 
     * @param r1 first register in (r1, r2, w)
     * @param r2 second register in (r1, r2, w)
     * @param w immediate in (r1, r2, w)
     */
    public Mem(Register r1, Register r2, Long w) {
        this.r1 = r1;
        this.r2 = r2;
        this.w = w;
    }

    /**
     * Assembly memory object in the form o(r1, r2, w).
     * 
     * @param r1 first register in o(r1, r2, w)
     * @param r2 second register in o(r1, r2, w)
     * @param w immediate in o(r1, r2, w)
     * @param o immediate in o(r1, r2, w)
     */
    public Mem(Register r1, Register r2, Long w, Long o) {
        this.r1 = r1;
        this.r2 = r2;
        this.w = w;
        this.o = o;
    }
    
    /**
     * Copy an assembly mem.
     * @return the copied mem
     */
    @Override
    public Mem copy() {
        // deep copy
        Mem ret = new Mem(null);
        if (this.r1 != null) {
            ret.r1 = this.r1.copy();
        }
        if (this.r2 != null) {
            ret.r2 = this.r2.copy();
        }
        ret.w = this.w;
        ret.o = this.o;
        return ret;
    }

    /**
     * Print the mem object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        String result = "";
        if (this.o != null)
            result += o.toString();
        result += "(";
        result += this.r1.toString();

        if (this.r2 != null) {
            result += ", ";
            result += this.r2.toString();
        }

        if (this.w != null) {
            result += ", ";
            result += this.w.toString();
        }
        result += ")";
        return result;
    }
}