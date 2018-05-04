package org.obicere.indy;

import org.obicere.indy.exec.Resolver;
import org.obicere.indy.filter.ConstructorMethodFilter;
import org.obicere.indy.filter.MethodFilter;
import org.obicere.indy.logging.Log;
import org.obicere.indy.task.IndyTask;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class Main {

    private static final String VERSION = "v0.004b";

    public static void main(final String[] args) {
        boolean debug = false;
        boolean stack = false;
        boolean version = false;
        boolean help = false;

        final List<String> paths = new LinkedList<>();

        final MethodFilter[] filters = new MethodFilter[]{
                new ConstructorMethodFilter()
        };

        for (final String arg : args) {
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
        Log.debug("filters: ");
        for (final MethodFilter filter : filters) {
            Log.debug("    %s", filter.getClass().getSimpleName());
        }

        if (paths.isEmpty()) {
            Log.info("Ran with no arguments, leaving.");
            printHelp();
            return;
        }

        if (version) {
            Log.info(VERSION);
            return;
        }
        if (help) {
            printHelp();
            return;
        }

        for (final String path : paths) {
            final IndyTask task = new IndyTask(path, filters, debug);
            try {
                task.run();

            } catch (final Throwable t) {
                Log.error("Error running task: %s", t, t.getMessage());
                return;
            }
        }

        Resolver.dumpInfo();
    }

    private static void printHelp() {
        Log.info("Help for indy %s", VERSION);
        Log.info("Usage: indy <options> <files>");
        Log.info("Where options include:");
        Log.info("    -d: enables debug printing");
        Log.info("    -s: enables stack printing when an error occurs");
        Log.info("    -h: prints help");
        Log.info("    -v: prints version information");
    }
}
