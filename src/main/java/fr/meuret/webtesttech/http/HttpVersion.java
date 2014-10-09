package fr.meuret.webtesttech.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Http version according to the RFC7230.
 *
 * @author Jerome
 * @see <a href ="http://tools.ietf.org/html/rfc7230">http://tools.ietf.org/html/rfc7230</a>
 */
public enum HttpVersion {

    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");
    private final static Map<String, HttpVersion> versions = new HashMap<String, HttpVersion>();

    static {
        final HttpVersion[] values = HttpVersion.values();
        for (HttpVersion version : values)
            versions.put(version.toString(), version);


    }

    private final String versionLabel;


    private HttpVersion(String versionLabel) {
        this.versionLabel = versionLabel;

    }

    public static HttpVersion fromVersionLabel(String value) {

        return versions.get(value);

    }

    @Override
    public String toString() {
        return versionLabel;
    }


}
