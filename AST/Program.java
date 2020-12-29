package zw494.AST;

import java.util.LinkedList;

/**
 * AST node for program
 */
public class Program extends Node {

    /**
     * Program takes in a list of Use and Method.
     */
    public Program(LinkedList<Node> ul, LinkedList<Node> fl, int row, int col) {
        children = new Node[2];
        children[0] = (Node) (new NodeList(ul, 1));
        children[1] = (Node) (new NodeList(fl, 1));
        this.rowpos = row;
        this.colpos = col;
    }

}