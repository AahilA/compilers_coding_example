package zw494.AST.XiTypes;

import java.util.List;

public class T extends XiType {

    public int tType;
    public List<Tao> tl;

    public T(int t, List<Tao> tl){
        this.tType = t;
        this.tl = tl;
    }
    
    @Override
    public String print() {
    	String s = tType+" ";
    	if(tl==null) {
    		return " " + s;
    	}
    	for(Tao t : tl) {
    		s+=" " + t.print();
    	}
    	return " "+s;
    }

}