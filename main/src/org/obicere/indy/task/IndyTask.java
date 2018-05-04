package org.obicere.indy.task;

import org.obicere.indy.filter.MethodFilter;
import org.obicere.indy.logging.Log;
import org.obicere.indy.visitor.IndyClassVisitor;
import org.obicere.indy.visitor.IndyMethodVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 */
public class IndyTask implements Runnable {

    private final String path;

    private final MethodFilter[] filters;

    private final boolean debug;

    public IndyTask(final String path, final MethodFilter[] filters, final boolean debug) {
        this.path = path;
        this.filters = filters;
        this.debug = debug;
    }

    @Override
    public void run() {
        Log.debug("Running indy on: %s", path);

        final File file = resolveFile();

        Log.debug("Resolved file: %s", file.getAbsolutePath());

        try {
            final FileInputStream input = new FileInputStream(file);
            final ClassReader reader = new ClassReader(input);
            final ClassWriter writer = new ClassWriter(reader, 0);

            final ClassVisitor visitor = new IndyClassVisitor(filters, Opcodes.ASM6, writer);

            reader.accept(visitor, 0);

            input.close();

            final byte[] bytes = writer.toByteArray();

            final FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();


        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File resolveFile() {
        final File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile() || !file.canRead() || !file.canWrite()) {
            throw new RuntimeException("Cannot run indy on file: " + file.getAbsolutePath());
        }

        return file;
    }
}
