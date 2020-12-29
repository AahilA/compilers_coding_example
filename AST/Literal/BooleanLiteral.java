package zw494.AST.Literal;

public class BooleanLiteral extends Literal {

    public Boolean b;

    /**
     * Handles boolean literal types.
     * 
     * @param b
     */
    public BooleanLiteral(Boolean b, int rowpos, int colpos) {
        this.rowpos = rowpos;
        this.colpos = colpos;
        this.b = b;

    }

}