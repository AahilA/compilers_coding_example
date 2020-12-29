package zw494.AST;

import java.util.LinkedList;

public class NodeList extends Node {

    int attr = -1;

    /**
     * Stores list of nodes, used for accessing nodes children.
     *
     * @param nl
     */
    public NodeList(LinkedList<Node> nl, int a) {
        this.attr = a;

        if (nl == null || nl.size() == 0)
            return;
        children = new Node[nl.size()];
        for (int i = 0; i < children.length; ++i) {
            children[i] = nl.get(i);
        }
        this.rowpos = children[children.length - 1].rowpos;
        this.colpos = children[children.length - 1].colpos;
    }

}