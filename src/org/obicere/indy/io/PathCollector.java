package org.obicere.indy.io;

import org.obicere.indy.logging.Log;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class PathCollector {

    private static final String CLASS_EXT = ".class";
    private static final String JAR_EXT = ".jar";

    private final Set<PathJob> paths = new LinkedHashSet<>();

    private final Path input;
    private final Path output;

    private final PathMatcher matcher;

    private final int maxDepth;

    private final boolean acceptJars;

    public PathCollector(final Path input, final Path output, final String pattern, final int maxDepth, final boolean acceptJars) {
        if (maxDepth == 0 || maxDepth < -1) {
            throw new IllegalArgumentException("maxDepth must be -1 or positive: " + maxDepth);
        }
        this.input = input;
        this.output = output;
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        this.maxDepth = maxDepth;
        this.acceptJars = acceptJars;
    }

    public PathJob[] collect() {
        final PathCollectorVisitor visitor = new PathCollectorVisitor(output, input);

        try {
            Files.walkFileTree(input, Collections.emptySet(), maxDepth, visitor);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return paths.toArray(new PathJob[0]);
    }

    private String getExtension(final Path name) {
        final String text = name.toString();
        final int index = text.indexOf('.');
        if (index <= 0) {
            return null;
        } else {
            return text.substring(index);
        }
    }

    private void processClass(final Path output, final Path input, final Path path, final Path relp) {
        final Path name = path.getFileName();
        Path outputPath = output.resolve(relp);
        //final Path outputPath = Paths.get(output.toAbsolutePath().toString(), path.getFileName().toString());
        //Log.debug("%s, %s, %s, %s", PathUtils.print(output), PathUtils.print(path), PathUtils.print(outputPath), PathUtils.print(relp));

        if (matcher.matches(name)) {
            //Log.debug("Accepted file: %s", path.toAbsolutePath());

            paths.add(new PathJob(PathOperation.PROCESS, path, outputPath));
        } else {
            //Log.debug("Rejected file: %s", path.toAbsolutePath());

            paths.add(new PathJob(PathOperation.COPY, path, outputPath));
        }
    }

    private void processJar(final Path output, final Path input, final Path path, final Path relp) throws IOException {
        Path outputJar = output.resolve(relp);
        final URI newJarURI = URI.create("jar:" + outputJar.toUri());
        Log.debug("%s, %s, %s, %s", PathUtils.print(output), PathUtils.print(path), PathUtils.print(outputJar), newJarURI);

        final Map<String, String> environment = new HashMap<>();
        environment.put("create", "true");

        final FileSystem newJar = FileSystems.newFileSystem(newJarURI, environment);
        final Path newJarPath = newJar.getPath("/");
        final Path newPath = newJar.getPath(relp.toString());

        if (acceptJars) {

            //Log.debug("Accessing jar: %s", path.toAbsolutePath());

            final FileSystem jar = FileSystems.newFileSystem(path, getClass().getClassLoader());

            final Path jarPath = jar.getPath("/");

            final FileVisitor<Path> visitor = new PathCollectorVisitor(newJarPath, jarPath);
            Files.walkFileTree(jarPath, Collections.emptySet(), maxDepth, visitor);

            //Log.debug("Leaving jar: %s", path.toAbsolutePath());

            //jar.close();

            //paths.add(new PathJob(PathOperation.PROCESS, path, newPath));
        } else {
            paths.add(new PathJob(PathOperation.COPY, path, newPath));
        }
    }

    private void processOther(final Path output, final Path input, final Path path, final Path relp) {
        //Log.debug("%s, %s", output.getFileSystem().provider(), relp.getFileSystem().provider());
        Path newPath = output.resolve(relp);
        //Log.debug("%s, %s, %s", PathUtils.print(output), PathUtils.print(path), PathUtils.print(newPath));
        paths.add(new PathJob(PathOperation.COPY, path, newPath));
    }

    private class PathCollectorVisitor extends SimpleFileVisitor<Path> {

        private final Path output;

        private final Path input;

        PathCollectorVisitor(final Path output, final Path input) {
            this.output = output;
            this.input = input;
        }

        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) throws IOException {
            final Path relp = input.relativize(path);
            final Path name = path.getFileName();
            if (name == null) {
                return FileVisitResult.CONTINUE;
            }

            final String ext = getExtension(name);
            if (ext == null) {
                processOther(output, input, path, relp);
                return FileVisitResult.CONTINUE;
            }

            switch (ext) {
                case JAR_EXT:
                    processJar(output, input, path, relp);
                    break;
                case CLASS_EXT:
                    processClass(output, input, path, relp);
                    break;
                default:
                    processOther(output, input, path, relp);
                    break;

            }
            return FileVisitResult.CONTINUE;
        }

    }
}
