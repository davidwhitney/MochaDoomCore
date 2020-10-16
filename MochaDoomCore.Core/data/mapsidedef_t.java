namespace data {  

using w.CacheableDoomObject;
using w.DoomBuffer;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * A SideDef, defining the visual appearance of a wall, by setting textures and
 * offsets. ON-DISK.
 */

public class mapsidedef_t : CacheableDoomObject
{

    public short textureoffset;
    public short rowoffset;
    // 8-char strings.
    public String toptexture;
    public String bottomtexture;
    public String midtexture;
    /**
     * Front sector, towards viewer.
     */
    public short sector;

    public mapsidedef_t()
    {

    }

    public static int sizeOf()
    {
        return 30;
    }

    
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        textureoffset = buf.getShort();
        rowoffset = buf.getShort();
        toptexture = DoomBuffer.getNullTerminatedString(buf, 8).toUpperCase();
        bottomtexture = DoomBuffer.getNullTerminatedString(buf, 8).toUpperCase();
        midtexture = DoomBuffer.getNullTerminatedString(buf, 8).toUpperCase();
        sector = buf.getShort();

    }
}
