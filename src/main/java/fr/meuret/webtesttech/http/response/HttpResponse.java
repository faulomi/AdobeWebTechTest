package fr.meuret.webtesttech.http.response;

import fr.meuret.webtesttech.http.HttpVersion;
import fr.meuret.webtesttech.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A Http response as per defined in the RFC7230.
 *
 * @see \http://tools.ietf.org/html/rfc7230
 */
public class HttpResponse {


    private final Map<HttpResponseHeader, String> headers = new HashMap<>();
    private final StringBuilder content = new StringBuilder();
    private StatusCode statusCode;
    private String statusLine;
    private HttpVersion httpVersion;

    public HttpResponse(HttpVersion version) {
        headers.put(HttpResponseHeader.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT"))));
        headers.put(HttpResponseHeader.SERVER, "AdobeWebTechTest/1.0");
        this.httpVersion = version;

    }

    private static String buildStatusLine(HttpVersion httpVersion, StatusCode statusCode) {

        return httpVersion + " " + statusCode;
    }


    public void setHeader(HttpResponseHeader httpResponseHeader, String value) {
        this.headers.put(httpResponseHeader, value);
    }

    public StringBuilder content() {
        return content;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public void setHttpVersion(HttpVersion version) {
        this.httpVersion = version;
    }


    public ByteBuffer toByteBuffer() throws Exception {


        //FOR HTTP 1.1, default charset is ISO-8859-1
        final CharsetEncoder responseEncoder = Charset.forName(StandardCharsets.ISO_8859_1.displayName()).newEncoder();
        final StringJoiner response = new StringJoiner(StringUtils.CRLF, "", StringUtils.CRLF);
        response.add(buildStatusLine(httpVersion, statusCode));

        headers.forEach((header, value) -> response.add(String.join(": ", header.getHeaderName(), value)));


        if (content.length() > 0) {
            response.add(StringUtils.CRLF).add(content);
        }
        final ByteBuffer responseBuffer = responseEncoder.encode(CharBuffer.wrap(response.toString()));
        return responseBuffer;

    }
}
