package zw494.AST.Statements;

import java.util.LinkedList;

import zw494.AST.Node;

public class CallStatement extends Stmt {

    public final String id;
    public String args = "";
    public String rets = "";

    /**
     * CallStatement is a special statement used to call processes.
     * 
     * @param id name of the process
     * @param el list of arguements.
     */
    public CallStatement(String id, LinkedList<Node> el, int row, int col) {
        this.id = id;
        children = new Node[el.size()];
        for (int i = 0; i < el.size(); ++i) {
            children[i] = el.get(i);
        }
        this.rowpos = row;
        this.colpos = col;
    }

}