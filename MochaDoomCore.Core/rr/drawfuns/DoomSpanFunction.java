package rr.drawfuns;

using i.IDoomSystem;

public abstract class DoomSpanFunction<T, V> : SpanFunction<T, V>
{

    protected readonly bool RANGECHECK = false;
    protected readonly int SCREENWIDTH;
    protected readonly int SCREENHEIGHT;
    protected readonly int[] ylookup;
    protected readonly int[] columnofs;
    protected readonly V screen;
    protected readonly IDoomSystem I;
    protected SpanVars<T, V> dsvars;

    public DoomSpanFunction(int sCREENWIDTH, int sCREENHEIGHT,
                            int[] ylookup, int[] columnofs, SpanVars<T, V> dsvars, V screen, IDoomSystem I)
    {
        SCREENWIDTH = sCREENWIDTH;
        SCREENHEIGHT = sCREENHEIGHT;
        this.ylookup = ylookup;
        this.columnofs = columnofs;
        this.dsvars = dsvars;
        this.screen = screen;
        this.I = I;
    }

    protected readonly void doRangeCheck()
    {
        if (dsvars.ds_x2 < dsvars.ds_x1 || dsvars.ds_x1 < 0 || dsvars.ds_x2 >= SCREENWIDTH
                || dsvars.ds_y > SCREENHEIGHT)
        {
            I.Error("R_DrawSpan: %d to %d at %d", dsvars.ds_x1, dsvars.ds_x2, dsvars.ds_y);
        }
    }

    
    public  void invoke(SpanVars<T, V> dsvars)
    {
        this.dsvars = dsvars;
        invoke();
    }

}
