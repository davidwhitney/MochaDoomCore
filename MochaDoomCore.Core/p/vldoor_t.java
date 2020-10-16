namespace p {  

using rr.SectorAction;
using w.DoomIO;
using w.IReadableDoomObject;

using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;

public class vldoor_t : SectorAction : IReadableDoomObject
{

    public vldoor_e type;
    /**
     * fixed_t
     */
    public int topheight, speed;

    /**
     * 1 = up, 0 = waiting at top, -1 = down
     */
    public int direction;

    /**
     * tics to wait at the top
     */
    public int topwait;

    /**
     * (keep in case a door going down is reset)
     * when it reaches 0, start going down
     */
    public int topcountdown;

    
    public void read(Stream f)  
    {

        super.read(f); // Call thinker reader first
        type = vldoor_e.values()[DoomIO.readLEInt(f)];
        sectorid = DoomIO.readLEInt(f); // Sector index (or pointer?)
        topheight = DoomIO.readLEInt(f);
        speed = DoomIO.readLEInt(f);
        direction = DoomIO.readLEInt(f);
        topwait = DoomIO.readLEInt(f);
        topcountdown = DoomIO.readLEInt(f);
    }

    
    public void pack(MemoryStream b)  
    {
        super.pack(b); //12
        b.putInt(type.ordinal()); // 16
        b.putInt(sectorid); // 20
        b.putInt(topheight); // 24
        b.putInt(speed); //28
        b.putInt(direction); // 32
        b.putInt(topwait); //36
        b.putInt(topcountdown); //40
    }

}