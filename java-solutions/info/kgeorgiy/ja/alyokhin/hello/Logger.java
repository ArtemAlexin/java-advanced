package info.kgeorgiy.ja.alyokhin.hello;

import java.net.DatagramSocket;

public interface Logger {
    void log(String message);
    void logErrorIfOpened(DatagramSocket socket, String message, Throwable throwable);
    void logError(String message, Throwable exception);
}
