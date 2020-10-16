package rr.drawfuns;

using i.IDoomSystem;
using v.tables.BlurryTable;

/**
 * Prototype for
 *
 * @param <T>
 * @author velktron
 */

public abstract class DoomColumnFunction<T, V> : ColumnFunction<T, V>
{

    protected readonly bool RANGECHECK = false;
    protected readonly int SCREENWIDTH;
    protected readonly int SCREENHEIGHT;
    protected readonly V screen;
    protected readonly IDoomSystem I;
    protected readonly int[] ylookup;
    protected readonly int[] columnofs;
    protected ColVars<T, V> dcvars;
    protected BlurryTable blurryTable;
    protected int flags;

    public DoomColumnFunction(int sCREENWIDTH, int sCREENHEIGHT, int[] ylookup,
                              int[] columnofs, ColVars<T, V> dcvars, V screen, IDoomSystem I)
    {
        SCREENWIDTH = sCREENWIDTH;
        SCREENHEIGHT = sCREENHEIGHT;
        this.ylookup = ylookup;
        this.columnofs = columnofs;
        this.dcvars = dcvars;
        this.screen = screen;
        this.I = I;
        blurryTable = null;
    }

    public DoomColumnFunction(int sCREENWIDTH, int sCREENHEIGHT, int[] ylookup,
                              int[] columnofs, ColVars<T, V> dcvars, V screen, IDoomSystem I, BlurryTable BLURRY_MAP)
    {
        SCREENWIDTH = sCREENWIDTH;
        SCREENHEIGHT = sCREENHEIGHT;
        this.ylookup = ylookup;
        this.columnofs = columnofs;
        this.dcvars = dcvars;
        this.screen = screen;
        this.I = I;
        blurryTable = BLURRY_MAP;
    }

    protected readonly void performRangeCheck()
    {
        if (dcvars.dc_x >= SCREENWIDTH || dcvars.dc_yl < 0 || dcvars.dc_yh >= SCREENHEIGHT)
            I.Error("R_DrawColumn: %d to %d at %d", dcvars.dc_yl, dcvars.dc_yh, dcvars.dc_x);
    }

    /**
     * Use ylookup LUT to avoid multiply with ScreenWidth.
     * Use columnofs LUT for subwindows?
     *
     * @return Framebuffer destination address.
     */

    protected readonly int computeScreenDest()
    {
        return ylookup[dcvars.dc_yl] + columnofs[dcvars.dc_x];
    }

    protected readonly int blockyDest1()
    {
        return ylookup[dcvars.dc_yl] + columnofs[dcvars.dc_x << 1];
    }

    protected readonly int blockyDest2()
    {
        return ylookup[dcvars.dc_yl] + columnofs[(dcvars.dc_x << 1) + 1];
    }

    
    public  void invoke(ColVars<T, V> dcvars)
    {
        this.dcvars = dcvars;
        invoke();
    }

    
    public  int getFlags()
    {
        return flags;
    }

}
