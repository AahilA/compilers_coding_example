package zw494.Assembly;

public class Label extends Instr {

    String name;

    /**
     * Assembly label object.
     * 
     * @param name the name of the label
     */
    public Label(String name) {
        this.name = name;
    }

    /**
     * Get the name of the label.
     * 
     * @return the name of the label
     */
    public String name() {
        return name;
    }

    /**
     * Print the label object into corresponding assembly code in AT&T syntax.
     */
    @Override
    public String toString() {
        return this.name + ":";
    }

    /**
     * Copy an assembly label.
     * 
     * @return the copied label
     */
    @Override
    public Instr copy() {
        Label l = new Label(this.name);
        return l;
    }

    @Override
    public boolean equals(Object l) {
        return l instanceof Label && ((Label) l).name.equals(name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

}