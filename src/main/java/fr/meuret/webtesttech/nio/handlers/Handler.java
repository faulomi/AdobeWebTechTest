package fr.meuret.webtesttech.nio.handlers;

import fr.meuret.webtesttech.nio.Session;

/**
 * Handler is an interface that should be implemented by any class that want to be notified when
 * a message is read from a client socket channel.
 *
 * @author Jerome
 */
public interface Handler {


    /**
     * Gets called when a message is received.
     *
     * @param session the session object that handles the communication between the server and the client.
     */
    public void onMessage(Session session);
}
