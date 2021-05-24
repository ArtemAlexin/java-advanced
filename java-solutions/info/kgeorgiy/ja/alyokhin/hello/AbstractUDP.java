package info.kgeorgiy.ja.alyokhin.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

public abstract class AbstractUDP {
    private DatagramChannel createNoConnectChannel(InetSocketAddress socketAddress) throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        return datagramChannel;
    }
    protected DatagramChannel createBindChannel(InetSocketAddress socketAddress) throws IOException {
        DatagramChannel datagramChannel = createNoConnectChannel(socketAddress);
        datagramChannel.bind(socketAddress);
        return datagramChannel;
    }

    protected DatagramChannel createConnectChannel(InetSocketAddress socketAddress) throws IOException {
        DatagramChannel datagramChannel = createNoConnectChannel(socketAddress);
        datagramChannel.connect(socketAddress);
        return datagramChannel;
    }
}
