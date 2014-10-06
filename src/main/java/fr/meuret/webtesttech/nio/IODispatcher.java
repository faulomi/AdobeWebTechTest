package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.handlers.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * An IO handler that dispatches asynchronous read and write operations.
 *
 * @author Jerome
 */
public class IODispatcher {


    private static final Logger logger = LoggerFactory.getLogger(IODispatcher.class);


    private final AsynchronousServerSocketChannel serverSocketChannel;

    private Handler protocolHandler;
    private CountDownLatch shutdownSignal = new CountDownLatch(1);


    public IODispatcher(AsynchronousServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;

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
                    final Session session = new Session(client, protocolHandler);
                    session.pendingRead();
                } catch (IOException e) {
                    logger.error("Error when getting the client remote address : ", e);
                }


            }

            @Override
            public void failed(Throwable exc, Object attachment) {

                logger.error("Error during accept phase: ", exc);
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





}
