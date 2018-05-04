package org.obicere.indy.visitor;

import org.obicere.indy.filter.MethodFilter;
import org.obicere.indy.logging.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class IndyClassVisitor extends ClassVisitor {
    private String className;

    private final MethodFilter[] filters;

    public IndyClassVisitor(final MethodFilter[] filters, final int api, final ClassVisitor cv) {
        super(api, cv);

        this.filters = filters;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        Log.debug("visited method: %d, %s, %s, %s, %s %n", access, name, desc, signature, Arrays.toString(exceptions));

        return new IndyMethodVisitor(filters, className, Opcodes.ASM6, mv);
    }
}
