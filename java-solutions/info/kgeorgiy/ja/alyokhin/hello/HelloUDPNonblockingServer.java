package info.kgeorgiy.ja.alyokhin.hello;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class HelloUDPNonblockingServer extends AbstractUPDServer {
    private static final Logger logger = ConsoleLogger.getInstance();

    private Selector select;
    private DatagramChannel datagramChannel;

    @Override
    void listen() {
        makeListening();
    }

    private void makeListening() {
        while (!select.keys().isEmpty() && !Thread.interrupted()) {
            try {
                select.select();
            } catch (IOException e) {
                logger.log("Socket Select failed");
                break;
            }
            iterateSelect();
        }
    }

    private void iterateSelect() {
        for (Iterator<SelectionKey> iterator = select.selectedKeys().iterator(); iterator.hasNext(); ) {
            SelectionKey cur = iterator.next();
            if (cur.isReadable()) {
                handleRead(cur);
            }
            if (cur.isWritable()) {
                handleWrite(cur);
            }
            iterator.remove();
        }
    }

    @Override
    void startSocket(int port, int threads) {
        try {
            select = Selector.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            datagramChannel = createBindChannel(inetSocketAddress);
            sz = datagramChannel.socket().getReceiveBufferSize();
            datagramChannel.register(select,
                    SelectionKey.OP_READ,
                    new BufferPacketContext(threads));
        } catch (IOException e) {
            logger.log("Failed to start UDP");
        }
    }

    private void handleRead(SelectionKey key) {
        BufferPacketContext context = (BufferPacketContext) key.attachment();
        ByteBuffer buffer = context.takeAndRemoveBuffer();
        submitNewTask(context, buffer);
    }

    private void submitNewTask(BufferPacketContext context, ByteBuffer buffer) {
        try {
            SocketAddress destination = datagramChannel.receive(buffer);
            executorServiceSend.submit(() -> runTask(buffer, destination, context));
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

    private void runTask(ByteBuffer buf, SocketAddress dst, BufferPacketContext context) {
        buf.flip();
        String req = StandardCharsets.UTF_8.decode(buf).toString();
        String res = Utils.buildServerResponse(req);
        Utils.processBufferWrite(buf, res);
        context.addBufferPacket(buf, dst);
    }

    @Override
    public void close() {
        Closeable[] objects = new Closeable[]{datagramChannel, select};
        try {
            Arrays.stream(objects).filter(Objects::nonNull).forEach(x -> {
                try {
                    x.close();
                } catch (IOException ignored) { }
            });
        }finally {
            Utils.shutdownAndAwaitTermination(executorServiceListen);
            Utils.shutdownAndAwaitTermination(executorServiceSend);
        }
    }

    class BufferPacketContext {
        private final List<ByteBuffer> availableBuffers;
        private final List<BufferPacket> bufferPackets;

        public BufferPacketContext(int threads) {
            availableBuffers = new ArrayList<>(threads);
            Stream.generate(() -> ByteBuffer.allocate(sz))
                    .limit(threads).forEach(availableBuffers::add);
            bufferPackets = new ArrayList<>();
        }

        public synchronized void add(ByteBuffer buffer) {
            if (availableBuffers.isEmpty()) {
                datagramChannel.keyFor(select).interestOpsOr(SelectionKey.OP_READ);
                select.wakeup();
            }
            availableBuffers.add(buffer);
        }

        public synchronized ByteBuffer takeAndRemoveBuffer() {
            if (availableBuffers.size() == 1) {
                datagramChannel.keyFor(select).interestOpsAnd(~SelectionKey.OP_READ);
                select.wakeup();
            }
            return availableBuffers.remove(availableBuffers.size() - 1);
        }

        synchronized void addBufferPacket(ByteBuffer buffer, SocketAddress destination) {
            if (bufferPackets.isEmpty()) {
                datagramChannel.keyFor(select).interestOpsOr(SelectionKey.OP_WRITE);
                select.wakeup();
            }
            bufferPackets.add(new BufferPacket(buffer, destination));
        }

        synchronized BufferPacket getBufferPacket() {
            if (bufferPackets.size() == 1) {
                datagramChannel.keyFor(select).interestOpsAnd(~SelectionKey.OP_WRITE);
                select.wakeup();
            }
            return bufferPackets.remove(bufferPackets.size() - 1);
        }
    }

    public static void main(String[] args) {
        Utils.serverRun(args, HelloUDPNonblockingServer.class);
    }
}
