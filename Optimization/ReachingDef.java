package zw494.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zw494.CFG.ControlFlowGraph;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRCallStmt;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRExpr;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRMove;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRNode;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRStmt;
import zw494.edu.cornell.cs.cs4120.xic.ir.IRTemp;

public class ReachingDef extends DataFlowAnal<IRNode, Map<String, Set<IRNode>>> {


    public ReachingDef(List<IRNode> instrs, ControlFlowGraph cfg){

        this.in = new HashMap<>();
        this.out = new HashMap<>();
        this.cfg = cfg;
        this.w = new LinkedList<>();
        w.addAll(instrs);

        for (IRNode n: w){
            in.put(n, new HashMap<>());
            out.put(n, new HashMap<>());
        }

    }

    @Override
    public Map<String, Set<IRNode>> meet(List<Map<String, Set<IRNode>>> t) {

        Map<String, Set<IRNode>> retMap = new HashMap<>();

        for (Map<String, Set<IRNode>> map : t){

            for (String x : map.keySet()){
                if (retMap.get(x) == null){
                    retMap.put(x, new HashSet<>());
                }
                retMap.get(x).addAll(map.get(x));
            }

        }
        return retMap;
    }

    @Override
    public Map<String, Set<IRNode>> transfer(IRNode n, Map<String, Set<IRNode>> t) {



        IRStmt stmt = (IRStmt) n;

        Map<String, Set<IRNode>> ret = new HashMap<>();

        ret.putAll(t);

        if (stmt instanceof IRMove){

            IRMove y = (IRMove) stmt;

            IRExpr target = y.target();

            if (target instanceof IRTemp){

                IRTemp temp = (IRTemp) target;

                Set<IRNode> newSet = new HashSet<>();

                newSet.add(stmt);

                ret.put(temp.name(), newSet);

            }
            
        }

        if (stmt instanceof IRCallStmt){

            IRCallStmt y = (IRCallStmt) stmt;

            List<String> collectors = y.collectors();

            for (String str : collectors){


                Set<IRNode> newSet = new HashSet<>();
                newSet.add(stmt);

                ret.put(str, newSet);

            }

        }
        
        return ret;
    }

    @Override
    public boolean hasSameElt(Map<String, Set<IRNode>> t1, Map<String, Set<IRNode>> t2) {
        
        if (!t1.keySet().equals(t2.keySet())) return false;

        for (String x : t1.keySet()){
            Set<IRNode> v1 = t1.get(x);
            Set<IRNode> v2 = t2.get(x);
            if (!v1.equals(v2)) return false;
        }

        return true;

    }

    @Override
    public void cond(List<IRNode> lst) {
        
        return;

    }

}