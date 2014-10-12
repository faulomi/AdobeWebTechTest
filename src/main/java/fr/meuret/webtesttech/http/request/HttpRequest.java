package fr.meuret.webtesttech.http.request;

import fr.meuret.webtesttech.http.HttpException;
import fr.meuret.webtesttech.http.HttpMethod;
import fr.meuret.webtesttech.http.HttpVersion;
import fr.meuret.webtesttech.http.response.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Http request as per defined in the RFC7230.
 *
 * @author Jerome
 * @see <a href ="http://tools.ietf.org/html/rfc7230">http://tools.ietf.org/html/rfc7231</a>
 */
public class HttpRequest {

    /**
     * Regex to parse HttpRequest Request Line
     */
    public static final Pattern REQUEST_LINE_PATTERN = Pattern.compile(" ");
    /**
     * Regex to parse out QueryString from HttpRequest
     */
    public static final Pattern QUERY_STRING_PATTERN = Pattern.compile("\\?");
    /**
     * Regex to parse out parameters from query string
     */
    public static final Pattern PARAM_STRING_PATTERN = Pattern.compile("\\&|;"); //Delimiter is either & or ;
    /**
     * Regex to parse out key/value pairs
     */
    public static final Pattern KEY_VALUE_PATTERN = Pattern.compile("=");
    /**
     * Regex to parse raw headers and body
     */
    public static final Pattern RAW_VALUE_PATTERN = Pattern.compile("\\r\\n\\r\\n");
    /**
     * Regex to parse raw headers from body
     */
    public static final Pattern HEADERS_BODY_PATTERN = Pattern.compile("\\r\\n");
    /**
     * Regex to parse header name and value
     */
    public static final Pattern HEADER_VALUE_PATTERN = Pattern.compile(":");
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private static boolean keepAlive = false;
    private final Map<HttpRequestHeader, String> headers;
    private Map<String, String> parameters;

    private HttpMethod method;
    private HttpVersion version;
    private String requestPath;


    public HttpRequest(String requestLine, Map<HttpRequestHeader, String> headers) {
        parseRequestLine(requestLine);
        this.headers = Collections.unmodifiableMap(headers);
        setKeepAlive();

    }

    public static HttpRequest from(ByteBuffer raw) throws HttpException {

        //FOR HTTP 1.1, default charset is ISO-8859-1
        final CharsetDecoder requestDecoder = Charset.forName(StandardCharsets.ISO_8859_1.displayName()).newDecoder();

        try {


            final CharBuffer packet = requestDecoder.decode(raw);
            final String httpRequest = packet.toString();
            final String[] httpHeadersAndBody = RAW_VALUE_PATTERN.split(httpRequest);
            final String[] httpHeadersFields = HEADERS_BODY_PATTERN.split(httpHeadersAndBody[0]);


            final Map<HttpRequestHeader, String> httpHeadersMap = Arrays.stream(httpHeadersFields)
                    .skip(1)
                    .map((String httpHeaderField) -> HEADER_VALUE_PATTERN.split(httpHeaderField))
                    .filter((httpFieldParts) -> httpFieldParts.length == 2)
                    .collect(Collectors.toMap((httpFieldParts) -> HttpRequestHeader.fromHeader(httpFieldParts[0]), (httpFieldParts) -> httpFieldParts[1].trim()));


            return new HttpRequest(httpHeadersFields[0], httpHeadersMap);


        } catch (Exception e) {
            logger.error("Error occured when parsing HTTP request :", e);
            throw new HttpException(StatusCode.BAD_REQUEST);
        }


    }

    private void setKeepAlive() {
        String connection = headers.get(HttpRequestHeader.CONNECTION);
        if ("keep-alive".equalsIgnoreCase(connection)) {
            keepAlive = true;
        } else if ("close".equalsIgnoreCase(connection) || HttpVersion.HTTP_1_0.equals(version)) {
            keepAlive = false;
        } else {
            keepAlive = true;
        }
    }

    public Map<HttpRequestHeader, String> getHeaders() {
        return headers;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    private void parseRequestLine(String requestLine) {


        String[] requestLineFields = REQUEST_LINE_PATTERN.split(requestLine);
        this.method = HttpMethod.valueOf(requestLineFields[0]);
        parseRequestTarget(requestLineFields[1]);
        parseHttpVersion(requestLineFields[2]);


    }

    private void parseHttpVersion(String httpVersionField) {
        this.version = HttpVersion.fromVersionLabel(httpVersionField);


    }

    private void parseRequestTarget(String requestTarget) {

        String[] requestPathAndQuery = QUERY_STRING_PATTERN.split(requestTarget);
        this.requestPath = requestPathAndQuery[0];
        if (requestPathAndQuery.length > 1)
            parseQuery(requestPathAndQuery[1]);


    }

    private void parseQuery(String query) {
        String[] queryParameters = PARAM_STRING_PATTERN.split(query);
        parameters = Collections.unmodifiableMap(Arrays.asList(queryParameters).stream().map((parameter) -> KEY_VALUE_PATTERN.split(parameter)).filter((parameterParts) -> parameterParts.length == 2).collect(Collectors.toMap((parametersParts) -> parametersParts[0], (parametersParts) -> parametersParts[1].trim())));
    }

    public String getParameter(String parameter) {

        return parameters.get(parameter);
    }


    public boolean isKeepAlive() {
        return keepAlive;
    }


    public String getHeader(HttpRequestHeader header) {
        return headers.get(header);
    }

}
