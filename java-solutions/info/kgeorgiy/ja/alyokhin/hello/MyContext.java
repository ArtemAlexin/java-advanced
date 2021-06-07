package info.kgeorgiy.ja.alyokhin.hello;

import java.nio.ByteBuffer;

class MyContext {
    private final int id;
    private int request;
    private final ByteBuffer buf;

    MyContext(final int channelId, final ByteBuffer buffer) {
        this.id = channelId;
        this.request = 0;
        this.buf = buffer;
    }

    public int getId() {
        return id;
    }

    public int getRequest() {
        return request;
    }

    void incrementRequestId() {
        this.request++;
    }

    ByteBuffer getBuf() {
        return buf;
    }
}
