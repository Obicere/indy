package org.obicere.indy.logging;

import java.io.PrintStream;

public class Log {

    private static boolean debug = false;

    private static boolean stack = false;

    public boolean isDebugPrinting() {
        return debug;
    }

    public static void setDebugPrinting(final boolean debug) {
        Log.debug = debug;
    }

    public boolean isStackPrinting() {
        return stack;
    }

    public static void setStackPrinting(final boolean stack) {
        Log.stack = stack;
    }

    public static void info(final Object message) {
        printTo(System.out, String.valueOf(message));
    }

    public static void info(final boolean message) {
        info(String.valueOf(message));
    }

    public static void info(final byte message) {
        info(String.valueOf(message));
    }

    public static void info(final char message) {
        info(String.valueOf(message));
    }

    public static void info(final short message) {
        info(String.valueOf(message));
    }

    public static void info(final int message) {
        info(String.valueOf(message));
    }

    public static void info(final float message) {
        info(String.valueOf(message));
    }

    public static void info(final long message) {
        info(String.valueOf(message));
    }

    public static void info(final double message) {
        info(String.valueOf(message));
    }

    public static void info(final String message, final Object... arguments) {
        printTo(System.out, message, arguments);
    }

    public static void debug(final String message, final Object... arguments) {
        if (debug) {
            printTo(System.out, message, arguments);
        }
    }

    public static void error(final String message, final Object... arguments) {
        printTo(System.err, message, arguments);
    }

    public static void error(final String message, final Throwable t, final Object... arguments) {
        printTo(System.err, message, arguments);
        if (stack) {
            t.printStackTrace(System.err);
        }
    }

    private static void printTo(final PrintStream stream, final String message, final Object... arguments) {
        final String info;
        if (message == null || arguments == null) {
            info = null;
        } else {
            info = String.format(message, arguments);
        }

        stream.println(info);
    }
}
