namespace data {  

using w.CacheableDoomObject;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * LineSeg, generated by splitting LineDefs
 * using partition lines selected by BSP builder.
 * MAES: this is the ON-DISK structure. The corresponding memory structure,
 * segs_t, has fixed_t members.
 */

public class mapseg_t : CacheableDoomObject
{

    public char v1;
    public char v2;
    public char angle;
    public char linedef;
    public char side;
    public char offset;
    public mapseg_t()
    {

    }

    public static int sizeOf()
    {
        return 12;
    }

    
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        v1 = buf.getChar();
        v2 = buf.getChar();
        angle = buf.getChar();
        linedef = buf.getChar();
        side = buf.getChar();
        offset = buf.getChar();

    }

    public String toString()
    {
        return String.format("mapseg_t v1,2: %d %d ang: %d ld: %d sd: %d off: %d",
                (int) v1, (int) v2, (int) angle, (int) linedef, (int) side, (int) offset);
    }

}
