namespace rr {  

using doom.DoomMain;
using i.IDoomSystem;
using rr.RendererState.IPlaneDrawer;
using rr.drawfuns.SpanVars;
using v.scale.VideoScale;
using v.tables.LightsAndColors;

using static data.Tables.*;
using static m.fixed_t.FixedMul;

public abstract class PlaneDrawer<T, V> : IPlaneDrawer
{

    private static readonly bool DEBUG2 = false;


    protected readonly bool RANGECHECK = false;
    protected readonly ViewVars view;
    protected readonly SegVars seg_vars;
    protected readonly SpanVars<T, V> dsvars;
    protected readonly LightsAndColors<V> colormap;
    protected readonly TextureManager<T> TexMan;
    protected readonly IDoomSystem I;
    protected readonly VideoScale vs;
    //
    // spanstart holds the start of a plane span
    // initialized to 0 at start
    //
    protected int[] spanstart, spanstop;
    //
    // texture mapping
    //
    protected V[] planezlight; // The distance lighting effect you see
    /**
     * To treat as fixed_t
     */
    protected int planeheight;
    /**
     * To treat as fixed_t
     */
    protected int[] distscale;
    /**
     * To treat as fixed_t
     */
    protected int[] cacheddistance, cachedxstep, cachedystep;
    /**
     * The visplane data. Set separately. For threads, use the same for
     * everyone.
     */
    protected Visplanes vpvars;


    protected PlaneDrawer(DoomMain<T, V> DOOM, SceneRenderer<T, V> R)
    {
        view = R.getView();
        vpvars = R.getVPVars();
        dsvars = R.getDSVars();
        seg_vars = R.getSegVars();
        colormap = R.getColorMap();
        TexMan = R.getTextureManager();
        I = R.getDoomSystem();
        vs = DOOM.vs;
        // Pre-scale stuff.

        spanstart = new int[vs.getScreenHeight()];
        spanstop = new int[vs.getScreenHeight()];
        distscale = new int[vs.getScreenWidth()];
        cacheddistance = new int[vs.getScreenHeight()];
        cachedxstep = new int[vs.getScreenHeight()];
        cachedystep = new int[vs.getScreenHeight()];

        // HACK: visplanes are initialized globally.
        visplane_t.setVideoScale(vs);
        vpvars.initVisplanes();
    }

    /**
     * R_MapPlane
     * <p>
     * Called only by R_MakeSpans.
     * <p>
     * This is where the actual span drawing function is called.
     * <p>
     * Uses global vars: planeheight ds_source -> flat data has already been
     * set. basexscale -> actual drawing angle and position is computed from
     * these baseyscale viewx viewy
     * <p>
     * BASIC PRIMITIVE
     */

    public void MapPlane(int y, int x1, int x2)
    {
        // MAES: angle_t
        int angle;
        // fixed_t
        int distance;
        int.Length;
        int index;

        if (RANGECHECK)
        {
            rangeCheck(x1, x2, y);
        }

        if (planeheight != vpvars.cachedheight[y])
        {
            vpvars.cachedheight[y] = planeheight;
            distance = cacheddistance[y] = FixedMul(planeheight, vpvars.yslope[y]);
            dsvars.ds_xstep = cachedxstep[y] = FixedMul(distance, vpvars.basexscale);
            dsvars.ds_ystep = cachedystep[y] = FixedMul(distance, vpvars.baseyscale);
        } else
        {
            distance = cacheddistance[y];
            dsvars.ds_xstep = cachedxstep[y];
            dsvars.ds_ystep = cachedystep[y];
        }

       .Length = FixedMul(distance, distscale[x1]);
        angle = (int) ((view.angle + view.xtoviewangle[x1] & BITS32) >>> ANGLETOFINESHIFT);
        dsvars.ds_xfrac = view.x + FixedMul(finecosine[angle],.Length);
        dsvars.ds_yfrac = -view.y - FixedMul(finesine[angle],.Length);

        if (colormap.fixedcolormap != null)
            dsvars.ds_colormap = colormap.fixedcolormap;
        else
        {
            index = distance >>> colormap.lightZShift();

            if (index >= colormap.maxLightZ())
                index = colormap.maxLightZ() - 1;

            dsvars.ds_colormap = planezlight[index];
        }

        dsvars.ds_y = y;
        dsvars.ds_x1 = x1;
        dsvars.ds_x2 = x2;

        // high or low detail
        dsvars.spanfunc.invoke();
    }

    protected readonly void rangeCheck(int x1, int x2, int y)
    {
        if (x2 < x1 || x1 < 0 || x2 >= view.width || y > view.height)
            I.Error("%s: %d, %d at %d", getClass().getName(), x1, x2, y);
    }


    /**
     * R_MakeSpans
     * <p>
     * Called only by DrawPlanes. If you wondered where the actual
     * boundaries for the visplane flood-fill are laid out, this is it.
     * <p>
     * The system of coords seems to be defining a sort of cone.
     *
     * @param x  Horizontal position
     * @param t1 Top-left y coord?
     * @param b1 Bottom-left y coord?
     * @param t2 Top-right y coord ?
     * @param b2 Bottom-right y coord ?
     */

    protected void MakeSpans(int x, int t1, int b1, int t2, int b2)
    {

        // If t1 = [sentinel value] then this part won't be executed.
        while (t1 < t2 && t1 <= b1)
        {
            MapPlane(t1, spanstart[t1], x - 1);
            t1++;
        }
        while (b1 > b2 && b1 >= t1)
        {
            MapPlane(b1, spanstart[b1], x - 1);
            b1--;
        }

        // So...if t1 for some reason is < t2, we increase t2 AND store the
        // current x
        // at spanstart [t2] :-S
        while (t2 < t1 && t2 <= b2)
        {
            // System.out.println("Increasing t2");
            spanstart[t2] = x;
            t2++;
        }

        // So...if t1 for some reason b2 > b1, we decrease b2 AND store the
        // current x
        // at spanstart [t2] :-S

        while (b2 > b1 && b2 >= t2)
        {
            // System.out.println("Decreasing b2");
            spanstart[b2] = x;
            b2--;
        }
    }

    /**
     * R_InitPlanes Only at game startup.
     */

    
    public void InitPlanes()
    {
        // Doh!
    }

    protected readonly void rangeCheckErrors()
    {
        if (seg_vars.ds_p > seg_vars.MAXDRAWSEGS)
            I.Error("R_DrawPlanes: drawsegs overflow (%d)", seg_vars.ds_p);

        if (vpvars.lastvisplane > vpvars.MAXVISPLANES)
            I.Error(" R_DrawPlanes: visplane overflow (%d)",
                    vpvars.lastvisplane);

        if (vpvars.lastopening > vpvars.MAXOPENINGS)
            I.Error("R_DrawPlanes: opening overflow (%d)", vpvars.lastopening);
    }

    /**
     * Default implementation which DOES NOTHING. MUST OVERRIDE
     */

    public void DrawPlanes()
    {

    }

    public void sync()
    {
        // Nothing required if serial.
    }

    /////////////// VARIOUS BORING GETTERS ////////////////////


    
    public int[] getDistScale()
    {
        return distscale;
    }

}
