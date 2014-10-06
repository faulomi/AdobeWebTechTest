package fr.meuret.webtesttech.nio;

import java.nio.ByteBuffer;

/**
 * Created by meuj on 10/6/2014.
 */
public class Message {

    private final ByteBuffer buffer;
    private final boolean flush;

    public Message(ByteBuffer buffer, boolean flush) {
        this.buffer = buffer;
        this.flush = flush;
    }
}
