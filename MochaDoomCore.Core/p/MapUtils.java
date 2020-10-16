namespace p {  

using static m.fixed_t.FixedDiv;
using static m.fixed_t.FixedMul;
using static utils.C2JUtils.eval;

public class MapUtils
{
    /**
     * AproxDistance
     * Gives an estimation of distance (not exact)
     *
     * @param dx fixed_t
     * @param dy fixed_t
     * @return fixed_t
     */
    //
    public static int
    AproxDistance
    (int dx,
     int dy)
    {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx < dy)
            return dx + dy - (dx >> 1);
        return dx + dy - (dy >> 1);
    }

    /**
     * P_InterceptVector
     * Returns the fractional intercept point
     * along the first divline.
     * This is only called by the addthings
     * and addlines traversers.
     *
     * @return int to be treated as fixed_t
     */

    public static int InterceptVector(divline_t v2, divline_t v1)
    {
        int frac;
        int num;
        int den;

        den = FixedMul(v1.dy >> 8, v2.dx) - FixedMul(v1.dx >> 8, v2.dy);

        if (den == 0)
            return 0;

        num =
                FixedMul(v1.x - v2.x >> 8, v1.dy)
                        + FixedMul(v2.y - v1.y >> 8, v1.dx);

        frac = FixedDiv(num, den);

        return frac;

    }

    /**
     * Used by CrossSubSector
     *
     * @param v2
     * @param v1
     * @return
     */
    public static int P_InterceptVector(divline_t v2, divline_t v1)
    {
        /* cph - This was introduced at prboom_4_compatibility - no precision/overflow problems */
        var den = (long) v1.dy * v2.dx - (long) v1.dx * v2.dy;
        den >>= 16;
        if (!eval(den))
            return 0;
        return (int) (((long) (v1.x - v2.x) * v1.dy - (long) (v1.y - v2.y) * v1.dx) / den);
    }
}
