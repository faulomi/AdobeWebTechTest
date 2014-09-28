package fr.meuret.webtesttech.http.response;

import fr.meuret.webtesttech.http.HttpVersion;

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
    private ByteBuffer payload;

    public HttpResponse(HttpVersion version) {
        headers.put(HttpResponseHeader.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT"))));
        headers.put(HttpResponseHeader.SERVER, "AdobeWebTechTest/1.0");
        this.httpVersion = version;

    }

    private static String buildStatusLine(HttpVersion httpVersion, StatusCode statusCode) {

        return httpVersion + " " + statusCode;
    }

    public void setPayload(ByteBuffer payload) {
        this.payload = payload;
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
        final StringJoiner statusLineAndHeaders = new StringJoiner("\r\n", "", "\r\n");
        statusLineAndHeaders.add(buildStatusLine(httpVersion, statusCode));

        headers.forEach((header, value) -> statusLineAndHeaders.add(String.join(": ", header.getHeaderName(), value)));

        if (payload != null) {
            ByteBuffer responseBuffer = responseEncoder.encode(CharBuffer.wrap(statusLineAndHeaders.add("").toString()));
            ByteBuffer completeResponseBuffer = ByteBuffer.allocate(responseBuffer.capacity() + payload.capacity() + 4);
            completeResponseBuffer.put(responseBuffer).put(payload).putChar('\r').putChar('\n');
            completeResponseBuffer.flip();
            return completeResponseBuffer;
        } else {
            String completeResponse = String.join("", statusLineAndHeaders.add("").toString(), content.append("\r\n").toString());
            ByteBuffer responseBuffer = responseEncoder.encode(CharBuffer.wrap(completeResponse.toString()));
            return responseBuffer;
        }

    }
}
