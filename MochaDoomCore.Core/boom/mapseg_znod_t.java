package boom;

import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * ZDoom node?
 */

public class mapseg_znod_t implements CacheableDoomObject
{

    public int v1, v2; // Those are unsigned :-/
    public char linedef;
    public byte side;
    public mapseg_znod_t()
    {

    }

    public static int sizeOf()
    {
        return 11;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        v1 = buf.getInt();
        v2 = buf.getInt();
        linedef = buf.getChar();
        side = buf.get();
    }

}
