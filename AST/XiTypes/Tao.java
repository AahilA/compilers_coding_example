package zw494.AST.XiTypes;

public class Tao extends XiType {

    public enum taoType {
        INT, BOOL, ANYTYPE, ANYPLUSTYPE // specifically for {}[] case
    }

    public taoType tao;
    public int numberOfBrackets;

    public Tao(taoType tt, int nb) {

        this.tao = tt;
        this.numberOfBrackets = nb;

    }

    public static boolean isEquals(Tao a, Tao b) {
        return a.tao == b.tao && a.numberOfBrackets == b.numberOfBrackets;
    }

    public static boolean isSubtype(Tao a, Tao b) {
        if (a.tao == taoType.ANYTYPE && a.numberOfBrackets <= b.numberOfBrackets) {
            return true;
        } else if (a.tao == taoType.ANYPLUSTYPE && a.numberOfBrackets <= b.numberOfBrackets && b.tao != taoType.BOOL) {
            return true;
        }
        return isEquals(a, b);
    }

    @Override
    public String print() {
        return this.tao + " " + this.numberOfBrackets;
    }

}