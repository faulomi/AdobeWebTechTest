package fr.meuret.webtesttech.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpVersionTest {

    @Test
    public void testGetHttpVersionFromValidVersionLabel() throws Exception {


        HttpVersion httpVersion1_0 = HttpVersion.fromVersionLabel("HTTP/1.0");
        HttpVersion httpVersion1_1 = HttpVersion.fromVersionLabel("HTTP/1.1");

        assertEquals(HttpVersion.HTTP_1_0, httpVersion1_0);
        assertEquals(HttpVersion.HTTP_1_1, httpVersion1_1);


    }

    @Test
    public void testGetHttpVersionFromInvalidVersionLabel() throws Exception {

        HttpVersion invalidHttpVersion = HttpVersion.fromVersionLabel("dummy");
        assertNull(invalidHttpVersion);

    }

}