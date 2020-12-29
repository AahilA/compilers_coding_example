package zw494.Optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.ResourceBundle.Control;

import zw494.AST.Factory;
import zw494.CFG.ControlFlowGraph;
import zw494.CFG.ControlFlowGraphMaster;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRBinOp;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRCJump;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRCallStmt;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRConst;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRExpr;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRLabel;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRMem;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRMove;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRNode;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRTemp;

public class LoopInvThingy extends DataFlowAnal<IRNode, Set<IRNode>> {



    public LoopInvThingy(ControlFlowGraph cfg, List<IRNode> w){

        this.cfg = cfg;
        this.w = new LinkedList<>();
        this.w.addAll(w);
        this.in = new HashMap<>();
        this.out = new HashMap<>();

        for (IRNode n : w) {
            Set<IRNode> inSet = new HashSet<>();
            if (n != w.get(0)){
                inSet.addAll(w);
            }
            in.put(n, inSet);
            Set<IRNode> outSet = new HashSet<>();
            if (n != w.get(0)){
                outSet.addAll(w);
            }
            out.put(n, outSet);
        }

    }

    public List<Loop> createLoop(){

        this.worklistAlgoForward();

        // System.out.println("creating loop");

        List<Loop> ret = new ArrayList<>();

        for(IRNode n : in.keySet()){

            Set<IRNode> doms = in.get(n);

            // System.out.println(n);
            // System.out.println("DOM" + doms);

            for(IRNode c: cfg.getChildren(n)){

                if(doms.contains(c)){

                    Loop loop = new Loop(c,n,this.cfg);

                    resetLoop(loop);

                    ret.add(loop);

                }

            }
        }

        Collections.sort(ret);

        return ret;

    }

    

    public void dfs(Loop loop, IRNode curNode){

        loop.members.add(curNode);

        for (IRNode n : cfg.getParent(curNode)){

            if (loop.members.contains(n))
                continue;

            if (!n.equals(loop.header)){
                dfs(loop, n);
            }
        }

    }

    public boolean isDominatingAllExits(Loop l, IRNode i){
        assert(l.members.contains(i));

        for (IRNode j: l.exitNodes){
    
            Set<IRNode> dom = in.get(j);

            if (!dom.contains(i)) return false;

        }

        return true;

    }

    @Override
    public Set<IRNode> meet(List<Set<IRNode>> t) {
        HashSet<IRNode> result = new HashSet<>();

        if(t.size() == 0)
            return result;

        result.addAll(t.get(0));

        for (Set<IRNode> s : t) {
            result.retainAll(s);
        }

        return result;
    }

    @Override
    public Set<IRNode> transfer(IRNode n, Set<IRNode> t) {
        HashSet<IRNode> rec = new HashSet<>();
        rec.addAll(t);
        rec.add(n);
        return rec;
    }

    @Override
    public boolean hasSameElt(Set<IRNode> t1, Set<IRNode> t2) {
        
        if(t1.size()!=t2.size()){
            return false;
        }

        for(IRNode s: t1){
            if(!t2.contains(s))
                return false;
        }

        return true;
    }

    @Override
    public void cond(List<IRNode> lst) {
        return;
    }

    public void addExitNodes(Loop loop){
        for (IRNode i : loop.members){

            Set<IRNode> child = new HashSet<>();
            child.addAll(cfg.getChildren(i));

            child.removeAll(loop.members);

            loop.exitNodes.addAll(child);

        }
    }

    public void loopInvariantCodeMotion(){

        ControlFlowGraph graph = (ControlFlowGraph) cfg;

        List<Loop> loops = createLoop();

        for (Loop i : loops){

            resetLoop(i);

            // System.out.println("resetting loop!");

            ReachingDef rdef = new ReachingDef(graph.getNodes(), graph);

            rdef.worklistAlgoForward();

            // System.out.println("members of a loop:");
            // System.out.println(i.members);
            // System.out.println("backward node of a loop:");
            // System.out.println(i.backwardNode);
            // System.out.println("header node of a loop:");
            // System.out.println(i.header);


            Map<IRNode, IRNode> limap = loopInvIdentify(i, rdef);

            // System.out.println(limap);

            graph.hoist(i.header, new IRLabel(Factory.labelFactory()), i.members);

            for (IRNode expr : limap.keySet()){

                i.hoist(limap.get(expr), (IRExpr)expr, graph);

            }
        }
    }

    public void resetLoop(Loop i){

        i.members = new HashSet<>();

        i.members.add(i.header);

        dfs(i, i.backwardNode);

    }

