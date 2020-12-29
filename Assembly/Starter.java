package zw494.Assembly;

public class Starter extends Instr {
    // special print statements in assembly
    String name;

    public Starter(String name) {
        this.name = name;
    }

    @Override
    public Instr copy() {

        return new Starter(this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object l) {
        return l instanceof Starter && ((Starter) l).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}