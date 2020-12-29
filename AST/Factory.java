package zw494.AST;

public class Factory {
    static int temp_n = 0;
    static int label_n = 0;
    static int reg_n = 0;
    static int node_n = 0;

    public static String labelFactory() {
        String s = "_l" + label_n;
        label_n++;

        return s;
    }

    public static String tempFactory() {
        String s = "_t" + temp_n;
        temp_n++;
        return s;
    }

    public static String nodeFactory(){
        String s = "n" + node_n;
        node_n++;
        return s;
    }

    public static void clear() {
        temp_n = 0;
        label_n = 0;
        reg_n = 0;
    }
}