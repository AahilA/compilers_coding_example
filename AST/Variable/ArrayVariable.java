package zw494.AST.Variable;

import java.util.LinkedList;

import zw494.AST.Node;
import zw494.AST.Expressions.Expr;

/**
 * AST node for array variable
 */
public class ArrayVariable extends Variable {

    /**
     * ArrayVariable is a type of variable that takes in an expression and an
     * ArrayVariable. It uses` the name of the ArrayVariable in the argument as its
     * name.
     */

    public ArrayVariable(Node av, Expr e, int row, int col) {

        children = new Node[2];
        children[0] = av;
        children[1] = e;
        this.rowpos = row;
        this.colpos = col;

    }

}