package zw494.AST.Statements;

import zw494.AST.Node;
import zw494.AST.Expressions.Expr;

public class WhileStatement extends Stmt {
    /**
     * while statement with expression to for while arguement and statements for
     * while to perform.
     * 
     * @param e
     * @param s
     */
    public WhileStatement(Expr e, Stmt s, int row, int col) {
        children = new Node[2];
        children[0] = (Node) e;
        children[1] = (Node) s;
        this.rowpos = row;
        this.colpos = col;
    }

}