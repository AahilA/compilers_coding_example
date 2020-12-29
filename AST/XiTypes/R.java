package zw494.AST.XiTypes;

public class R extends XiType {

    public enum Rtype{
        UNIT,
        VOID
    }

    public Rtype rt;

    public R(Rtype rt){
        this.rt = rt;
    }

}