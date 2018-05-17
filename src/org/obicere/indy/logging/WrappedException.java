package org.obicere.indy.logging;

public class WrappedException extends RuntimeException {

    private final ErrorCode code;

    private final Throwable t;

    public WrappedException(final ErrorCode code, final Throwable t) {
        this.code = code;
        this.t = t;
    }

}
