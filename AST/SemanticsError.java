package zw494.AST;

public class SemanticsError extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int row, col;
    public String msg;

    public SemanticsError(Node x, String msg) {
        this.row = x.rowpos;
        this.col = x.colpos;
        this.msg = msg;
    }

}