package info.kgeorgiy.ja.alyokhin.hello;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Utils {
    private static final long TIMEOUT = 10; // :NOTE: time unit in the name

    private Utils() {
    }

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Finds first position where predicate does not met, starting from start pos or {@code s.length()} if there is no such position.
     *
     * @param s         string to search in
     * @param startPos  starting position
     * @param predicate predicate to test
     * @return first position
     */
    public static int dropWhile(String s, int startPos, Predicate<Character> predicate) {
        while (startPos < s.length() && predicate.test(s.charAt(startPos))) {
            startPos++;
        }
        return startPos;
    }

    /**
     * Check that {@code String} of all sign that matches {@code predicate}
     * starting from {@code startingPos} equals to {@code toCompare}
     *
     * @param s         {@code String} to compare
     * @param startPos  starting pos
     * @param predicate predicate to test
     * @param toCompare {@code String} to compare with
     * @return -1 if string are not equal, index of first sign that does
     * not match predicate or{@code s.length()}(if there is no such sign) if string are equal
     */
    public static int dropWhileAndTest(String s,
                                       int startPos,
                                       Predicate<Character> predicate,
                                       String toCompare) {
        int currentPos = 0;
        while (testPos(startPos, s, predicate) && currentPos < toCompare.length()) {
            if (s.charAt(startPos++) != toCompare.charAt(currentPos++)) {
                return -1;
            }
        }
        return currentPos == toCompare.length() && !testPos(startPos, s, predicate) ? startPos : -1;

    }

    private static boolean testPos(int pos, String s, Predicate<Character> predicate) {
        return pos < s.length() && predicate.test(s.charAt(pos));
    }

    /**
     * Correct shutdown of executor service
     * @param executorService executor Service to shut down
     */
    public static void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                // :NOTE: does not actually close resources
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
