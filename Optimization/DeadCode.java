package zw494.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import zw494.CFG.ControlFlowGraph;
import zw494.CFG.ControlFlowGraphMaster;
import zw494.edu.cornell.cs.cs4120.xic.ir.*;

public class DeadCode extends DataFlowAnal<IRNode, HashSet<String>> {

    public DeadCode(ControlFlowGraphMaster<IRNode> cfg, List<IRNode> w) {

        this.cfg = cfg;
        this.w = new LinkedList<>();
        this.w.addAll(w);
        this.in = new HashMap<>();
        this.out = new HashMap<>();

        for (IRNode n : w) {
            in.put(n, new HashSet<String>());
            out.put(n, new HashSet<String>());
        }

    }

    public void apply(){

        this.worklistAlgoBackward(); 

        for(IRNode n: out.keySet()){

            // System.out.println(n.toString());
            // System.out.println(out.get(n).toString());

            if(n instanceof IRMove){
                IRMove m = (IRMove) n;
                IRExpr e = m.target();
                if(e instanceof IRTemp){
                    IRTemp tmp = (IRTemp) e;
                    String name = tmp.name();

                    if(name.contains("_RET")){
                        continue;
                    }

                    if(!out.get(n).contains(name)){
                        
                        ControlFlowGraph cfg2 = (ControlFlowGraph) cfg;
                        cfg2.deleteStatement(n);
                    }
                }
            }

        }

    }

    @Override
    public HashSet<String> meet(List<HashSet<String>> t) {

        HashSet<String> result = new HashSet<>();

        for (Set<String> s : t) {
            result.addAll(s);
        }

        return result;
    }

    @Override
    public HashSet<String> transfer(IRNode n, HashSet<String> t) {

        HashSet<String> rec = new HashSet<>();
        rec.addAll(t);

        if (n instanceof IRMove) {
            IRMove m = (IRMove) n;

            if (m.target() instanceof IRTemp) {

                IRTemp tar = (IRTemp) m.target();

                rec.remove(tar.name());

                IRExpr src = m.source();

                rec.addAll(use(src));

            } else if (m.target() instanceof IRMem) {

                IRMem tar = (IRMem) m.target();
                IRExpr src = m.source();

                rec.addAll(use(tar.expr()));
                rec.addAll(use(src));

            } else {
                System.out.println();
                throw new Error("SHOULD NOT BE HERE" + n.label());
            }

        }
        if (n instanceof IRCJump) {
            IRCJump c = (IRCJump) n;
            IRExpr e = c.cond();

            rec.addAll(use(e));

        }
        if (n instanceof IRCallStmt) {

            IRCallStmt c = (IRCallStmt) n;

            for (String tmp : c.collectors()) {
                rec.remove(tmp);
            }

            for(IRExpr e: c.args()){
                rec.addAll(use(e));
            }
        }

        return rec;
    }

    private HashSet<String> use(IRExpr src) {

        HashSet<String> set = new HashSet<>();

        if(src instanceof IRBinOp){

            IRBinOp b1 = (IRBinOp) src;

            set.addAll(use(b1.left()));
            set.addAll(use(b1.right()));

            return set;

        }

        if(src instanceof IRConst){

            return set;

        }

        if(src instanceof IRTemp){

            IRTemp t1 = (IRTemp) src;

            set.add(t1.name());

            return set;

        }

        if(src instanceof IRMem){

            IRMem m1 = (IRMem) src;

            set.addAll(use(m1.expr()));

            return set;

        }

        return set;

    }

    @Override
    public boolean hasSameElt(HashSet<String> t1, HashSet<String> t2) {

        if(t1.size() != t2.size())
            return false;
        
        for(String s: t1){

            if(!t2.contains(s))
                return false;

        }

        return true;
    }

    @Override
    public void cond(List<IRNode> lst) {
        return;
    }
    
}