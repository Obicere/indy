package org.obicere.indy.exec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class Resolver {

    private static final String FILE_NAME = "FILE_NAME";

    static final String SPLIT = "%";

    private static final Map<String, String> INFO = new HashMap<>();

    static {
        try {
            final InputStream input = Resolver.class.getResourceAsStream(FILE_NAME);
            if (input != null) {
                final BufferedReader scanner = new BufferedReader(new InputStreamReader(input));

                String line;
                while ((line = scanner.readLine()) != null) {
                    String[] parts = line.split("=");
                    String res = parts[0];
                    String call = parts[1];

                    INFO.put(res, call);
                }
                scanner.close();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static CallSite resolve(MethodHandles.Lookup callerClass, String dynMethodName, MethodType dynMethodType, Object... args) throws Throwable {
        final String resolution;

        final StringBuilder builder = new StringBuilder();

        builder.append(callerClass.toString().replace('.', '/'));
        builder.append(Resolver.SPLIT);
        builder.append(dynMethodName);
        builder.append(Resolver.SPLIT);
        builder.append(dynMethodType.toMethodDescriptorString());

        for (final Object arg : args) {
            builder.append(Resolver.SPLIT);
            if(arg instanceof Class) {
                Class a = (Class) arg;
                boolean array = false;
                while (a.isArray()) {
                    builder.append('[');
                    a = a.getComponentType();
                    array = true;
                }
                switch (a.getName()) {
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
                        builder.append(a.getCanonicalName());
                }
            } else {
                builder.append(arg);
            }
        }
        resolution = builder.toString();

        final int opcode;
        final String owner;
        final String name;
        final String desc;
        final boolean objectEnforcement;

        final String info = INFO.get(resolution);

        if (info == null) {
            throw new AssertionError("Resolution is incomplete. Failed to load resolution for: " + resolution);
        }
        final String[] infoSplit = info.split("\\" + SPLIT);
        opcode = Integer.parseInt(infoSplit[0]);
        owner = infoSplit[1];
        name = infoSplit[2];
        desc = infoSplit[3];
        objectEnforcement = Boolean.parseBoolean(infoSplit[4]);

        String fixedName = owner.replace('/', '.');//.replace('$', '.');
        MethodType methodType = MethodType.fromMethodDescriptorString(desc, Resolver.class.getClassLoader());
        final Class<?> ownerClass = Class.forName(fixedName);

        MethodHandle mh;

        if (name.equals("<init>")) {
            mh = callerClass.findConstructor(ownerClass, methodType);
        } else {
            try {
                switch (opcode) {
                    case 182: // INVOKEVIRTUAL
                        if (objectEnforcement) {
                            mh = MethodHandles.publicLookup().findVirtual(Object[].class, name, methodType);
                        } else {
                            mh = callerClass.findVirtual(ownerClass, name, methodType);
                        }
                        break;
                    case 185: // INVOKEINTERFACE
                        mh = callerClass.findVirtual(ownerClass, name, methodType);
                        break;
                    case 183: // INVOKESPECIAL
                        mh = callerClass.findSpecial(ownerClass, name, methodType, callerClass.lookupClass());
                        break;
                    case 184: // INVOKESTATIC
                        mh = callerClass.findStatic(ownerClass, name, methodType);
                        break;
                    default:
                        throw new AssertionError("illegal instruction: " + opcode);
                }
            } catch (final Throwable e) {
                e.printStackTrace();
                return null;
            }
            if (!dynMethodType.equals(mh.type())) {
                mh = mh.asType(dynMethodType);
            }
        }
        return new ConstantCallSite(mh);
    }
    /*
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
        while (arg.isArray()) {
            builder.append('[');
            arg = arg.getComponentType();
        }
        switch (arg.getName()) {
            case "int":
                builder.append('I');
                break;
            case "boolean":
                builder.append('Z');
                break;
            case "char":
                builder.append('C');
                break;
            case "double":
                builder.append('D');
                break;
            case "float":
                builder.append('F');
                break;
            case "long":
                builder.append('J');
                break;
            case "short":
                builder.append('S');
                break;
            case "byte":
                builder.append('B');
                break;
            default:
                builder.append(arg.getCanonicalName().replace('/', '.'));
        }
        return builder.toString();
    }*/
}
