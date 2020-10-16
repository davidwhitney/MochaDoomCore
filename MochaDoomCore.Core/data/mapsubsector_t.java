namespace data {  

using w.CacheableDoomObject;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * SubSector, as generated by BSP.
 */

public class mapsubsector_t : CacheableDoomObject
{

    public char numsegs;
    /**
     * Index of first one, segs are stored sequentially.
     */
    public char firstseg;
    public mapsubsector_t()
    {

    }

    public static int sizeOf()
    {
        return 4;
    }

    
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        numsegs = buf.getChar();
        firstseg = buf.getChar();

    }
}
