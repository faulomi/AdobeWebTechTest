package fr.meuret.webtesttech.http;

import fr.meuret.webtesttech.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * An HTTP chunk, as per defined in the RFC 7230.
 *
 * @author Jerome
 * @see <a href ="http://tools.ietf.org/html/rfc7230">http://tools.ietf.org/html/rfc7230</a>
 */
public class HttpChunk {


    public static final HttpChunk LAST_CHUNK = new HttpChunk("", 0);


    private final int size;
    private final String datas;


    public HttpChunk(String datas, int size) {
        this.datas = datas; this.size = size;
    }

    public ByteBuffer toByteBuffer() throws Exception {
        final String hexSize = Integer.toHexString(size);
        StringJoiner chunkBuilder = new StringJoiner(StringUtils.CRLF, "", StringUtils.CRLF);
        chunkBuilder.add(hexSize).add(datas);

        final CharsetEncoder responseEncoder = Charset.forName(StandardCharsets.ISO_8859_1.displayName()).newEncoder();
        return responseEncoder.encode(CharBuffer.wrap(chunkBuilder.toString()));

    }

    @Override
    public String toString() {
        return String.join(StringUtils.CRLF, Integer.toString(size), datas);
    }
}
