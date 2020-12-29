package zw494.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import zw494.CFG.ControlFlowGraphMaster;
import zw494.edu.cornell.cs.cs4120.xic.ir.*;

public class CopyProp extends DataFlowAnal<IRNode, HashMap<String, String>>{

    public CopyProp(ControlFlowGraphMaster<IRNode> cfg, List<IRNode> list){
        
        this.cfg = cfg;
        this.w = new LinkedList<>();
        this.w.addAll(list);
        this.in = new HashMap<>();
        this.out = new HashMap<>();

        for (IRNode n : list) {
            in.put(n, new HashMap<String, String>());
            out.put(n, new HashMap<String, String>());
        }


    }

    public void apply(){

        this.worklistAlgoForward();

        for(IRNode n: out.keySet()){

            // System.out.println("n \n" + n.toString());
            // System.out.println("out n \n" + out.get(n).toString());

            if(n instanceof IRMove){
                IRMove m = (IRMove) n;
                IRExpr e = m.source();
                IRExpr e2 = m.target();

                // System.out.print("CopyProp IR Move 0 before replace" + m);

                String a = replace(e2, e, out.get(n));
                if(a != null){
                    m.setSource(new IRTemp(a));
                }

                // System.out.print("CopyProp IR Move 0 after replace" + m);
            }
            else if(n instanceof IRCallStmt){
                IRCallStmt c = (IRCallStmt) n;
                for(int i = 0; i < c.args().size(); i++){
                    IRExpr e = c.args().get(i);
                    String a = replace(null, e, out.get(n));
                    if(a != null){
                        c.args().set(i,new IRTemp(a));
                    }
                }
            }
            else if(n instanceof IRCJump){
                IRCJump c = (IRCJump) n;
                IRExpr e = c.cond();
                String a = replace(null, e, out.get(n));
                if(a != null){
                    c.setCond(new IRTemp(a));
                }
            }

        }

    }

    private String replace(IRExpr tar, IRExpr src, HashMap<String, String> newV) {

        if(src instanceof IRBinOp){

            IRBinOp b1 = (IRBinOp) src;

            String left = replace(null, b1.left(), newV);
            String right = replace(null, b1.right(),newV);

            if(left!=null){
                b1.setLeft(new IRTemp(left));
            }

            if(right!=null){
                b1.setRight(new IRTemp(right));
            }

            return null;

        }

        if(src instanceof IRConst){

            return null;

        }

        if(src instanceof IRTemp){

            IRTemp t1 = (IRTemp) src;

            // System.out.println("T1 NAME\t" + t1);
            // System.out.println("T2 NAME\t" + tar);


            if(newV.containsKey(t1.name())){

                String a = newV.get(t1.name());
                if(a==null)
                    throw new Error("WHY IS A NULL??");

                // if (tar instanceof IRTemp) {
                //     IRTemp tmp = (IRTemp) tar;
                //     // System.out.println(tmp.label() + " " + a);
                //     if(tmp.name().equals(a)){
                //         return;
                //     }
                // }
                // System.out.println("AAAAAAAA IISSSS THIIISSSS::::::" + a);

                // if(a.equals("_ARG0")){
                //     return null;
                // }

                return a;
                
            }

        }

        if(src instanceof IRMem){

            IRMem m1 = (IRMem) src;

            String m = replace(null, m1.expr(), newV);

            if(m != null){
                m1.setExpr(new IRTemp(m));
            }

            return null;

        }

        return null;

    }

    @Override
    public HashMap<String, String> meet(List<HashMap<String, String>> t) {
        
        Set<String> vars = new HashSet<>();

        for(HashMap<String,String> map: t){
            if(map.isEmpty()) continue;
            
            for(String x: map.keySet()){
                vars.add(x);
            }
        }

        HashMap<String, String> n =  new HashMap<>();

        if(t.size() == 0){
            return n;
        }

        for(String x: vars){

            HashMap<String, String> rec = t.get(0);

            String a = rec.get(x);

            if(a == null){
                continue;
            }

            boolean flag = false;

            for(HashMap<String,String> map: t){
                if(map.isEmpty()) {
                    flag = true;
                    break;
                }

                String b = map.get(x);    
                
                if(b == null){
                    flag = true;
                    break;
                }

                if(!b.equals(a))
                    continue; 

            }

            if(flag){
                continue;
            }

            n.put(x, a);

        }

        return n;
    }

    @Override
    public HashMap<String, String> transfer(IRNode n, HashMap<String, String> t) {

        // System.out.println("TRANFSERGINGINIGNIGNINGINGINGING");

        HashMap<String,String> rec = new HashMap<>();
        rec.putAll(t);

        if(n instanceof IRMove){
            IRMove m = (IRMove) n;

            if (m.target() instanceof IRTemp){

                IRTemp tmp = (IRTemp) m.target();

                if(m.source() instanceof IRTemp){

                    IRTemp tmp2 = (IRTemp) m.source();
                    String tempName = tmp2.name();
                    if(tempName.contains("_ARG")){

                            for(String x: t.keySet()){
                                if(x.equals(tmp.name()))
                                    rec.remove(x);
                                
                                if(t.containsKey(x)){
                                    if(t.get(x).equals(tmp.name()))
                                        rec.remove(x);
                                }   
                            
                        }
                        return rec;
                    }


                    for(String x: t.keySet()){

                        if(x.equals(tmp.name()))
                            rec.remove(x);

                        if(t.containsKey(x)){
                            if(t.get(x).equals(tmp.name()))
                                rec.remove(x);
                        }
                             
                    }

                    // System.out.println("THIS IS THE MOVE++++" +m);

                    if(!rec.containsKey(tmp2.name()))
                        rec.put(tmp.name(), tmp2.name());
                    else
                        rec.put(tmp.name(), rec.get(tmp2.name()));

                }
                else{

                    for(String x: t.keySet()){
                        if(x.equals(tmp.name()))
                            rec.remove(x);
                        
                        if(t.containsKey(x)){
                            if(t.get(x).equals(tmp.name()))
                                rec.remove(x);
                        }   
                    }

                }

                // System.out.print("CopyProp IR Move 1" + m);
                
                return rec;

            }
            else if(m.target() instanceof IRMem){
                // System.out.print("CopyProp IR Move 2" + m);
                return rec;
            }
            else{
                System.out.println();
                throw new Error("SHOULD NOT BE HERE" + n.label());
            }

        }
        else if(n instanceof IRCallStmt){
            IRCallStmt c = (IRCallStmt) n;

            for(String tmp: c.collectors()){

                for(String x: t.keySet()){
                    if(x.equals(tmp))
                        rec.remove(x);

                    if(t.containsKey(x)){
                        if(t.get(x).equals(tmp))
                            rec.remove(x);
                    }
                         
                }

            }

            return rec;
        }
        else
            return rec;
    }

    @Override
    public boolean hasSameElt(HashMap<String, String> t1, HashMap<String, String> t2) {

        if(t1.size() != t2.size())
            return false;

        for(String x: t1.keySet()){

            if(t1.get(x).equals(t2.get(x)))
                continue;

            return false;

        }

        return true;


    }

    @Override
    public void cond(List<IRNode> lst) {
        return;
    }
    
}