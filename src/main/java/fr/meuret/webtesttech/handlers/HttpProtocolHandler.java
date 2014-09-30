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


    public void onMessage(Session session) {


        try {
            HttpRequest request = HttpRequest.from(session.getReadBuffer());

            buildResponse(request, session);
        } catch (HttpException e) {
            sendError(e.getStatusCode());
        }


    }

    private void sendError(StatusCode statusCode) {

        final HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_1);
        httpResponse.setHeader(HttpResponseHeader.CONNECTION, "close");


    }

    private void buildResponse(HttpRequest request, Session session) {

        final HttpResponse response = new HttpResponse(request.getVersion());

        switch (request.getMethod()) {

            case GET:
                doGet(request, response, session);
                break;
            default:
                sendError(StatusCode.NOT_IMPLEMENTED);
        }


    }

    private void doGet(HttpRequest request, HttpResponse response, Session session) {

        String requestPath = request.getRequestPath();
        String sanitizedRequestPath = HttpUtils.sanitizeRequestPath(request.getRequestPath());


        try {
            Path resolvedRequestPath = rootPath.resolve(sanitizedRequestPath);
            if (Files.notExists(resolvedRequestPath) || Files.isHidden(resolvedRequestPath)) {
                sendNotFound(session, response, requestPath);
                return;
            }
            //At this time, we know that real request path exists
            final Path realRequestPath = resolvedRequestPath.toRealPath();

            if (Files.isDirectory(realRequestPath)) {

                //By redirecting to the directory path suffixed by "/"
                //we let the browser handle the path browsing and the parent->child relation
                if (requestPath.endsWith("/")) {
                    sendListing(session, response, realRequestPath);
                    return;
                } else {
                    sendRedirect(session, response, requestPath + "/");
                    return;
                }

            }

            if (Files.isRegularFile(realRequestPath)) {
                sendFile(session, response, realRequestPath);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendNotFound(Session session, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.NOT_FOUND);
        session.write(response.toByteBuffer());

    }

    private void sendRedirect(Session session, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.FOUND);
        response.setHeader(HttpResponseHeader.LOCATION, requestPath);
        session.write(response.toByteBuffer());


    }

    private void sendFile(Session session, HttpResponse response, Path file) throws Exception {

        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, Files.probeContentType(file));
        response.setHeader(HttpResponseHeader.CONNECTION, "close");


        try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {


            long size = Math.min(Integer.MAX_VALUE, fileChannel.size());
            MappedByteBuffer mappedFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);


            response.setHeader(HttpResponseHeader.CONTENT_LENGTH, String.valueOf(fileChannel.size()));
            session.write(response.toByteBuffer());
        }


    }


    private void sendListing(Session session, HttpResponse response, Path realRequestPath) throws IOException {
        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.setHeader(HttpResponseHeader.CONNECTION, "keep-alive");

        //In case of disk mirroring, or just links, we need to know the real rootPath
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
            session.write(response.toByteBuffer());
        } catch (Exception e) {

        }


    }


}
