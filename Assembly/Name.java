package zw494.Assembly;

public class Name extends Arg {
    String name;
    Boolean is_func;

    /**
     * Assembly name object, usually label or function name used in instructions.
     * 
     * @param name the name of the name
     */
    public Name(String name) {
        this.name = name;
    }

    /**
     * Copy an assembly name.
     * @return the copied name
     */
    @Override
    public Arg copy() {
        Name ret = new Name(this.name);
        ret.is_func = this.is_func;
        return ret;
    }

    /**
     * Print the name object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        // print FUNC to help parsing in the register allocation stage
        if (!is_func)
            return this.name;
        else
            return "FUNC(" + this.name + ")";
    }
}