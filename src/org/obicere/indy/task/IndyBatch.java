package org.obicere.indy.task;

import org.obicere.indy.exec.NameInfo;
import org.obicere.indy.exec.Obscurer;
import org.obicere.indy.exec.Resolver;
import org.obicere.indy.filter.ClassFilter;
import org.obicere.indy.filter.InstructionFilter;
import org.obicere.indy.filter.MethodFilter;
import org.obicere.indy.io.PathJob;
import org.obicere.indy.io.PathOperation;
import org.obicere.indy.io.PathUtils;
import org.obicere.indy.logging.Log;
import org.obicere.indy.logging.Statistics;
import org.obicere.indy.util.NameGenerator;
import org.obicere.indy.visitor.IndyClassVisitor;
import org.obicere.indy.visitor.ResolverClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class IndyBatch {

    private final Set<String> names;

    private final Path dbPath;

    private final Obscurer obscurer;

    private final PathJob[] paths;

    private final ClassFilter[] classFilters;

    private final MethodFilter[] methodFilters;

    private final InstructionFilter[] instructionFilters;

    private final Statistics statistics;

    public IndyBatch(final Set<String> names, final Path dbPath, final PathJob[] paths, final ClassFilter[] classFilters, final MethodFilter[] methodFilters, final InstructionFilter[] instructionFilters, final Statistics statistics) {
        this.names = names;
        this.obscurer = new Obscurer();
        this.dbPath = dbPath;
        this.paths = paths;
        this.classFilters = classFilters;
        this.methodFilters = methodFilters;
        this.instructionFilters = instructionFilters;
        this.statistics = statistics;
    }

    public void run() {
        final NameInfo info = new NameInfo(names);

        for (final PathJob path : paths) {
            Log.debug("Path collected: %s", path);

            final PathOperation op = path.getOperation();

            switch (op) {
                case COPY:
                    copy(path);
                    break;
                case PROCESS:
                    process(info, path, classFilters, methodFilters, instructionFilters, statistics);
                    break;
                default:
                    // shouldn't happen
                    throw new AssertionError("Reached new operator? Fix me.");
            }
        }

        try {
            final Path outputPath = dbPath.resolve(info.getClassName() + ".class");

            final ClassReader reader = new ClassReader(Resolver.class.getName());
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            final ResolverClassVisitor visitor = new ResolverClassVisitor(info, Opcodes.ASM6, writer);

            reader.accept(visitor, 0);

            final byte[] bytes = writer.toByteArray();

            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, bytes);

        } catch (final IOException e) {
            e.printStackTrace();
        }
        final Path outputPath = dbPath.resolve(info.getFileName());
        Log.debug("Writing out db file to: %s", outputPath.toAbsolutePath());
        obscurer.writeCredentials(outputPath);
    }

    private void copy(final PathJob job) {
        try {
            PathUtils.copy(job.getFrom(), job.getTo());
        } catch (final IOException e) {
            Log.error("Failed to copy file %s to %s", e, job.getFrom(), job.getTo());
        }
    }

    private void process(final NameInfo info, final PathJob job, final ClassFilter[] classFilters, final MethodFilter[] methodFilters, final InstructionFilter[] instructionFilters, final Statistics statistics) {
        final Path input = job.getFrom();
        final Path output = job.getTo();

        final IndyTask task = new IndyTask(info, obscurer, input, output, classFilters, methodFilters, instructionFilters, statistics);
        try {

            task.run();

        } catch (final Throwable t) {
            Log.error("Error running task: %s", t, t.getMessage());
            copy(job);
        }
    }

}
