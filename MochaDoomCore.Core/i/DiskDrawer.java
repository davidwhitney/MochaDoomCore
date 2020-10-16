namespace i {  

using doom.DoomMain;
using rr.patch_t;

using static v.renderers.DoomScreen.FG;

public class DiskDrawer : IDiskDrawer
{

    public static readonly String STDISK = "STDISK";
    public static readonly String STCDROM = "STCDROM";
    private DoomMain<?, ?> DOOM;
    private patch_t disk;
    private int timer = 0;
    private String diskname;

    public DiskDrawer(DoomMain<?, ?> DOOM, String icon)
    {
        this.DOOM = DOOM;
        diskname = icon;
    }

    
    public void Init()
    {
        disk = DOOM.wadLoader.CachePatchName(diskname);
    }

    
    public void Drawer()
    {
        if (timer > 0)
        {
            if (timer % 2 == 0)
                DOOM.graphicSystem.DrawPatchScaled(FG, disk, DOOM.vs, 304, 184);
        }
        if (timer >= 0)
            timer--;
    }

    
    public bool isReading()
    {
        return timer > 0;
    }

    
    public void setReading(int reading)
    {
        timer = reading;
    }

    
    public bool justDoneReading()
    {
        return timer == 0;
    }

}
