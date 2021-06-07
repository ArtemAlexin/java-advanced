package info.kgeorgiy.ja.alyokhin.hello;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class BufferPacket {
    private final ByteBuffer byteBuffer;
    private final SocketAddress socketAddress;

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public BufferPacket(ByteBuffer byteBuffer, SocketAddress socketAddress) {
        this.byteBuffer = byteBuffer;
        this.socketAddress = socketAddress;
    }
}
