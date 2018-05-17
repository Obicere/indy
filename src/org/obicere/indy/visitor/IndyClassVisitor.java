package org.obicere.indy.visitor;

import org.obicere.indy.exec.NameInfo;
import org.obicere.indy.exec.Obscurer;
import org.obicere.indy.filter.ClassFilter;
import org.obicere.indy.filter.InstructionFilter;
import org.obicere.indy.filter.MethodFilter;
import org.obicere.indy.logging.Log;
import org.obicere.indy.logging.Statistics;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class IndyClassVisitor extends ClassVisitor {

    private final NameInfo info;

    private final Obscurer obscurer;

    private final ClassFilter[] classFilters;

    private final MethodFilter[] methodFilters;

    private final InstructionFilter[] filters;

    private final Statistics statistics;

    private boolean rejected = false;

    private String className;

    public IndyClassVisitor(final NameInfo info, final Obscurer obscurer, final ClassFilter[] classFilters, final MethodFilter[] methodFilters, final InstructionFilter[] filters, final int api, final ClassVisitor cv, final Statistics statistics) {
        super(api, cv);

        this.info = info;
        this.obscurer = obscurer;
        this.classFilters = classFilters;
        this.methodFilters = methodFilters;
        this.filters = filters;
        this.statistics = statistics;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        final int fixedVersion = Math.max(Opcodes.V1_7, version);
        super.visit(fixedVersion, access, name, signature, superName, interfaces);

        for (final ClassFilter filter : classFilters) {
            if (!filter.accept(version, access, name, signature, superName, interfaces)) {
                rejected = true;
                break;
            }
        }
        this.className = name;

        if (!rejected) {
            statistics.classProcessed();
        }
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (rejected) {
            return mv;
        }

        for (final MethodFilter filter : methodFilters) {
            if (!filter.accept(access, name, desc, signature, exceptions)) {
                return mv;
            }
        }
        statistics.methodVisited();
        //Log.debug("visited method: %d, %s, %s, %s, %s %n", access, name, desc, signature, Arrays.toString(exceptions));
        return new IndyMethodVisitor(info, obscurer, filters, className, name, Opcodes.ASM6, mv, statistics);

    }

    public String getName() {
        return className;
    }
}
