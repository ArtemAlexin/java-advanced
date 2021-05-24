package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.DatagramSocket;

public interface Packet<T>{
    void send(T message, DatagramSocket socket) throws IOException;

    T receive(DatagramSocket socket) throws IOException;
}
