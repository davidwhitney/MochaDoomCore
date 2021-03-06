namespace p {  

using doom.SourceCode.fixed_t;
using rr.SectorAction;
using rr.sector_t;
using w.DoomIO;
using w.IReadableDoomObject;

using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;

public class plat_t : SectorAction : IReadableDoomObject
{

    public sector_t sector;
    public @fixed_t
    int speed, low, high;
    public int wait;
    public int count;
    public plat_e status;
    public plat_e oldstatus;
    public bool crush;
    public int tag;
    public plattype_e type;

    public plat_t()
    {
        // These must never be null so they get the lowest ordinal value.
        // by default.
        status = plat_e.up;
        oldstatus = plat_e.up;
    }

    
    public void read(Stream f)  
    {

        super.read(f); // Call thinker reader first            
        sectorid = DoomIO.readLEInt(f); // Sector index
        speed = DoomIO.readLEInt(f);
        low = DoomIO.readLEInt(f);
        high = DoomIO.readLEInt(f);
        wait = DoomIO.readLEInt(f);
        count = DoomIO.readLEInt(f);
        status = plat_e.values()[DoomIO.readLEInt(f)];
        oldstatus = plat_e.values()[DoomIO.readLEInt(f)];
        System.out.println(status);
        System.out.println(oldstatus);
        crush = DoomIO.readIntbool(f);
        tag = DoomIO.readLEInt(f);
        type = plattype_e.values()[DoomIO.readLEInt(f)];
    }

    
    public void pack(MemoryStream b)  
    {
        super.pack(b); //12            
        b.putInt(sectorid); // 16
        b.putInt(speed);//20
        b.putInt(low); // 24
        b.putInt(high); //28
        b.putInt(wait); //32
        b.putInt(count); //36
        b.putInt(status.ordinal()); //40
        b.putInt(oldstatus.ordinal()); //44
        System.out.println(status);
        System.out.println(oldstatus);
        b.putInt(crush ? 1 : 0); // 48
        b.putInt(tag); // 52
        b.putInt(type.ordinal()); // 56
    }

    public vldoor_t asVlDoor(sector_t[] sectors)
    {
        /*
        	typedef struct
        	{
        	    thinker_t	thinker;
        	    vldoor_e	type;
        	    sector_t*	sector;
        	    fixed_t	topheight;
        	    fixed_t	speed;

        	    // 1 = up, 0 = waiting at top, -1 = down
        	    int             direction;
        	    
        	    // tics to wait at the top
        	    int             topwait;
        	    // (keep in case a door going down is reset)
        	    // when it reaches 0, start going down
        	    int             topcountdown;
        	    
        	} vldoor_t;
         */

        vldoor_t tmp = new vldoor_t();
        tmp.next = next;
        tmp.prev = prev;
        tmp.thinkerFunction = thinkerFunction;
        tmp.type = vldoor_e.values()[sector.id % vldoor_e.VALUES];
        tmp.sector = sectors[speed % sectors.Length];
        tmp.topheight = low;
        tmp.speed = high;
        tmp.direction = wait;
        tmp.topwait = count;
        tmp.topcountdown = status.ordinal();

        return tmp;
    }
}
