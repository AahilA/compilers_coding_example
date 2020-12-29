package zw494.AST;

import zw494.AST.Statements.*;
import java.util.ArrayList;
import zw494.AST.Expressions.*;
import zw494.AST.Literal.*;
import zw494.AST.Variable.*;

public class Preprocessor {
    public Preprocessor() {

    }

    /**
     * Translate an AST node [x] into an IR node.
     * 
     * @param irList results of translation of all [x]'s children
     * @param x      AST node to translate
     * @return the IR node translated
     */
    public void preprocess(Node x) {
        try {
            if (x instanceof NodeList && ((NodeList) x).attr == 4) {
                NodeList y = (NodeList) x;
                ArrayList<Node> nl = new ArrayList<>();
                if (y.children == null)
                    return;
                for (int i = 0; i < y.children.length; i++) {
                    Node c = y.children[i];
                    if (c instanceof DeclStatement) {
                        DeclStatement d = (DeclStatement) c;
                        if (d.children[1] == null) {
                            NodeList left = (NodeList) (d.children[0]);
                            Decl e = (Decl) left.children[0];
                            Type t = (Type) e.children[0];
                            ArrayList<Wrapper> aw = typeProcesser(t);

                            for (Wrapper w : aw) {
                                AssignmentStmt as = new AssignmentStmt(new StringVariable(w.varName, -100, -100),
                                        (Expr) w.e, -100, -100);
                                nl.add(as);
                            }
                        }
                    } else if (c instanceof BlockStmt) {
                        preprocess(c);
                    }
                    nl.add(c);
                }

                y.children = new Node[nl.size()];
                for (int i = 0; i < nl.size(); i++) {
                    y.children[i] = nl.get(i);
                }

            } else {
                if (x != null && x.children != null) {
                    for (int i = 0; i < x.children.length; i++) {
                        preprocess(x.children[i]);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<Wrapper> typeProcesser(Type t) {
        ArrayList<Wrapper> list = new ArrayList<>();
        if (t == null || t.children[1] == null)
            return list;
        Node e = (Node) t.children[1];

        ArrayList<Wrapper> children_t = typeProcesser((Type) t.children[0]);
        if (e instanceof BooleanLiteral || e instanceof CharacterLiteral || e instanceof StringLiteral
                || e instanceof IntegerLiteral || e instanceof StringVariable) {
            return children_t;
        }
        String name = Factory.tempFactory();
        Wrapper w = new Wrapper(name, e);
        t.children[1] = new VariableExpression(new StringVariable(name, -100, -100), -100, -100);
        list.add(w);
        list.addAll(children_t);
        return list;
    }

}

class Wrapper {
    String varName;
    Node e;

    Wrapper(String varName, Node e) {
        this.varName = varName;
        this.e = e;
    }
}