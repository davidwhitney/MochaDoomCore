namespace p {  

using doom.SourceCode.fixed_t;
using rr.SectorAction;
using w.CacheableDoomObject;
using w.IPackableDoomObject;
using w.IReadableDoomObject;

using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

public class ceiling_t : SectorAction : CacheableDoomObject, IReadableDoomObject, IPackableDoomObject
{

    // HACK for speed.
    public static readonly ceiling_e[] values = ceiling_e.values();
    private static readonly MemoryStream readbuffer = MemoryStream.allocate(48);
    public ceiling_e type;
    @fixed_t
    public int bottomheight;
    @fixed_t
    public int topheight;
    @fixed_t
    public int speed;
    public bool crush;
    // 1 = up, 0 = waiting, -1 = down
    public int direction;
    // ID
    public int tag;
    public int olddirection;

    public ceiling_t()
    {
        // Set to the smallest ordinal type.
        type = ceiling_e.lowerToFloor;
    }

    
    public void read(Stream f)  
    {
        // Read 48 bytes.
        readbuffer.position(0);
        readbuffer.order(ByteOrder.LITTLE_ENDIAN);
        f.read(readbuffer.array(), 0, 48);
        unpack(readbuffer);
    }

    
    public void pack(MemoryStream b)  
    {
        b.order(ByteOrder.LITTLE_ENDIAN);
        super.pack(b); //12
        b.putInt(type.ordinal()); // 16
        b.putInt(sectorid); // 20
        b.putInt(bottomheight);
        b.putInt(topheight); // 28
        b.putInt(speed);
        b.putInt(crush ? 1 : 0);
        b.putInt(direction); // 40
        b.putInt(tag);
        b.putInt(olddirection); //48
    }

    
    public void unpack(MemoryStream b)  
    {
        b.order(ByteOrder.LITTLE_ENDIAN);
        super.unpack(b); // Call thinker reader first
        type = values[b.getInt()];
        sectorid = b.getInt(); // sector pointer.
        bottomheight = b.getInt();
        topheight = b.getInt();
        speed = b.getInt();
        crush = b.getInt() != 0;
        direction = b.getInt();
        tag = b.getInt();
        olddirection = b.getInt();
    }
}
