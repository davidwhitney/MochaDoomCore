package data;

import w.CacheableDoomObject;
import w.DoomBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Sector definition, from editing.
 */
public class mapsector_t implements CacheableDoomObject
{

    public short floorheight;
    public short ceilingheight;
    public String floorpic;
    public String ceilingpic;
    public short lightlevel;
    public short special;
    public short tag;
    public mapsector_t()
    {

    }

    public static int sizeOf()
    {
        return 26;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        floorheight = buf.getShort();
        ceilingheight = buf.getShort();
        floorpic = DoomBuffer.getNullTerminatedString(buf, 8).toUpperCase();
        ceilingpic = DoomBuffer.getNullTerminatedString(buf, 8).toUpperCase();
        lightlevel = buf.getShort();
        special = buf.getShort();
        tag = buf.getShort();
    }

}
