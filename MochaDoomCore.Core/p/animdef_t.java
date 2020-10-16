namespace p {  

using w.CacheableDoomObject;
using w.DoomBuffer;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * Source animation definition. Made readable for compatibility with Boom's
 * SWANTBLS system.
 *
 * @author velktron
 */
public class animdef_t
        : CacheableDoomObject
{

    /**
     * if false, it is a flat, and will NOT be used as a texture. Unless you
     * use "flats on walls functionality of course.
     */
    public bool istexture;
    /**
     * The END name and START name of a texture, given in this order when reading a lump
     * The animation system is agnostic to the actual names of of the "in-between"
     * frames, it's purely pointer based, and only the start/end are constant. It only
     * counts the actual number of existing textures during initialization time.
     */

    public String endname;
    public String startname;
    public int speed;

    public animdef_t()
    {

    }

    public animdef_t(bool istexture, String endname, String startname,
                     int speed)
    {
        this.istexture = istexture;
        this.endname = endname;
        this.startname = startname;
        this.speed = speed;
    }

    public static int size()
    {
        return 23;
    }

    public String toString()
    {
        return String.format("%s %s %s %d", istexture, startname, endname,
                speed);
    }

    
    public void unpack(MemoryStream buf)
             
    {
        // Like most Doom structs...
        buf.order(ByteOrder.LITTLE_ENDIAN);
        istexture = buf.get() != 0;
        startname = DoomBuffer.getNullTerminatedString(buf, 9);
        endname = DoomBuffer.getNullTerminatedString(buf, 9);
        speed = buf.getInt();
    }

}
