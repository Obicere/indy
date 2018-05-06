package org.obicere.indy.filter;

public interface ClassFilter {

    public boolean accept(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces);

}
