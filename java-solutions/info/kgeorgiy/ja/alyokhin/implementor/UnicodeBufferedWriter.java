package info.kgeorgiy.ja.alyokhin.implementor;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;

/**
 * Class that wraps {@link BufferedWriter} and writes to ir converted to Unicode {@link String}
 */
public class UnicodeBufferedWriter implements Closeable {
    /**
     * Wrapped instance of {@link BufferedWriter}.
     */
    private final BufferedWriter bufferedWriter;

    /**
     * Constructor for wrapped {@link BufferedWriter}
     *
     * @param bufferedWriter {@code BufferedWriter} instance to be wrapped.
     */
    public UnicodeBufferedWriter(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    /**
     * Converts given string to Unicode
     *
     * @param s {@code String} to convert
     * @return converted string
     */
    private static String toUnicode(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Writes given <var>str</var> to wrapped {@link BufferedWriter} in Unicode.
     * Uses {@link BufferedWriter#write(String)}
     *
     * @param str {@code String} to be written.
     * @throws IOException if {@link BufferedWriter#write(String)} throws any error.
     */
    public void write(String str) throws IOException {
        bufferedWriter.write(toUnicode(str));
    }

    /**
     * Implementation of {@link Closeable#close()}.
     * Invokes {@link BufferedWriter#close()}.
     *
     * @throws IOException if {@link BufferedWriter#close()} throws any error.
     */
    @Override
    public void close() throws IOException {
        bufferedWriter.close();
    }
}
