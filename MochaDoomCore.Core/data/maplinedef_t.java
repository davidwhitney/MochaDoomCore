namespace data {  

using w.CacheableDoomObject;
using w.DoomBuffer;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * A LineDef, as used for editing, and as input to the BSP builder.
 */
public class maplinedef_t : CacheableDoomObject
{

    public char v1;
    public char v2;
    public short flags;
    public short special;
    public short tag;
    /**
     * sidenum[1] will be 0xFFFF if one sided
     */
    public char[] sidenum;

    public maplinedef_t()
    {
        sidenum = new char[2];
    }

    public static int sizeOf()
    {
        return 14;
    }

    
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        v1 = buf.getChar();
        v2 = buf.getChar();
        flags = buf.getShort();
        special = buf.getShort();
        tag = buf.getShort();
        DoomBuffer.readCharArray(buf, sidenum, 2);
    }
}
