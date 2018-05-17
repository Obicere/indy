package org.obicere.indy.filter;

public class ConstructorMethodFilter implements MethodFilter {
    @Override
    public boolean accept(int access, String name, String desc, String signature, String[] exceptions) {
        return !name.equals("<init>");
    }
}
