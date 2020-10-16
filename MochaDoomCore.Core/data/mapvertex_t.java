package data;

import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This is the structure of a map vertex ON DISK: in memory it gets shifted and
 * expanded to fixed_t. Also, on disk it only exists as part of the VERTEXES
 * lump: it is not individually cacheable, even though it implements
 * CacheableDoomObject.
 */

public class mapvertex_t
        implements CacheableDoomObject
{

    public short x;
    public short y;

    public mapvertex_t(short x, short y)
    {
        this.x = x;
        this.y = y;
    }

    public mapvertex_t()
    {
        this((short) 0, (short) 0);
    }

    public static int sizeOf()
    {
        return 4;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        x = buf.getShort();
        y = buf.getShort();
    }

}
