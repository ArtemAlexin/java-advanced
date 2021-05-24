package info.kgeorgiy.ja.alyokhin.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractUPDServer extends AbstractUDP implements HelloServer {
    protected ExecutorService executorServiceListen;
    protected ExecutorService executorServiceSend;
    protected int size;

    abstract void listen();

    abstract void startSocket(int port, int threads);

    @Override
    public void start(int port, int threads) {
        startSocket(port, threads);
        initExecutors(threads);
        executorServiceListen.submit(this::listen);
    }
    protected void initExecutors(int threads) {
        executorServiceListen = Executors.newSingleThreadExecutor();
        executorServiceSend = Executors.newFixedThreadPool(threads);
    }
}
