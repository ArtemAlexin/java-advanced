package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class PacketImpl implements Packet {
    private final DatagramPacket datagramPacket;
    private final byte[] bytes;

    public PacketImpl(int len, SocketAddress socketAddress) {
        bytes = new byte[len];
        datagramPacket = new DatagramPacket(bytes, bytes.length);
        datagramPacket.setSocketAddress(socketAddress);
    }
    public PacketImpl(int len) {
        bytes = new byte[len];
        datagramPacket = new DatagramPacket(bytes, bytes.length);
    }
    @Override
    public void send(String message, DatagramSocket socket) throws IOException {
        byte[] toSend = message.getBytes(Utils.CHARSET);
        datagramPacket.setData(toSend);
        datagramPacket.setLength(toSend.length);
        socket.send(datagramPacket);
    }

    @Override
    public String receive(DatagramSocket socket) throws IOException {
        datagramPacket.setData(bytes);
        datagramPacket.setLength(bytes.length);
        socket.receive(datagramPacket);
        return new String(datagramPacket.getData(),
                datagramPacket.getOffset(),
                datagramPacket.getLength(),
                Utils.CHARSET);
    }
}
