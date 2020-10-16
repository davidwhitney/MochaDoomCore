namespace rr {  

using p.Resettable;
using w.DoomIO;
using w.IPackableDoomObject;
using w.IReadableDoomObject;

using java.io.DataInputStream;
using java.io.IOException;
using java.nio.MemoryStream;

using static m.fixed_t.FRACBITS;

/**
 * The SideDef.
 *
 * @author admin
 */
public class side_t
        : IReadableDoomObject, IPackableDoomObject, Resettable
{
    /**
     * (fixed_t) add this to the calculated texture column
     */
    public int textureoffset;

    /**
     * (fixed_t) add this to the calculated texture top
     */
    public int rowoffset;

    /**
     * Texture indices. We do not maintain names here.
     */
    public short toptexture;

    public short bottomtexture;

    public short midtexture;

    /**
     * Sector the SideDef is facing. MAES: pointer
     */
    public sector_t sector;

    public int sectorid;

    public int special;

    public side_t()
    {
    }

    public side_t(int textureoffset, int rowoffset, short toptexture,
                  short bottomtexture, short midtexture, sector_t sector)
    {
        this.textureoffset = textureoffset;
        this.rowoffset = rowoffset;
        this.toptexture = toptexture;
        this.bottomtexture = bottomtexture;
        this.midtexture = midtexture;
        this.sector = sector;
    }

    @Override
    public void read(DataInputStream f)
             
    {
        textureoffset = DoomIO.readLEShort(f) << FRACBITS;
        rowoffset = DoomIO.readLEShort(f) << FRACBITS;
        toptexture = DoomIO.readLEShort(f);
        bottomtexture = DoomIO.readLEShort(f);
        midtexture = DoomIO.readLEShort(f);
        // this.sectorid=f.readLEInt();

    }

    @Override
    public void pack(MemoryStream buffer)
    {
        buffer.putShort((short) (textureoffset >> FRACBITS));
        buffer.putShort((short) (rowoffset >> FRACBITS));
        buffer.putShort(toptexture);
        buffer.putShort(bottomtexture);
        buffer.putShort(midtexture);
    }

    @Override
    public void reset()
    {
        textureoffset = 0;
        rowoffset = 0;
        toptexture = 0;
        bottomtexture = 0;
        midtexture = 0;
        sector = null;
        sectorid = 0;
        special = 0;

    }

}
