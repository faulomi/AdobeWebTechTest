package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.handlers.Connection;
import fr.meuret.webtesttech.handlers.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Jérôme on 07/09/2014.
 */
public class IODispatcher {


    private static final Logger logger = LoggerFactory.getLogger(IODispatcher.class);

    private final ConcurrentLinkedQueue<ByteBuffer> byteBufferPool = new ConcurrentLinkedQueue<>();
    private final AsynchronousChannelGroup serverChannelGroup;
    private final AsynchronousServerSocketChannel serverSocketChannel;
    private final HttpConfiguration configuration;
    private HttpProtocolHandler protocolHandler;
    private CountDownLatch shutdownSignal = new CountDownLatch(1);

    public IODispatcher(AsynchronousServerSocketChannel serverSocketChannel, AsynchronousChannelGroup serverChannelGroup, HttpConfiguration configuration) {
        this.serverSocketChannel = serverSocketChannel;
        this.configuration = configuration;
        this.serverChannelGroup = serverChannelGroup;
    }

    private ByteBuffer getBuffer() {
        final ByteBuffer buffer = byteBufferPool.poll();

        if (buffer == null)
            return ByteBuffer.allocateDirect(8192);
        return buffer;

    }

    public void registerHandler(HttpProtocolHandler protocolHandler) {

        this.protocolHandler = protocolHandler;

    }


    public void stop() {
        shutdownSignal.countDown();
    }


    public void start() {


        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                //Accept further connection
                serverSocketChannel.accept(null, this);


                try {
                    logger.debug("New client accepted: {}", client.getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Create new Context
                final Connection context = new Connection(client, IODispatcher.this);
                read(context);

            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                serverSocketChannel.accept(null, this);
            }
        });

        try {
            shutdownSignal.await();
        } catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
        }


    }

    public void read(final Connection connection) {


        final ByteBuffer buffer = getBuffer();
        connection.getClient().read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer in) {
                try {
                    logger.debug("Datas received from the client : {}", connection.getClient().getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in.flip();
                protocolHandler.onMessage(connection, in);

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

            }
        });

    }


    public void write(Connection connection, ByteBuffer buffer) {

        connection.getClient().write(buffer, connection, new CompletionHandler<Integer, Connection>() {
            @Override
            public void completed(Integer bytesWritten, Connection connection) {
                try {
                    logger.debug("Some bytes have been sent to the client {}", connection.getClient().getRemoteAddress());
                    if (buffer.hasRemaining())
                        logger.debug("Mais ce n'est pas fini!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, Connection connection) {

            }
        });

    }
}
