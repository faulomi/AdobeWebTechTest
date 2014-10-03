package fr.meuret.webtesttech.handlers;

import fr.meuret.webtesttech.nio.IODispatcher;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by Jérôme on 21/09/2014.
 */
public class Session {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Session.class);
    private final AsynchronousSocketChannel client;
    private final IODispatcher ioDispatcher;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
    private boolean keepAlive;
    private String remoteAddress;


    public Session(AsynchronousSocketChannel client, IODispatcher ioDispatcher) {
        this.client = client;
        this.ioDispatcher = ioDispatcher;
        try {
            this.remoteAddress = client.getRemoteAddress().toString();
            client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
            client.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
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
        ioDispatcher.asyncWrite(this, out, false);
    }

    public void writeAndFlush(ByteBuffer out) {
        ioDispatcher.asyncWrite(this, out, true);
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
}
