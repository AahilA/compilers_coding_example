package zw494.AST;

import zw494.AST.Statements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zw494.AST.Expressions.*;
import zw494.AST.Literal.*;
import zw494.AST.Variable.*;
import zw494.AST.XiTypes.*;
import zw494.edu.cornell.cs.cs4120.xic.ir.*;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRBinOp.OpType;
import zw494.edu.cornell.cs.cs4120.xic.ir.interpret.Configuration;

public class ASTIRVisitor {
    public ASTIRVisitor() {

    }

    /**
     * Convert the name of the function in standard Xi format specified in the ABI.
     * 
     * @param args suffix for arguments
     * @param rets suffix for return values
     * @param id   original function name
     * @return the new function name
     */
    public String getFuncName(String args, String rets, String id) {
        String new_id = "";
        for (int i = 0; i < id.length(); i++) {
            if (id.charAt(i) != '_')
                new_id += id.substring(i, i + 1);
            else
                new_id += "_" + "_";
        }
        if (rets == "") {
            rets = "p";
        }
        String name = "_I" + new_id + "_" + rets + args;
        return name;
    }

    /**
     * Translate an AST node [x] into an IR node.
     * 
     * @param irList results of translation of all [x]'s children
     * @param x      AST node to translate
     * @return the IR node translated
     */
    public IRNode IRTranslate(List<IRNode> irList, Node x) {
        try {
            if (x instanceof Program) {
                return irList.get(1);
            }
            if (x instanceof NodeList) {
                if (((NodeList) x).attr == 1) {
                    Map<String, IRFuncDecl> map = new HashMap<String, IRFuncDecl>();
                    for (int i = 0; i < irList.size(); ++i) {
                        IRFuncDecl f = (IRFuncDecl) irList.get(i);
                        if (f != null)
                            map.put(f.name(), f);
                    }
                    return new IRCompUnit("any", map);
                }

                List<IRStmt> stmts = new ArrayList<IRStmt>();
                for (int i = 0; i < irList.size(); ++i) {
                    if (irList.get(i) instanceof IRStmt)
                        stmts.add((IRStmt) irList.get(i));
                    if (irList.get(i) instanceof IRExpr)
                        stmts.add(new IRExp((IRExpr) irList.get(i)));
                    if (irList.get(i) == null) {
                        stmts.add(null);
                    }
                }
                return new IRSeq(stmts);
            }
            if (x instanceof Use) {
                return null; // use nodes are not translated
            }
            if (x instanceof Method) {
                Method y = (Method) x;
                List<IRStmt> stmts = new ArrayList<IRStmt>();
                // stmts.add(new IRLabel(y.methodName));
                IRSeq seq = (IRSeq) (irList.get(0));
                if (y.children[0].children != null) {
                    for (int i = 0; i < y.children[0].children.length; ++i) {
                        IRExp stmt = (IRExp) seq.stmts().get(i);
                        IRExpr expr = stmt.expr();
                        stmts.add(new IRMove(expr, new IRTemp(Configuration.ABSTRACT_ARG_PREFIX + i)));
                    }
                }
                IRSeq stmt = (IRSeq) irList.get(2);
                if (stmt.stmts().size() == 0) {
                    stmt.stmts().add(new IRReturn());
                } else if (!(stmt.stmts().get(stmt.stmts().size() - 1) instanceof IRReturn)) {
                    stmt.stmts().add(new IRReturn());
                }
                stmts.add((IRStmt) irList.get(2));
                // return new IRSeq(stmts);
                return new IRFuncDecl(getFuncName(y.args, y.rets, y.methodName), new IRSeq(stmts));
            }
            if (x instanceof BlockStmt) {
                return (IRStmt) irList.get(0);
            }
            if (x instanceof AssignmentStmt) {
                return new IRMove((IRExpr) irList.get(0), (IRExpr) irList.get(1));
            }
            if (x instanceof CallStatement) {
                CallStatement y = (CallStatement) x;
                List<IRExpr> list = new ArrayList<IRExpr>();
                List<IRStmt> stmts = new ArrayList<>();
                for (int i = 0; i < irList.size(); ++i) {
                    // stmts.add(new IRMove(new IRTemp("_ARG" + i), (IRExpr) irList.get(i)));
                    list.add((IRExpr) irList.get(i));
                }
                // TODO implement multiple returns
                stmts.add(new IRCallStmt(new ArrayList<String>(), new IRName(getFuncName(y.args, y.rets, y.id)), list));
                return new IRSeq(stmts);
            }
            if (x instanceof DeclStatement) {
                if (irList.size() == 1) {
                    IRSeq temp = (IRSeq) irList.get(0);
                    if (temp.stmts().get(0) != null)
                        return temp.stmts().get(0);
                    return new IRSeq(new ArrayList<IRStmt>());
                } else if (irList.get(1) instanceof IRCall) {
                    // multiple decls
                    IRCall y = (IRCall) irList.get(1);
                    IRSeq seqstmt = (IRSeq) irList.get(0);
                    List<String> collectors = new ArrayList<String>();
                    if (seqstmt == null) {
                        collectors.add(Factory.tempFactory());
                    } else {
                        for (int i = 0; i < seqstmt.stmts().size(); ++i) {
                            if ((IRExp) seqstmt.stmts().get(i) == null) {
                                collectors.add(Factory.tempFactory());
                                continue;
                            }
                            IRExp e = (IRExp) seqstmt.stmts().get(i);
                            if (e.expr() instanceof IRTemp) {
                                collectors.add(((IRTemp) e.expr()).name());
                            } else {
                                IRExpr er = ((IRESeq) e.expr()).expr();
                                if (!(er instanceof IRTemp))
                                    throw new Exception("left of multiple assign not a temp");
                                collectors.add(((IRTemp) er).name());
                            }
                            // stmts.add(new IRMove(er.expr(), new IRTemp("_RET" + i)));
                        }
                    }
                    IRCallStmt y_stmt = new IRCallStmt(collectors, y.target(), y.args());

                    return y_stmt;
                } else {
                    // save effort to allocate useless space
                    IRSeq seq = (IRSeq) irList.get(0);
                    if (seq == null || seq.stmts() == null)
                        return new IRSeq(new ArrayList<IRStmt>());
                    IRExpr exp = ((IRExp) seq.stmts().get(0)).expr();
                    if ((IRExpr) irList.get(1) != null) {
                        if (exp instanceof IRESeq) {
                            // this decl is a declaration with initialization wasted.
                            exp = ((IRESeq) exp).expr();
                        }
                        return new IRMove(exp, (IRExpr) irList.get(1));
                    } else {
                        IRSeq temp = (IRSeq) irList.get(0);
                        if (temp.stmts().get(0) != null)
                            return temp.stmts().get(0);
                        return new IRSeq(new ArrayList<IRStmt>());
                    }
                }
            }
            if (x instanceof ReturnStmt) {
                if (irList.size() == 0)
                    return new IRReturn();
                List<IRStmt> stmts = new ArrayList<IRStmt>();
                int n = irList.size();
                String[] w = new String[n];
                for (int i = 0; i < n; ++i) {
                    w[i] = Factory.tempFactory();
                }

                for (int i = 0; i < irList.size(); ++i) {
                    stmts.add(new IRMove(new IRTemp(w[i]), (IRExpr) irList.get(i)));
                }
                for (int i = 0; i < irList.size(); ++i) {
                    stmts.add(new IRMove(new IRTemp(Configuration.ABSTRACT_RET_PREFIX + i), new IRTemp(w[i])));
                }
                stmts.add(new IRReturn());
                return new IRSeq(stmts);
            }
            if (x instanceof IfStatement) {
                IfStatement y = (IfStatement) x;
                List<IRStmt> stmts = new ArrayList<IRStmt>();
                String lt = Factory.labelFactory();
                String lf = Factory.labelFactory();
                String le = Factory.labelFactory();
                stmts.add(CTranslate((Expr) y.children[0], new IRName(lt), new IRName(lf)));
                stmts.add(new IRLabel(lt));
                stmts.add((IRStmt) irList.get(1));
                stmts.add(new IRJump(new IRName(le)));
                stmts.add(new IRLabel(lf));
                if (irList.get(2) != null) {
                    stmts.add((IRStmt) irList.get(2));
                }
                stmts.add(new IRLabel(le));
                return new IRSeq(stmts);
            }
            if (x instanceof WhileStatement) {
                WhileStatement y = (WhileStatement) x;
                List<IRStmt> stmts = new ArrayList<IRStmt>();
                String lh = Factory.labelFactory();
                String lt = Factory.labelFactory();
                String lf = Factory.labelFactory();
                stmts.add(new IRLabel(lh));
                stmts.add(CTranslate((Expr) y.children[0], new IRName(lt), new IRName(lf)));
                stmts.add(new IRLabel(lt));
                stmts.add((IRStmt) irList.get(1));
                stmts.add(new IRJump(new IRName(lh)));
                stmts.add(new IRLabel(lf));
                return new IRSeq(stmts);
            }
            if (x instanceof BinaryExpression) {
                BinaryExpression y = (BinaryExpression) x;
                IRExpr left = (IRExpr) irList.get(0);
                // System.out.println(left + " " + ((CharacterLiteral)
                // y.children[0].children[0]).c);
                IRExpr right = (IRExpr) irList.get(1);
                XiType type = y.children[0].xitype;
                switch (y.op) {
                    case ADD:
                        // array addition
                        if (((Tao) type).numberOfBrackets == 0)
                            return new IRBinOp(OpType.ADD, left, right);
                        else {
                            List<IRStmt> stmts = new ArrayList<IRStmt>();
                            IRTemp lptr = new IRTemp(Factory.tempFactory());
                            stmts.add(new IRMove(lptr, left));
                            IRTemp rptr = new IRTemp(Factory.tempFactory());
                            stmts.add(new IRMove(rptr, right));

                            IRTemp len = new IRTemp(Factory.tempFactory());
                            stmts.add(new IRMove(len, new IRMem(new IRBinOp(OpType.SUB, lptr, new IRConst((long) 8)))));
                            IRTemp ren = new IRTemp(Factory.tempFactory());
                            stmts.add(new IRMove(ren, new IRMem(new IRBinOp(OpType.SUB, rptr, new IRConst((long) 8)))));

                            IRExpr totlen = new IRBinOp(OpType.ADD, len, ren);
                            IRTemp storelen = new IRTemp(Factory.tempFactory());
                            stmts.add(new IRMove(storelen, totlen));

                            IRESeq newarr = array_initialize(storelen);

                            IRTemp addr = new IRTemp(Factory.tempFactory());
                            IRTemp retval = new IRTemp(Factory.tempFactory());
                            stmts.add(new IRMove(retval, newarr));
                            stmts.add(new IRMove(addr, retval));

                            IRTemp i = new IRTemp(Factory.tempFactory());
                            String lh = Factory.labelFactory(), lt = Factory.labelFactory(),
                                    lf = Factory.labelFactory(), rh = Factory.labelFactory(),
                                    rt = Factory.labelFactory(), rf = Factory.labelFactory();
                            stmts.add(new IRMove(i, len));
                            stmts.add(new IRLabel(lh));
                            stmts.add(new IRCJump(new IRBinOp(OpType.GT, i, new IRConst(0)), lt, lf));
                            stmts.add(new IRLabel(lt));
                            stmts.add(new IRMove(new IRMem(addr), new IRMem(lptr)));
                            stmts.add(new IRMove(i, new IRBinOp(OpType.SUB, i, new IRConst(1))));
                            stmts.add(new IRMove(addr, new IRBinOp(OpType.ADD, addr, new IRConst(8))));
                            stmts.add(new IRMove(lptr, new IRBinOp(OpType.ADD, lptr, new IRConst(8))));
                            stmts.add(new IRJump(new IRName(lh)));
                            stmts.add(new IRLabel(lf));
                            stmts.add(new IRMove(i, ren));
                            stmts.add(new IRLabel(rh));
                            stmts.add(new IRCJump(new IRBinOp(OpType.GT, i, new IRConst(0)), rt, rf));
                            stmts.add(new IRLabel(rt));
                            stmts.add(new IRMove(new IRMem(addr), new IRMem(rptr)));
                            stmts.add(new IRMove(i, new IRBinOp(OpType.SUB, i, new IRConst(1))));
                            stmts.add(new IRMove(addr, new IRBinOp(OpType.ADD, addr, new IRConst(8))));
                            stmts.add(new IRMove(rptr, new IRBinOp(OpType.ADD, rptr, new IRConst(8))));
                            stmts.add(new IRJump(new IRName(rh)));
                            stmts.add(new IRLabel(rf));
                            return new IRESeq(new IRSeq(stmts), retval);
                        }
                    case SUB:
                        return new IRBinOp(OpType.SUB, left, right);
                    case MUL:
                        return new IRBinOp(OpType.MUL, left, right);
                    case DIV:
                        return new IRBinOp(OpType.DIV, left, right);
                    case MOD:
                        return new IRBinOp(OpType.MOD, left, right);
                    case LT:
                        return new IRBinOp(OpType.LT, left, right);
                    case LEQ:
                        return new IRBinOp(OpType.LEQ, left, right);
                    case GT:
                        return new IRBinOp(OpType.GT, left, right);
                    case GEQ:
                        return new IRBinOp(OpType.GEQ, left, right);
                    case NEQ:
                        return new IRBinOp(OpType.NEQ, left, right);
                    case AND: // TODO or is it?
                        ArrayList<IRStmt> stmts = new ArrayList<>();
                        String w = Factory.tempFactory();
                        stmts.add(new IRMove(new IRTemp(w), new IRConst(0)));
                        String l1 = Factory.labelFactory();
                        String l2 = Factory.labelFactory();
                        String lf = Factory.labelFactory();
                        stmts.add(new IRCJump(left, l1, lf));
                        stmts.add(new IRLabel(l1));
                        stmts.add(new IRCJump(right, l2, lf));
                        stmts.add(new IRLabel(l2));
                        stmts.add(new IRMove(new IRTemp(w), new IRConst(1)));
                        stmts.add(new IRLabel(lf));
                        IRSeq seq = new IRSeq(stmts);
                        IRESeq eseq = new IRESeq(seq, new IRTemp(w));
                        return eseq;
                    case OR: // TODO or is it?
                        stmts = new ArrayList<>();
                        w = Factory.tempFactory();
                        stmts.add(new IRMove(new IRTemp(w), new IRConst(1)));
                        l1 = Factory.labelFactory();
                        l2 = Factory.labelFactory();
                        lf = Factory.labelFactory();
                        stmts.add(new IRCJump(left, lf, l1));
                        stmts.add(new IRLabel(l1));
                        stmts.add(new IRCJump(right, lf, l2));
                        stmts.add(new IRLabel(l2));
                        stmts.add(new IRMove(new IRTemp(w), new IRConst(0)));
                        stmts.add(new IRLabel(lf));
                        seq = new IRSeq(stmts);
                        eseq = new IRESeq(seq, new IRTemp(w));
                        return eseq;
                    case EQUAL:
                        return new IRBinOp(OpType.EQ, left, right);
                    case HIGHMUL:
                        return new IRBinOp(OpType.HMUL, left, right);
                }
            }
            if (x instanceof UnaryExpression) {
                UnaryExpression y = (UnaryExpression) x;
                IRExpr z = (IRExpr) irList.get(0);
                switch (y.op) {
                    case NOT:
                        return new IRBinOp(OpType.SUB, new IRConst(1), z);
                    case SUB:
                        return new IRBinOp(OpType.SUB, new IRConst((long) 0), z);
                }
            }
            if (x instanceof FunctionCallExpression) {
                FunctionCallExpression y = (FunctionCallExpression) x;
                List<IRExpr> list = new ArrayList<IRExpr>();
                for (int i = 0; i < irList.size(); ++i) {
                    list.add((IRExpr) irList.get(i));
                }
                return new IRCall(new IRName(getFuncName(y.args, y.rets, y.id)), list);
            }
            if (x instanceof LengthExpression) {
                IRExpr z = (IRExpr) irList.get(0);
                return new IRMem(new IRBinOp(OpType.SUB, z, new IRConst((long) 8)));
            }
            if (x instanceof LiteralExpression) {
                // LiteralExpression y = (LiteralExpression) x;
                // System.out.println("lit " + y.children[0] + " " + irList.get(0));
                return (IRExpr) irList.get(0);
                // DO NOTHING
            }
            if (x instanceof VariableExpression) {
                return (IRExpr) irList.get(0);
                // DO NOTHING
            }
            if (x instanceof ArrayLiteral) {
                ArrayLiteral y = (ArrayLiteral) x;
                IRESeq result;
                if (y.children == null) {
                    result = array_initialize(0, new ArrayList<IRExpr>());
                } else {
                    Long arrLen = (long) y.children.length;
                    List<IRExpr> irexprlist = new ArrayList<IRExpr>();
                    for (int i = 0; i < arrLen; ++i)
                        irexprlist.add((IRExpr) irList.get(i));
                    result = array_initialize(arrLen, irexprlist);
                }
                return result;
            }
            if (x instanceof BooleanLiteral) {
                BooleanLiteral y = (BooleanLiteral) x;
                if (y.b == true)
                    return new IRConst(1);
                return new IRConst(0);
            }
            if (x instanceof CharacterLiteral) {
                CharacterLiteral y = (CharacterLiteral) x;
                long z = (long) y.c.charAt(0);
                if (z == '\\') {
                    switch (y.c.charAt(1)) {
                        case 'n':
                            z = (long) '\n';
                            break;
                        case '\"':
                            z = (long) '\"';
                            break;
                        case '\\':
                            z = (long) '\\';
                            break;
                        default:
                            // do nothing

                    }
                }
                IRConst w = new IRConst(z);
                // List<IRExpr> irexprlist = new LinkedList<IRExpr>();
                // irexprlist.add(new IRConst(Long.parseLong(y.c)));
                // IRESeq result = array_initialize(1, irexprlist);
                return w;
            }
            if (x instanceof StringLiteral) {
                StringLiteral y = (StringLiteral) x;
                List<IRExpr> irexprlist = new ArrayList<IRExpr>();
                int n = 0;
                Long strLen = (long) y.s.length();
                for (int i = 0; i < strLen; i++) {
                    n++;
                    long chr = (long) y.s.charAt(i);
                    if (chr == '\\') {
                        switch (y.s.charAt(i + 1)) {
                            case 'n':
                                chr = (long) '\n';
                                i++;
                                break;
                            case '\"':
                                chr = (long) '\"';
                                i++;
                                break;
                            case '\\':
                                chr = (long) '\\';
                                i++;
                                break;
                            default:
                                // do nothing

                        }
                    }
                    irexprlist.add(new IRConst(chr));
                }
                return array_initialize(n, irexprlist);
            }
            if (x instanceof IntegerLiteral) {
                IntegerLiteral y = (IntegerLiteral) x;
                long z = Long.parseUnsignedLong(y.i);
                return new IRConst(z);
            }
            if (x instanceof ArrayVariable) {
                List<IRStmt> stmts = new ArrayList<IRStmt>();

                // bounds check
                IRTemp left = new IRTemp(Factory.tempFactory());
                IRTemp right = new IRTemp(Factory.tempFactory());

                stmts.add(new IRMove(left, (IRExpr) irList.get(0)));
                stmts.add(new IRMove(right, (IRExpr) irList.get(1)));

                IRExpr length = new IRMem(new IRBinOp(OpType.SUB, left, new IRConst(((long) 8))));
                IRExpr cond = new IRBinOp(OpType.OR, new IRBinOp(OpType.LEQ, length, right),
                        new IRBinOp(OpType.LT, right, new IRConst(0)));
                String l1 = Factory.labelFactory();
                String l2 = Factory.labelFactory();
                IRStmt jump = new IRCJump(cond, l1, l2);
                IRExpr middle = new IRBinOp(OpType.MUL, right, new IRConst(((long) 8)));
                IRStmt label = new IRLabel(l1);
                IRCallStmt error = new IRCallStmt(new ArrayList<String>(), new IRName("_xi_out_of_bounds"));
                stmts.add(jump);
                stmts.add(label);
                stmts.add((IRStmt) error);
                label = new IRLabel(l2);
                stmts.add(label);
                IRExpr mem = new IRMem(new IRBinOp(OpType.ADD, left, middle));
                return new IRESeq(new IRSeq(stmts), mem);
            }
            if (x instanceof StringVariable) {
                StringVariable y = (StringVariable) x;
                return new IRTemp(y.id);
            }
            if (x instanceof Decl) {
                Decl y = (Decl) x;
                Type t = (Type) y.children[0];

                IRExpr z = new IRTemp(y.id);
                if (irList.get(0) == null)
                    return null;
                if (t.children[0] == null)
                    return z;
                return new IRESeq(new IRMove(z, (IRExpr) irList.get(0)), z);
            }
            if (x instanceof ParamDecl) {
                ParamDecl y = (ParamDecl) x;
                return new IRTemp(y.funcName);
            }
            if (x instanceof FuncType) {
                return null;
            }
            if (x instanceof Type) {
                // System.out.println(irList.get(0));
                if (irList.get(0) == null) {
                    // System.out.print("irList.get(0): ");
                    // System.out.println(irList.get(0));
                    assert (irList.get(1) == null);
                    return new IRCall(new IRName("_xi_alloc"), new IRConst(8));
                }
                if (irList.get(0) instanceof IRESeq) {
                    IRESeq y = (IRESeq) irList.get(0);
                    List<IRStmt> stmts = new ArrayList<IRStmt>();

                    IRTemp val = new IRTemp(Factory.tempFactory());
                    // System.out.print("Eseq irList.get(1): ");
                    // System.out.println((IRExpr) irList.get(1));
                    if ((IRExpr) irList.get(1) == null)
                        return array_initialize((IRExpr) irList.get(1));
                    stmts.add(new IRMove(val, (IRExpr) irList.get(1)));
                    IRESeq newarr = array_initialize(val);
                    IRTemp addr = new IRTemp(Factory.tempFactory());
                    IRTemp retval = new IRTemp(Factory.tempFactory());
                    stmts.add(new IRMove(retval, newarr));
                    stmts.add(new IRMove(addr, retval));

                    IRTemp i = new IRTemp(Factory.tempFactory());
                    String lh = Factory.labelFactory(), lt = Factory.labelFactory(), lf = Factory.labelFactory();
                    stmts.add(new IRMove(i, val));
                    stmts.add(new IRLabel(lh));
                    stmts.add(new IRCJump(new IRBinOp(OpType.GT, i, new IRConst(0)), lt, lf));
                    stmts.add(new IRLabel(lt));
                    stmts.add(new IRMove(new IRMem(addr), y));
                    stmts.add(new IRMove(i, new IRBinOp(OpType.SUB, i, new IRConst(1))));
                    stmts.add(new IRMove(addr, new IRBinOp(OpType.ADD, addr, new IRConst(8))));
                    stmts.add(new IRJump(new IRName(lh)));
                    stmts.add(new IRLabel(lf));
                    return new IRESeq(new IRSeq(stmts), retval);
                } else if (irList.get(0) instanceof IRExpr) {
                    // System.out.print("Expr irList.get(1): ");
                    // System.out.println((IRExpr) irList.get(1));
                    return array_initialize((IRExpr) irList.get(1));
                }
            }
            // if (x instanceof ErrNode) {
            // this.printer.printAtom(((ErrNode) x).message);
            // System.out.println(((ErrNode) x).message);
            // }

            if (x instanceof Interface) {
            }
            if (x instanceof Signature) {

            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate IR code to initialize a Xi array.
     * 
     * @param n   the size of the Xi array to initialize
     * @param lst list of expressions to be inserted in the array
     * @return a series of statements that initializes the array as intended
     */
    public IRESeq array_initialize(long n, List<IRExpr> lst) {
        List<IRStmt> stmts = new ArrayList<>();
        String return_addr = Factory.tempFactory();
        IRStmt call = new IRCallStmt(Arrays.asList(return_addr), new IRName("_xi_alloc"), new IRConst(n * 8 + 8));
        IRStmt res = new IRMove(new IRMem(new IRTemp(return_addr)), new IRConst(n));
        stmts.add(call);
        stmts.add(res);
        if (lst != null) {
            for (int i = 0; i < lst.size(); i++) {
                IRConst offset = new IRConst((i + 1) * 8);
                IRMove storeArr = new IRMove(new IRMem(new IRBinOp(OpType.ADD, new IRTemp(return_addr), offset)),
                        (IRExpr) lst.get(i));
                stmts.add(storeArr);
            }
        }
        return new IRESeq(new IRSeq(stmts), new IRBinOp(OpType.ADD, new IRTemp(return_addr), new IRConst(8)));
    }

    /**
     * Generate IR code to initialize a Xi array.
     * 
     * @param n an expression of the size of the Xi array to initialize
     * @return a series of statements that initializes the array as intended
     */
    public IRESeq array_initialize(IRExpr n) {
        List<IRStmt> stmts = new ArrayList<>();
        IRExpr addr;
        String return_addr = Factory.tempFactory();
        IRTemp new_n = new IRTemp(Factory.tempFactory());
        if (n != null) {
            stmts.add(new IRMove(new_n, n));
            IRBinOp temp = new IRBinOp(OpType.MUL, new_n, new IRConst(8));
            IRStmt call = new IRCallStmt(Arrays.asList(return_addr), new IRName("_xi_alloc"),
                    new IRBinOp(OpType.ADD, temp, new IRConst(8)));
            IRStmt res = new IRMove(new IRMem(new IRTemp(return_addr)), new_n);
            stmts.add(call);
            stmts.add(res);
            addr = new IRConst(8);
        } else {
            IRStmt call = new IRCallStmt(Arrays.asList(return_addr), new IRName("_xi_alloc"), new IRConst(8));
            IRStmt res = new IRMove(new IRMem(new IRTemp(return_addr)), new IRConst(0));
            stmts.add(call);
            stmts.add(res);
            addr = new IRConst(8);
        }
        return new IRESeq(new IRSeq(stmts), new IRBinOp(OpType.ADD, new IRTemp(return_addr), addr));
    }

    /**
     * Translate an C expression C(E[e], t, f).
     * 
     * @return a series of IR statements that translates C(E[e], t, f)
     */
    public IRStmt CTranslate(Expr e, IRExpr t, IRExpr f) {
        if (e instanceof BinaryExpression) {
            BinaryExpression y = (BinaryExpression) e;
            switch (y.op) {
                case AND:
                    List<IRStmt> stmts = new ArrayList<IRStmt>();
                    String l1 = Factory.labelFactory();
                    IRStmt cstmt = CTranslate((Expr) y.children[0], new IRName(l1), f);
                    stmts.add(cstmt);
                    stmts.add(new IRLabel(l1));
                    stmts.add(CTranslate((Expr) y.children[1], t, f));
                    IRStmt retstmt = new IRSeq(stmts);
                    return retstmt;
                case OR:
                    stmts = new ArrayList<IRStmt>();
                    l1 = Factory.labelFactory();
                    cstmt = CTranslate((Expr) y.children[0], t, new IRName(l1));
                    stmts.add(cstmt);
                    stmts.add(new IRLabel(l1));
                    stmts.add(CTranslate((Expr) y.children[1], t, f));
                    retstmt = new IRSeq(stmts);
                    return retstmt;
                // TODO: add new cases
                default:

            }
        }
        if (e instanceof UnaryExpression) {
            UnaryExpression y = (UnaryExpression) e;
            if (y.op == UnaryExpression.UnOp.NOT) {
                return CTranslate((Expr) y.children[0], f, t);
            }

        }
        if (e instanceof LiteralExpression) {
            LiteralExpression y = (LiteralExpression) e;
            if (y.children[0] instanceof BooleanLiteral) {
                Boolean b = ((BooleanLiteral) (y.children[0])).b;
                if (b == true) {
                    // System.out.println("I am here!");
                    return new IRJump(t);
                } else {
                    // System.out.println("I am there!");
                    return new IRJump(f);
                }
            }
        }

        String lname = ((IRName) t).name();
        String rname = ((IRName) f).name();
        return new IRCJump((IRExpr) e.visit(this), lname, rname);
    }
}