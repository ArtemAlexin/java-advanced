package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class HelloUDPNonblockingServer extends AbstractUPDServer {
    private Selector selector;
    private DatagramChannel datagramChannel;
    private static final Logger logger = ConsoleLogger.getInstance();

    @Override
    void listen() {
        while (!selector.keys().isEmpty() && !Thread.interrupted()) {
            try {
                selector.select();
            } catch (IOException e) {
                logger.log("Selection socket failed");
                break;
            }
            for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                SelectionKey next = iterator.next();
                if (next.isWritable()) {
                    handleWrite(next);
                }
                if (next.isReadable()) {
                    handleRead(next);
                }
                iterator.remove();
            }
        }
    }

    @Override
    void startSocket(int port, int threads) {
        try {
            selector = Selector.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            datagramChannel = createBindChannel(inetSocketAddress);
            size = datagramChannel.socket().getReceiveBufferSize();
            datagramChannel.register(selector, SelectionKey.OP_READ, new BufferPacketContext(threads));
        } catch (IOException e) {
            logger.log("Failed to start UDP");
        }
    }

    private void handleRead(SelectionKey key) {
        BufferPacketContext context = (BufferPacketContext) key.attachment();
        ByteBuffer buffer = context.takeAndRemoveBuffer();

        try {
            SocketAddress destination = datagramChannel.receive(buffer);
            executorServiceSend.submit(() -> process(buffer, destination, context));
        } catch (IOException e) {
            logger.logError("IO exception occurred during read handling", e);
        }
    }

    private void handleWrite(SelectionKey key) {
        BufferPacketContext context = (BufferPacketContext) key.attachment();
        BufferPacket packet = context.getBufferPacket();
        try {
            datagramChannel.send(packet.getByteBuffer(), packet.getSocketAddress());
            context.add(packet.getByteBuffer().clear());
        } catch (IOException e) {
            logger.logError("IO exception occurred during write handling", e);
        }
    }

    private void process(ByteBuffer buffer, SocketAddress destination, BufferPacketContext context) {
        buffer.flip();
        String request = StandardCharsets.UTF_8.decode(buffer).toString();
        String response = Utils.buildServerResponse(request);
        buffer.clear();
        buffer.put(response.getBytes());
        buffer.flip();
        context.addBufferPacket(buffer, destination);
    }

    @Override
    public void close() {
        try {
            if (datagramChannel != null) {
                datagramChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException ignored) {
        } finally {
            Utils.shutdownAndAwaitTermination(executorServiceListen);
            Utils.shutdownAndAwaitTermination(executorServiceSend);
        }
    }

    class BufferPacketContext {
        private final List<ByteBuffer> availableBuffers;
        private final List<BufferPacket> bufferPackets;

        public BufferPacketContext(int threads) {
            availableBuffers = new ArrayList<>(threads);
            Stream.generate(() -> ByteBuffer.allocate(size))
                    .limit(threads).forEach(availableBuffers::add);
            bufferPackets = new ArrayList<>();
        }

        public synchronized void add(ByteBuffer buffer) {
            if (availableBuffers.isEmpty()) {
                datagramChannel.keyFor(selector).interestOpsOr(SelectionKey.OP_READ);
                selector.wakeup();
            }
            availableBuffers.add(buffer);
        }

        public synchronized ByteBuffer takeAndRemoveBuffer() {
            if (availableBuffers.size() == 1) {
                datagramChannel.keyFor(selector).interestOpsAnd(~SelectionKey.OP_READ);
                selector.wakeup();
            }
            return availableBuffers.remove(availableBuffers.size() - 1);
        }

        synchronized void addBufferPacket(ByteBuffer buffer, SocketAddress destination) {
            if (bufferPackets.isEmpty()) {
                datagramChannel.keyFor(selector).interestOpsOr(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
            bufferPackets.add(new BufferPacket(buffer, destination));
        }

        synchronized BufferPacket getBufferPacket() {
            if (bufferPackets.size() == 1) {
                datagramChannel.keyFor(selector).interestOpsAnd(~SelectionKey.OP_WRITE);
                selector.wakeup();
            }
            return bufferPackets.remove(bufferPackets.size() - 1);
        }
    }

    public static void main(String[] args) {
        Utils.serverRun(args, HelloUDPNonblockingServer.class);
    }
}
