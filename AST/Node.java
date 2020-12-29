package zw494.AST;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import zw494.AST.Expressions.FunctionCallExpression;
import zw494.AST.Statements.*;
import zw494.AST.XiTypes.*;
import zw494.AST.XiTypes.Sigma.SigmaType;
import zw494.Parser;
import zw494.edu.cornell.cs.cs4120.xic.ir.*;

/**
 * Parent class of all nodes. Enforces Node Visitor design pattern.
 */
public abstract class Node {

    public Node[] children;
    public int rowpos, colpos;
    public XiType xitype;

    /**
     * Visit function for print visitor
     * 
     * @param nv
     */
    public void visit(PrintVisitor nv) {
        nv.enter(this);
        if (children != null)
            for (Node i : children) {
                if (i != null) {
                    i.visit(nv);
                }
            }
        nv.exit(this);
    }

    /**
     * First pass for the type hcekcer to handle interface and class functions
     * 
     * @param interfacepath
     * @param pw
     * @throws SemanticsError
     */
    public void rootCheck(String interfacepath, PrintWriter pw) throws SemanticsError {

        TypeCheckVisitor nv = new TypeCheckVisitor();

        if (this instanceof Program) {
            Program p = (Program) this;

            for (Node n : p.children[1].children) {
                Method m = (Method) n;
                nv.functionProcess(m);
            }

            processUses(this, nv, interfacepath, pw);

            nv.st.print();

            xitype = this.visit(nv);
        }
        if (this instanceof Interface) {
            xitype = this.visit(nv);
        }

    }

    /**
     * To process interface signatures and add them to the symbol table
     * 
     * @param node
     * @param nv
     * @param interfacepath
     * @param pw
     * @throws SemanticsError
     */
    private void processUses(Node node, TypeCheckVisitor nv, String interfacepath, PrintWriter pw)
            throws SemanticsError {
        Program p = (Program) this;

        if (p.children == null || p.children[0] == null || p.children[0].children == null)
            return;

        for (Node n : p.children[0].children) {
            Use u = (Use) n;
            try {

                File f = new File(interfacepath + File.separator + u.useName + ".ixi");
                FileInputStream fr = new FileInputStream(f);
                Reader reader = new InputStreamReader(fr);

                Parser parser = new Parser(reader, pw, 1);
                Symbol symbol = parser.parse();
                Node iface = (Node) (symbol.value);

                for (Node n1 : iface.children[0].children) {
                    Signature s = (Signature) n1;
                    nv.signatureProcess(s);
                }

            } catch (SemanticsError e) {
                throw e;
            } catch (IOException e) {
                throw new SemanticsError(node, "ixi file not found");
            } catch (Exception e) {
                throw new SemanticsError(node, "error while parsing file " + u.useName + ".ixi");
            }

        }

    }

    /**
     * Visit function for type check visitor
     * 
     * @param nv
     * @return
     * @throws SemanticsError
     */
    public XiType visit(TypeCheckVisitor nv) throws SemanticsError {
        List<XiType> typeList = new ArrayList<XiType>();
        TypeCheckVisitor tcv;
        if (this instanceof Method || this instanceof BlockStmt || this instanceof IfStatement
                || this instanceof WhileStatement || this instanceof Signature) {
            tcv = nv.deepClone();
        } else
            tcv = nv;

        if (this instanceof Method) {

            Method m = (Method) this;
            Sigma balls = (Sigma) tcv.st.lookup(m.methodName);
            assert (balls != null);

            if (balls.st == SigmaType.FN2)
                tcv.st.add("0", new Sigma(SigmaType.RET, balls.tp));
            else if (balls.st == SigmaType.FN1)
                tcv.st.add("0", new Sigma(SigmaType.RET, balls.tao));
            else
                throw new SemanticsError(this, "should never happen");

        }

        // Speed Up Function call checking.
        if (this instanceof FunctionCallExpression) {
            FunctionCallExpression f = (FunctionCallExpression) this;

            Sigma balls = (Sigma) tcv.st.lookup(f.id);

            if (balls == null)
                throw new SemanticsError(this, "No such function exists");
        }

        if (this instanceof CallStatement) {
            CallStatement c = (CallStatement) this;

            Sigma balls = (Sigma) tcv.st.lookup(c.id);

            if (balls == null)
                throw new SemanticsError(this, "No such function exists");
        }

        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                Node x = children[i];

                if (x != null) {
                    typeList.add(x.visit(tcv));
                }
            }
        }
        xitype = nv.typeCheck(typeList, this);
        return xitype; // this must be kept as nv!
    }

    public IRNode visit(ASTIRVisitor irv) {
        List<IRNode> irList = new ArrayList<IRNode>();
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                Node x = children[i];

                if (x != null) {
                    irList.add(x.visit(irv));
                } else {
                    irList.add(null);
                }
            }
        }
        return irv.IRTranslate(irList, this);
    }

}
