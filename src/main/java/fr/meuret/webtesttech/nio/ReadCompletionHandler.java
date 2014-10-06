package fr.meuret.webtesttech.nio;

import org.slf4j.LoggerFactory;

import java.nio.channels.CompletionHandler;

/**
 * Created by meuj on 10/6/2014.
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, Session> {


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
        }

    }

    @Override
    public void failed(Throwable exc, Session session) {


    }
}
