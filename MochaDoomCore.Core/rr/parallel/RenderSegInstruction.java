package rr.parallel;

/**
 * This is all the information needed to draw a particular SEG.
 * It's quite a lot, actually, but much better than in testing
 * versions.
 */

public class RenderSegInstruction<V>
{
    public int rw_x;
    public int rw_stopx;
    public int toptexture;
    public int midtexture;
    public int bottomtexture;
    public int pixhigh;
    public int pixlow;
    public int pixhighstep;
    public int pixlowstep;
    public int topfrac;
    public int topstep;
    public int bottomfrac;
    public int bottomstep;
    public boolean segtextured;
    public boolean markfloor;
    public boolean markceiling;
    public long rw_centerangle; // angle_t
    /**
     * fixed_t
     */
    public int rw_offset;
    public int rw_distance;
    public int rw_scale;
    public int rw_scalestep;
    public int rw_midtexturemid;
    public int rw_toptexturemid;
    public int rw_bottomtexturemid;
    public int viewheight;
    public int centery;
    V[] walllights;

}
