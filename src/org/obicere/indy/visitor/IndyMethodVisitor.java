package org.obicere.indy.visitor;

import org.obicere.indy.exec.Credential;
import org.obicere.indy.exec.NameInfo;
import org.obicere.indy.exec.Obscurer;
import org.obicere.indy.exec.Resolver;
import org.obicere.indy.filter.InstructionFilter;
import org.obicere.indy.logging.Log;
import org.obicere.indy.logging.Statistics;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 */
public class IndyMethodVisitor extends MethodVisitor {

    private final NameInfo info;

    private final Obscurer obscurer;

    private final InstructionFilter[] filters;

    private final String callingClass;

    private final String callingMethod;

    private final Statistics statistics;

    private boolean superConstructorVisited = false;

    public IndyMethodVisitor(final NameInfo info, final Obscurer obscurer, final InstructionFilter[] filters, final String callingClass, final String callingMethod, final int api, final MethodVisitor mv, final Statistics statistics) {
        super(api, mv);
        this.info = info;
        this.obscurer = obscurer;
        this.filters = filters;
        this.callingClass = callingClass;
        this.callingMethod = callingMethod;
        this.statistics = statistics;
    }


    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
        for (final InstructionFilter filter : filters) {
            if (!filter.accept(opcode, owner, name, desc, itf)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
        }
        if (name.equals("<init>")) {
            if(callingMethod.equals("<init>")) {
                if(!superConstructorVisited) {
                    superConstructorVisited = true;
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    return;
                }
            }
            statistics.constructorCallVisited();
        } else {
            switch (opcode) {
                case Opcodes.INVOKESTATIC:
                    statistics.invokeStaticVisited();
                    break;
                case Opcodes.INVOKEVIRTUAL:
                    statistics.invokeVirtualVisited();
                    break;
                case Opcodes.INVOKEINTERFACE:
                    statistics.invokeInterfaceVisited();
                    break;
                case Opcodes.INVOKESPECIAL:
                    statistics.invokeSpecialVisited();
                    break;
            }
        }

        final Credential resolution = obscurer.obscure(callingClass, opcode, owner, name, desc, itf);

        final MethodType type = MethodType.methodType(CallSite.class, new Class[]{MethodHandles.Lookup.class, String.class, MethodType.class, Object[].class});

        final Handle handle = new Handle(Opcodes.H_INVOKESTATIC, info.getClassName(), info.getMethodName(), type.toMethodDescriptorString(), false);

        final String methodType = resolution.getMethodType();

        final Object[] argTypes = resolution.getArgs();
        for (int i = 0; i < argTypes.length; i++) {
            Object obj = argTypes[i];
            if (obj instanceof Class) {
                argTypes[i] = Type.getType((Class) obj);
            }
        }

        Log.debug("Method type: %s", methodType);

        super.visitInvokeDynamicInsn(resolution.getMethodName(), methodType, handle, argTypes);
    }
}
