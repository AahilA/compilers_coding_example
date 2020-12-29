package zw494.AST;

import java.util.LinkedList;

import zw494.AST.Statements.BlockStmt;

public class Method extends Node {

   final String methodName;
   String args = "";
   String rets = "";

   /**
    * Method structure node with return type specified.
    * 
    * @param methodName
    * @param fdl        Arguements
    * @param rl         Return type
    * @param b
    */
   public Method(String methodName, LinkedList<Node> fdl, LinkedList<Node> rl, BlockStmt b, int row, int col) {
      children = new Node[3];
      children[0] = new NodeList(fdl, 2);
      children[1] = new NodeList(rl, 3);
      children[2] = (Node) b;
      this.methodName = methodName;
      this.rowpos = row;
      this.colpos = col;
   }

   /**
    * Method structure node without return type specified.
    * 
    * @param methodName
    * @param fdl
    * @param b
    */
   public Method(String methodName, LinkedList<Node> fdl, BlockStmt b, int row, int col) {
      children = new Node[3];
      children[0] = new NodeList(fdl, 2);
      children[1] = new NodeList(null, 3);
      children[2] = (Node) b;
      this.methodName = methodName;
      this.rowpos = row;
      this.colpos = col;
   }

}
