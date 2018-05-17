package org.obicere.indy.task;

import org.obicere.indy.exec.NameInfo;
import org.obicere.indy.exec.Obscurer;
import org.obicere.indy.filter.ClassFilter;
import org.obicere.indy.filter.InstructionFilter;
import org.obicere.indy.filter.MethodFilter;
import org.obicere.indy.logging.Log;
import org.obicere.indy.logging.Statistics;
import org.obicere.indy.visitor.IndyClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 */
public class IndyTask implements Runnable {

    private final NameInfo info;

    private final Obscurer obscurer;

    private final Path input;
    private final Path output;

    private final ClassFilter[] classFilters;
    private final MethodFilter[] methodFilters;
    private final InstructionFilter[] instructionFilters;

    private final Statistics statistics;

    public IndyTask(final NameInfo info, final Obscurer obscurer, final Path input, final Path output, final ClassFilter[] classFilters, final MethodFilter[] methodFilters, final InstructionFilter[] instructionFilters, final Statistics statistics) {
        this.info = info;
        this.obscurer= obscurer;
        this.input = input.normalize();
        this.output = output.normalize().toAbsolutePath();
        this.classFilters = classFilters;
        this.methodFilters = methodFilters;
        this.instructionFilters = instructionFilters;
        this.statistics = statistics;
    }

    @Override
    public void run() {
        //Log.debug("Running indy on: %s", input);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            validateInput();
            validateOutput();

            //Log.debug("Validated input and output: %s, %s", input.toAbsolutePath(), output.toAbsolutePath());

            final int calls = statistics.getCallsVisited();

            inputStream = Files.newInputStream(input);
            final ClassReader reader = new ClassReader(inputStream);
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            final IndyClassVisitor visitor = new IndyClassVisitor(info, obscurer, classFilters, methodFilters, instructionFilters, Opcodes.ASM6, writer, statistics);

            reader.accept(visitor, 0);

            inputStream.close();

            final byte[] bytes = writer.toByteArray();

            Files.createDirectories(output.getParent());
            //Files.createFile(output);

            //Log.debug("Writing class file: %s, %d bytes", output.toAbsolutePath(), bytes.length);

            outputStream = Files.newOutputStream(output);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();

            final int newCalls = statistics.getCallsVisited();

            statistics.record(newCalls - calls, visitor.getName());


        } catch (final IOException e) {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (final IOException e1) {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    private void validateInput() throws IOException {
        if (!Files.exists(input)) {
            throw new IOException("File does not exist: " + input.toAbsolutePath());
        }
        if (!Files.isRegularFile(input) || !Files.isReadable(input)) {
            Log.debug("%s: isFile=%s, canRead=%s", input.toAbsolutePath(), Files.isRegularFile(input), Files.isReadable(input));
            throw new IOException("Cannot read from file: " + input.toAbsolutePath());
        }
    }

    private void validateOutput() throws IOException {
        if (Files.exists(output) && !Files.isWritable(output)) {
            Log.debug("%s: exists=%s, isFile=%s, canWrite=%s", output.toAbsolutePath(), Files.exists(output), Files.isRegularFile(output), Files.isWritable(output));
            throw new IOException("Cannot write to file: " + output.toAbsolutePath());
        }
    }
}
