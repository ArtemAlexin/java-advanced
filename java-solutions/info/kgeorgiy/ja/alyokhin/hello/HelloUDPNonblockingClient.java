package info.kgeorgiy.ja.alyokhin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.alyokhin.hello.Utils.*;

public class HelloUDPNonblockingClient extends AbstractUDP implements HelloClient {
    private static final Logger logger = ConsoleLogger.getInstance();

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        Selector selector;
        try {
            selector = Selector.open();
            IntStream.range(0, threads).forEach(x -> {
                try {
                    register(host, port, selector, x);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException | UncheckedIOException e) {
            logger.logError("Failed to init channels", e);
            return;
        }
        selectAction(prefix, requests, selector);
    }

    private void register(String host,
                          int port,
                          Selector selector,
                          int i) throws IOException {
        DatagramChannel channel = createConnectChannel(new InetSocketAddress(InetAddress.getByName(host), port));
        int sz = channel.socket().getReceiveBufferSize();
        channel.register(selector,
                SelectionKey.OP_WRITE,
                new MyContext(i, ByteBuffer.allocate(sz)));
    }
    private void selectAction(String prefix, int requests, Selector selector) {
        while (!Thread.interrupted() && !selector.keys().isEmpty()) {
            if (selectBreak(selector)) {
                break;
            }
            if (findKeys(prefix, selector)) {
                continue;
            }
            chooseSelect(prefix, requests, selector);
        }
    }
    private void chooseSelect(String prefix,
                              int requests,
                              Selector selector) {
        for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
            SelectionKey key = i.next();
            if (key.isWritable()) {
                processWriting(key, prefix);
            }
            if (key.isReadable()) {
                processReading(key, requests);
            }
            i.remove();
        }
    }
    private boolean findKeys(String text,
                             Selector selector) {
        if (!selector.selectedKeys().isEmpty()) {
            return false;
        }
        selector.keys().forEach(key -> {
            if (key.isWritable()) {
                processWriting(key, text);
            }
        });
        return true;
    }
    private boolean selectBreak(Selector selector) {
        try {
            selector.select(SOCKET_TIMEOUT_MS);
        } catch (IOException e) {
            logger.logError("Error during select");
            return true;
        }
        return false;
    }
    public static void main(String[] args) {
        Utils.clientRun(new HelloUDPNonblockingClient()::run, args);
    }
}
