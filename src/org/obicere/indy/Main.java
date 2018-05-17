package org.obicere.indy;

import org.obicere.indy.filter.*;
import org.obicere.indy.io.PathCollector;
import org.obicere.indy.io.PathJob;
import org.obicere.indy.io.PathUtils;
import org.obicere.indy.logging.ErrorCode;
import org.obicere.indy.logging.Log;
import org.obicere.indy.logging.Statistics;
import org.obicere.indy.task.IndyBatch;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 */
public class Main {

    private static final String VERSION = "v0.005b";

    public static final int EXIT_SUCCESS = 0;

    public static final int EXIT_UNKNOWN = 1;

    public static final int EXIT_SYSTEM = 2;

    public static final int EXIT_COMMAND = 3;

    public static final int EXIT_INTERNAL = 4;

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

        @Deprecated final ClassFilter[] classFilters = new ClassFilter[]{
                //new ResolverClassFilter(),
                //new ResolverResolutionClassFilter()
        };

        @Deprecated final MethodFilter[] methodFilters = new MethodFilter[]{
                // new ConstructorMethodFilter()
        };

        final InstructionFilter[] instructionFilters = new InstructionFilter[]{
                new ConstructorInstructionFilter()
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

        final Statistics statistics = new Statistics();


        for (final String path : paths) {
            System.out.println(path);
            final Path inputPath = Paths.get(input);
            final Path outputPath = Paths.get(output);

            final PathCollector collector = new PathCollector(inputPath, outputPath, path, maxDepth, jars);

            final PathJob[] collected = collector.collect();

            final Set<String> names = new HashSet<>();

            final Set<FileSystem> systems = new HashSet<>();

            final Map<FileSystem, List<PathJob>> buckets = new HashMap<>();

            for (final PathJob job : collected) {
                systems.add(job.getFrom().getFileSystem());
                systems.add(job.getTo().getFileSystem());

                final FileSystem to = job.getTo().getFileSystem();
                List<PathJob> jobs = buckets.get(to);
                if (jobs == null) {
                    jobs = new ArrayList<>();
                    buckets.put(to, jobs);
                }
                jobs.add(job);
                names.add(job.getTo().getFileName().toString());
            }

            int batchCount  = 0;
            try {
                final int min = 5;
                final int max = 20;
                final Random random = new Random();

                for (final FileSystem system : buckets.keySet()) {
                    int start = 0;
                    final List<PathJob> jobs = buckets.get(system);
                    while (start < jobs.size()) {
                        final int limit = random.nextInt(max - min + 1) + min;
                        final int end = Math.min(start + limit, jobs.size());
                        final List<PathJob> sublist = jobs.subList(start, end);
                        final PathJob[] batchJobs = sublist.toArray(new PathJob[sublist.size()]);

                        Path dbPath;
                        if (system.provider().getScheme().equals("jar")) {
                            dbPath = system.getPath(".");
                        } else {
                            dbPath = outputPath;
                        }

                        final IndyBatch batch = new IndyBatch(names, dbPath, batchJobs, classFilters, methodFilters, instructionFilters, statistics);

                        Log.debug("Running batch on files: %s, %s", system, sublist.size());
                        try {
                            Log.info("Running batch %s", batchCount++);
                            batch.run();
                        } catch (final Throwable e) {
                            Log.error("Error running batch: ", e);
                        }

                        start += limit;
                    }
                }
            } catch(final Throwable e) {
                Log.error("Error running batch: %s", e, e.getMessage());
            }

            for (final FileSystem system : systems) {
                try {
                    system.close();
                } catch (final Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        printStatistics(statistics);
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
        Log.info("    -i path: specify the input folder. Defaults to \".\"");
        Log.info("    -o path: specify the output folder. Defaults to \"./indyout\"");
        Log.info("    -f flags: specify which features should be enabled or disabled. Flags are:");
        Log.info("        !: disables the following features");
        Log.info("        v: toggles invokevirtual replacement");
        Log.info("        s: toggles invokespecial replacement");
        Log.info("        i: toggles invokeinterface replacement");
        Log.info("        S: toggles invokestatic replacement");
        Log.info("        c: toggles constructor invokespecial replacement (currently disabled)");
        Log.info("        d: toggles invokedynamic replacement");
        Log.info("        g: toggles getfield replacement");
        Log.info("        G: toggles getstatic replacement");
        Log.info("        f: toggles setfield replacement");
        Log.info("        F: toggles setstatic replacement");
        Log.info("        l: toggles ldc and ldc_w replacement");
        Log.info("        a: toggles aaload replacement");
        Log.info("        A: toggles astore replacement");
        Log.info("        n: toggles anewarray replacement");
        Log.info("        L: toggles arraylength replacement");
    }

    private static void printStatistics(final Statistics statistics) {
        final long duration = statistics.getDuration();
        final int classesProcessed = statistics.getClassesProcessed();
        final int methodsVisited = statistics.getMethodsVisited();
        final int methodCallsVisited = statistics.getMethodCallsVisited();
        final int fieldCallsVisited = statistics.getFieldCallsVisited();
        final int prolificCalls = statistics.getProlificCalls();
        final String prolificClass = statistics.getProlificClass();

        final long seconds = duration / 1000;
        final long milliseconds = duration % 1000;

        Log.info("Execution completed in %s seconds and %s milliseconds", seconds, milliseconds);
        Log.info("Processed %s classes, %s methods", classesProcessed, methodsVisited);
        Log.info("Obscured %s method calls, %s field calls", methodCallsVisited, fieldCallsVisited);
        Log.info("Most prolific class was %s with %s calls", prolificClass, prolificCalls);
    }
}
