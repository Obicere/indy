package org.obicere.indy.exec;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;

public class Credential {

    private final String caller;

    private final String methodName;

    private final String methodType;

    private final Object[] args;

    public Credential(String caller, String methodName, String methodType, Object[] args) {
        this.caller = caller;
        this.methodName = methodName;
        this.methodType = methodType;
        this.args = args;
    }

    public String getCaller() {
        return caller;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodType() {
        return methodType;
    }

    public Object[] getArgs() {
        return args;
    }

    public Credential(MethodHandles.Lookup caller, String methodName, MethodType methodType, Object[] args) {
        this.caller = caller.lookupClass().getName().replace(".", "/");
        this.methodName = methodName;
        this.methodType = methodType.toMethodDescriptorString();
        this.args = args;

    }

    public String getIdentifier() {
        final StringBuilder builder = new StringBuilder();

        builder.append(caller);
        builder.append(Resolver.SPLIT);
        builder.append(methodName);
        builder.append(Resolver.SPLIT);
        builder.append(methodType);

        for (final Object arg : args) {
            builder.append(Resolver.SPLIT);
            builder.append(arg instanceof Class ? getName((Class) arg) : arg);
        }
        return builder.toString();
    }

    private String getName(Class arg) {
        StringBuilder builder = new StringBuilder();
        boolean array = false;
        while (arg.isArray()) {
            builder.append('[');
            arg = arg.getComponentType();
            array = true;
        }
        switch (arg.getName()) {
            case "byte":
                builder.append(array ? 'B' : "byte");
                break;
            case "boolean":
                builder.append(array ? 'Z' : "boolean");
                break;
            case "char":
                builder.append(array ? 'C' : "char");
                break;
            case "double":
                builder.append(array ? 'D' : "double");
                break;
            case "float":
                builder.append(array ? 'F' : "float");
                break;
            case "int":
                builder.append(array ? 'I' : "int");
                break;
            case "long":
                builder.append(array ? 'J' : "long");
                break;
            case "short":
                builder.append(array ? 'S' : "short");
                break;
            default:
                builder.append(arg.getCanonicalName().replace('/', '.'));
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof Credential) {
            return ((Credential) other).getIdentifier().equals(getIdentifier());
        }
        return false;
    }
}
