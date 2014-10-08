package fr.meuret.webtesttech.nio;

import org.slf4j.LoggerFactory;

import java.nio.channels.CompletionHandler;

/**
 * A read completion handler that is notified by the OS when a asynchronous read operation has been performed.
 */
public final class ReadCompletionHandler implements CompletionHandler<Integer, Session> {


    private org.slf4j.Logger logger = LoggerFactory.getLogger(ReadCompletionHandler.class);

    @Override
    public void completed(Integer bytesRead, Session session) {

        logger.debug("CompletionHandler : READ {} bytes : {}", bytesRead, session.getRemoteAddress());

        if (bytesRead < 0)
            //Client closed the session
            session.close();
        else {
            session.getReadBuffer().flip();
            if (session.getHandler() != null)
                session.getHandler().onMessage(session);

            if (session.isKeepAlive())
                session.pendingRead();


        }

    }

    @Override
    public void failed(Throwable exc, Session session) {
        session.close();

    }
}
