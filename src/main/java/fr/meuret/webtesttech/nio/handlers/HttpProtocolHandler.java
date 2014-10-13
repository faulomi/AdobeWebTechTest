package fr.meuret.webtesttech.nio.handlers;

import fr.meuret.webtesttech.http.HttpException;
import fr.meuret.webtesttech.http.request.HttpRequest;
import fr.meuret.webtesttech.http.response.HttpResponse;
import fr.meuret.webtesttech.http.response.HttpResponseHeader;
import fr.meuret.webtesttech.http.response.StatusCode;
import fr.meuret.webtesttech.nio.Session;
import fr.meuret.webtesttech.util.HttpUtils;
import fr.meuret.webtesttech.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;


/**
 * A HTTP protocol handler that parses request and build responses.
 *
 * @author Jerome
 * @see fr.meuret.webtesttech.nio.handlers.Handler
 */
public class HttpProtocolHandler implements Handler {


    private static final Logger logger = LoggerFactory.getLogger(HttpProtocolHandler.class);
    private final Path rootPath;


    public HttpProtocolHandler(Path rootPath) {
        this.rootPath = rootPath;
    }


    @Override
    public void onMessage(Session session) {

        try {
            HttpRequest request = HttpRequest.from(session.getReadBuffer()); buildResponse(request, session);
        } catch (HttpException e) {
            sendError(e.getStatusCode(), session);
        }


    }


    private void sendError(StatusCode statusCode, Session session) {

        try {
            session.write(HttpResponse.error(statusCode).toByteBuffer());
        } catch (Exception e) {
            logger.error("Error ");
        }


    }

    private void buildResponse(HttpRequest request, Session session) {

        final HttpResponse response = new HttpResponse(request.getVersion());
        session.setKeepAlive(request.isKeepAlive()); if (request.isKeepAlive()) {
            response.setHeader(HttpResponseHeader.CONNECTION, "keep-alive");
        } switch (request.getMethod()) {

            case GET: doGet(request, response, session); break; default: sendError(StatusCode.NOT_IMPLEMENTED, session);
        }


    }


    private void doGet(HttpRequest request, HttpResponse response, Session session) {

        String requestPath = request.getRequestPath();
        String sanitizedRequestPath = HttpUtils.sanitizeRequestPath(request.getRequestPath());


        try {
            Path resolvedRequestPath = rootPath.resolve(sanitizedRequestPath);
            if (Files.notExists(resolvedRequestPath) || Files.isHidden(resolvedRequestPath)) {
                sendNotFound(session, response, requestPath); return;
            }
            //At this time, we know that real request path exists
            final Path realRequestPath = resolvedRequestPath.toRealPath();

            if (Files.isDirectory(realRequestPath)) {

                //By redirecting to the directory path suffixed by "/"
                //we let the browser handle the path browsing and the parent->child relation
                if (requestPath.endsWith("/")) {
                    sendListing(session, response, realRequestPath); return;
                } else {
                    sendRedirect(session, response, requestPath + "/"); return;
                }

            }

            if (Files.isRegularFile(realRequestPath)) {
                sendFile(session, response, realRequestPath); return;
            }
        } catch (Exception e) {
            sendError(StatusCode.INTERNAL_SERVER_ERROR, session);
        }


    }

    private void sendNotFound(Session session, HttpResponse response, String requestPath) throws Exception {


        response.setStatusCode(StatusCode.NOT_FOUND);

        response.content()
                .append("<!DOCTYPE HTML>")
                .append("<html><head><title>Not Found</title></head><body>")
                .append(StringUtils.CRLF)
                .append("<h1>Not Found!!!</h1>")
                .append(StringUtils.CRLF)
                .append("</body></html>");

        response.setHeader(HttpResponseHeader.CONTENT_LENGTH, Integer.toString(response.content().length()));
        session.write(response.toByteBuffer());

    }

    private void sendRedirect(Session session, HttpResponse response, String requestPath) throws Exception {

        response.setStatusCode(StatusCode.FOUND); response.setHeader(HttpResponseHeader.LOCATION, requestPath);
        //Firefox doesn't handle redirection if no content-length header (#see https://bugzilla.mozilla.org/show_bug.cgi?id=347149)
        response.setHeader(HttpResponseHeader.CONTENT_LENGTH, Integer.toString(0));
        response.content().append("").append(""); session.write(response.toByteBuffer());


    }

    private void sendFile(Session session, HttpResponse response, Path file) throws Exception {

        response.setStatusCode(StatusCode.OK);

        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file.toFile());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setHeader(HttpResponseHeader.CONTENT_TYPE, contentType);


        try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {


            long size = Math.min(Integer.MAX_VALUE, fileChannel.size());
            MappedByteBuffer mappedFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            response.setHeader(HttpResponseHeader.CONTENT_LENGTH, String.valueOf(fileChannel.size()));
            //Write status and headers
            session.write(response.toByteBuffer());
            //Write file
            session.write(mappedFile);

        }


    }


    private void sendListing(Session session, HttpResponse response, Path realRequestPath) throws IOException {
        response.setStatusCode(StatusCode.OK);
        response.setHeader(HttpResponseHeader.CONTENT_TYPE, "text/html; charset=UTF-8");

        //In case of disk mirroring, or just links, we need to know the real rootPath
        final Path relativeFilePath = rootPath.relativize(realRequestPath);


        //Root filename (in case the root path corresponds to the root of the filesystem (i.e c:\\)
        String filename = "/";

        if (!relativeFilePath.getFileName().toString().isEmpty()) {
            filename = relativeFilePath.getFileName().toString();
        }


        response.content()
                .append("<!DOCTYPE html>")
                .append(StringUtils.CRLF)
                .append("<html><head><title>")
                .append("Listing of: ");


        response.content().append(filename);
        response.content().append("</title></head><body>").append(StringUtils.CRLF);

        response.content().append("<h3>Listing of: "); response.content().append(filename);
        response.content().append("</h3>").append(StringUtils.CRLF);

        response.content().append("<ul>");
        response.content().append("<li><a href=\"../\">..</a></li>").append(StringUtils.CRLF);

        logger.info("Browsing the path : {}", realRequestPath);


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(realRequestPath,
                                                                     new DirectoryStream.Filter<Path>() {


                                                                         @Override
                                                                         public boolean accept(Path entry) throws IOException {
                                                                             return !Files.isHidden(
                                                                                     entry) && Files.isReadable(
                                                                                     entry) && HttpUtils.isAllowedFilename(
                                                                                     entry.getFileName().toString());
                                                                         }
                                                                     })) {
            for (Path entry : stream) {
                logger.debug("Appending the path entry {} to the listing.", entry);
                response.content().append("<li><a href=\""); response.content().append(entry.getFileName().toString());
                response.content().append("\">"); response.content().append(entry.getFileName());
                response.content().append("</a></li>" + StringUtils.CRLF);
            }
        } catch (IOException | DirectoryIteratorException e) {
            logger.error("Error occuring when browsing " + realRequestPath, e);

        }


        response.content().append("</ul></body></html>" + StringUtils.CRLF);
        response.setHeader(HttpResponseHeader.CONTENT_LENGTH, String.valueOf(response.content().length()));


        try {
            logger.debug("HTTP WRITE: content-length={} : {}", response.content().length(), session.getRemoteAddress());
            session.write(response.toByteBuffer());
        } catch (Exception e) {
            sendError(StatusCode.INTERNAL_SERVER_ERROR, session);
        }


    }


}
