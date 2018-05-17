package org.obicere.indy.visitor;

import org.obicere.indy.exec.NameInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ResolverClassVisitor extends ClassVisitor {

    private final NameInfo info;

    public ResolverClassVisitor(final NameInfo info, final int api, final ClassVisitor cv) {
        super(api, cv);
        this.info = info;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, info.getClassName(), signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        return super.visitField(access, name, desc, signature, name.equals("FILE_NAME") ? info.getFileName() : value);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv;
        if(name.equals("resolve")) {
            mv = super.visitMethod(access, info.getMethodName(), desc, signature, exceptions);
        } else {
            mv = super.visitMethod(access, name, desc, signature, exceptions);
        }
        return new ResolverMethodVisitor(api, mv);
    }

    private class ResolverMethodVisitor extends MethodVisitor {

        private final String swap = "org/obicere/indy/exec/Resolver";

        private final String with = info.getClassName();

        public ResolverMethodVisitor(final int api, final MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
            super.visitTypeInsn(opcode, type.replace(swap, with));
        }

        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            super.visitFieldInsn(opcode, owner.replace(swap, with), name, desc);
        }

        @Override
        public void visitLdcInsn(final Object cst) {
            if(cst instanceof Type) {
                final Type type = (Type)cst;
                if(type.getInternalName().equals("org/obicere/indy/exec/Resolver")) {
                    super.visitLdcInsn(Type.getObjectType(info.getClassName()));
                    return;
                }
            } else if(cst instanceof String) {
                if("FILE_NAME".equals(cst)) {
                    super.visitLdcInsn(info.getFileName());
                    return;
                }
            }
            super.visitLdcInsn(cst);
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
            super.visitMethodInsn(opcode, owner.replace(swap, with), name, desc, itf);
        }


    }
}
