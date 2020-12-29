package zw494.AST.Variable;

/**
 * AST node for string variable
 */
public class StringVariable extends Variable {

    public final String id;

    /**
     * StringVariable is a type of variable that takes in an id as its name.
     */
    public StringVariable(String id, int row, int col) {
        this.id = id;
        this.rowpos = row;
        this.colpos = col;
    }

}
