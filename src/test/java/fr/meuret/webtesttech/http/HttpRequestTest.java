package fr.meuret.webtesttech.http;

import fr.meuret.webtesttech.http.request.HttpRequest;
import fr.meuret.webtesttech.http.request.HttpRequestHeader;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class HttpRequestTest {

    @Test
    public void testHttpRequestFromByteBuffer() throws Exception {

        String request = "GET /path/file.html HTTP/1.1\r\n" +
                "From: someuser@jmarshall.com\r\n" +
                "User-Agent: HTTPTool/1.0\r\n\r\n";


        ByteBuffer buffer = ByteBuffer.wrap(request.getBytes(StandardCharsets.ISO_8859_1));

        final HttpRequest httpRequest = HttpRequest.from(buffer);

        assertNotNull(httpRequest); assertEquals("Wrong HTTP method", HttpMethod.GET, httpRequest.getMethod());
        assertEquals("Wrong HTTP version", HttpVersion.HTTP_1_1, httpRequest.getVersion()); assertTrue("Header FROM",
                                                                                                       "someuser@jmarshall.com"
                                                                                                               .equalsIgnoreCase(
                                                                                                                       httpRequest
                                                                                                                               .getHeader(
                                                                                                                                       HttpRequestHeader.FROM)));
        assertTrue("Header User-agent",
                   "HTTPTool/1.0".equalsIgnoreCase(httpRequest.getHeader(HttpRequestHeader.USER_AGENT)));

    }


}