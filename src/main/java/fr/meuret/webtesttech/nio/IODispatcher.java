package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.handlers.ConnectionContext;
import fr.meuret.webtesttech.handlers.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    private final AsynchronousServerSocketChannel serverSocketChannel;
    private final HttpConfiguration configuration;
    private HttpProtocolHandler protocolHandler;
    private CountDownLatch shutdownSignal = new CountDownLatch(1);


    public IODispatcher(AsynchronousServerSocketChannel serverSocketChannel, HttpConfiguration configuration) {
        this.serverSocketChannel = serverSocketChannel;
        this.configuration = configuration;
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
                    //Create new Context
                    final ConnectionContext connectionContext = new ConnectionContext(client, IODispatcher.this);
                    read(connectionContext);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                serverSocketChannel.accept(null, this);
            }
        });

        try {
            shutdownSignal.await();
            stop();
        } catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
        }


    }

    public void read(final ConnectionContext connectionContext) {


        connectionContext.getClient().read(connectionContext.getReadBuffer(), connectionContext, new CompletionHandler<Integer, ConnectionContext>() {
            @Override
            public void completed(Integer bytesRead, ConnectionContext connectionContext) {
                try {
                    logger.debug("Datas received from the client : {}", connectionContext.getClient().getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bytesRead < 0) {
                    //Client closed the connectionContext
                    try {
                        connectionContext.getClient().close();
                    } catch (IOException e) {
                        logger.error("Unable to close the client socket channel : {}", e);
                    }

                } else {
                    connectionContext.getReadBuffer().flip();
                    protocolHandler.onMessage(connectionContext);

                }


            }

            @Override
            public void failed(Throwable exc, ConnectionContext connectionContext) {


            }
        });

    }


    public void write(ConnectionContext connectionContext, ByteBuffer buffer) {

        connectionContext.getClient().write(buffer, connectionContext, new CompletionHandler<Integer, ConnectionContext>() {
            @Override
            public void completed(Integer bytesWritten, ConnectionContext connectionContext) {
                try {
                    logger.debug("Some bytes have been sent to the client {}", connectionContext.getClient().getRemoteAddress());
                    if (buffer.hasRemaining())
                        logger.debug("Mais ce n'est pas fini!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, ConnectionContext connectionContext) {

            }
        });

    }
}
