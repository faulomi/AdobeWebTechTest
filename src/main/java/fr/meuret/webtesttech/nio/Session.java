package fr.meuret.webtesttech.nio;

import fr.meuret.webtesttech.nio.handlers.Handler;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Use <code>Session</code> to handle the socket connection between the server and a client.
 *
 * @author Jerome
 */
public class Session {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Session.class);

    /**
     * A read completion handler that is notified by the OS when a asynchronous read operation has been performed.
     *
     * @author Jerome
     */
    public static final class ReadCompletionHandler implements CompletionHandler<Integer, Session> {


        private org.slf4j.Logger logger = LoggerFactory.getLogger(ReadCompletionHandler.class);

        @Override
        public void completed(Integer bytesRead, Session session) {

            logger.debug("ReadCompletionHandler : READ {} bytes : {}", bytesRead, session.getRemoteAddress());

            if (bytesRead < 0)
            //Client closed the session
            {
                session.close();
            } else {
                session.getReadBuffer().flip(); if (session.getHandler() != null) {
                    session.getHandler().onMessage(session);
                }

                if (session.isKeepAlive()) {
                    session.pendingRead();
                }


            }

        }

        @Override
        public void failed(Throwable exc, Session session) {
            session.close();

        }
    }

    /**
     * A Write completion handler that is notified by the OS when a asynchronous write operation has been performed.
     *
     * @author Jerome
     */
    public static final class WriteCompletionHandler implements CompletionHandler<Integer, Session> {


        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WriteCompletionHandler.class);

        @Override
        public void completed(Integer bytesWritten, Session session) {

            logger.debug("WriteCompletionHandler : {} bytes written : {}", bytesWritten, session.getRemoteAddress());
            ByteBuffer next; Queue<ByteBuffer> writeQueue = session.getWriteQueue(); synchronized (writeQueue) {
                next = writeQueue.peek(); if (!next.hasRemaining()) {
                    writeQueue.remove(); next = writeQueue.peek();

                }
            } if (next != null) {
                session.pendingWrite(next);
            }


        }

        @Override
        public void failed(Throwable exc, Session session) {
            session.close();
        }
    }

    private final AsynchronousSocketChannel client;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
    //To guarantee a thread-safe writing process
    //(please refer to : https://webtide.com/on-jdk-7-asynchronous-io/ for more details)
    private final Queue<ByteBuffer> writeQueue = new LinkedList<>();
    private boolean keepAlive;
    private String remoteAddress;
    private Handler handler;

    public Session(AsynchronousSocketChannel client) {
        this.client = client;

    }

    public String getRemoteAddress() {

        String remoteAddress = ""; try {
            InetSocketAddress socketAddress = (InetSocketAddress) client.getRemoteAddress();
            remoteAddress = socketAddress.getHostName();
        } catch (IOException e) {
            logger.error("Unable to read client's remote address : ", e);

        } finally {
            return remoteAddress;
        }

    }

    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public void write(ByteBuffer out) {
        boolean needToWrite = false; synchronized (writeQueue) {

            needToWrite = writeQueue.isEmpty(); writeQueue.offer(out);
        }

        if (needToWrite) {
            pendingWrite(out);
        }
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
            if (client.isOpen())
                client.close();
        } catch (IOException e) {
            logger.error("Error when closing the client socket : ", e);
        }
    }

    private void pendingRead() {

        getReadBuffer().clear(); if (getClient().isOpen()) {
            getClient().read(getReadBuffer(), this, new ReadCompletionHandler());
        }

    }

    private void pendingWrite(ByteBuffer out) {
        if (getClient().isOpen()) {
            this.getClient().write(out, this, new WriteCompletionHandler());
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void registerHandler(Handler handler) {
        this.handler = handler;

    }

    public Queue<ByteBuffer> getWriteQueue() {
        return writeQueue;
    }

    public void start() {

        pendingRead();
    }
}
