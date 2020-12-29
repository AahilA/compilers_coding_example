package zw494.AST.Expressions;

import java.util.LinkedList;

import zw494.AST.Node;

/**
 * AST node for function call expression
 */
public class FunctionCallExpression extends Expr {

    public final String id;
    public String args = "";
    public String rets = "";

    /**
     * FunctionCallExpression is a type of expression that takes in id as its name
     * and a list of expression as its children.
     */
    public FunctionCallExpression(String id, LinkedList<Node> el, int rowpos, int colpos) {
        children = new Node[el.size()];
        this.id = id;
        this.rowpos = rowpos;
        this.colpos = colpos;
        for (int i = 0; i < el.size(); ++i) {
            children[i] = el.get(i);
        }
    }

}