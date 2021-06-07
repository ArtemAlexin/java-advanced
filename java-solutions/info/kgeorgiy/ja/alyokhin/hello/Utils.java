package info.kgeorgiy.ja.alyokhin.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Utils {
    private static final long TIMEOUT_SECONDS = 10;
    public static final long SOCKET_TIMEOUT_MS = 200;

    private static final Logger logger = ConsoleLogger.getInstance();

    private Utils() {
    }

    public static String buildServerResponse(String message) {
        return "Hello, " + message;
    }

    public static String buildRequest(String prefix, int index, int requesNum) {
        return (prefix + index + "_" + requesNum);
    }

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static int testPart(String response, int start, int value) {
        int pos = Utils.dropWhile(response, start, Predicate.not(Character::isDigit));
        return Utils.dropWhileAndTest(response, pos, Character::isDigit, String.valueOf(value));
    }

    public static boolean validate(String response, int index, int requestNum) {
        int firstPartPos = testPart(response, 0, index);
        if (firstPartPos == -1 || (testPart(response, firstPartPos, requestNum) == -1)) {
            logger.log("error:" + response);
        }
        return (firstPartPos != -1) && (testPart(response, firstPartPos, requestNum) != -1);
    }

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
     *
     * @param executorService executor Service to shut down
     */
    public static void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    logger.logError("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void clientRun(Runner runner, String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        final int port = Integer.parseInt(args[1]);
        final int threads = Integer.parseInt(args[3]);
        final int requests = Integer.parseInt(args[4]);
        runner.run(args[0], port, args[2], threads, requests);
    }

    static void serverRun(final String[] args, final Class<? extends HelloServer> clazz) {
        if (args == null || args.length != 2) {
            logger.logError("Usage: HelloUDP[Nonblocking]Server port threads_count");
            return;
        }

        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            logger.logError("Null arguments are not allowed");
            return;
        }

        final int port, threads;
        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (final NumberFormatException e) {
            System.err.println("Failed to parse expected numeric argument: " + e.getMessage());
            return;
        }
        try (final HelloServer server = clazz.getDeclaredConstructor().newInstance()) {
            server.start(port, threads);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            logger.logError("Server has been started. Press any key to terminate");
            reader.readLine();
        } catch (final IOException e) {
            logger.logError("IO error occurred: " + e.getMessage());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            logger.logError("Reflection error occurred: " + e.getMessage());
        }
    }
}

@FunctionalInterface
interface Runner {
    void run(String arg1, int port, String args2, int threads, int requests);
}
