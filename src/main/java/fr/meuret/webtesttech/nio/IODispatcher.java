package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.handlers.Handler;
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
    private Handler protocolHandler;
    private CountDownLatch shutdownSignal = new CountDownLatch(1);


    public IODispatcher(AsynchronousServerSocketChannel serverSocketChannel, HttpConfiguration configuration) {
        this.serverSocketChannel = serverSocketChannel;
        this.configuration = configuration;
    }


    public void registerHandler(Handler protocolHandler) {

        this.protocolHandler = protocolHandler;

    }


    public void stop() {

        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            logger.error("Unable to close the server socket {}, e");
        } finally {

            shutdownSignal.countDown();
        }

    }


    public void start() {


        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                //Accept further connection
                serverSocketChannel.accept(null, this);
                try {
                    logger.debug("ACCEPT: {}", client.getRemoteAddress());
                    //Create new Session
                    final Session session = new Session(client, IODispatcher.this);
                    asyncRead(session);
                } catch (IOException e) {
                    logger.error("Error when getting the client remote address : {}", e);
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

    public void asyncRead(final Session session) {
        logger.debug("Thread {} => Trying to read buffer from {}", Thread.currentThread().toString(), session.getRemoteAddress());
        session.getReadBuffer().clear();
        session.getClient().read(session.getReadBuffer(), session, new CompletionHandler<Integer, Session>() {
            @Override
            public void completed(Integer bytesRead, Session session) {

                logger.debug("CompletionHandler : READ {} bytes : {}", bytesRead, session.getRemoteAddress());


                if (bytesRead < 0)
                    //Client closed the session

                    session.close();


                else {
                    session.getReadBuffer().flip();
                    protocolHandler.onMessage(session);

                }


            }

            @Override
            public void failed(Throwable exc, Session session) {


            }
        });

    }


    public void asyncWrite(Session session, ByteBuffer buffer, boolean flush) {
        logger.debug("Thread {}=> Trying to write buffer : {}, flush={} to {}", Thread.currentThread().toString(), buffer, flush, session.getRemoteAddress());
        session.getClient().write(buffer, session, new CompletionHandler<Integer, Session>() {
            @Override
            public void completed(Integer bytesWritten, Session session) {


                logger.debug("CompletionHandler : WRITE {} bytes : {}", bytesWritten, session.getRemoteAddress());


                if (buffer.hasRemaining()) {
                    session.getClient().write(buffer, session, this);
                } else if (flush) {

                    if (session.isKeepAlive())
                        asyncRead(session);
                    else session.close();
                }


            }

            @Override
            public void failed(Throwable exc, Session session) {

            }
        });

    }
}
