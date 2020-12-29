package zw494.AST.Statements;

import java.util.LinkedList;

import zw494.AST.Node;
import zw494.AST.Decl;
import zw494.AST.NodeList;
import zw494.AST.Expressions.Expr;

public class DeclStatement extends Stmt {

    /**
     * DeclStatement Linkedlist of declaration nodes of things to declare.
     * 
     * @param dt
     */
    public DeclStatement(LinkedList<Node> dt, int row, int col) {

        if (dt.size() > 1) {
            for (int i = 0; i < dt.size(); ++i) {
                ((Decl) (dt.get(i))).isAlone = false;
            }
        }
        children = new Node[2];
        if (dt.size() == 1)
            children[0] = ((Decl) (dt.get(0))).id == null ? new NodeList(new LinkedList<Node>(), 5)
                    : new NodeList(dt, 5);
        else
            children[0] = new NodeList(dt, 5);
        children[1] = null;
        this.rowpos = row;
        this.colpos = col;
    }

    /**
     * DeclStatement linked list of declaration nodes of things to declare then
     * assign a value with the expresionn node.
     * 
     * @param dt
     * @param e
     */
    public DeclStatement(LinkedList<Node> dt, Expr e, int row, int col) {

        if (dt.size() > 1) {
            for (int i = 0; i < dt.size(); ++i) {
                ((Decl) (dt.get(i))).isAlone = false;
            }
        }
        children = new Node[2];
        if (dt.size() == 1)
            children[0] = ((Decl) (dt.get(0))).id == null ? dt.get(0) : new NodeList(dt, 5);
        else
            children[0] = new NodeList(dt, 5);
        children[1] = (Node) e;
        this.rowpos = row;
        this.colpos = col;

    }

    /**
     * NodeVisitor design pattern setup.
     */

}