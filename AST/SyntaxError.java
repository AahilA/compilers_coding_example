package zw494.AST;

public class SyntaxError extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int row, col;
    public String msg;

    public SyntaxError(int row, int col, String msg) {
        this.row = row;
        this.col = col;
        this.msg = msg;
    }

}