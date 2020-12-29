package zw494.AST;

import zw494.AST.XiTypes.*;

import java.util.HashMap;
import java.util.Map;

public class SymTable {

    Map<String, XiType> symTable;

    public SymTable() {

        symTable = new HashMap<String, XiType>();

    }

    /**
     * Lookup symbol table for matching id
     * 
     * @param id
     * @return
     */
    XiType lookup(String id) {

        return this.symTable.get(id);
    }

    /**
     * Add a pair of id and XiType to the symbol table
     * 
     * @param id
     * @param binding
     */
    void add(String id, XiType binding) {
        this.symTable.put(id, binding);
    }

    /**
     * Print current symbol table for debugging
     */
    void print() {
        // for(String s : symTable.keySet()) {
        // System.out.println(s+": " + symTable.get(s).print());

        // }
    }

}