package zw494.AST;

import zw494.AST.Statements.*;
import zw494.AST.Type.DataType;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import zw494.AST.Expressions.*;
import zw494.AST.FuncType.FuncTypeType;
import zw494.AST.Literal.*;
import zw494.AST.Variable.*;
import zw494.AST.XiTypes.*;
import zw494.AST.XiTypes.R.Rtype;
import zw494.AST.XiTypes.Sigma.SigmaType;
import zw494.AST.XiTypes.Tao.taoType;

public class TypeCheckVisitor {

    SymTable st;
    HashMap<String, String> argsTable = new HashMap<>();
    HashMap<String, String> retsTable = new HashMap<>(); 

    public TypeCheckVisitor() {
        st = new SymTable();
    }

    /**
     * Creates a deep clone of the typevisitor in order to save scope.
     * 
     * @return
     */
    protected TypeCheckVisitor deepClone() {
        TypeCheckVisitor cloned = new TypeCheckVisitor();

        for (String key : this.st.symTable.keySet()) {
            cloned.st.add(key, this.st.symTable.get(key));
        }

        for (String key: this.argsTable.keySet()) {
            cloned.argsTable.put(key, this.argsTable.get(key));
        }

        for (String key: this.retsTable.keySet()) {
            cloned.retsTable.put(key, this.retsTable.get(key));
        }

        return cloned;
    }

    /**
     * Single first pass through all the functions collecting their return types and
     * arguement types and storing them within the symbol table.
     * 
     * @param m Method node to be passed
     */
    public void functionProcess(Method m) throws SemanticsError {

        String name = m.methodName;
        if (this.st.lookup(name) != null) {
            throw new SemanticsError(m, "function with name " + name + " already declared");
        }

        NodeList args = (NodeList) m.children[0];
        NodeList ret = (NodeList) m.children[1];
        T argT;
        if (args.children == null) {
            argT = new T(0, null);
        } else if (args.children.length == 0) {
            argT = new T(0, null);
        } else {
            List<Tao> tl = new LinkedList<Tao>();

            for (int i = 0; i < args.children.length; ++i) {

                ParamDecl d = (ParamDecl) args.children[i];
                FuncType funki = (FuncType) d.children[0];

                taoType tt;

                for (int j = 0; j < funki.numBrackets; j ++) {
                    m.args += "a";
                }

                if (funki.t == FuncTypeType.INT) {
                    tt = taoType.INT;
                    m.args += "i";
                }
                else {
                    tt = taoType.BOOL;
                    m.args += "b";
                }
                tl.add(new Tao(tt, funki.numBrackets));
            }
            argT = new T(tl.size(), tl);
        }

        T retT;
        if (ret.children == null) {
            retT = new T(0, null);
        } else if (ret.children.length == 0) {
            retT = new T(0, null);
        } else {
            List<Tao> tl = new LinkedList<Tao>();
            if (ret.children.length > 1) {
                m.rets += "t";
                m.rets += String.valueOf(ret.children.length);
            }
            for (int i = 0; i < ret.children.length; ++i) {

                FuncType funki = (FuncType) ret.children[i];

                taoType tt;

                for (int j = 0; j < funki.numBrackets; j ++) {
                    m.rets += "a";
                }

                if (funki.t == FuncTypeType.INT) {
                    tt = taoType.INT;
                    m.rets += "i";
                }
                else {
                    tt = taoType.BOOL;
                    m.rets += "b";
                }

                tl.add(new Tao(tt, funki.numBrackets));
            }
            retT = new T(tl.size(), tl);
        }

        Sigma balls = new Sigma(SigmaType.FN2, argT, retT);

        this.st.add(name, balls);
        // System.out.println(m.methodName + " args: " + m.args);
        // System.out.println(m.methodName + " rets: " + m.rets);
        this.argsTable.put(m.methodName, m.args);
        this.retsTable.put(m.methodName, m.rets);
    }

