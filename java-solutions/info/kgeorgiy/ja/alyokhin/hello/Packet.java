package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.DatagramSocket;

public interface Packet {
    void send(String message, DatagramSocket socket) throws IOException;

    String receive(DatagramSocket socket) throws IOException;
}
