package org.obicere.indy.exec;

import org.obicere.indy.logging.Log;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class Obscurer {

    private static final Class<?>[] CLASSES = new Class[]{
            Object.class, Throwable.class, Exception.class, String.class, Serializable.class, Integer.class, Float.class,
            Boolean.class, Byte.class, Character.class, Double.class, Long.class, Short.class, Class.class, Cloneable.class,
            int[].class, byte[].class, short[].class, boolean[].class, float[].class, char[].class, long[].class, double[].class,
            //int.class, byte.class, short.class, boolean.class, float.class, char.class, long.class, double.class
    };

    private final Map<String, String> credentials = new HashMap<>();

    public Credential obscure(final String caller, final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        String methodName = generateName();
        Object[] args = generateArguments();

        boolean objectEnforcement = false;
        final String fixedOwner;
        if (owner.startsWith("[")) {
            fixedOwner = "java/lang/Object";
            objectEnforcement = true;
        } else {
            fixedOwner = owner;
        }

        final String methodType;
        if (opcode != Opcodes.INVOKESTATIC) {
            if (name.equals("<init>")) {
                final String formal = insertFormalArgument(fixedOwner, desc);
                methodType = formal.substring(0, formal.length() - 1) + "V";
                //methodType = desc.substring(0, desc.length() - 1) + "L" +  fixedOwner + ";";
            } else {
                methodType = insertFormalArgument(fixedOwner, desc);
            }
        } else {
            methodType = desc;
        }

        final Credential resolution = new Credential(caller, methodName, methodType, args);

        final StringBuilder info = new StringBuilder();

        info.append(opcode);
        info.append(Resolver.SPLIT);
        info.append(fixedOwner);
        info.append(Resolver.SPLIT);
        info.append(name);
        info.append(Resolver.SPLIT);
        info.append(desc);
        info.append(Resolver.SPLIT);
        info.append(objectEnforcement);
        final String fullInfo = info.toString();

        credentials.put(resolution.getIdentifier(), fullInfo);

        return resolution;
    }

    public void writeCredentials(final Path path) {
        if(credentials.isEmpty()) {
            return;
        }
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final PrintStream stream = new PrintStream(bytes);
            for (final Map.Entry<String, String> entry : credentials.entrySet()) {
                stream.print(entry.getKey());
                stream.print('=');
                stream.println(entry.getValue());
            }
            stream.flush();
            stream.close();

            final byte[] outputBytes = bytes.toByteArray();
            if (outputBytes.length > 0) {
                Files.write(path, outputBytes);
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    private String generateName() {
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

    private Object[] generateArguments() {
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

    private String insertFormalArgument(final String owner, final String descriptor) {
        final int start = descriptor.indexOf('(');
        final int end = descriptor.lastIndexOf(')');
        if (start < 0) {
            Log.debug("Failed to find start of parameters: %s", descriptor);
            return descriptor;
        }
        if (end < start) {
            Log.debug("Failed to find end of parameters: %s", descriptor);
            return descriptor;
        }
        final String properOwner;
        if (owner.startsWith("[")) {
            properOwner = owner;
        } else {
            properOwner = "L" + owner + ";";
        }

        final String parameters = descriptor.substring(start + 1, end);
        final String newParameters = properOwner + parameters;

        final String newDescriptor = "(" + newParameters + ")" + descriptor.substring(end + 1);

        //Log.debug("Added parameter to descriptor %s, %s: %s", owner, descriptor, newDescriptor);

        return newDescriptor;
    }


}
