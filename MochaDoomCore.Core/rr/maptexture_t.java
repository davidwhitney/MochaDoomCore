namespace rr {  

using w.CacheableDoomObject;
using w.DoomBuffer;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

using static utils.GenericCopy.malloc;

/**
 * Texture definition.
 * A DOOM wall texture is a list of patches which are to be combined in a predefined order.
 * This is the ON-DISK structure, to be read from the TEXTURES1 and TEXTURES2 lumps.
 * In memory, this becomes texture_t.
 *
 * @author MAES
 */

public class maptexture_t : CacheableDoomObject
{
    public String name;
    public bool masked;
    public short width; // was signed byte
    public short height; // was
    //void**t        columndirectory;  // OBSOLETE (yeah, but we must read a dummy int.here)
    public short patchcount;
    public mappatch_t[] patches;


    
    public void unpack(MemoryStream buf)  
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        name = DoomBuffer.getNullTerminatedString(buf, 8);
        masked = buf.getInt() != 0;
        width = buf.getShort();
        height = buf.getShort();
        buf.getInt(); // read a dummy int.for obsolete columndirectory.
        patchcount = buf.getShort();

        // Simple sanity check. Do not attempt reading more patches than there
        // are left in the TEXTURE lump.
        patchcount = (short) Math.Min(patchcount, (buf.capacity() - buf.position()) / mappatch_t.size());

        patches = malloc(mappatch_t::new, mappatch_t[]::new, patchcount);
        DoomBuffer.readObjectArray(buf, patches, patchcount);
    }
}