package org.obicere.indy.logging;

import org.obicere.indy.Main;

import static org.obicere.indy.Main.EXIT_INTERNAL;
import static org.obicere.indy.Main.EXIT_SYSTEM;
import static org.obicere.indy.Main.EXIT_UNKNOWN;

public enum ErrorCode {

    UNKNOWN("Unknown error has occurred", 0, EXIT_UNKNOWN),

    // 1 - 100 is reserved for system errors

    OUT_OF_MEMORY("", 10, EXIT_SYSTEM),
    STACK_OVERLOW("Stack overflow error", 11, EXIT_SYSTEM),
    STACK_UNDERFLOW("", 12, EXIT_SYSTEM),


    // 101 - 1000 is reserved for internal errors

    NULL_REFERENCE("Reached an unexpected null reference", 101, EXIT_INTERNAL),
    ARRAY_INDEX("", 102, EXIT_INTERNAL),


    ;

    private final String descriptor;

    private final int code;

    private final int exit;

    private ErrorCode(final String descriptor, final int code, final int exit) {
        this.descriptor = descriptor;
        this.code = code;
        this.exit = exit;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isWarning() {
        return exit == Main.EXIT_SUCCESS;
    }

    public int getErrorCode() {
        return code;
    }

    public int getExitCode() {
        return exit;
    }

}