    /**
     * Pass through all the signatures in interface file and store the return type
     * and arg type in the symbol table.
     * 
     * @param s
     */
    public void signatureProcess(Signature s) throws SemanticsError {

        String name = s.sigName;
        NodeList args = (NodeList) s.children[0];
        NodeList ret = (NodeList) s.children[1];
        T argT;
        Sigma olddecl = (Sigma) this.st.lookup(name);

        if (olddecl != null) {
            if (olddecl.st != SigmaType.FN1 && olddecl.st != SigmaType.FN2)
                throw new SemanticsError(s, "should never happen");
            if (args.children == null)
                if (olddecl.t.tl != null && olddecl.t.tl.size() != 0)
                    throw new SemanticsError(s, "repeating defining signatures that does not match");
            if (olddecl.t.tl == null)
                if (args.children != null && args.children.length != 0)
                    throw new SemanticsError(s, "repeating defining signatures that does not match");
            if (args.children != null && olddecl.t.tl != null) {
                if (args.children.length != olddecl.t.tl.size())
                    throw new SemanticsError(s, "repeating defining signatures that does not match");
                throw new SemanticsError(s, "function with name " + name + " already declared");
            }
            if (ret.children == null)
                if (olddecl.tp.tl != null && olddecl.tp.tl.size() != 0)
                    throw new SemanticsError(s, "repeating defining signatures that does not match");
            if (olddecl.tp.tl == null)
                if (ret.children != null && ret.children.length != 0)
                    throw new SemanticsError(s, "repeating defining signatures that does not match");
            if (ret.children != null && olddecl.tp.tl != null) {
                if (ret.children.length != olddecl.tp.tl.size())
                    throw new SemanticsError(s, "repeating defining signatures that does not match");
                throw new SemanticsError(s, "function with name " + name + " already declared");
            }
        }

        if (args.children == null) {
            argT = new T(0, null);
        } else if (args.children.length == 0) {
            if (olddecl != null && olddecl.t.tl.size() != 0)
                throw new SemanticsError(s, "repeating defining signatures that does not match");
            argT = new T(0, null);
        } else {
            List<Tao> tl = new LinkedList<Tao>();
            for (int i = 0; i < args.children.length; ++i) {

                ParamDecl d = (ParamDecl) args.children[i];
                FuncType funki = (FuncType) d.children[0];

                for (int j = 0; j < funki.numBrackets; j ++) {
                    s.args += "a";
                }

                taoType tt;

                if (funki.t == FuncTypeType.INT) {
                    tt = taoType.INT;
                    s.args += "i";
                }
                else {
                    tt = taoType.BOOL;
                    s.args += "b";
                }
                Tao curTao = new Tao(tt, funki.numBrackets);
                if (olddecl != null) {
                    if (!Tao.isEquals(olddecl.t.tl.get(i), curTao))
                        throw new SemanticsError(s, "repeating defining signatures that does not match");
                }
                tl.add(curTao);
            }

            argT = new T(tl.size(), tl);
        }

        T retT;
        if (ret.children == null) {
            retT = new T(0, null);
        } else if (ret.children.length == 0) {
            if (olddecl != null && olddecl.tp.tl.size() != 0)
                throw new SemanticsError(s, "repeating defining signatures that does not match");
            retT = new T(0, null);
        } else {
            List<Tao> tl = new LinkedList<Tao>();
            if (ret.children.length > 1) {
                s.rets += "t";
                s.rets += String.valueOf(ret.children.length);
            }

            for (int i = 0; i < ret.children.length; ++i) {

                FuncType funki = (FuncType) ret.children[i];

                for (int j = 0; j < funki.numBrackets; j ++) {
                    s.rets += "a";
                }

                taoType tt;

                if (funki.t == FuncTypeType.INT) {
                    tt = taoType.INT;
                    s.rets += "i";
                }
                else {
                    tt = taoType.BOOL;
                    s.rets += "b";
                }
                Tao curTao = new Tao(tt, funki.numBrackets);
                if (olddecl != null) {
                    if (!Tao.isEquals(olddecl.tp.tl.get(i), curTao))
                        throw new SemanticsError(s, "repeating defining signatures that does not match");
                }
                tl.add(curTao);
            }
            retT = new T(tl.size(), tl);
        }

        Sigma balls = new Sigma(SigmaType.FN2, argT, retT);

        this.st.add(name, balls);
        this.argsTable.put(name, s.args);
        this.retsTable.put(name, s.rets);

    }

