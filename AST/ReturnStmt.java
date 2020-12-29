package zw494.AST;

import java.util.LinkedList;

import zw494.AST.Node;

/**
 * AST node for return statement
 */
public class ReturnStmt extends Node {

    /**
     * ReturnStmt is a special statement that is different from other statements. It
     * can only appear at the end of a block. It takes in a list of expressions.
     */
    public ReturnStmt(LinkedList<Node> el, int row, int col) {
        children = new Node[el.size()];
        for (int i = 0; i < el.size(); ++i) {
            children[i] = el.get(i);
        }
        this.rowpos = row;
        this.colpos = col;
        // TODO add info on return type of method
    }

    /**
     * Empty ReturnStmt sets its children to be None.
     */
    public ReturnStmt(int row, int col) {
        children = null;
        this.rowpos = row;
        this.colpos = col;
    }

}
