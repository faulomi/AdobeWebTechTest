package fr.meuret.webtesttech.nio;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.nio.handlers.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A HTTP Server based on the NIO framework.
 *
 * @author Jerome
 */
public class HttpServer {


    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private final HttpConfiguration configuration;
    private final ConnectionListener connectionListener;
    private final CompletionHandler<AsynchronousSocketChannel, Object> acceptCompletionHandler = new CompletionHandler<AsynchronousSocketChannel, Object>() {
        @Override
        public void completed(AsynchronousSocketChannel client, Object attachment) {
            //Accept further connection
            serverSocketChannel.accept(null, this); try {
                logger.info("New client: {}", client.getRemoteAddress());
                //Create new Session
                final Session session = new Session(client); if (connectionListener != null) {
                    connectionListener.onConnect(session);
                } session.start();
            } catch (IOException e) {
                logger.error("Error when getting the client remote address : ", e);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            logger.error("Error during accept phase: ", exc); serverSocketChannel.accept(null, this);
        }
    };
    private AsynchronousServerSocketChannel serverSocketChannel;
    private AsynchronousChannelGroup channelGroup;

    public HttpServer(HttpConfiguration configuration, ConnectionListener connectionListener) {

        this.connectionListener = connectionListener; this.configuration = configuration;

    }

    public HttpServer(ConnectionListener connectionListener) {
        this(HttpConfiguration.defaultConfiguration(), connectionListener);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        //Initialize http server configuration
        HttpConfiguration.Builder configurationBuilder = new HttpConfiguration.Builder();
        //Initialize jCommander
        final JCommander jCommander = new JCommander(configurationBuilder); try {
            //Parse command line arguments and set values in the configuration
            jCommander.parse(args); HttpConfiguration configuration = configurationBuilder.build();
            final HttpServer httpServer = new HttpServer(configuration, (Session session) -> session.registerHandler(
                    new HttpProtocolHandler(configuration.getRootPath()))); httpServer.start();
        } catch (ParameterException e) {
            logger.error("Invalid configuration for the HTTP server.", e); jCommander.usage();
        }


    }

    private void bind(int port) throws IOException {

        channelGroup = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(),
                                                                     Runtime.getRuntime().availableProcessors());

        serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);

        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    public void start() throws IOException {

        bind(configuration.getPort()); pendingAccept(); try {
            channelGroup.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //Main thread is going to exit at this point so we can swallow the exception


        }


    }

    private void pendingAccept() throws IOException {
        if (serverSocketChannel == null) {
            throw new IllegalStateException("Server socket channel is null, please invoke start.");
        }


        serverSocketChannel.accept(null, acceptCompletionHandler);
    }

    public void stop() throws IOException {
        logger.info("Trying to shutdown the HTTP server..."); if (serverSocketChannel != null) {
            serverSocketChannel.close();
        }

        if (channelGroup != null) {
            channelGroup.shutdown();
        }


    }


    @FunctionalInterface
    public static interface ConnectionListener {

        public void onConnect(Session session);

    }
}
