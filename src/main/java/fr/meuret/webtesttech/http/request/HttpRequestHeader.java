package fr.meuret.webtesttech.http.request;

import com.google.common.base.CaseFormat;

/**
 * Http request headers as per defined in the RFC7231.
 *
 * @author Jerome
 * @see <a href ="http://tools.ietf.org/html/rfc7231">http://tools.ietf.org/html/rfc7231</a>
 */
public enum HttpRequestHeader {


    EXPECT,
    MAX_FORWARDS,
    //Conditionals
    IF_NONE_MATCH,
    IF_MODIFIED_SINCE,
    IF_RANGE,
    //Content negotiation
    ACCEPT,
    ACCEPT_CHARSET,
    ACCEPT_ENCODING,
    ACCEPT_LANGUAGE,
    //Authentication Credentials
    AUTHORIZATION,
    PROXY_AUTHORIZATION,
    //RequestContext
    FROM,
    REFERER,
    USER_AGENT,
    CONNECTION;


    //values() instantiates an array everytime it's called
    private final static HttpRequestHeader[] httpRequestHeaders = values();
    private String headerName;

    private HttpRequestHeader() {
        //headers are case-insensitive
        headerName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, toString());
    }

    public static HttpRequestHeader fromHeader(String header) {
        for (HttpRequestHeader httpResponseHeader : httpRequestHeaders) {
            if (httpResponseHeader.getHeaderName().equalsIgnoreCase(header))
                return httpResponseHeader;
        } return null;


    }

    public String getHeaderName() {

        return headerName;
    }


}