namespace p {  

using rr.SectorAction;
using w.DoomIO;
using w.IReadableDoomObject;

using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;

public class floormove_t : SectorAction : IReadableDoomObject
{

    public floor_e type;
    public bool crush;
    public int direction;
    public int newspecial;
    public short texture;
    /**
     * fixed_t
     */
    public int floordestheight;
    /**
     * fixed_t
     */
    public int speed;
    public floormove_t()
    {
        // MAES HACK: floors are implied to be at least of "lowerFloor" type
        // unless set otherwise, due to implicit zero-enum value.
        type = floor_e.lowerFloor;
    }

    
    public void read(Stream f)  
    {

        super.read(f); // Call thinker reader first            
        type = floor_e.values()[DoomIO.readLEInt(f)];
        crush = DoomIO.readIntbool(f);
        sectorid = DoomIO.readLEInt(f); // Sector index (or pointer?)
        direction = DoomIO.readLEInt(f);
        newspecial = DoomIO.readLEInt(f);
        texture = DoomIO.readLEShort(f);
        floordestheight = DoomIO.readLEInt(f);
        speed = DoomIO.readLEInt(f);
    }

    
    public void pack(MemoryStream b)  
    {
        super.pack(b); //12            
        b.putInt(type.ordinal()); // 16
        b.putInt(crush ? 1 : 0); //20
        b.putInt(sectorid); // 24
        b.putInt(direction); // 28
        b.putInt(newspecial); // 32
        b.putShort(texture); // 34
        b.putInt(floordestheight); // 38
        b.putInt(speed); // 42
    }

}