package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class HelloUDPServer extends AbstractUPDServer {
    private static final Logger logger = ConsoleLogger.getInstance();

    private DatagramSocket datagramSocket;
    private BlockingQueue<Packet<String>> waitingTasks;

    @Override
    void startSocket(int port, int threads) {
        try {
            datagramSocket = new DatagramSocket(port);
            sz = datagramSocket.getReceiveBufferSize();
        } catch (SocketException e) {
            logger.logError("Failed to start HelloUDPServer", e);
        }
        waitingTasks = new ArrayBlockingQueue<>(threads);
        Stream.generate(() -> new PacketStringImpl(sz)).limit(threads).forEach(waitingTasks::add);
    }

    @Override
    protected void listen() {
        while (!datagramSocket.isClosed() && !Thread.interrupted()) {
            try {
                Packet<String> packet = waitingTasks.take();
                String response = packet.receive(datagramSocket);
                executorServiceSend.submit(() -> {
                    send(packet, response);
                    waitingTasks.add(packet);
                });
            } catch (InterruptedException e) {
                logger.logErrorIfOpened(datagramSocket, "Getting free packet failed", e);
            } catch (IOException e) {
                logger.logErrorIfOpened(datagramSocket, "Failed to receive request", e);
            }
        }
    }

    private void send(Packet<String> packet, String response) {
        try {
            String toSend = Utils.buildServerResponse(response);
            packet.send(toSend, datagramSocket);
        } catch (IOException e) {
            logger.logErrorIfOpened(datagramSocket, "Failed to send package", e);
        }
    }

    @Override
    public void close() {
        datagramSocket.close();
        Utils.shutdownAndAwaitTermination(executorServiceListen);
        Utils.shutdownAndAwaitTermination(executorServiceSend);
    }

    /**
     * Runs {@code HelloUDPServer} on given args.
     *
     * @param args port threads.
     */
    public static void main(final String[] args) {
        Utils.serverRun(args, HelloUDPServer.class);
    }
}
