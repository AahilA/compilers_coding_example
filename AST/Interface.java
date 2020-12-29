package zw494.AST;

import java.util.LinkedList;

public class Interface extends Node {

    /**
     * Interface node to handle all interface code lines.
     * 
     * @param sl
     */
    public Interface(LinkedList<Node> sl, int row, int col) {
        children = new Node[1];
        children[0] = (Node) (new NodeList(sl, 1));
        this.rowpos = row;
        this.colpos = col;
    }

}