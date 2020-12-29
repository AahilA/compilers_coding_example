package zw494.Assembly;

import java.util.ArrayList;

/**
 * Data structure that stores the result of register allocation
 */
public class Tile {
    public int cost; // the cost of using instrs to translate
    public ArrayList<Instr> instrs; // the instructions of translation
    public Arg reg; // the return registers of this set of instructions

    public Tile(int cost, ArrayList<Instr> instrs, Arg reg) {
        this.cost = cost;
        this.instrs = instrs;
        this.reg = reg;
    }

}