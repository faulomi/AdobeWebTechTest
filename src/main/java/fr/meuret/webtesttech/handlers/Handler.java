package fr.meuret.webtesttech.handlers;

import fr.meuret.webtesttech.nio.Session;

/**
 * Created by meuj on 10/3/2014.
 */
public interface Handler {

    public void onMessage(Session session);
}
