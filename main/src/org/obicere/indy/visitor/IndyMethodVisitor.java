package org.obicere.indy.visitor;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.obicere.indy.exec.Resolver;
import org.obicere.indy.filter.MethodFilter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 */
public class IndyMethodVisitor extends MethodVisitor {

    private final MethodFilter[] filters;

    private final String calling;

    public IndyMethodVisitor(final MethodFilter[] filters, final String calling, final int api) {
        super(api);
        this.filters = filters;
        this.calling = calling;
    }

    public IndyMethodVisitor(final MethodFilter[] filters, final String calling, final int api, final MethodVisitor mv) {
        super(api, mv);
        this.filters = filters;
        this.calling = calling;
    }


    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        for (final MethodFilter filter : filters) {
            if (!filter.accept(opcode, owner, name, desc, itf)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }

        final Resolver.Resolution resolution = Resolver.obscure(calling, opcode, owner, name, desc, itf);

        final MethodType type = MethodType.methodType(CallSite.class, new Class[]{MethodHandles.Lookup.class, String.class, MethodType.class, Object[].class});

        final Handle handle = new Handle(Opcodes.H_INVOKESTATIC, "org/obicere/indy/exec/Resolver", "resolve", type.toMethodDescriptorString(), false);

        MethodType methodType = resolution.getMethodType();
        
        /*
        if (opcode != Opcodes.ACC_STATIC) {
            List<Class<?>> params = new LinkedList<>(methodType.parameterList());
            try {
                params.add(0, Class.forName(owner.replaceAll("\\?|/", ".")));
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();

                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
            methodType = MethodType.methodType(methodType.returnType(), params);
        }   */


        final Object[] argTypes = resolution.getArgs();
        for (int i = 0; i < argTypes.length; i++) {
            Object obj = argTypes[i];
            if (obj instanceof Class) {
                argTypes[i] = Type.getType((Class) obj);
            }
        }

        super.visitInvokeDynamicInsn(resolution.getMethodName(), methodType.toMethodDescriptorString(), handle, argTypes);
    }
}
