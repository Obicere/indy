package org.obicere.indy.filter;

/**
 */
public interface InstructionFilter {

    public boolean accept(final int opcode, final String owner, final String name, final String desc, final boolean itf);


}
