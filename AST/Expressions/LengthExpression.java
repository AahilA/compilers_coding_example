package zw494.AST.Expressions;

import zw494.AST.Node;

/**
 * AST node for length expression
 */
public class LengthExpression extends Expr {

    /**
     * LengthExpression is a type of expression that takes in an expression.
     */
    public LengthExpression(Expr e, int rowpos, int colpos) {
        children = new Node[1];
        children[0] = (Node) e;
        this.rowpos = rowpos;
        this.colpos = colpos;
    }

}