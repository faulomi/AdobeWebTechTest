package fr.meuret.webtesttech.http;

import fr.meuret.webtesttech.http.response.StatusCode;

/**
 * Created by Jérôme on 28/09/2014.
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
