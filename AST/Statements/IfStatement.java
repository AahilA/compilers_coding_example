package zw494.AST.Statements;

import zw494.AST.Node;
import zw494.AST.Expressions.Expr;

public class IfStatement extends Stmt {

    /**
     * If statement to check expression, then print the then branch if true or the
     * otherwise branch if false.
     * 
     * @param check
     * @param then
     * @param otherwise
     */
    public IfStatement(Expr check, Stmt then, Stmt otherwise, int row, int col) {
        children = new Node[3];
        children[0] = (Node) check;
        children[1] = (Node) then;
        children[2] = (Node) otherwise;
        this.rowpos = row;
        this.colpos = col;
    }

    /**
     * If statement to check expression, then print the then branch if true
     * 
     * @param check
     * @param then
     */
    public IfStatement(Expr check, Stmt then, int row, int col) {
        children = new Node[3];
        children[0] = (Node) check;
        children[1] = (Node) then;
        children[2] = null;
        this.rowpos = row;
        this.colpos = col;
    }

}