package zw494.AST;

public class Decl extends Node {

    public final String id;
    public boolean isAlone = true;

    /**
     * Declaration of name id and type t.
     * 
     * @param id
     * @param t
     */
    public Decl(String id, Type t, int row, int col) {
        this.id = id;
        children = new Node[1];
        children[0] = (Node) t;
        this.rowpos = row;
        this.colpos = col;
    }

    /**
     * Empty declaration
     */
    public Decl(int row, int col) {
        this.id = null;
        children = new Node[1];
        children[0] = null;
        this.rowpos = row;
        this.colpos = col;
    }

}
