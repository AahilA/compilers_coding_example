package zw494.AST.Statements;

import java.util.LinkedList;

import zw494.AST.Node;
import zw494.AST.NodeList;
import zw494.AST.ReturnStmt;

public class BlockStmt extends Stmt {

    /**
     * BlockStmt list of statements within it to pass through, without return
     * statement.
     * 
     * @param sl
     */
    public BlockStmt(LinkedList<Node> sl, int row, int col) {
        children = new Node[1];
        children[0] = new NodeList(sl, 4);
        this.rowpos = row;
        this.colpos = col;
    }

    /**
     * BlockStmt list of statements within it to pass through, without return
     * statement.
     * 
     * @param sl
     * @param rs
     */
    public BlockStmt(LinkedList<Node> sl, ReturnStmt rs, int row, int col) {
        children = new Node[1];
        sl.addLast(rs);
        children[0] = new NodeList(sl, 4);
        this.rowpos = row;
        this.colpos = col;
    }

}
