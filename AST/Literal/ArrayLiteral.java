package zw494.AST.Literal;

import java.util.LinkedList;

import zw494.AST.Node;

public class ArrayLiteral extends Literal {

    /**
     * Array literal has a list of expressions within it; For example a[i+j][10]
     * where i+j and 10 are the expressions.
     * 
     * @param el
     */
    public ArrayLiteral(LinkedList<Node> el, int rowpos, int colpos) {
        children = new Node[el.size()];
        for (int i = 0; i < el.size(); i++) {
            children[i] = el.get(i);
        }
        this.rowpos = rowpos;
        this.colpos = colpos;
    }

    /**
     * Empty Array literal.
     */
    public ArrayLiteral(int rowpos, int colpos) {
        children = null;
        this.rowpos = rowpos;
        this.colpos = colpos;
    }

}