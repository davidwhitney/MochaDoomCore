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

    @Override
    public void Init()
    {
        disk = DOOM.wadLoader.CachePatchName(diskname);
    }

    @Override
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

    @Override
    public bool isReading()
    {
        return timer > 0;
    }

    @Override
    public void setReading(int reading)
    {
        timer = reading;
    }

    @Override
    public bool justDoneReading()
    {
        return timer == 0;
    }

}
