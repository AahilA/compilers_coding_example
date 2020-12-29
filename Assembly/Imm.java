package zw494.Assembly;

public class Imm extends Arg {
    public Long value;

    public Imm() {

    }

    /**
     * Assembly immediate object.
     * 
     * @param value the value of the immediate
     */
    public Imm(Long value) {
        this.value = value;
    }

    /**
     * Get the value of the immediate.
     * 
     * @return the value of the immeidate
     */
    public Long value(){
        return this.value;
    }

    /**
     * Print the immeidiate object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        return "$" + this.value;
    }
    
    /**
     * Copy an assembly immeidate.
     * @return the copied imm
     */
    @Override
    public Arg copy() {
        Imm ret = new Imm();
        ret.value = this.value;
        return ret;
    }
}