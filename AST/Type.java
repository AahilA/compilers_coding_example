package zw494.AST;

import zw494.AST.Expressions.Expr;

/**
 * AST node for type
 */
public class Type extends Node {

	public enum DataType {
		INT, BOOL
	}

	DataType dt;

	/**
	 * Type takes in a Type t and an expression. it uses the DataType of t as its
	 * DataType.
	 */
	public Type(Type t, Expr child, int row, int col) {
		this.dt = t.dt;
		children = new Node[2];
		children[0] = (Node) t;
		children[1] = child;
		this.rowpos = row;
		this.colpos = col;
	}

	/**
	 * Type takes in a DataType dt as its DataType. When there is no other argument,
	 * set the children to null.
	 */
	public Type(DataType dt, int row, int col) {
		this.dt = dt;
		children = new Node[2];
		children[0] = null;
		children[1] = null;
		this.rowpos = row;
		this.colpos = col;
	}

}

//int || bool[2][]

// int[][] x = new int[2][0]

// bool[2][3][] 
// bool[3][] 2 
// bool[]  3
// bool null

// bool[2][3]
// bool[3] 2
// bool 3
