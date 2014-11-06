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
 * @author Jerome
 * @see <a href ="http://tools.ietf.org/html/rfc7230">http://tools.ietf.org/html/rfc7230</a>
 */
public class HttpResponse {


    private final Map<HttpResponseHeader, String> headers = new HashMap<>();
    private final StringBuilder content = new StringBuilder();
    private StatusCode statusCode;
    private String statusLine;
    private HttpVersion httpVersion;

    public HttpResponse(HttpVersion version) {
        headers.put(HttpResponseHeader.DATE,
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT"))));
        headers.put(HttpResponseHeader.SERVER, "AdobeWebTechTest/1.0");
        this.httpVersion = version;

    }

    private static String buildStatusLine(HttpVersion httpVersion, StatusCode statusCode) {

        return httpVersion + " " + statusCode;
    }


    public static HttpResponse error(StatusCode statusCode) {
        final HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_1);
        httpResponse.setHeader(HttpResponseHeader.CONNECTION, "close");
        httpResponse.setStatusCode(statusCode);
        httpResponse.content().append(statusCode.getReasonPhrase());

        return httpResponse;

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
        final StringBuilder response = new StringBuilder();
        final StringJoiner headersBuffer = new StringJoiner(StringUtils.CRLF, "", StringUtils.CRLF);

        boolean hasContent = content.length() > 0;
        if (hasContent) {
            setHeader(HttpResponseHeader.CONTENT_LENGTH, Integer.toString(content.length()));
        }
        headersBuffer.add(buildStatusLine(httpVersion, statusCode));
        headers.forEach((header, value) -> headersBuffer.add(String.join(": ", header.getHeaderName(), value)));

        response.append(headersBuffer.toString());
        //Add HTML content
        if (hasContent) {
            response.append(StringUtils.CRLF);
            response.append(content);
        }
        return responseEncoder.encode(CharBuffer.wrap(response.toString()));


    }
}
