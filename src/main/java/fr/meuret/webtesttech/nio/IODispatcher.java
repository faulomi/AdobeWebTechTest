package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.handlers.HttpProtocolHandler;
import fr.meuret.webtesttech.handlers.Session;
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
                    final Session session = new Session(client, IODispatcher.this);
                    read(session);
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

    public void read(final Session session) {


        session.getClient().read(session.getReadBuffer(), session, new CompletionHandler<Integer, Session>() {
            @Override
            public void completed(Integer bytesRead, Session session) {
                try {
                    logger.debug("Datas received from the client : {}", session.getClient().getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bytesRead < 0) {
                    //Client closed the session
                    try {
                        session.getClient().close();
                    } catch (IOException e) {
                        logger.error("Unable to close the client socket channel : {}", e);
                    }

                } else {
                    session.getReadBuffer().flip();
                    protocolHandler.onMessage(session);

                }


            }

            @Override
            public void failed(Throwable exc, Session session) {


            }
        });

    }


    public void write(Session session, ByteBuffer buffer) {

        session.getClient().write(buffer, session, new CompletionHandler<Integer, Session>() {
            @Override
            public void completed(Integer bytesWritten, Session session) {
                try {
                    logger.debug("Some bytes have been sent to the client {}", session.getClient().getRemoteAddress());
                    if (buffer.hasRemaining())
                        logger.debug("Mais ce n'est pas fini!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, Session session) {

            }
        });

    }
}
