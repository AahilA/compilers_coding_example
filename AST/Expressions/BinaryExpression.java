package zw494.AST.Expressions;

import zw494.AST.Node;

/**
 * AST node for binary expression
 */
public class BinaryExpression extends Expr {

	public enum BinOp {
		ADD, SUB, MUL, DIV, MOD, LT, LEQ, GT, GEQ, NEQ, AND, OR, EQUAL, HIGHMUL
	}

	public BinOp op;

	/**
	 * BinaryExpression is a type of expression that takes in a left expression and
	 * a right expression and an enum for the op.
	 */
	public BinaryExpression(Expr left, Expr right, BinOp op, int rowpos, int colpos) {
		children = new Node[2];
		children[0] = left;
		children[1] = right;
		this.op = op;
		this.rowpos = rowpos;
		this.colpos = colpos;
	}

}