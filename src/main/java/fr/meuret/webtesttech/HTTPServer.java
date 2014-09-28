package fr.meuret.webtesttech;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import fr.meuret.webtesttech.conf.HttpConfiguration;
import fr.meuret.webtesttech.handlers.HttpProtocolHandler;
import fr.meuret.webtesttech.nio.IODispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;

/**
 * Created by Jérôme on 24/08/2014.
 */
public class HttpServer {


    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private final HttpConfiguration configuration;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private AsynchronousChannelGroup serverChannelGroup;

    public HttpServer(HttpConfiguration configuration) {


        this.configuration = configuration;
    }

    public HttpServer() {
        configuration = HttpConfiguration.defaultConfiguration();
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

    public void bind(int port) throws IOException {

        serverChannelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));


    }

    public void start() throws IOException {
        bind(configuration.getPort());
        final IODispatcher dispatcher = new IODispatcher(serverSocketChannel, serverChannelGroup, configuration);
        final HttpProtocolHandler httpHandler = new HttpProtocolHandler(configuration.getRootPath());

        dispatcher.registerHandler(httpHandler);
        dispatcher.start();

    }


    public void stop() {


        try {
            if (serverSocketChannel.isOpen())
                serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}