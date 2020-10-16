package boom;

import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class mapsubsector_v4_t implements CacheableDoomObject
{

    public char numsegs;
    /**
     * Index of first one, segs are stored sequentially.
     */
    public int firstseg;

    public static int sizeOf()
    {
        return 6;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        numsegs = buf.getChar();
        firstseg = buf.getInt();
    }

}
