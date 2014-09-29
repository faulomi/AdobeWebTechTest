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
public class ConnectionContext {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConnectionContext.class);
    private final AsynchronousSocketChannel client;
    private final IODispatcher ioDispatcher;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
    private String remoteAddress;


    public ConnectionContext(AsynchronousSocketChannel client, IODispatcher ioDispatcher) {
        this.client = client;
        this.ioDispatcher = ioDispatcher;
        try {
            this.remoteAddress = client.getRemoteAddress().toString();
            client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
        } catch (IOException e) {
            logger.error("Unable to set Option for client socket channel : {}", e);
        }

    }


    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public void write(ByteBuffer out) {
        ioDispatcher.write(this, out);
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }
}
