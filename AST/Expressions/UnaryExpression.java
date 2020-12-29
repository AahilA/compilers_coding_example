package zw494.AST.Expressions;

import zw494.AST.Node;

/**
 * AST node for unary expression
 */
public class UnaryExpression extends Expr {

    public enum UnOp {
        NOT, SUB
    }

    public UnOp op;

    /**
     * UnaryExpression takes in an expression and an enum for op.
     */
    public UnaryExpression(Expr arg, UnOp op, int rowpos, int colpos) {
        children = new Node[1];
        children[0] = (Node) arg;
        this.op = op;
        this.rowpos = rowpos;
        this.colpos = colpos;
    }

}