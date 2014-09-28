package fr.meuret.webtesttech.handlers;

import fr.meuret.webtesttech.nio.IODispatcher;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by Jérôme on 21/09/2014.
 */
public class Connection {


    private final AsynchronousSocketChannel client;
    private final IODispatcher ioDispatcher;


    public Connection(AsynchronousSocketChannel client, IODispatcher ioDispatcher) {
        this.client = client;
        try {
            client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.ioDispatcher = ioDispatcher;
    }


    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public void write(ByteBuffer out) {
        ioDispatcher.write(this, out);

    }

}
