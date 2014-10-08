package fr.meuret.webtesttech.nio;

import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.Queue;


/**
 * A Write completion handler that is notified by the OS when a asynchronous write operation has been performed.
 */
public final class WriteCompletionHandler implements CompletionHandler<Integer, Session> {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WriteCompletionHandler.class);

    @Override
    public void completed(Integer bytesWritten, Session session) {


        ByteBuffer next;
        Queue<ByteBuffer> writeQueue = session.getWriteQueue();
        synchronized (writeQueue) {
            next = writeQueue.peek();
            if (!next.hasRemaining()) {
                writeQueue.remove();
                next = writeQueue.peek();

            }
        }
        if (next != null) {
            session.getClient().write(next, session, new WriteCompletionHandler());
        }


    }

    @Override
    public void failed(Throwable exc, Session session) {
        session.close();
    }
}
