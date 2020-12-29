package zw494.AST;

import java.util.LinkedList;

/**
 * AST node for signature
 */
public class Signature extends Node {

    public String sigName;
    public LinkedList<Node> pdl;
    public LinkedList<Node> rl;
    public String args = "";
    public String rets = "";

    /**
     * Signature takes in a sigName as its name and a list parameter declaration and
     * a list of return type.
     */
    public Signature(String sigName, LinkedList<Node> pdl, LinkedList<Node> rl, int curRow, int curCol) {
        this.sigName = sigName;
        children = new Node[2];
        children[0] = (Node) (new NodeList(pdl, 2));
        children[1] = (Node) (new NodeList(rl, 3));
        this.pdl = pdl;
        this.rl = rl;
        this.rowpos = curRow;
        this.colpos = curCol;
    }

    /**
     * Signature takes in a sigName as its name and a list parameter declaration.
     * The list of return types is set to null if it does not appear.
     */
    public Signature(String sigName, LinkedList<Node> pdl, int curRow, int curCol) {
        this.sigName = sigName;
        children = new Node[2];
        children[0] = (Node) (new NodeList(pdl, 2));
        children[1] = (Node) (new NodeList(null, 3));
        this.pdl = pdl;
        this.rl = null;
        this.rowpos = curRow;
        this.colpos = curCol;
    }

}