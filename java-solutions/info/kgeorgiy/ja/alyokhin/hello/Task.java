package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.function.Predicate;

public class Task {
    private static final Logger logger = ConsoleLogger.getInstance();
    private final Packet packet;
    private final DatagramSocket datagramSocket;
    private final Predicate<String> responseValidator;

    public Task(Packet packet, DatagramSocket datagramSocket, Predicate<String> responseValidator) {
        this.packet = packet;
        this.datagramSocket = datagramSocket;
        this.responseValidator = responseValidator;
    }

    private boolean sendPacket(String message) {
        try {
            packet.send(message, datagramSocket);
        } catch (final IOException e) {
            logger.logError("Failed to send request", e);
            return false;
        }
        return true;
    }

    private boolean receivePacket() {
        try {
            String response = packet.receive(datagramSocket);
            logger.log("Response: " + response);
            return responseValidator.test(response);
        } catch (final IOException e) {
            logger.logError("Failed to receive response", e);
            return false;
        }
    }

    public void completeTask(String message) {
        while (!datagramSocket.isClosed() && !Thread.interrupted()) {
            if (sendPacket(message) && receivePacket()) {
                break;
            }
        }
    }
}
