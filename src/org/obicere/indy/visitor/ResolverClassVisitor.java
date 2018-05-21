package org.obicere.indy.visitor;

import org.obicere.indy.exec.NameInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

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
        final Object constant = name.equals("FILE_NAME") ? info.getFileName() : value;
        final FieldVisitor fv = super.visitField(access, name, desc, signature, constant);
        return new ResolverFieldVisitor(api, fv);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final String newName = name.equals("resolve") ? info.getMethodName() : name;
        final MethodVisitor mv = super.visitMethod(access, newName, desc, signature, exceptions);
        return new ResolverMethodVisitor(api, mv);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        // super.visitOuterClass(owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        return null;
        // return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
        return null;
        //return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        //super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visitSource(final String source, final String debug) {
        //super.visitSource(source, debug);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        //super.visitAttribute(attr);
    }

    private class ResolverFieldVisitor extends FieldVisitor {

        public ResolverFieldVisitor(final int api, final FieldVisitor fv) {
            super(api, fv);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return null;
            //return super.visitAnnotation(desc, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
            return null;
            //return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public void visitAttribute(final Attribute attr) {
            //super.visitAttribute(attr);
        }
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

        @Override
        public void visitParameter(final String name, final int access) {
            // super.visitParameter(name, access);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return null;
            // return super.visitAnnotationDefault();
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return null;
            // return super.visitAnnotation(desc, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
            return null;
            // return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
            return null;
            // return super.visitParameterAnnotation(parameter, desc, visible);
        }

        @Override
        public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
            super.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
            return null;
            // return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
            return null;
            // return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
        }

        @Override
        public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
            // super.visitLocalVariable(name, desc, signature, start, end, index);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String desc, final boolean visible) {
            return null;
            // return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        }

        @Override
        public void visitLineNumber(final int line, final Label start) {
            // super.visitLineNumber(line, start);
        }

        @Override
        public void visitAttribute(final Attribute attr) {
            // super.visitAttribute(attr);
        }
    }
}
