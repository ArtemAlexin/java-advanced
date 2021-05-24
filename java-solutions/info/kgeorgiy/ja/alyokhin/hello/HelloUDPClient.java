package info.kgeorgiy.ja.alyokhin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    private static final Logger logger = ConsoleLogger.getInstance();

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            ExecutorService executorService = Executors.newFixedThreadPool(threads);
            IntStream.range(0, threads).forEach(index -> executorService.submit(() ->
                    createTask(address, prefix, index, requests)));
            Utils.shutdownAndAwaitTermination(executorService);
        } catch (UnknownHostException e) {
            logger.logError("Failed to resolve host [" + host + "] ", e);
        }
    }

    private void createTask(SocketAddress address, String prefix, int index, int numberToSend) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(100);
            Packet<String> packet = new PacketStringImpl(datagramSocket.getReceiveBufferSize(), address);
            IntStream.range(0, numberToSend).forEach(requesNum -> {
                String request = (prefix + index + "_" + requesNum);
                logger.log("Request:" + request);
                Task task = new Task(packet, datagramSocket, response -> Utils.validate(response, index, requesNum));
                task.completeTask(request);
            });
        } catch (SocketException e) {
            logger.logError("Socket Failed in thread " + index, e);
        }
    }


    /**
     * Runs {@code HelloUDPClient}.
     *
     * @param args arguments.
     */
    public static void main(final String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        final int port = Integer.parseInt(args[1]);
        final int threads = Integer.parseInt(args[3]);
        final int requests = Integer.parseInt(args[4]);
        new HelloUDPClient().run(args[0], port, args[2], threads, requests);
    }
}