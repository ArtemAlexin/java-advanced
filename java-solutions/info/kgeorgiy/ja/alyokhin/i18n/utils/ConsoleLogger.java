package info.kgeorgiy.ja.alyokhin.i18n.utils;

import java.net.DatagramSocket;

public class ConsoleLogger implements Logger {
    private static final ConsoleLogger logger = new ConsoleLogger();

    public static ConsoleLogger getInstance() {
        return logger;
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void logErrorIfOpened(DatagramSocket socket, String message, Throwable throwable) {
        if (!socket.isClosed()) {
            logError(message, throwable);
        }
    }

    @Override
    public void logError(String message, Throwable throwable) {
        System.err.println(message + " " + throwable.getMessage());
    }

    @Override
    public void logError(String message) {
        System.err.println(message);
    }
}
