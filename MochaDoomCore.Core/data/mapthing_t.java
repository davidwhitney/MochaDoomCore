package data;

import w.CacheableDoomObject;
import w.IPackableDoomObject;
import w.IWritableDoomObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * mapthing_t ... same on disk AND in memory, wow?!
 */

public class mapthing_t implements CacheableDoomObject, IPackableDoomObject, IWritableDoomObject, Cloneable
{
    private static ByteBuffer iobuffer = ByteBuffer.allocate(10);
    public short x;
    public short y;
    public short angle;
    public short type;
    public short options;

    public mapthing_t()
    {
    }

    public mapthing_t(mapthing_t source)
    {
        copyFrom(source);
    }

    public static int sizeOf()
    {
        return 10;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        x = buf.getShort();
        y = buf.getShort();
        angle = buf.getShort();
        type = buf.getShort();
        options = buf.getShort();

    }

    public void copyFrom(mapthing_t source)
    {

        x = source.x;
        y = source.y;
        angle = source.angle;
        options = source.options;
        type = source.type;
    }

    @Override
    public void write(DataOutputStream f)
            throws IOException
    {

        // More efficient, avoids duplicating code and
        // handles little endian better.
        iobuffer.position(0);
        iobuffer.order(ByteOrder.LITTLE_ENDIAN);
        pack(iobuffer);
        f.write(iobuffer.array());

    }

    public void pack(ByteBuffer b)
    {
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putShort(x);
        b.putShort(y);
        b.putShort(angle);
        b.putShort(type);
        b.putShort(options);
    }
}