package zw494.AST;

import zw494.edu.cornell.cs.cs4120.util.*;
import zw494.AST.Statements.*;
import zw494.AST.Expressions.*;
import zw494.AST.Literal.*;
import zw494.AST.Variable.*;

public class PrintVisitor {

    SExpPrinter printer;

    /**
     * Printer object created for printing from cornell given printer.
     * 
     * @param printer
     */
    public PrintVisitor(SExpPrinter printer) {
        this.printer = printer;
    }

    /**
     * Node entering command for printing, based on type of node encountered.
     */
    public void enter(Node x) {

        if (x instanceof Program) {
            this.printer.startUnifiedList();
        }
        if (x instanceof NodeList) {
            this.printer.startUnifiedList();
        }
        if (x instanceof Use) {
            this.printer.startList();
            this.printer.printAtom("use");
            this.printer.printAtom(((Use) x).useName);
        }
        if (x instanceof Method) {
            this.printer.startList();
            this.printer.printAtom(((Method) x).methodName);
        }
        if (x instanceof BlockStmt) {
            // DO NOTHING
        }
        if (x instanceof AssignmentStmt) {
            this.printer.startList();
            this.printer.printAtom("=");
        }
        if (x instanceof CallStatement) {
            this.printer.startList();
            this.printer.printAtom(((CallStatement) x).id);
        }
        if (x instanceof DeclStatement) {

            DeclStatement ds = (DeclStatement) x;
            if (ds.children[1] != null) {
                this.printer.startList();
                this.printer.printAtom("=");
            }
        }
        if (x instanceof ReturnStmt) {
            this.printer.startList();
            this.printer.printAtom("return");
        }
        if (x instanceof IfStatement) {
            this.printer.startUnifiedList();
            this.printer.printAtom("if");
        }
        if (x instanceof WhileStatement) {
            this.printer.startUnifiedList();
            this.printer.printAtom("while");
        }
        if (x instanceof BinaryExpression) {
            this.printer.startList();
            switch (((BinaryExpression) x).op) {
                case ADD:
                    this.printer.printAtom("+");
                    break;
                case SUB:
                    this.printer.printAtom("-");
                    break;
                case MUL:
                    this.printer.printAtom("*");
                    break;
                case DIV:
                    this.printer.printAtom("/");
                    break;
                case MOD:
                    this.printer.printAtom("%");
                    break;
                case LT:
                    this.printer.printAtom("<");
                    break;
                case LEQ:
                    this.printer.printAtom("<=");
                    break;
                case GT:
                    this.printer.printAtom(">");
                    break;
                case GEQ:
                    this.printer.printAtom(">=");
                    break;
                case NEQ:
                    this.printer.printAtom("!=");
                    break;
                case AND:
                    this.printer.printAtom("&");
                    break;
                case OR:
                    this.printer.printAtom("|");
                    break;
                case EQUAL:
                    this.printer.printAtom("==");
                    break;
                case HIGHMUL:
                    this.printer.printAtom("*>>");
                    break;
            }
        }
        if (x instanceof UnaryExpression) {
            this.printer.startList();
            switch (((UnaryExpression) x).op) {
                case NOT:
                    this.printer.printAtom("!");
                    break;
                case SUB:
                    this.printer.printAtom("-");
                    break;
            }
        }
        if (x instanceof FunctionCallExpression) {
            this.printer.startList();
            this.printer.printAtom(((FunctionCallExpression) x).id);
        }
        if (x instanceof LengthExpression) {
            this.printer.startList();
            this.printer.printAtom("length");
        }
        if (x instanceof LiteralExpression) {
            // DO NOTHING
        }
        if (x instanceof VariableExpression) {
            // DO NOTHING
        }
        if (x instanceof ArrayLiteral) {
            this.printer.startList();
        }
        if (x instanceof BooleanLiteral) {
            this.printer.printAtom(((BooleanLiteral) x).b.toString());
        }
        if (x instanceof CharacterLiteral) {
            this.printer.printAtom("\'" + ((CharacterLiteral) x).c.toString() + "\'");
        }
        if (x instanceof StringLiteral) {
            this.printer.printAtom("\"" + ((StringLiteral) x).s.toString() + "\"");
        }
        if (x instanceof IntegerLiteral) {
            this.printer.printAtom(((IntegerLiteral) x).i.toString());
        }
        if (x instanceof ArrayVariable) {
            this.printer.startList();
            this.printer.printAtom("[]");
        }
        if (x instanceof StringVariable) {
            this.printer.printAtom(((StringVariable) x).id);
        }
        if (x instanceof Decl) {
            Decl y = (Decl) x;
            if (y.id != null) {
                if (!y.isAlone)
                    this.printer.startList();
                this.printer.printAtom(((Decl) x).id);
            } else
                this.printer.printAtom("_");
        }
        if (x instanceof ParamDecl) {
            this.printer.startList();
            this.printer.printAtom(((ParamDecl) x).funcName);
        }
        if (x instanceof FuncType) {

            for (int i = 0; i < ((FuncType) x).numBrackets; i++) {
                this.printer.startList();
                this.printer.printAtom("[]");
            }

            switch (((FuncType) x).t) {
                case BOOL:
                    this.printer.printAtom("bool");
                    break;
                case INT:
                    this.printer.printAtom("int");
                    break;
            }
        }
        if (x instanceof Type) {
            Type y = (Type) x;
            Type z = (Type) (y.children[0]);
            if (z == null) {
                Type.DataType dt = y.dt;
                switch (dt) {
                    case INT:
                        this.printer.printAtom("int");
                        break;
                    case BOOL:
                        this.printer.printAtom("bool");
                        break;
                }
            } else {
                this.printer.startList();
                this.printer.printAtom("[]");
            }
        }
        // if (x instanceof ErrNode) {
        // this.printer.printAtom(((ErrNode) x).message);
        // System.out.println(((ErrNode) x).message);
        // }

        if (x instanceof Interface) {
            this.printer.startList();
        }
        if (x instanceof Signature) {
            this.printer.startList();
            this.printer.printAtom(((Signature) x).sigName);
        }
    }

