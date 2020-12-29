package zw494.AST;

public class FuncType extends Node {
  public enum FuncTypeType {
    INT, BOOL
  }

  public FuncTypeType t;
  public int numBrackets;

  /**
   * Function return type node.
   * 
   * @param t
   * @param numBrackets
   */
  public FuncType(FuncTypeType t, int numBrackets, int row, int col) {

    this.t = t;
    this.numBrackets = numBrackets;
    this.rowpos = row;
    this.colpos = col;
  }

  /**
   * Function return type node.
   * 
   * @param t
   */
  public FuncType(FuncTypeType t, int row, int col) {

    this.t = t;
    this.numBrackets = 0;
    this.rowpos = row;
    this.colpos = col;

  }

}
