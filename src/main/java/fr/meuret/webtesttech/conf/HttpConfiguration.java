package fr.meuret.webtesttech.conf;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The configuration for the http server.
 * <p>
 * Basically, there are three parameters :
 * <p>
 * <ol>
 * <li>port : defaulted to <i>8080</i></li>
 * <li>rootPath : path for the static files directory. Defaulted to the working directory path.</li>
 * <li>useSSL : if the http server runs on SSL. Defaulted to <i>false</i>. </li>
 * </ol>
 * <p>
 * This class is using the Builder Pattern described in the Effective Java book written by Joshua Bloch.
 *
 * @author Jerome
 */
public class HttpConfiguration {


    private static final int MIN_PORT_NUMBER = 1;
    private static final int MAX_PORT_NUMBER = 65535;

    public static class Builder {


        @Parameter(names = {"-port"}, description = "Http server listening port.", validateWith = PositiveInteger.class)
        private int port = 8080;
        @Parameter(names = {"-rootPath"}, description = "Static files root path.",
                   converter = fr.meuret.webtesttech.conf.PathConverter.class)
        private Path rootPath = Paths.get(System.getProperty("user.dir"));
        @Parameter(names = {"-useSSL"}, description = "SSL mode.")
        private Boolean useSSL = false;


        public Builder() {


        }

        @Override
        public int hashCode() {
            int result = port;
            result = 31 * result + (rootPath != null ? rootPath.hashCode() : 0);
            result = 31 * result + (useSSL != null ? useSSL.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Builder builder = (Builder) o;

            if (port != builder.port) {
                return false;
            }
            if (rootPath != null ? !rootPath.equals(builder.rootPath) : builder.rootPath != null) {
                return false;
            }
            return !(useSSL != null ? !useSSL.equals(builder.useSSL) : builder.useSSL != null);

        }

        @Override
        public String toString() {
            return "Builder{" + "port=" + port + ", rootPath=" + rootPath + ", useSSL=" + useSSL + '}';
        }

        public Builder port(int port) {
            this.port = port;

            return this;
        }

        public Builder rootPath(String rootPath) {
            if (rootPath != null) {
                this.rootPath = Paths.get(rootPath);
            }
            return this;

        }

        public Builder useSSL(boolean useSSL) {

            this.useSSL = useSSL;
            return this;
        }


        public HttpConfiguration build() {

            //Check rootPath
            if (Files.notExists(rootPath) || !Files.isDirectory(rootPath)) {
                throw new IllegalArgumentException("Invalid value for rootpath directive : " + rootPath);
            }
            try {
                rootPath = rootPath.toRealPath();
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid value for rootpath directive : " + rootPath, e);
            }
            if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
                throw new IllegalArgumentException(
                        "Invalid value for port number. Valid range is [" + MIN_PORT_NUMBER + ", " + MAX_PORT_NUMBER + "]");
            }

            return new HttpConfiguration(this);
        }

    }

    private final int port;
    private final Path rootPath;
    private final boolean useSSL;

    private HttpConfiguration(Builder builder) {

        this.port = builder.port;
        this.rootPath = builder.rootPath;
        this.useSSL = builder.useSSL;

    }

    public static HttpConfiguration defaultConfiguration() {

        return new Builder().build();
    }

    public boolean useSSL() {
        return useSSL;
    }

    public Path getRootPath() {
        return rootPath;
    }

    public int getPort() {
        return port;
    }


}
