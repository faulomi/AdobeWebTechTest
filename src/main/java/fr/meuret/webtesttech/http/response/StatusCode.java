package fr.meuret.webtesttech.http.response;

/**
 * Represents a status code, as per the RFC 7231.
 *
 * @see <a href ="http://tools.ietf.org/html/rfc7231#section-6">http://tools.ietf.org/html/rfc7231#section-6</a>
 */
public enum StatusCode {


    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    OK(200, "OK"),
    CREATED(201, "Created", true),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content", true),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content", true),
    MULTIPLE_CHOICES(300, "Multiple Choices", true),
    MOVED_PERMANENTLY(301, "Moved Permanently", true),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found", true),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", true),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone", true),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    URI_TOO_LONG(414, "URI Too Long", true),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented", true),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");
    private final int code;
    private final String reasonPhrase;
    private final boolean cacheable;


    private StatusCode(int code, String reasonPhrase, boolean cacheable) {

        this.code = code;
        this.reasonPhrase = reasonPhrase;
        this.cacheable = cacheable;
    }

    private StatusCode(int code, String reasonPhrase) {

        this(code, reasonPhrase, false);
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public int getCode() {
        return code;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public String toString() {
        return String.join(" ", Integer.toString(code), reasonPhrase);
    }
}
