package fr.meuret.webtesttech.handlers;

import fr.meuret.webtesttech.http.HttpException;
import fr.meuret.webtesttech.http.HttpMethod;
import fr.meuret.webtesttech.http.HttpVersion;
import fr.meuret.webtesttech.http.request.HttpRequest;
import fr.meuret.webtesttech.http.response.HttpResponse;
import fr.meuret.webtesttech.http.response.HttpResponseHeader;
import fr.meuret.webtesttech.http.response.StatusCode;
import fr.meuret.webtesttech.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

/**
 * A HTTP protocol handler that parses request and build responses.
 *
 * @author Jérôme
 */
public class HttpProtocolHandler {


    private static final Logger logger = LoggerFactory.getLogger(HttpProtocolHandler.class);
    private final Path rootPath;


    public HttpProtocolHandler(Path rootPath) {


        this.rootPath = rootPath;
    }


    public void onMessage(Connection connection, ByteBuffer buffer) {


        try {
            HttpRequest request = HttpRequest.from(buffer);
            buildResponse(request, connection);
        } catch (HttpException e) {
            sendError(e.getStatusCode());
        }


    }

    private void sendError(StatusCode statusCode) {

        final HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_1);
        httpResponse.setHeader(HttpResponseHeader.CONNECTION, "close");


    }

    private void buildResponse(HttpRequest request, Connection connection) {

        final HttpResponse response = new HttpResponse(request.getVersion());
        if (HttpMethod.GET.equals(request.getMethod())) {
            doGet(request, response, connection);
        } else {
            sendError(StatusCode.NOT_IMPLEMENTED);


        }


    }

    private void doGet(HttpRequest request, HttpResponse response, Connection connection) {

        String requestPath = request.getRequestPath();
        String sanitizedRequestPath = HttpUtils.sanitizeRequestPath(request.getRequestPath());


        try {
            final Path realRequestPath = rootPath.resolve(sanitizedRequestPath).toRealPath();
            if (Files.notExists(realRequestPath) || Files.isHidden(realRequestPath)) {
                sendNotFound(connection, response, requestPath);

            }

            if (Files.isDirectory(realRequestPath)) {

                //By redirecting to the directory path suffixed by "/"
                //we let the browser handle the path browsing and the parent->child relation
                if (requestPath.endsWith("/"))
                    sendListing(connection, response, realRequestPath);
                else
                    sendRedirect(connection, response, requestPath + "/");
                return;
            }

            if (Files.isRegularFile(realRequestPath)) {
                sendFile(connection, response, realRequestPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendNotFound(Connection connection, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.NOT_FOUND);
        connection.write(response.toByteBuffer());

    }

    private void sendRedirect(Connection connection, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.FOUND);
        response.setHeader(HttpResponseHeader.LOCATION, requestPath);
        connection.write(response.toByteBuffer());


    }

    private void sendFile(Connection connection, HttpResponse response, Path file) throws Exception {

        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, "application/octet-stream");
        response.setHeader(HttpResponseHeader.CONNECTION, "close");

        try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {


            MappedByteBuffer mappedFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            response.setHeader(HttpResponseHeader.CONTENT_LENGTH, String.valueOf(fileChannel.size()));


        }


    }


    private void sendListing(Connection connection, HttpResponse response, Path realRequestPath) {
        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.setHeader(HttpResponseHeader.CONNECTION, "close");


        final Path relativeFilePath = rootPath.relativize(realRequestPath);


        //Root filename (in case the root path corresponds to the root of the filesystem (i.e c:\\)
        String filename = "/";

        if (!relativeFilePath.getFileName().toString().isEmpty())
            filename = relativeFilePath.getFileName().toString();


        response.content().append("<!DOCTYPE html>\r\n")
                .append("<html><head><title>")
                .append("Listing of: ");


        response.content().append(filename);
        response.content().append("</title></head><body>\r\n");

        response.content().append("<h3>Listing of: ");
        response.content().append(filename);
        response.content().append("</h3>\r\n");

        response.content().append("<ul>");
        response.content().append("<li><a href=\"../\">..</a></li>\r\n");

        logger.info("Browsing the path : {}", realRequestPath);


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(realRequestPath, new DirectoryStream.Filter<Path>() {


            @Override
            public boolean accept(Path entry) throws IOException {
                return !Files.isHidden(entry) && Files.isReadable(entry) && HttpUtils.isAllowedFilename(entry.getFileName().toString());
            }
        })) {
            for (Path entry : stream) {
                logger.debug("Appending the path entry {} to the listing.", entry);
                response.content().append("<li><a href=\"");
                response.content().append(entry.getFileName().toString());
                response.content().append("\">");
                response.content().append(entry.getFileName());
                response.content().append("</a></li>\r\n");
            }
        } catch (IOException | DirectoryIteratorException e) {
            logger.error("Error occuring when browsing " + realRequestPath, e);

        }


        response.content().append("</ul></body></html>\r\n");
        response.setHeader(HttpResponseHeader.CONTENT_LENGTH, String.valueOf(response.content().length()));

        try {
            connection.write(response.toByteBuffer());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
