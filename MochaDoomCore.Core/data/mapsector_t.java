namespace data {  

using w.CacheableDoomObject;
using w.DoomBuffer;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * Sector definition, from editing.
 */
public class mapsector_t : CacheableDoomObject
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
    public void unpack(MemoryStream buf)
             
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
