package zw494.AST.Expressions;

import zw494.AST.Node;
import zw494.AST.Variable.*;

/**
 * AST node for variable expression
 */
public class VariableExpression extends Expr {

    /**
     * VariableExpression is a type of expression that takes in a variable
     */
    public VariableExpression(Variable v, int rowpos, int colpos) {
        children = new Node[1];
        children[0] = (Node) v;
        this.rowpos = rowpos;
        this.colpos = colpos;
    }

}