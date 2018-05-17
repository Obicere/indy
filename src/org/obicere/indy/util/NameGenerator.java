package org.obicere.indy.util;

import java.util.Arrays;
import java.util.Random;

public class NameGenerator {

    private static final String[] KEYWORDS = new String[]{
            "_", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "var", "void", "volatile", "while"
    };

    private static final char[] JAVA_PART = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '$', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static final char[] JAVA_START;

    static {
        // take out the digits, $, and _
        JAVA_START = Arrays.copyOfRange(JAVA_PART, 0, JAVA_PART.length - 12);
    }

    private final Random random;

    public NameGenerator() {
        this.random = createRandom();
    }

    protected Random createRandom() {
        return new Random();
    }

    public String getNextName() {
        final int min = 1;
        final int max = 15;

        final int size = randomInt(min, max);
        final char[] chars = new char[size];

        String str;
        int index;
        do {
            chars[0] = randomChar(JAVA_START);
            for (int i = 1; i < size; i++) {
                chars[i] = randomChar(JAVA_PART);
            }

            str = new String(chars);
            index = Arrays.binarySearch(KEYWORDS, str);
        } while (index >= 0);
        return str;
    }

    private char randomChar(final char[] chars) {
        final int min = 0;
        final int max = chars.length;
        return chars[randomInt(min, max)];
    }

    private int randomInt(final int min, final int max) {
        return random.nextInt(max - min) + min;
    }

}
