package fr.meuret.webtesttech.nio;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.nio.handlers.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * A HTTP Server based on the NIO framework.
 *
 * @author Jerome
 */
public class HttpServer {


    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel client, Object attachment) {
            //Accept further connection
            serverSocketChannel.accept(null, this); try {
                logger.debug("ACCEPT: {}", client.getRemoteAddress());
                //Create new Session
                final Session session = new Session(client, protocolHandler); session.start();
            } catch (IOException e) {
                logger.error("Error when getting the client remote address : ", e);
            }


        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            logger.error("Error during accept phase: ", exc); serverSocketChannel.accept(null, this);
        }
    }

    private final HttpConfiguration configuration;
    private AsynchronousServerSocketChannel serverSocketChannel;


    public HttpServer(HttpConfiguration configuration) {


        this.configuration = configuration;

    }

    public HttpServer() {
        this(HttpConfiguration.defaultConfiguration());
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        //Initialize http server configuration
        HttpConfiguration.Builder configurationBuilder = new HttpConfiguration.Builder();
        //Initialize jCommander
        final JCommander jCommander = new JCommander(configurationBuilder);
        try {
            //Parse command line arguments and set values in the configuration
            jCommander.parse(args);
            final HttpServer httpServer = new HttpServer(configurationBuilder.build());
            httpServer.start();
        } catch (ParameterException e) {
            logger.error("Invalid configuration for the HTTP server.", e);
            jCommander.usage();
        }


    }

    private void bind(int port) throws IOException {
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    public void start() throws IOException {

        bind(configuration.getPort());
        final HttpProtocolHandler httpHandler = new HttpProtocolHandler(configuration.getRootPath()); pendingAccept();


    }


    private void pendingAccept() {
        if (serverSocketChannel == null) {
            throw new IllegalStateException("Server socket channel is null, please invoke start.");
        } serverSocketChannel.accept(null, new AcceptCompletionHandler());
    }


    public void stop() throws IOException {
        serverSocketChannel.close();
    }
}
