package zw494.AST.Literal;

public class CharacterLiteral extends Literal {

    public String c;

    /**
     * Handles Character literal types.
     * 
     * @param c
     */
    public CharacterLiteral(String c, int rowpos, int colpos) {
        this.rowpos = rowpos;
        this.colpos = colpos;
        this.c = c;

    }

}