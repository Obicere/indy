package org.obicere.indy.filter;

@Deprecated
public interface MethodFilter {

    public boolean accept(final int access, final String name, final String desc, final String signature, final String[] exceptions);

}
