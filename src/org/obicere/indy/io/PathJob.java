package org.obicere.indy.io;

import java.nio.file.Path;

public class PathJob {

    private final PathOperation operation;

    private final Path from;

    private final Path to;

    public PathJob(final PathOperation operation, final Path from, final Path to) {
        this.operation = operation;
        this.from = from;
        this.to = to;
    }

    public PathOperation getOperation() {
        return operation;
    }

    public Path getFrom() {
        return from;
    }

    public Path getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "PathJob{" +
                "operation=" + operation +
                ", from=" + from.toAbsolutePath() +
                ", ffs=" + from.getFileSystem().getPath("/").toUri() +
                ", to=" + to.toAbsolutePath() +
                ", tfs=" + to.getFileSystem().getPath("/").toUri() +
                '}';
    }
}
