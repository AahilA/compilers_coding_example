package zw494.AST.Literal;

public class IntegerLiteral extends Literal {

    public String i;

    /**
     * Handles integer literals
     */
    public IntegerLiteral(String i, int rowpos, int colpos) {
        this.rowpos = rowpos;
        this.colpos = colpos;
        this.i = i;

    }

}