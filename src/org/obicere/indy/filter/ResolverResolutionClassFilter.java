package org.obicere.indy.filter;

public class ResolverResolutionClassFilter implements ClassFilter {
    @Override
    public boolean accept(int version, int access, String name, String signature, String superName, String[] interfaces) {
        return !name.equals("org/obicere/indy/exec/Credential");
    }
}
