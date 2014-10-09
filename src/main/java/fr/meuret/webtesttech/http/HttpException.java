package fr.meuret.webtesttech.http;

import fr.meuret.webtesttech.http.response.StatusCode;

/**
 * A Http exception that may be thrown during the HTTP message processing.
 * <p>
 * Typically, this exception has to be catched and an HTTP response must be sent according to
 * the status code.
 *
 * @author Jerome
 *
 */
public class HttpException extends Exception {

    private final StatusCode statusCode;

    public HttpException(StatusCode statusCode) {
        this.statusCode = statusCode;

    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return String.join(" ", HttpVersion.HTTP_1_1.toString(), statusCode.toString());
    }
}
