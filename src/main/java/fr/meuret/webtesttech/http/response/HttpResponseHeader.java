package fr.meuret.webtesttech.http.response;

import com.google.common.base.CaseFormat;

/**
 * Http response headers as per defined in the RFC7231.
 *
 * @author Jerome
 * @see <a href ="http://tools.ietf.org/html/rfc7231">http://tools.ietf.org/html/rfc7231</a>
 */
public enum HttpResponseHeader {

    //Control data
    CACHE_CONTROL,
    EXPIRES,
    DATE,
    LOCATION,
    RETRY_AFTER,
    VARY,
    WARNING,
    //Validator
    ETAG,
    LAST_MODIFIED,
    //Authentication challenges
    WWW_AUTHENTICATE,
    PROXY_AUTHENTICATE,
    //Response context
    ALLOW,
    ACCEPT_RANGES,
    SERVER,
    //Content
    CONTENT_LENGTH,
    CONTENT_TYPE,
    TRANSFER_ENCODING, CONNECTION;

    //values() instantiates an array everytime it's called
    private final static HttpResponseHeader[] httpResponseHeaders = values();
    private String headerName;


    private HttpResponseHeader() {
        //headers are case-insensitive
        headerName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, toString());
    }

    public static HttpResponseHeader fromHeader(String header) {
        for (HttpResponseHeader httpResponseHeader : httpResponseHeaders) {
            if (httpResponseHeader.getHeaderName().equalsIgnoreCase(header))
                return httpResponseHeader;
        }
        return null;


    }


    public String getHeaderName() {

        return headerName;
    }

}
