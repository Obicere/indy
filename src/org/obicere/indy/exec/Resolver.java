package org.obicere.indy.exec;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.obicere.indy.logging.Log;

import java.io.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VolatileCallSite;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class Resolver {

    private static final String SPLIT = "%";

    private static final Map<Resolution, CallSite> LINKAGES = new HashMap<>();

    private static final Map<String, String> INFO = new HashMap<>();

    private static boolean doubleDip = false;

    static {
        try {
            final InputStream input = Resolver.class.getClassLoader().getResourceAsStream("db.info");
            final BufferedReader scanner = new BufferedReader(new InputStreamReader(input));

            String line;
            while ((line = scanner.readLine()) != null) {
                String[] parts = line.split("=");
                String res = parts[0];
                String call = parts[1];

                INFO.put(res, call);
            }
            scanner.close();
            doubleDip = true;
        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static CallSite resolve(MethodHandles.Lookup callerClass, String dynMethodName, MethodType dynMethodType, Object... args) throws Throwable {
        Resolution resolution = new Resolution(callerClass, dynMethodName, dynMethodType, args);
        final CallSite existing = LINKAGES.get(resolution);
        if (existing != null) {
            return existing;
        }

        final int opcode;
        final String owner;
        final String name;
        final String desc;
        final boolean itf;

        final String info = INFO.get(resolution.getIdentifier());

        final String[] infoSplit = info.split("\\" + SPLIT);
        opcode = Integer.parseInt(infoSplit[0]);
        owner = infoSplit[1];
        name = infoSplit[2];
        desc = infoSplit[3];
        itf = Boolean.parseBoolean(infoSplit[4]);

        final String fixedName = owner.replace('/', '.').replace('$', '.');
        MethodType methodType = MethodType.fromMethodDescriptorString(desc, Resolver.class.getClassLoader());
        final Class<?> ownerClass = Class.forName(fixedName);

        MethodHandle mh;

        if (name.equals("<init>")) {
            mh = callerClass.findConstructor(ownerClass, methodType);
        } else {
            try {
                switch (opcode) {
                    case 0xb6:
                        mh = callerClass.findVirtual(ownerClass, name, methodType);
                        break;
                    case 0xb9:
                        mh = callerClass.findVirtual(ownerClass, name, methodType);
                        break;
                    case 0xb7:
                        mh = callerClass.findSpecial(ownerClass, name, methodType, ownerClass);
                        break;
                    case 0xb8:
                        mh = callerClass.findStatic(ownerClass, name, methodType);
                        break;
                    default:
                        throw new AssertionError("illegal instruction: " + opcode);
                }
            } catch (final Throwable e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            if (!dynMethodType.equals(mh.type())) {
                mh = mh.asType(dynMethodType);
            }

            final CallSite callSite = new VolatileCallSite(mh);
            LINKAGES.put(resolution, callSite);
            return callSite;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Resolution obscure(final String caller, final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        if (doubleDip) {
            INFO.clear();
            doubleDip = false;
        }

        String methodName = generateName();
        MethodType methodType = MethodType.fromMethodDescriptorString(desc, Resolver.class.getClassLoader());
        Object[] args = generateArguments();
        if (opcode != Opcodes.INVOKESTATIC) {
            List<Class<?>> params = new LinkedList<>(methodType.parameterList());
            try {
                params.add(0, Class.forName(owner.replaceAll("\\?|/", ".")));
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
            methodType = MethodType.methodType(methodType.returnType(), params);
        }


        final Resolution resolution = new Resolution(caller, methodName, methodType, args);

        final StringBuilder info = new StringBuilder();

        info.append(opcode);
        info.append(SPLIT);
        info.append(owner);
        info.append(SPLIT);
        info.append(name);
        info.append(SPLIT);
        info.append(desc);
        info.append(SPLIT);
        info.append(itf);
        final String fullInfo = info.toString();

        INFO.put(resolution.getIdentifier(), fullInfo);

        return resolution;
    }

    public static void dumpInfo() {
        try {
            final File file = new File("db.info");
            final PrintStream stream = new PrintStream(new FileOutputStream(file));
            for (final Map.Entry<String, String> entry : INFO.entrySet()) {
                stream.print(entry.getKey());
                stream.print('=');
                stream.println(entry.getValue());
            }
            stream.flush();
            stream.close();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    private static final Class<?>[] CLASSES = new Class[]{
            Object.class, Throwable.class, Exception.class, String.class, Serializable.class, Integer.class, Float.class, Boolean.class, Byte.class, Character.class, Double.class, Long.class, Short.class, Class.class, Cloneable.class//, int[].class, byte[].class, short[].class, boolean[].class, float[].class, char[].class, long[].class, double[].class
    };

    private static String generateName() {
        final SecureRandom random = new SecureRandom();
        final int num = 1 + random.nextInt(32);

        final char[] chars = new char[num];

        for (int i = 0; i < num; i++) {
            chars[i] = (char) ((random.nextBoolean() ? 'a' : 'A') + random.nextInt(26));
        }
        return new String(chars);
    }

    private static MethodType generateType() {
        final SecureRandom random = new SecureRandom();
        final int num = random.nextInt(10);

        final Class<?> rType = CLASSES[random.nextInt(CLASSES.length)];

        final Class<?>[] pTypes = new Class[num];

        for (int i = 0; i < num; i++) {
            pTypes[i] = CLASSES[random.nextInt(CLASSES.length)];
        }

        return MethodType.methodType(rType, pTypes);
    }

    private static Object[] generateArguments() {
        final SecureRandom random = new SecureRandom();
        final int num = 1 + random.nextInt(5);

        final Object[] args = new Object[num];

        for (int i = 0; i < num; i++) {
            switch (random.nextInt(5)) {
                case 0:
                    args[i] = random.nextInt();
                    break;
                case 1:
                    args[i] = random.nextFloat();
                    break;
                case 2:
                    args[i] = random.nextLong();
                    break;
                case 3:
                    args[i] = random.nextDouble();
                    break;
                case 4:
                    args[i] = CLASSES[random.nextInt(CLASSES.length)];
                    break;
            }
        }

        return args;
    }

    public static class Resolution {

        private final String caller;

        private final String methodName;

        private final MethodType methodType;

        private final Object[] args;

        public Resolution(String caller, String methodName, MethodType methodType, Object[] args) {
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

        public MethodType getMethodType() {
            return methodType;
        }

        public Object[] getArgs() {
            return args;
        }

        public Resolution(MethodHandles.Lookup caller, String methodName, MethodType methodType, Object[] args) {
            this.caller = caller.lookupClass().getName().replace('.', '/');
            this.methodName = methodName;
            this.methodType = methodType;
            this.args = args;

        }

        private String getIdentifier() {

            final StringBuilder builder = new StringBuilder();

            builder.append(caller);
            builder.append(SPLIT);
            builder.append(methodName);
            builder.append(SPLIT);
            builder.append(methodType.toMethodDescriptorString());

            for (final Object arg : args) {
                builder.append(SPLIT);
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
                    builder.append(arg.getCanonicalName().replace('/', '.').replace('$', '.'));
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
            if (other instanceof Resolution) {
                return ((Resolution) other).getIdentifier().equals(getIdentifier());
            }
            return false;
        }
    }
}
