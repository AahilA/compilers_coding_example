package zw494.AST.Expressions;

import zw494.AST.Node;
import zw494.AST.Literal.*;

/**
 * AST node for literal expression
 */
public class LiteralExpression extends Expr {

    Literal l;

    /**
     * LiteralExpression is a type of expression that takes in a literal.
     */
    public LiteralExpression(Literal l, int rowpos, int colpos) {
        children = new Node[1];
        children[0] = (Node) l;
        this.rowpos = rowpos;
        this.colpos = colpos;
    }

}