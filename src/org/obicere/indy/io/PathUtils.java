package org.obicere.indy.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class PathUtils {

    private PathUtils() {
        throw new AssertionError();
    }

    public static void makeDirectory(final Path path) throws IOException {
        if(Files.exists(path)) {
            return;
        }
        Files.createDirectories(path);
    }

    public static void clearDirectory(final Path path) throws IOException  {
        if(!Files.exists(path)) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                if(dir == path) {
                    return FileVisitResult.CONTINUE;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copy(final Path from, final Path to) throws IOException {
        Files.createDirectories(to.getParent());
        //Files.createFile(to);
        Files.copy(from.toAbsolutePath(), to.toAbsolutePath());
    }

    public static String print(final Path path) {
        return path.getFileSystem().getPath("/").toUri().toString()  + "#" + path.toAbsolutePath();
    }

}
