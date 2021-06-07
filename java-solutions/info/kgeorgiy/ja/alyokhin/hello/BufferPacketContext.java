package info.kgeorgiy.ja.alyokhin.hello;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

class BufferPacketContext {
    private final List<ByteBuffer> availableBuffers;
    private final List<BufferPacket> bufferPackets;
    private final DatagramChannel datagramChannel;
    private final Selector select;
    private final int sz;

    private <T> void add(T obj, List<T> available, Runnable action) {
        if (available.isEmpty()) {
            action.run();
            select.wakeup();
        }
        available.add(obj);
    }

    private <T> T remove(List<T> available, Runnable action) {
        T obj = available.remove(available.size() - 1);
        if (available.isEmpty()) {
            action.run();
            select.wakeup();
        }
        return obj;
    }

    public BufferPacketContext(int threads, DatagramChannel datagramChannel, Selector select, int sz) {
        availableBuffers = new ArrayList<>(threads);
        this.datagramChannel = datagramChannel;
        this.select = select;
        this.sz = sz;
        Stream.generate(() -> ByteBuffer.allocate(this.sz))
                .limit(threads).forEach(availableBuffers::add);
        bufferPackets = new ArrayList<>();
    }

    public synchronized void add(ByteBuffer buffer) {
        add(buffer, availableBuffers, () -> {
            datagramChannel.keyFor(select).interestOpsOr(SelectionKey.OP_READ);
        });
    }

    public synchronized ByteBuffer takeAndRemoveBuffer() {
        return remove(availableBuffers, () -> {
            datagramChannel.keyFor(select).interestOpsAnd(~SelectionKey.OP_READ);
        });
    }

    synchronized void addBufferPacket(ByteBuffer buffer, SocketAddress destination) {
        BufferPacket packet = new BufferPacket(buffer, destination);
        add(packet, bufferPackets, () -> {
            datagramChannel.keyFor(select).interestOpsOr(SelectionKey.OP_WRITE);
        });
    }

    synchronized BufferPacket getBufferPacket() {
        return remove(bufferPackets, () -> {
            datagramChannel.keyFor(select).interestOpsAnd(~SelectionKey.OP_WRITE);
        });
    }
}
