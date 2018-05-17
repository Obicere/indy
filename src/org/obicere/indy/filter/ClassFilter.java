package org.obicere.indy.filter;

@Deprecated
public interface ClassFilter {

    public boolean accept(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces);

}
