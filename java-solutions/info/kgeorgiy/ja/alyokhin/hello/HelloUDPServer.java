package info.kgeorgiy.ja.alyokhin.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class HelloUDPServer implements HelloServer {
    private static final Logger logger = ConsoleLogger.getInstance();

    private DatagramSocket datagramSocket;
    private int size;
    private ExecutorService executorServiceListen;
    private ExecutorService executorServiceSend;
    private BlockingQueue<Packet> waitingTasks;

    @Override
    public void start(int port, int threads) {
        try {
            datagramSocket = new DatagramSocket(port);
            size = datagramSocket.getReceiveBufferSize();
        } catch (SocketException e) {
            logger.logError("Failed to start HelloUDPServer", e);
        }
        waitingTasks = new ArrayBlockingQueue<>(threads);
        Stream.generate(() -> new PacketImpl(size)).limit(threads).forEach(waitingTasks::add);
        executorServiceSend = Executors.newFixedThreadPool(threads);
        executorServiceListen = Executors.newSingleThreadExecutor();
        executorServiceListen.submit(this::listen);
    }

    private void listen() {
        while (!datagramSocket.isClosed() && !Thread.interrupted()) {
            try {
                Packet packet = waitingTasks.take();
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

    private void send(Packet packet, String response) {
        try {
            String toSend = "Hello, " + response;
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
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Correct usage: HelloUDPServer port threads");
        }
        try (final HelloUDPServer server = new HelloUDPServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            reader.readLine();
        } catch (final IOException e) {
            logger.logError("Failed to read input", e);
        }
    }
}
