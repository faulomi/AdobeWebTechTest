package fr.meuret.webtesttech.handlers;

import fr.meuret.webtesttech.http.HttpException;
import fr.meuret.webtesttech.http.HttpVersion;
import fr.meuret.webtesttech.http.request.HttpRequest;
import fr.meuret.webtesttech.http.response.HttpResponse;
import fr.meuret.webtesttech.http.response.HttpResponseHeader;
import fr.meuret.webtesttech.http.response.StatusCode;
import fr.meuret.webtesttech.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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


    public void onMessage(ConnectionContext connectionContext) {


        try {
            HttpRequest request = HttpRequest.from(connectionContext.getReadBuffer());

            buildResponse(request, connectionContext);
        } catch (HttpException e) {
            sendError(e.getStatusCode());
        }


    }

    private void sendError(StatusCode statusCode) {

        final HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_1);
        httpResponse.setHeader(HttpResponseHeader.CONNECTION, "close");


    }

    private void buildResponse(HttpRequest request, ConnectionContext connectionContext) {

        final HttpResponse response = new HttpResponse(request.getVersion());

        switch (request.getMethod()) {

            case GET:
                doGet(request, response, connectionContext);
                break;
            default:
                sendError(StatusCode.NOT_IMPLEMENTED);
        }


    }

    private void doGet(HttpRequest request, HttpResponse response, ConnectionContext connectionContext) {

        String requestPath = request.getRequestPath();
        String sanitizedRequestPath = HttpUtils.sanitizeRequestPath(request.getRequestPath());


        try {
            final Path realRequestPath = rootPath.resolve(sanitizedRequestPath).toRealPath();
            if (Files.notExists(realRequestPath) || Files.isHidden(realRequestPath)) {
                sendNotFound(connectionContext, response, requestPath);

            }

            if (Files.isDirectory(realRequestPath)) {

                //By redirecting to the directory path suffixed by "/"
                //we let the browser handle the path browsing and the parent->child relation
                if (requestPath.endsWith("/"))
                    sendListing(connectionContext, response, realRequestPath);
                else
                    sendRedirect(connectionContext, response, requestPath + "/");
                return;
            }

            if (Files.isRegularFile(realRequestPath)) {
                sendFile(connectionContext, response, realRequestPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendNotFound(ConnectionContext connectionContext, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.NOT_FOUND);
        connectionContext.write(response.toByteBuffer());

    }

    private void sendRedirect(ConnectionContext connectionContext, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.FOUND);
        response.setHeader(HttpResponseHeader.LOCATION, requestPath);
        connectionContext.write(response.toByteBuffer());


    }

    private void sendFile(ConnectionContext connectionContext, HttpResponse response, Path file) throws Exception {

        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, "application/octet-stream");
        response.setHeader(HttpResponseHeader.CONNECTION, "close");

        try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {


            MappedByteBuffer mappedFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            response.setHeader(HttpResponseHeader.CONTENT_LENGTH, String.valueOf(fileChannel.size()));


        }


    }


    private void sendListing(ConnectionContext connectionContext, HttpResponse response, Path realRequestPath) {
        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.setHeader(HttpResponseHeader.CONNECTION, "keep-alive");


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
            connectionContext.write(response.toByteBuffer());
        } catch (Exception e) {

        }


    }


}