    // ir expr -- ir stmts that defines the loop invariant
    public Map<IRNode, IRNode> loopInvIdentify(Loop i, ReachingDef rdef){

        Map<IRNode, IRNode> ret = new HashMap<>();

        // step 1: initialize all expressions in the loop into loop invariant
        // except memory

        for (IRNode j : i.members){

            addStatement(ret, j);

        }

        // System.out.println(ret);
        // System.out.println("______________");



        // step 2.1: remove all uses that has more than one def inside the 
        // loop

        List<IRNode> removeExpr = new ArrayList<>();

        for (IRNode expr : ret.keySet()){


            if (!(expr instanceof IRTemp)) continue;

            IRNode stmt = ret.get(expr);

            Map<String, Set<IRNode>> map = rdef.in.get(stmt);


            IRTemp temp = (IRTemp) expr;

            Set<IRNode> defs = new HashSet<>();

            if (map.get(temp.name()) != null){
            
                defs.addAll(map.get(temp.name()));
            
            }


            defs.retainAll(i.members);

            if (defs.size() > 0)
                removeExpr.add(expr);

        }

        for (IRNode k : removeExpr){
            ret.remove(k);
        }

        // System.out.println(ret);
        // System.out.println("_______________");


        // step 2.2 remove all x = e where e is not in ret

        boolean done = true;

        while (done){

            done = false;

            for (IRNode j : i.members){


                done |= statementProcess(j, ret, rdef);

            }

        }

        // System.out.println(ret);
        // System.out.println("______________");

        // step 3: get the top exprs that are loop invariant, but not consts or temps only

        for (IRNode j : i.members){
            filterStatement(j, ret);
        }

        return ret;

    }

    public void addStatement(Map<IRNode, IRNode> ret, IRNode stmt){

        List<IRNode> exprs = new ArrayList<>();

        if (stmt instanceof IRMove){
            IRMove k = (IRMove) stmt;
            addExpression(exprs, k.source());
        }
        if (stmt instanceof IRCJump){
            IRCJump k = (IRCJump) stmt;
            addExpression(exprs, k.cond());
        }
        if (stmt instanceof IRCallStmt){
            IRCallStmt k = (IRCallStmt) stmt;
            for (IRNode l : k.args()){
                addExpression(exprs, l);
            }
        }

        for (IRNode expr : exprs){

            ret.put(expr, stmt);

        }
    }

    public boolean addExpression(List<IRNode> list, IRNode e){

        if (e instanceof IRConst || e instanceof IRTemp){

            list.add(e);
            return true;

        }

        if (e instanceof IRMem){

            IRMem m = (IRMem) e;
            addExpression(list, m.expr());
            return false;

        }

        if (e instanceof IRBinOp){


            IRBinOp m = (IRBinOp) e;
            boolean b1 = addExpression(list, m.left());
            boolean b2 = addExpression(list, m.right());
            if (b1 && b2){
                list.add(e);
                return true;
            }

        }
        return false;

    }

    // ir expr -- ir stmt
    public boolean removeExpr(Map<IRNode, IRNode> map, IRNode expr){


        if (expr instanceof IRTemp){

            return map.get(expr) == null;

        }
        if (expr instanceof IRMem){

            IRMem m = (IRMem) expr;

            if (removeExpr(map, m.expr())){
                map.remove(m.expr());
            }

        }
        if (expr instanceof IRBinOp){

            IRBinOp m = (IRBinOp) expr;

            boolean b1 = removeExpr(map, m.left());
            boolean b2 = removeExpr(map, m.right());

            if (b1) map.remove(m.left());
            if (b2) map.remove(m.right());

            return b1 || b2;

        }

        return false;

    }

    public boolean statementProcess(IRNode j, Map<IRNode, IRNode> ret, ReachingDef rd){


        if (j instanceof IRMove){
            IRMove k = (IRMove) j;
            if (removeExpr(ret, k.source())){
                ret.remove(k.source());

                IRNode target = k.target();

                if (target instanceof IRTemp){

                    IRTemp w = (IRTemp) target;


                    boolean flag = false;

                    Set<IRNode> retSet = new HashSet<>();

                    for (IRNode i : ret.keySet()){

                        if (i instanceof IRTemp){

                            IRTemp t = (IRTemp) i;
                            
                            if (w.name().equals(t.name()) && rd.in.get(ret.get(t)).get(w.name()).contains(j))
                            {    
                                retSet.add(t);
                                flag = true;
                            }

                        }

                    }
                    for (IRNode i : retSet){
                        ret.remove(i);
                    }

                    return flag;

                }
            }
        }
        if (j instanceof IRCJump){

            IRCJump k = (IRCJump) j;
            if (removeExpr(ret, k.cond())){
                ret.remove(k.cond());
            }

        }
        if (j instanceof IRCallStmt){

            IRCallStmt k = (IRCallStmt) j;
            for (IRNode l : k.args()){
                if (removeExpr(ret, l)){
                    ret.remove(l);
                }
            }

        }

        return false;
    }

    public void filterStatement(IRNode j, Map<IRNode, IRNode> ret){

        if (j instanceof IRMove){
            IRMove k = (IRMove) j;
            
            filterExpr(k.source(), ret);
            filterExpr(k.target(), ret);
        }
        if (j instanceof IRCJump){

            IRCJump k = (IRCJump) j;
            filterExpr(k.cond(), ret);

        }
        if (j instanceof IRCallStmt){

            IRCallStmt k = (IRCallStmt) j;
            for (IRNode l : k.args()){
                filterExpr(l, ret);
            }

        }


    }

    public void filterExpr(IRNode expr, Map<IRNode, IRNode> map){

        if (expr instanceof IRTemp || expr instanceof IRConst){

            map.remove(expr);

        }
        if (expr instanceof IRMem){

            IRMem m = (IRMem) expr;

            filterExpr(m.expr(), map);

        }
        if (expr instanceof IRBinOp){

            IRBinOp m = (IRBinOp) expr;

            if (map.get(m) != null){

                map.remove(m.left());
                map.remove(m.right());

            }

            filterExpr(m.left(), map);

            filterExpr(m.right(), map);
            

        }

    }
    
}