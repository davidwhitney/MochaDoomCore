namespace p {  

using rr.SectorAction;
using w.DoomIO;

using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;

public class strobe_t : SectorAction
{

    public int count;
    public int minlight;
    public int maxlight;
    public int darktime;
    public int brighttime;

    //
    // T_StrobeFlash
    //
    public void StrobeFlash()
    {
        if (--count != 0)
        {
            return;
        }

        if (sector.lightlevel == minlight)
        {
            sector.lightlevel = (short) maxlight;
            count = brighttime;
        } else
        {
            sector.lightlevel = (short) minlight;
            count = darktime;
        }

    }

    
    public void read(Stream f)  
    {

        super.read(f); // Call thinker reader first            
        sectorid = DoomIO.readLEInt(f); // Sector index
        count = DoomIO.readLEInt(f);
        maxlight = DoomIO.readLEInt(f);
        minlight = DoomIO.readLEInt(f);
        darktime = DoomIO.readLEInt(f);
        brighttime = DoomIO.readLEInt(f);
    }

    
    public void pack(MemoryStream b)  
    {
        super.pack(b); //12            
        b.putInt(sectorid); // 16
        b.putInt(count); //20
        b.putInt(maxlight);//24
        b.putInt(minlight);//28
        b.putInt(darktime);//32
        b.putInt(brighttime);//36
    }
}
