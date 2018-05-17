package org.obicere.indy.logging;

import java.io.PrintStream;

public class Log {

    private static final int ERROR_SOURCE_STACK_ELEMENT = 0;

    private static final char ERROR_CLASS_METHOD_SEPARATOR = '.';

    private static final char ERROR_METHOD_LINE_SEPARATOR = ':';

    private static final String WARNING_CODE = "Warning";
    private static final String ERROR_CODE = "Error  ";

    private static final String ERROR_FORMAT = "%s (%04x) %s at %s";
    private static final String SPECIFIC_ERROR_FORMAT = "%s (%04x) %s: %s at %s";

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

    @Deprecated
    public static void error(final String message, final Object... arguments) {
        printTo(System.err, message, arguments);
    }

    @Deprecated
    public static void error(final String message, final Throwable t, final Object... arguments) {
        printTo(System.err, message, arguments);
        if (stack) {
            t.printStackTrace(System.err);
        }
    }

    public static void error(final ErrorCode code, final Throwable t) {
        errorImpl(code, t, null);
    }

    public static void error(final ErrorCode code, final Throwable t, final String messageSpecific) {
        errorImpl(code, t, messageSpecific);
    }

    private static void errorImpl(final ErrorCode code, final Throwable t, final String messageSpecific) {
        final boolean warning = code.isWarning();
        final String errorType = warning ? WARNING_CODE : ERROR_CODE;
        final int errorCode = code.getErrorCode();
        final String descriptor = code.getDescriptor();
        final String stackEntry;

        final StackTraceElement[] elements = t.getStackTrace();
        final StackTraceElement element = elements[ERROR_SOURCE_STACK_ELEMENT];

        stackEntry = element.getClassName() + ERROR_CLASS_METHOD_SEPARATOR + element.getMethodName() + ERROR_METHOD_LINE_SEPARATOR + element.getLineNumber();

        if (messageSpecific == null) {
            printTo(System.err, ERROR_FORMAT, errorType, errorCode, descriptor, stackEntry);
        } else {
            printTo(System.err, SPECIFIC_ERROR_FORMAT, errorType, errorCode, descriptor, messageSpecific, stackEntry);
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