    /**
     * Node entering command for printing, based on type of node encountered.
     */
    public XiType typeCheck(List<XiType> typelist, Node x) throws SemanticsError {
        try {
            if (x instanceof Program) {
                return new Success();
            }
            if (x instanceof NodeList) {
                // 1: list of methods/signatures -> correct program
                // 2: list of parameter decls -> T
                // 3: list of return types -> T
                // 4: list of stmts -> R
                // 5: list of decls -> T
                switch (((NodeList) x).attr) {
                    case 1:
                        return new Success();
                    case 2:
                    case 3:
                    case 5:
                        if (typelist == null || typelist.size() == 0)
                            return new T(0, null);
                        assert (typelist.get(0) instanceof Tao);
                        List<Tao> taolist = new LinkedList<Tao>();
                        for (int i = 0; i < typelist.size(); i++) {
                            if (typelist.get(i) == null)
                                taolist.add(null);
                            else {
                                taolist.add((Tao) typelist.get(i));
                            }
                        }
                        return new T(typelist.size(), taolist);
                    case 4:
                        if (typelist == null || typelist.size() == 0) {
                            return new R(Rtype.UNIT);
                        }
                        assert (typelist.get(typelist.size() - 1) instanceof R);
                        for (int i = 0; i < typelist.size() - 1; ++i)
                            if (((R) typelist.get(i)).rt != Rtype.UNIT)
                                throw new SemanticsError(x.children[i], "early return statement");
                        return (R) typelist.get(typelist.size() - 1);
                }
            }
            if (x instanceof Use) {
                return new Success();
            }
            if (x instanceof Method) {
                Method m = (Method) x;
                if (m.children[1].children == null || m.children[1].children.length == 0) {
                    return new Success();
                } else {
                    R r = (R) typelist.get(2);
                    if (r.rt == R.Rtype.UNIT) {
                        throw new Error("Return statement expected");
                    }
                    return new Success();
                }
            }
            if (x instanceof BlockStmt) {
                // System.out.println(((NodeList) x.children[0]).attr);
                // System.out.println(typelist.get(0).getClass());
                return typelist.get(0);
            }
            if (x instanceof AssignmentStmt) {

                assert (typelist.get(0) instanceof Tao);

                assert (typelist.get(1) instanceof Tao);

                Tao varType = (Tao) typelist.get(0);
                Tao exprType = (Tao) typelist.get(1);
                if (!Tao.isSubtype(exprType, varType))
                    throw new Error("types of expressions left and right of '=' do not match");

                return new R(Rtype.UNIT);

            }
            if (x instanceof CallStatement) {
                CallStatement c = (CallStatement) x;

                Sigma balls = (Sigma) this.st.lookup(c.id);
                c.args = this.argsTable.get(c.id);
                c.rets = this.retsTable.get(c.id);
                // System.out.println("CallStmt " + c.id + " args: " + c.args);
                // System.out.println("CallStmt " + c.id + " rets: " + c.rets);

                if (balls == null) {
                    throw new Error("Procedure does not exist");
                }

                T t = balls.t;

                if (balls.st == SigmaType.FN2) {

                    T tp = balls.tp;

                    if (tp.tType != 0)
                        throw new Error("This is not a procedure");

                    switch (t.tType) {
                        case 0:
                            return new R(Rtype.UNIT);
                        default:
                            for (int i = 0; i < t.tType; i++) {
                                assert (typelist.get(i) instanceof Tao);
                                if (!(Tao.isSubtype((Tao) typelist.get(i), t.tl.get(i))))
                                    throw new Error("Incompatible arguement types");
                            }
                            return new R(Rtype.UNIT);
                    }
                } else if (balls.st == SigmaType.FN1) {
                    Tao tao = balls.tao;

                    if (tao != null)
                        throw new Error("This is not a procedure");

                    switch (t.tType) {
                        case 0:
                            return new R(Rtype.UNIT);
                        default:
                            for (int i = 0; i < t.tType; i++) {
                                assert (typelist.get(i) instanceof Tao);
                                if (!(Tao.isSubtype((Tao) typelist.get(i), t.tl.get(i))))
                                    throw new Error("Incompatible arguement types");
                            }
                            return new R(Rtype.UNIT);
                    }
                }

            }
            if (x instanceof DeclStatement) {
                assert (typelist.get(0) instanceof T);
                T varList = (T) typelist.get(0); // <>
                if (typelist.size() == 1) {
                    assert (varList.tType == 0);
                    return new R(Rtype.UNIT);
                }
                XiType right = typelist.get(1);
                if (right == null) {
                    assert (varList.tType == 0);
                    return new R(Rtype.UNIT);
                } else if (right instanceof Tao) {
                    assert (varList.tType == 1);
                    if (varList == null) {
                        return new R(Rtype.UNIT);
                    }
                    if (varList.tl == null || varList.tl.size() != 1)
                        throw new Error("declaration type and declaration assignment type do not match");
                    if (!Tao.isSubtype((Tao) right, varList.tl.get(0))) {
                        throw new Error("declaration type and declaration assignment type do not match");
                    }
                    return new R(Rtype.UNIT);
                } else if (right instanceof T) {
                    T t = (T) right;
                    if (varList == null) {
                        if (t.tl == null || t.tl.size() != 1)
                            throw new Error("declaration type and declaration assignment type do not match");
                        return new R(Rtype.UNIT);
                    }
                    if (varList.tType != t.tType)
                        throw new Error("incompatible types at left and right of a declaration");
                    if ((varList.tl == null || varList.tl.size() == 0) && (t.tl == null || t.tl.size() == 0))
                        return new R(Rtype.UNIT);
                    if (varList.tl == null || varList.tl.size() == 0)
                        throw new Error("declaration type and declaration assignment type do not match");
                    if (t.tl == null || t.tl.size() == 0)
                        throw new Error("declaration type and declaration assignment type do not match");
                    for (int i = 0; i < varList.tType; i++) {
                        Tao var = varList.tl.get(i);
                        if (var == null)
                            continue; // CHANGE when OO
                        if (!(Tao.isSubtype(t.tl.get(i), var))) {
                            throw new Error("declaration type and declaration assignment type do not match");
                        }
                    }
                    return new R(Rtype.UNIT);
                } else {
                    assert (false); // should never happen
                }
            }
            if (x instanceof Decl) {
                //
                Decl d = (Decl) x;
                if (d.id != null) {
                    assert (typelist.size() == 1 && typelist.get(0) instanceof Tao);
                    Tao typeTao = (Tao) typelist.get(0);
                    if (this.st.lookup(d.id) != null)
                        throw new Error("declaring variables already declared");
                    this.st.add(d.id, new Sigma(SigmaType.VAR, typeTao));

                    return typeTao;
                }
                return null;
            }
            if (x instanceof ReturnStmt) {
                ReturnStmt r = (ReturnStmt) x;

                Sigma balls = (Sigma) this.st.lookup("0");
                assert (balls != null) : "return null?";
                if (balls.st != SigmaType.RET)
                    throw new Error("Not a return sigma");
                T t;
                Tao tao;
                if (balls.t != null) {
                    t = balls.t;
                    if (r.children == null) {
                        if (t.tType != 0)
                            throw new Error("Return type not unit");
                        return (new R(Rtype.VOID));
                    } else {
                        assert (t.tType == r.children.length) : "Children and number of types not same?";
                        for (int i = 0; i < t.tType; i++) {
                            assert (typelist.get(i) instanceof Tao);
                            if (!(Tao.isSubtype((Tao) typelist.get(i), t.tl.get(i))))
                                throw new Error("Incompatible argument types");
                        }
                    }
                } else if (balls.tao != null) {
                    tao = balls.tao;
                    if (r.children == null) {
                        throw new Error("Return type not unit");
                    } else {
                        assert (r.children.length == 1) : "Children and number of types not same?";
                        if (!(Tao.isSubtype((Tao) typelist.get(0), tao)))
                            throw new Error("Incompatible argument types");
                    }
                } else {
                    throw new Error("should never happen");
                }

                return new R(Rtype.VOID);
            }
            if (x instanceof IfStatement) {
                IfStatement ifstmt = (IfStatement) x;
                if (ifstmt.children[2] == null) {
                    assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof R);
                    Tao e = (Tao) typelist.get(0);

                    if (!Tao.isSubtype(e, new Tao(taoType.BOOL, 0)))
                        throw new Error("if statement condition not boolean");

                    assert (typelist.get(1) instanceof R);

                    return new R(Rtype.UNIT);
                } else {
                    assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof R
                            && typelist.get(2) instanceof R);
                    Tao e = (Tao) typelist.get(0);

                    if (!Tao.isSubtype(e, new Tao(taoType.BOOL, 0)))
                        throw new Error("if statement condition not boolean");

                    assert (typelist.get(1) instanceof R);
                    assert (typelist.get(2) instanceof R);

                    Rtype r1 = ((R) typelist.get(1)).rt;
                    Rtype r2 = ((R) typelist.get(2)).rt;

                    Rtype r = (r1 == Rtype.UNIT || r2 == Rtype.UNIT) ? Rtype.UNIT : r1;

                    return new R(r);
                }
            }
            if (x instanceof WhileStatement) {

                assert (typelist.get(0) instanceof Tao);

                Tao e = (Tao) typelist.get(0);

                if (!Tao.isSubtype(e, new Tao(taoType.BOOL, 0))) {
                    throw new Error("incorrect guard type in while statement");
                }

                assert (typelist.get(1) instanceof R);

                return new R(Rtype.UNIT);

            }
            if (x instanceof BinaryExpression) {
                BinaryExpression b = (BinaryExpression) x;
                Tao e1;
                Tao e2;
                assert (typelist.size() == 2);
                switch (b.op) {
                    case ADD: // ({}[1] + {}[2][3]) & true
                        assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof Tao);
                        e1 = (Tao) typelist.get(0);
                        e2 = (Tao) typelist.get(1);
                        if (Tao.isSubtype(e1, new Tao(taoType.INT, 0)))
                            if (Tao.isSubtype(e2, new Tao(taoType.INT, 0))) {
                                if ((e1.tao == taoType.ANYPLUSTYPE || e1.tao == taoType.ANYTYPE)
                                        && (e2.tao == taoType.ANYPLUSTYPE || e2.tao == taoType.ANYTYPE))
                                    return new Tao(taoType.ANYPLUSTYPE, 0);
                                return new Tao(taoType.INT, 0);
                            }

                        if ((e1.tao != taoType.ANYPLUSTYPE) && (e1.tao != taoType.ANYTYPE)) {
                            // System.out.println(e1.tao);
                            if (e1.numberOfBrackets == 0)
                                throw new Error("int or arrays expected at operator PLUS #1");
                        }
                        if ((e2.tao != taoType.ANYPLUSTYPE) && (e2.tao != taoType.ANYTYPE)) {
                            if (e2.numberOfBrackets == 0)
                                throw new Error("int or arrays expected at operator PLUS #2");
                        }
                        if (!Tao.isSubtype(e1, e2) && !Tao.isSubtype(e2, e1))
                            throw new Error("int or arrays expected at operator PLUS #3");
                        if ((e1.tao == taoType.ANYTYPE || e1.tao == taoType.ANYPLUSTYPE)
                                && (e2.tao == taoType.ANYTYPE || e2.tao == taoType.ANYPLUSTYPE))
                            return new Tao(taoType.ANYPLUSTYPE, Integer.max(e1.numberOfBrackets, e2.numberOfBrackets));
                        if (e1.tao == taoType.ANYTYPE || e1.tao == taoType.ANYPLUSTYPE)
                            return e2;
                        else
                            return e1;
                    case HIGHMUL:
                    case DIV:
                    case MOD:
                    case SUB:
                    case MUL:
                        assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof Tao);
                        e1 = (Tao) typelist.get(0);
                        e2 = (Tao) typelist.get(1);

