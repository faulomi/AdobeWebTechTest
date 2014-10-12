package fr.meuret.webtesttech.util;

import java.nio.ByteBuffer;

/**
 * Created by Jerome on 18/09/2014.
 */
public abstract class ByteBufferPool {


    public abstract ByteBuffer acquireBuffer();

    public abstract ByteBuffer releaseBuffer(ByteBuffer byteBuffer);
}
