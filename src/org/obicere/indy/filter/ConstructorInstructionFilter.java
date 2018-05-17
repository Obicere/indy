package org.obicere.indy.filter;

/**
 */
public class ConstructorInstructionFilter implements InstructionFilter {
    @Override
    public boolean accept(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        return !name.equals("<init>");
    }
}
