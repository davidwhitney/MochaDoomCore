package boom;

import utils.C2JUtils;
import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class mapsubsector_znod_t implements CacheableDoomObject
{

    public long numsegs;

    public static int sizeOf()
    {
        return 4;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        numsegs = C2JUtils.unsigned(buf.getInt());
    }

}
