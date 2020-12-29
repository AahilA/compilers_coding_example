package zw494.AST.XiTypes;

public class Sigma extends XiType {

    public enum SigmaType {
        VAR, RET, FN1, FN2
    }

    public SigmaType st;

    public Tao tao;
    public T t;
    public T tp;

    public Sigma(SigmaType st, Tao tao) {
        this.tao = tao;
        this.st = st;
    }

    public Sigma(SigmaType st, T t, T tp) {
        this.st = st;
        this.t = t;
        if (tp.tType == 1) {
            this.st = SigmaType.FN1;
            this.tao = tp.tl.get(0);
        }
        this.tp = tp;
    }

    public Sigma(SigmaType st, T t) {
        this.st = st;
        this.t = t;
    }

    @Override
    public String print() {
        String s = st.toString() + " ";
        if (t != null) {
            s += t.print();
        }
        if (tp != null) {
            s += tp.print();
        }
        return s;
    }
}