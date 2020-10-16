package rr.parallel;

using i.IDoomSystem;
using rr.IDetailAware;
using rr.drawfuns.*;
using v.tables.BlurryTable;

using java.util.concurrent.BrokenBarrierException;
using java.util.concurrent.CyclicBarrier;

/**
 * This is what actual executes the RenderWallInstruction. Essentially it's a
 * self-contained column rendering function.
 *
 * @author velktron
 */

public abstract class RenderMaskedExecutor<T, V>
        : Runnable, IDetailAware
{

    protected readonly int SCREENWIDTH;
    protected readonly int SCREENHEIGHT;
    protected CyclicBarrier barrier;
    protected ColVars<T, V>[] RMI;
    protected int rmiend;
    protected bool lowdetail = false;
    protected int start, end;
    protected DoomColumnFunction<T, V> colfunchi, colfunclow;
    protected DoomColumnFunction<T, V> fuzzfunchi, fuzzfunclow;
    protected DoomColumnFunction<T, V> transfunchi, transfunclow;
    protected DoomColumnFunction<T, V> colfunc;

    public RenderMaskedExecutor(int SCREENWIDTH, int SCREENHEIGHT,
                                ColVars<T, V>[] RMI, CyclicBarrier barrier
    )
    {
        this.RMI = RMI;
        this.barrier = barrier;
        this.SCREENWIDTH = SCREENWIDTH;
        this.SCREENHEIGHT = SCREENHEIGHT;
    }

    public void setRange(int start, int end)
    {
        this.end = end;
        this.start = start;
    }

    public void setDetail(int detailshift)
    {
        lowdetail = detailshift != 0;
    }

    public void run()
    {

        // System.out.println("Wall executor from "+start +" to "+ end);
        int dc_flags = 0;

        // Check out ALL valid RMIs, but only draw those on YOUR side of the screen.
        for (int i = 0; i < rmiend; i++)
        {

            if (RMI[i].dc_x >= start && RMI[i].dc_x <= end)
            {
                // Change function type according to flags.
                // No flag change means reusing the last used type
                dc_flags = RMI[i].dc_flags;
                //System.err.printf("Flags transition %d\n",dc_flags);
                if (lowdetail)
                {
                    if ((dc_flags & DcFlags.FUZZY) != 0)
                        colfunc = fuzzfunclow;
                    else if ((dc_flags & DcFlags.TRANSLATED) != 0)
                        colfunc = transfunclow;
                    else
                        colfunc = colfunclow;
                } else
                {
                    if ((dc_flags & DcFlags.FUZZY) != 0)
                        colfunc = fuzzfunchi;
                    else if ((dc_flags & DcFlags.TRANSLATED) != 0)
                        colfunc = transfunchi;
                    else
                        colfunc = colfunchi;
                }

                // No need to set shared DCvars, because it's passed with the arg.
                colfunc.invoke(RMI[i]);
            }
        }

        try
        {
            barrier.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (BrokenBarrierException e)
        {
            e.printStackTrace();
        }
    }

    /////////////// VIDEO SCALE STUFF//////////////////////

    public void setRMIEnd(int rmiend)
    {
        this.rmiend = rmiend;
    }

    public void updateRMI(ColVars<T, V>[] RMI)
    {
        this.RMI = RMI;

    }
    /*
     * protected IVideoScale vs;
     *  public void setVideoScale(IVideoScale vs) { this.vs=vs; }
     *  public void initScaling() {
     * this.SCREENHEIGHT=vs.getScreenHeight();
     * this.SCREENWIDTH=vs.getScreenWidth(); }
     */

    public static readonly class HiColor : RenderMaskedExecutor<byte[], short[]>
    {

        public HiColor(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                       int[] ylookup, short[] screen, ColVars<byte[], short[]>[] RMI,
                       CyclicBarrier barrier, IDoomSystem I, BlurryTable BLURRY_MAP)
        {
            super(SCREENWIDTH, SCREENHEIGHT, RMI, barrier);

            // Regular masked columns
            colfunc = new R_DrawColumnBoom.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);
            colfunclow = new R_DrawColumnBoomLow.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);

            // Fuzzy columns
            fuzzfunchi = new R_DrawFuzzColumn.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I, BLURRY_MAP);
            fuzzfunclow = new R_DrawFuzzColumnLow.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I, BLURRY_MAP);

            // Translated columns
            transfunchi = new R_DrawTranslatedColumn.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);
            transfunclow = new R_DrawTranslatedColumnLow.HiColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);

        }

    }

    public static readonly class Indexed : RenderMaskedExecutor<byte[], byte[]>
    {

        public Indexed(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                       int[] ylookup, byte[] screen, ColVars<byte[], byte[]>[] RMI,
                       CyclicBarrier barrier, IDoomSystem I, BlurryTable BLURRY_MAP)
        {
            super(SCREENWIDTH, SCREENHEIGHT, RMI, barrier);

            // Regular masked columns
            colfunc = new R_DrawColumnBoom.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);
            colfunclow = new R_DrawColumnBoomLow.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);

            // Fuzzy columns
            fuzzfunchi = new R_DrawFuzzColumn.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I, BLURRY_MAP);
            fuzzfunclow = new R_DrawFuzzColumnLow.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I, BLURRY_MAP);

            // Translated columns
            transfunchi = new R_DrawTranslatedColumn.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);
            transfunclow = new R_DrawTranslatedColumnLow.Indexed(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);

        }

    }

    public static readonly class TrueColor : RenderMaskedExecutor<byte[], int[]>
    {

        public TrueColor(int SCREENWIDTH, int SCREENHEIGHT, int[] columnofs,
                         int[] ylookup, int[] screen, ColVars<byte[], int[]>[] RMI,
                         CyclicBarrier barrier, IDoomSystem I, BlurryTable BLURRY_MAP)
        {
            super(SCREENWIDTH, SCREENHEIGHT, RMI, barrier);

            // Regular masked columns
            colfunc = new R_DrawColumnBoom.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);
            colfunclow = new R_DrawColumnBoomLow.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);

            // Fuzzy columns
            fuzzfunchi = new R_DrawFuzzColumn.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I, BLURRY_MAP);
            fuzzfunclow = new R_DrawFuzzColumnLow.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I, BLURRY_MAP);

            // Translated columns
            transfunchi = new R_DrawTranslatedColumn.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);
            transfunclow = new R_DrawTranslatedColumnLow.TrueColor(SCREENWIDTH, SCREENHEIGHT, ylookup, columnofs, null, screen, I);


        }

    }

}
