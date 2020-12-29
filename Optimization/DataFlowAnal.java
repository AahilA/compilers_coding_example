package zw494.Optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import zw494.CFG.ControlFlowGraphMaster;

public abstract class DataFlowAnal<N, E> {

    Map<N, E> in;
    Map<N, E> out;
    ControlFlowGraphMaster<N> cfg;

    Queue<N> w;

    public abstract E meet(List<E> t);

    // returns null if no need to transfer
    public abstract E transfer(N n, E t);

    public abstract boolean hasSameElt(E t1, E t2);

    public abstract void cond(List<N> lst);

    public void worklistAlgoBackward() {

        while (!w.isEmpty()) {

            N node = w.poll();

            List<E> ins = new ArrayList<E>();

            List<N> nodes = cfg.getChildren(node);
            if (nodes == null)
                continue;

            // System.out.println(node.toString().substring(0, node.toString().length() - 2));
            // System.out.println(nodes.toString());

            for (N n : nodes) {
                E in_n = in.get(n);
                if (in_n != null)
                    ins.add(in_n);
            }

            E n_out = meet(ins);

            out.put(node, n_out);

            E n_in = transfer(node, n_out);

            // System.out.println(out.get(node).toString());
            // System.out.println(n_in.toString());
            // System.out.println(in.get(node).toString());

            if (n_in != null) {

                if (!hasSameElt(n_in, in.get(node))) {

                    in.put(node, n_in);

                    List<N> lst = cfg.getParent(node);

                    if (lst != null)
                        w.addAll(lst);
                }

            }

            // System.out.println(w.toString());

        }

    }

    public void worklistAlgoForward(){

        while(!w.isEmpty()){

            N node = w.poll();

            List<E> ins = new ArrayList<E>();
                        
            List<N> preds = cfg.getParent(node);  
            
            // System.out.println(node.toString().substring(0, node.toString().length() - 2));
            // System.out.println(preds.toString());

            for(N n: preds){
                ins.add(out.get(n));
            }

            in.put(node, meet(ins));

            E outN = transfer(node, in.get(node));  
            
            // System.out.println(in.get(node).toString());
            // System.out.println(outN.toString());
            // System.out.println(out.get(node).toString());

            if(outN != null){

                if (!hasSameElt(outN, out.get(node))) {

                    out.put(node, outN);

                    List<N> lst = cfg.getChildren(node);

                    cond(lst);

                    if (lst != null)
                        w.addAll(lst);

                }

            }

            // System.out.println(w.toString());

        }

    }



}
