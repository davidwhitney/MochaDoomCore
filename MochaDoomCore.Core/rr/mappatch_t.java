namespace rr {  

using w.CacheableDoomObject;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * Texture definition.
 * Each texture is composed of one or more patches,
 * with patches being lumps stored in the WAD.
 * The lumps are referenced by number, and patched
 * into the rectangular texture space using origin
 * and possibly other attributes.
 */

public class mappatch_t : CacheableDoomObject
{
    public short originx;
    public short originy;
    public short patch;
    public short stepdir;
    public short colormap;

    public static int size()
    {
        return 10;
    }

    @Override
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        originx = buf.getShort();
        originy = buf.getShort();
        patch = buf.getShort();
        stepdir = buf.getShort();
        colormap = buf.getShort();
    }


}
