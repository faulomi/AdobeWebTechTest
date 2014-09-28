package fr.meuret.webtesttech.http;

import fr.meuret.webtesttech.http.request.HttpRequest;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;

public class HttpRequestTest {

    @Test
    public void testHttpRequestFromByteBuffer() throws Exception {

        String request = "GET /path/file.html HTTP/1.0\r\n" +
                "From: someuser@jmarshall.com\r\n" +
                "User-Agent: HTTPTool/1.0\r\n\r\n";


        ByteBuffer buffer = ByteBuffer.wrap(request.getBytes(StandardCharsets.ISO_8859_1));

        final HttpRequest httpRequest = HttpRequest.from(buffer);

        assertNotNull(httpRequest);

    }


}