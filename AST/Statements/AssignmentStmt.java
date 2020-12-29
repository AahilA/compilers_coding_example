package zw494.AST.Statements;

import zw494.AST.Variable.*;
import zw494.AST.Node;
import zw494.AST.Expressions.Expr;

public class AssignmentStmt extends Stmt {

    /**
     * Assignment statement takes in a varibale on one side and the expression on
     * the other for example, a = 10
     * 
     * @param v
     * @param e
     */
    public AssignmentStmt(Variable v, Expr e, int row, int col) {
        children = new Node[2];
        children[0] = (Node) v;
        children[1] = (Node) e;
        this.rowpos = row;
        this.colpos = col;
    }

}