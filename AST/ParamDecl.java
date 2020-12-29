package zw494.AST;

/**
 * AST node for param declaration.
 */
public class ParamDecl extends Node {
    final String funcName;

    /**
     * ParamDecl takes in a funcName as its name and a function type.
     */
    public ParamDecl(String funcName, FuncType type, int row, int col) {
        this.funcName = funcName;
        children = new Node[1];
        children[0] = (Node) type;
        this.rowpos = row;
        this.colpos = col;
    }
}
