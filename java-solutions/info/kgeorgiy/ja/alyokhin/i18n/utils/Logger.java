package info.kgeorgiy.ja.alyokhin.i18n.utils;

import java.net.DatagramSocket;

public interface Logger {
    void log(String message);
    void logErrorIfOpened(DatagramSocket socket, String message, Throwable throwable);
    void logError(String message, Throwable exception);
    void logError(String message);
}
