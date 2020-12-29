package zw494.Optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import zw494.AST.Factory;
import zw494.CFG.ControlFlowGraph;
import zw494.CFG.ControlFlowGraphMaster;
import zw494.CFG.FlowNode;
import zw494.edu.cornell.cs.cs4120.xic.ir.*;

public class Loop implements Comparable<Loop>{

    IRNode header;

    IRNode backwardNode;

    Set<IRNode> members;

    Set<IRNode> exitNodes;

    public Loop(IRNode header, IRNode backwardNode, ControlFlowGraphMaster<IRNode> cfg ) {
 
        this.header = header;
        this.backwardNode = backwardNode;
        this.members = new HashSet<>();
        this.exitNodes = new HashSet<>();
        this.members.add(header);

        
    }

    public void hoist(IRNode ir, IRExpr e, ControlFlowGraph cfg){

        ControlFlowGraph graph =  (ControlFlowGraph) cfg;

        IRTemp frshTmp = new IRTemp(Factory.tempFactory());
        
        IRNode fill = new IRMove(frshTmp, e);

        frshTmp = new IRTemp(frshTmp.name());

        identify(ir, e, frshTmp); 

        graph.hoist(this.header, fill, members);

    }

    private void identify(IRNode ir, IRExpr e2, IRTemp frshTmp) {
        // System.out.println("identifying");
        // System.out.println(ir + " " + e2);

        if(ir instanceof IRMove){
            IRMove m = (IRMove) ir;
            IRExpr src = m.source();

            if (src.equals(e2)){

                m.setSource(frshTmp);
            }
            else findNreplace(src, e2, frshTmp);

        }
        else if(ir instanceof IRCallStmt){
            IRCallStmt c = (IRCallStmt) ir;
            List<IRExpr> args = c.args();

            List<IRExpr> newArgs = new ArrayList<>();

            for (IRExpr arg: args){
                if (arg.equals(e2)){
                    newArgs.add(frshTmp);
                }
                else{
                    findNreplace(arg, e2, frshTmp);
                    newArgs.add(arg);
                }
            }
            
        }
        else if(ir instanceof IRCJump){
            IRCJump cj = (IRCJump) ir;
            IRExpr tar = cj.cond();

            if (tar.equals(e2)){
                cj.setCond(frshTmp);
            }

            else findNreplace(tar, e2, frshTmp);
            
        }
        else{
            throw new Error("More than just 3 Guys");
        }

    }

    private void findNreplace(IRExpr e1, IRExpr e2, IRTemp frshTmp) {
        // System.out.println("replacing");
        // System.out.println(e1 + " " + e2);

        if(e1 instanceof IRBinOp){
            IRBinOp b = (IRBinOp) e1;
            IRExpr left = b.left();
            IRExpr right = b.right();

            if(left.equals(e2)){
                b.setLeft(frshTmp);
            }
            else if(right.equals(e2)){
                b.setRight(frshTmp);
            }
            else{
                findNreplace(left, e2, frshTmp);
                findNreplace(right, e2, frshTmp);
            }
        }
        else if(e1 instanceof IRTemp){
        }
        else if(e1 instanceof IRMem){
            IRMem m = (IRMem) e1;
            IRExpr mem = m.expr();

            if(mem.equals(e2)){
                m.setExpr(frshTmp);
                // System.out.println("replaced!");
            }
            else{
                // System.out.println("not replaced");
                findNreplace(mem, e2, frshTmp);
            }
        }
        else if(e1 instanceof IRConst){
        }
        else{
            throw new Error("Peter sent me the wrong shiz");
        }



    }
    
    public Set<IRNode>  allUses(IRNode n) {

        Set<IRNode> use = new HashSet<>();


        if (n instanceof IRMove) {
            IRMove m = (IRMove) n;

            if (m.target() instanceof IRTemp) {

                IRNode src = m.source();

                use.addAll(use(src));

            } else if (m.target() instanceof IRMem) {

                IRMem tar = (IRMem) m.target();
                IRNode src = m.source();

                use.addAll(use(tar.expr()));
                use.addAll(use(src));

            } else {
                // System.out.println();
                throw new Error("SHOULD NOT BE HERE" + n.label());
            }

        }
        if (n instanceof IRCJump) {
            IRCJump c = (IRCJump) n;
            IRNode e = c.cond();

            use.addAll(use(e));

        }
        if (n instanceof IRCallStmt) {

            IRCallStmt c = (IRCallStmt) n;

            for (IRNode tmp : c.args()) {
                use.add(tmp);
            }
        }

        return use;

    }

    private HashSet<IRNode> use(IRNode src) {

        HashSet<IRNode> set = new HashSet<>();

        if (src instanceof IRBinOp) {

            IRBinOp b1 = (IRBinOp) src;

            set.addAll(use(b1.left()));
            set.addAll(use(b1.right()));

            return set;

        }

        if (src instanceof IRConst) {

            return set;

        }

        if (src instanceof IRTemp) {

            IRTemp t1 = (IRTemp) src;

            set.add(t1);

            return set;

        }

        if (src instanceof IRMem) {

            IRMem m1 = (IRMem) src;

            set.addAll(use(m1.expr()));

            return set;

        }

        return set;

    }

    public int compareTo(Loop l){
        
        // large size first
        return l.members.size() - members.size();

    }


}