package zw494.Assembly;

/**
 * Data structure representing assembly instructions.
 */
public abstract class Instr {
	public Instr copy() {
		return this;
	}

	// the stack offset of memory operands if spilled right BEFORE the execution
	// of the current instruction
	public int stackOffset = 0;
}