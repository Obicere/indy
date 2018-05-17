package org.obicere.indy.exec;

import org.obicere.indy.util.NameGenerator;

import java.util.HashSet;
import java.util.Set;

public class NameInfo {
    private final NameGenerator generator = new NameGenerator();

    private final String className;

    private final String methodName;

    private final String fileName;

    public NameInfo(final Set<String> names) {
        this.className = createUniqueName(names, ".class");
        this.fileName = createUniqueName(names, "");

        final Set<String> methodNames = new HashSet<>();
        this.methodName = createUniqueName(methodNames, "");
    }

    private String createUniqueName(final Set<String> names, final String extension) {
        String name;
        do {
            name = generator.getNextName();
        } while (names.contains(name + extension));

        names.add(name + extension);

        return name;

    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFileName() {
        return fileName;
    }
}