                        if (Tao.isSubtype(e1, new Tao(taoType.INT, 0)))
                            if (Tao.isSubtype(e2, new Tao(taoType.INT, 0)))
                                return new Tao(taoType.INT, 0);

                        throw new Error("int expected at operator " + b.op.toString());
                    case GEQ:
                    case GT:
                    case LEQ:
                    case LT:
                        assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof Tao);
                        e1 = (Tao) typelist.get(0);
                        e2 = (Tao) typelist.get(1);

                        if (Tao.isSubtype(e1, new Tao(taoType.INT, 0)))
                            if (Tao.isSubtype(e2, new Tao(taoType.INT, 0)))
                                return new Tao(taoType.BOOL, 0);
                        throw new Error("int expected at operator " + b.op.toString());
                    case AND:
                    case OR:
                        assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof Tao);
                        e1 = (Tao) typelist.get(0);
                        e2 = (Tao) typelist.get(1);

                        if (Tao.isSubtype(e1, new Tao(taoType.BOOL, 0)))
                            if (Tao.isSubtype(e2, new Tao(taoType.BOOL, 0)))
                                return new Tao(taoType.BOOL, 0);
                        throw new Error("bool expected at operator " + b.op.toString());
                    case NEQ:
                    case EQUAL: // anytype[] == bool[][]
                        assert (typelist.get(0) instanceof Tao && typelist.get(1) instanceof Tao);
                        e1 = (Tao) typelist.get(0);
                        e2 = (Tao) typelist.get(1);

                        if (Tao.isSubtype(e1, e2) || (Tao.isSubtype(e2, e1)))
                            return new Tao(taoType.BOOL, 0);
                        throw new Error("int or bool expected at operator " + b.op.toString());
                }

            }
            if (x instanceof UnaryExpression)

            {
                UnaryExpression u = (UnaryExpression) x;
                Tao e;

                assert (typelist.size() == 1);
                switch (u.op) {
                    case NOT:
                        assert (typelist.get(0) instanceof Tao);
                        e = (Tao) typelist.get(0);

                        if (Tao.isSubtype(e, new Tao(taoType.BOOL, 0)))
                            return new Tao(taoType.BOOL, 0);

                        throw new Error("expected bool after operator '!'");

                    case SUB:
                        assert (typelist.get(0) instanceof Tao);
                        e = (Tao) typelist.get(0);

                        if (Tao.isSubtype(e, new Tao(taoType.INT, 0)))
                            return new Tao(taoType.INT, 0);

                        throw new Error("expected int after operator '-'");
                }
            }
            if (x instanceof FunctionCallExpression) {
                FunctionCallExpression f = (FunctionCallExpression) x;

                Sigma balls = (Sigma) this.st.lookup(f.id);
                f.args = this.argsTable.get(f.id);
                f.rets = this.retsTable.get(f.id);
                // System.out.println("FunctionCall " + f.id + " args: " + f.args);
                // System.out.println("FunctionCall " + f.id + " rets: " + f.rets);

                if (balls == null)
                    throw new Error("unbound function " + f.id);

                assert (balls.st == SigmaType.FN1 || balls.st == SigmaType.FN2);

                T t = balls.t;
                if (balls.st == SigmaType.FN2) {
                    T tp = balls.tp;

                    if (tp == null || tp.tType == 0)
                        throw new Error("function call expression cannot call on procedure");

                    assert (t != null);
                    switch (t.tType) {
                        case 0:
                            return tp;
                        default:
                            assert (t.tl != null || t.tl.size() != 0);
                            for (int i = 0; i < t.tType; i++) {
                                assert (typelist.get(i) instanceof Tao);
                                if (!(Tao.isSubtype((Tao) typelist.get(i), t.tl.get(i))))
                                    throw new Error("incompatiable arguement types");
                            }
                            return tp;
                    }
                } else if (balls.st == SigmaType.FN1) {
                    Tao tao = balls.tao;

                    if (tao == null)
                        throw new Error("function call expression cannot call on procedure");

                    assert (t != null);
                    switch (t.tType) {
                        case 0:
                            return tao;
                        default:
                            assert (t.tl != null || t.tl.size() != 0);
                            for (int i = 0; i < t.tType; i++) {
                                assert (typelist.get(i) instanceof Tao);
                                if (!(Tao.isSubtype((Tao) typelist.get(i), t.tl.get(i))))
                                    throw new Error("incompatiable arguement types");
                            }
                            return tao;
                    }
                }

            }
            if (x instanceof LengthExpression) {
                assert (typelist.size() == 1);
                assert (typelist.get(0) instanceof Tao);
                Tao t = (Tao) typelist.get(0);
                if ((t.tao != taoType.ANYTYPE && t.tao != taoType.ANYPLUSTYPE) && t.numberOfBrackets == 0) {
                    throw new Error("'length' keyword applied to non array");
                }

                return new Tao(taoType.INT, 0);

            }
            if (x instanceof LiteralExpression) {
                LiteralExpression le = (LiteralExpression) x;
                assert (le.children.length == 1);
                assert (typelist.size() == 1 && typelist.get(0) instanceof Tao);
                return typelist.get(0);
            }
            if (x instanceof VariableExpression) {
                VariableExpression le = (VariableExpression) x;
                assert (le.children.length == 1);
                assert (typelist.size() == 1 && typelist.get(0) instanceof Tao);
                return typelist.get(0);
            }
            if (x instanceof ArrayLiteral) {
                ArrayLiteral arrlit = (ArrayLiteral) x;
                if (arrlit.children == null)
                    return new Tao(taoType.ANYTYPE, 1); // {} {}[1] {}[1][1] {{}[1], {}[1], {true}} bool[][]
                if (arrlit.children.length == 0)
                    return new Tao(taoType.ANYTYPE, 1);
                assert (typelist.get(0) instanceof Tao);
                Tao retType = (Tao) typelist.get(0);
                for (int i = 1; i < typelist.size(); i++) {
                    assert (typelist.get(i) instanceof Tao);
                    Tao thisType = (Tao) typelist.get(i);
                    if (Tao.isSubtype(retType, thisType)) {
                        retType = new Tao(thisType.tao, thisType.numberOfBrackets);
                    } else if (Tao.isSubtype(thisType, retType)) { // do nothing
                    } else {
                        throw new Error("incompatible types within an array initializer");
                    }
                }
                retType = new Tao(retType.tao, retType.numberOfBrackets + 1);
                return retType;
                // throw new Error("expected consistant tao types in array literal");
            }
            if (x instanceof BooleanLiteral) {
                return new Tao(taoType.BOOL, 0);
            }
            if (x instanceof CharacterLiteral) {
                return new Tao(taoType.INT, 0);
            }
            if (x instanceof StringLiteral) {
                return new Tao(taoType.INT, 1);
            }
            if (x instanceof IntegerLiteral) {
                return new Tao(taoType.INT, 0);
            }
            if (x instanceof ArrayVariable) {

                assert (typelist.get(0) instanceof Tao);
                assert (typelist.get(1) instanceof Tao);
                Tao t1 = (Tao) typelist.get(0);
                Tao t2 = (Tao) typelist.get(1);
                assert (t1.numberOfBrackets >= -1);
                if (t1.tao == taoType.ANYTYPE)
                    return new Tao(t1.tao, t1.numberOfBrackets > 0 ? t1.numberOfBrackets - 1 : 0);
                if (t1.numberOfBrackets == 0) {
                    throw new Error("Attempt to index an object that is not an array");
                }
                if (!Tao.isSubtype(t2, new Tao(taoType.INT, 0))) {
                    throw new Error("Array index should be an integer");
                }

                return new Tao(t1.tao, t1.numberOfBrackets - 1);

            }
            if (x instanceof StringVariable) {

                StringVariable s = (StringVariable) x;

                Sigma balls = (Sigma) this.st.lookup(s.id);

                if (balls == null)
                    throw new Error("Unbound variable " + s.id);

                assert (balls.st == SigmaType.VAR);

                return balls.tao;

            }

            if (x instanceof ParamDecl) {

                ParamDecl d = (ParamDecl) x;

                assert (typelist.get(0) instanceof Tao);
                Tao f = (Tao) typelist.get(0);
                if (this.st.lookup(d.funcName) != null)
                    throw new Error("function parameter name already declared");
                this.st.add(d.funcName, new Sigma(SigmaType.VAR, f));
                return f;
            }
            if (x instanceof FuncType) {
                FuncType f = (FuncType) x;

                if (f.t == FuncTypeType.BOOL) {
                    return new Tao(taoType.BOOL, f.numBrackets);
                } else {
                    return new Tao(taoType.INT, f.numBrackets);
                }

            }
            if (x instanceof Type) {

                Type t = (Type) x;
                if (t.children[0] == null) {
                    if (t.dt == DataType.INT) {
                        return new Tao(taoType.INT, 0);
                    } else if (t.dt == DataType.BOOL) {
                        return new Tao(taoType.BOOL, 0);
                    }
                } else {
                    assert (typelist.size() > 0);
                    assert (typelist.get(0) instanceof Tao);
                    Tao tao = (Tao) typelist.get(0);
                    if (t.children[1] != null) {
                        assert (typelist.size() == 2);
                        assert (typelist.get(1) instanceof Tao);
                        Tao t2 = (Tao) typelist.get(1);
                        if (!Tao.isSubtype(t2, new Tao(taoType.INT, 0)))
                            throw new Error("Type index should be an integer");
                    }
                    return new Tao(tao.tao, tao.numberOfBrackets + 1);
                }

            }

            if (x instanceof Interface) {
                return new Success();
            }
            if (x instanceof Signature) {
                signatureProcess((Signature) x);
                return new Success();
            }

            // should never be here
            return new T(0, null);
        } catch (Error e) {
            throw new SemanticsError(x, e.getMessage());
        }
    }

}
