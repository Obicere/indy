package org.obicere.indy;

import org.obicere.indy.exec.Resolver;
import org.obicere.indy.filter.*;
import org.obicere.indy.io.PathCollector;
import org.obicere.indy.io.PathJob;
import org.obicere.indy.io.PathOperation;
import org.obicere.indy.io.PathUtils;
import org.obicere.indy.logging.Log;
import org.obicere.indy.task.IndyTask;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 */
public class Main {

    private static final String VERSION = "v0.004b";

    public static void main(final String[] args) {
        boolean debug = false;
        boolean stack = false;
        boolean version = false;
        boolean help = false;
        int maxDepth = Integer.MAX_VALUE;
        boolean jars = true;
        String input = ".";
        String output = "./indyout/";

        final List<String> paths = new LinkedList<>();

        final ClassFilter[] classFilters  = new ClassFilter[] {
                new ResolverClassFilter(),
                new ResolverResolutionClassFilter()
        };

        final MethodFilter[] methodFilters = new MethodFilter[]{
                new ConstructorMethodFilter()
        };

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            switch (arg) {
                case "-d":
                    debug = true;
                    break;
                case "-s":
                    stack = true;
                    break;
                case "-h":
                case "?":
                case "help":
                case "--help":
                    help = true;
                    break;
                case "-v":
                    version = true;
                    break;
                case "-r":
                    try {
                        maxDepth = Integer.parseInt(args[++i]);
                    } catch (final NumberFormatException e) {
                        Log.error("Option -r must be followed by an integer. Received: " + args[i]);
                    }
                    break;
                case "-j":
                    jars = false;
                    break;
                case "-i":
                    input = args[++i];
                    break;
                case "-o":
                    output = args[++i];
                    break;
                default:
                    paths.add(arg);
            }
        }

        Log.setDebugPrinting(debug);
        Log.setStackPrinting(stack);

        Log.debug("Running indy ", VERSION);
        Log.debug("paths: ");
        for (final String path : paths) {
            Log.debug("    %s", path);
        }
        Log.debug("classFilters: ");
        for (final ClassFilter filter : classFilters) {
            Log.debug("    %s", filter.getClass().getSimpleName());
        }
        Log.debug("methodFilters: ");
        for (final MethodFilter filter : methodFilters) {
            Log.debug("    %s", filter.getClass().getSimpleName());
        }

        if (version) {
            Log.info(VERSION);
            return;
        }
        if (help) {
            printHelp();
            return;
        }

        if (paths.isEmpty()) {
            Log.info("Ran with no arguments, leaving.");
            printHelp();
            return;
        }

        // recursive depth must be non-negative
        if (maxDepth < 0) {
            Log.error("Recursive depth parameter (-r) must be non-negative.");
            return;
        }

        final Path outputDirectory = Paths.get(output);
        try {
            PathUtils.makeDirectory(outputDirectory);
        } catch (final IOException e) {
            Log.error("Failed to make directory: %s", e, outputDirectory);
        }
        try {
            PathUtils.clearDirectory(outputDirectory);
        } catch (final IOException e) {
            Log.error("Failed to clear directory: %s", e, outputDirectory);
        }


        for (final String path : paths) {
            final Path inputPath = Paths.get(input);
            final Path outputPath =Paths.get(output);

            final PathCollector collector = new PathCollector(inputPath, outputPath, path, maxDepth, jars);

            final PathJob[] collected = collector.collect();

            final Set<FileSystem> systems = new HashSet<>();

            for (final PathJob c : collected) {
                Log.debug("Path collected: %s, %s", path, c);

                final PathOperation op = c.getOperation();

                switch(op) {
                    case COPY:
                        copy(c);
                        break;
                    case PROCESS:
                        process(c, classFilters, methodFilters);
                        break;
                        default:
                            // shouldn't happen
                            throw new AssertionError("Reached new operator? Fix me.");
                }
                systems.add(c.getFrom().getFileSystem());
                systems.add(c.getTo().getFileSystem());
            }

            for (final FileSystem system : systems) {
                try {
                    system.close();
                } catch (final Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        Resolver.dumpInfo();
    }

    private static void copy(final PathJob job) {
        try {
            PathUtils.copy(job.getFrom(), job.getTo());
        } catch(final IOException e) {
            Log.error("Failed to copy file %s to %s", e, job.getFrom(), job.getTo());
        }
    }

    private static void process(final PathJob job, final ClassFilter[] classFilters, final MethodFilter[] methodFilters) {
        final Path input = job.getFrom();
        final Path output = job.getTo();

        final IndyTask task = new IndyTask(input, output, classFilters, methodFilters);
        try {

            task.run();

        } catch (final Throwable t) {
            Log.error("Error running task: %s", t, t.getMessage());
            copy(job);
        }
    }

    private static void printHelp() {
        Log.info("Help for indy %s", VERSION);
        Log.info("Usage: indy <options> <files>");
        Log.info("Where options include:");
        Log.info("    -d: enables debug printing");
        Log.info("    -s: enables stack printing when an error occurs");
        Log.info("    -h: prints help");
        Log.info("    -v: prints version information");
        //Log.info("    -r: set the recursive depth for any paths and jars");
        Log.info("    -j: disable processing on jars found");
        Log.info("    -i: specify the input folder. Defaults to \".\"");
        Log.info("    -o: specify the output folder. Defaults to \"./indyout\"");
    }
}
