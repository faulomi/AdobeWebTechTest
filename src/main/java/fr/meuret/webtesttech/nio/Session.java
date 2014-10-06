package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.handlers.Handler;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents a session between the server and the client.
 */
public class Session {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Session.class);
    private final AsynchronousSocketChannel client;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
    //To guarantee a thread-safe writing process
    //(please refer to : https://webtide.com/on-jdk-7-asynchronous-io/ for more details)
    private final Queue<ByteBuffer> writeQueue = new LinkedList<>();
    private boolean keepAlive;
    private String remoteAddress;
    private Handler handler;


    public Session(AsynchronousSocketChannel client, Handler protocolHandler) {
        this.client = client;
        this.handler = protocolHandler;

        try {
            this.remoteAddress = client.getRemoteAddress().toString();
            client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
            client.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
        } catch (IOException e) {
            logger.error("Unable to set Option for client socket channel : ", e);
        }

    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public void write(ByteBuffer out) {
        pendingWrite(out);

    }

    public void writeAndFlush(ByteBuffer out) {
        pendingWrite(out);
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }


    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("Error when closing the client socket : ", e);
        }
    }


    public void pendingRead() {
        logger.debug("{} Pending read : {} ", getRemoteAddress());
        getReadBuffer().clear();
        getClient().read(getReadBuffer(), this, new ReadCompletionHandler());

    }


    public void pendingWrite(ByteBuffer out) {
        logger.debug("{} Pending write : {} ", getRemoteAddress(), out);
        boolean needToWrite = false;
        synchronized (writeQueue) {

            needToWrite = writeQueue.isEmpty();
            writeQueue.offer(out);

        }

        if (needToWrite) {
            this.getClient().write(out, this, new WriteCompletionHandler());
            if (!keepAlive)
                pendingRead();
        }
    }


    public Handler getHandler() {
        return handler;
    }

    public Queue<ByteBuffer> getWriteQueue() {
        return writeQueue;
    }
}
