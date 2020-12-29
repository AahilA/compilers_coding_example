package zw494.AST;

/**
 * AST node for use
 */
public class Use extends Node {
    final String useName;

    /**
     * Use takes in a useName as its name.
     */
    public Use(String useName, int row, int col) {
        this.useName = useName;
        this.rowpos = row;
        this.colpos = col;
    }

}
