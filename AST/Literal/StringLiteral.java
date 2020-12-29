package zw494.AST.Literal;

public class StringLiteral extends Literal {

    public String s;

    /**
     * Handles string literals.
     */
    public StringLiteral(String s, int rowpos, int colpos) {
        this.rowpos = rowpos;
        this.colpos = colpos;
        this.s = s;

    }

}