    /**
     * Exit printing for a node based on the type of Node x.
     */
    public void exit(Node x) {
        if (x instanceof Program) {
            this.printer.endList();
        }
        if (x instanceof NodeList) {
            this.printer.endList();
        }
        if (x instanceof Use) {
            this.printer.endList();
        }
        if (x instanceof Method) {
            this.printer.endList();
        }
        if (x instanceof BlockStmt) {
            // DO NOTHING
        }
        if (x instanceof AssignmentStmt) {
            this.printer.endList();
        }
        if (x instanceof CallStatement) {
            this.printer.endList();
        }
        if (x instanceof DeclStatement) {
            if (((DeclStatement) x).children[1] != null)
                this.printer.endList();
        }
        if (x instanceof ReturnStmt) {
            this.printer.endList();
        }
        if (x instanceof IfStatement) {
            this.printer.endList();
        }
        if (x instanceof WhileStatement) {
            this.printer.endList();
        }
        if (x instanceof BinaryExpression) {
            this.printer.endList();
        }
        if (x instanceof UnaryExpression) {
            this.printer.endList();
        }
        if (x instanceof FunctionCallExpression) {
            this.printer.endList();
        }
        if (x instanceof LengthExpression) {
            this.printer.endList();
        }
        if (x instanceof LiteralExpression) {
            // DO NOTHING
        }
        if (x instanceof VariableExpression) {
            // DO NOTHING
        }
        if (x instanceof BooleanLiteral) {
            // DO NOTHING
        }
        if (x instanceof CharacterLiteral) {
            // DO NOTHING
        }
        if (x instanceof StringLiteral) {
            // DO NOTHING
        }
        if (x instanceof IntegerLiteral) {
            // DO NOTHING
        }
        if (x instanceof ArrayLiteral) {
            this.printer.endList();
        }
        if (x instanceof ArrayVariable) {
            this.printer.endList();
        }
        if (x instanceof StringVariable) {
            // DO NOTHING
        }
        if (x instanceof Decl) {
            Decl y = (Decl) x;
            if (y.id != null && !y.isAlone)
                this.printer.endList();
        }
        if (x instanceof ParamDecl) {
            this.printer.endList();
        }
        if (x instanceof FuncType) {
            for (int i = 0; i < ((FuncType) x).numBrackets; i++) {
                this.printer.endList();
            }
        }
        if (x instanceof Type) {
            Type y = (Type) x;
            Type z = (Type) (y.children[0]);
            if (z != null) {
                this.printer.endList();
            }
        }
        if (x instanceof Interface) {
            this.printer.endList();
        }
        if (x instanceof Signature) {
            this.printer.endList();
        }
    }
}