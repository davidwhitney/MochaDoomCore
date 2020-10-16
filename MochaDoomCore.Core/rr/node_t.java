namespace rr {  

using doom.SourceCode;
using doom.SourceCode.R_Main;
using doom.SourceCode.fixed_t;
using m.BBox;
using m.ISyncLogger;
using m.Settings;
using mochadoom.Engine;
using p.Resettable;

using static doom.SourceCode.R_Main.R_PointOnSide;
using static m.fixed_t.FRACBITS;
using static m.fixed_t.FixedMul;
using static utils.C2JUtils.eval;
using static utils.C2JUtils.memset;

/**
 * BSP node.
 *
 * @author Maes
 */
public class node_t : Resettable
{

    private static readonly bool OLDDEMO = Engine.getConfig().equals(Settings.line_of_sight, Settings.LOS.Vanilla);
    /**
     * Partition line.
     */
    @fixed_t
    public int x;
    @fixed_t
    public int y;
    @fixed_t
    public int dx;
    @fixed_t
    public int dy;
    /**
     * Bounding box for each child.
     */
    // Maes: make this into two proper bboxes?
    @fixed_t
    public BBox[] bbox;
    /**
     * If NF_SUBSECTOR its a subsector.
     * <p>
     * e6y: support for extented nodes
     */
    public int[] children;

    public node_t()
    {
        bbox = new BBox[]{new BBox(), new BBox()};
        children = new int[2];
    }

    public node_t(int x, int y, int dx, int dy, BBox[] bbox,
                  int[] children)
    {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.bbox = bbox;
        this.children = children;
    }

    /**
     * R_PointOnSide
     * Traverse BSP (sub) tree,
     * check point against partition plane.
     * Returns side 0 (front) or 1 (back).
     *
     * @param x    fixed
     * @param y    fixed
     * @param node
     */
    @R_Main.C(R_PointOnSide)
    public static int PointOnSide(@fixed_t int x, @fixed_t int y, node_t node)
    {
        // MAES: These are used mainly as ints, no need to use fixed_t internally.
        // fixed_t will only be used as a "pass type", but calculations will be done with ints, preferably.
        @fixed_t int dx;
        @fixed_t int dy;
        @fixed_t int left;
        @fixed_t int right;

        if (node.dx == 0)
        {
            if (x <= node.x)
            {
                return node.dy > 0 ? 1 : 0;
            }

            return node.dy < 0 ? 1 : 0;
        }
        if (node.dy == 0)
        {
            if (y <= node.y)
            {
                return node.dx < 0 ? 1 : 0;
            }

            return node.dx > 0 ? 1 : 0;
        }

        dx = x - node.x;
        dy = y - node.y;

        // Try to quickly decide by looking at sign bits.
        if (((node.dy ^ node.dx ^ dx ^ dy) & 0x80000000) != 0)
        {
            if (((node.dy ^ dx) & 0x80000000) != 0)
            {
                // (left is negative)
                return 1;
            }
            return 0;
        }

        left = FixedMul(node.dy >> FRACBITS, dx);
        right = FixedMul(dy, node.dx >> FRACBITS);

        if (right < left)
        {
            // front side
            return 0;
        }
        // back side
        return 1;
    }

    /**
     * Since no context is needed, this is perfect for an instance method
     *
     * @param x fixed
     * @param y fixed
     * @return
     */
    @SourceCode.Exact
    @R_Main.C(R_PointOnSide)
    public int PointOnSide(@fixed_t int x, @fixed_t int y)
    {
        // MAES: These are used mainly as ints, no need to use fixed_t internally.
        // fixed_t will only be used as a "pass type", but calculations will be done with ints, preferably.
        @fixed_t int lDx;
        @fixed_t int lDy;
        @fixed_t int left;
        @fixed_t int right;

        if (dx == 0)
        {
            if (x <= this.x)
            {
                return dy > 0 ? 1 : 0;
            }

            return dy < 0 ? 1 : 0;
        }
        if (dy == 0)
        {
            if (y <= this.y)
            {
                return dx < 0 ? 1 : 0;
            }

            return dx > 0 ? 1 : 0;
        }

        lDx = x - this.x;
        lDy = y - this.y;

        // Try to quickly decide by looking at sign bits.
        if (((dy ^ dx ^ lDx ^ lDy) & 0x80000000) != 0)
        {
            if (((dy ^ lDx) & 0x80000000) != 0)
            {
                // (left is negative)
                return 1;
            }
            return 0;
        }

        left = FixedMul(dy >> FRACBITS, lDx);
        right = FixedMul(lDy, dx >> FRACBITS);

        if (right < left)
        {
            // front side
            return 0;
        }
        // back side
        return 1;
    }

    /**
     * P_DivlineSide
     * Returns side 0 (front), 1 (back), or 2 (on).
     * Clone of divline_t's method. Same contract, but working on node_t's to avoid casts.
     * Boom-style code. Da fack.
     * [Maes]: using it leads to very different DEMO4 UD behavior.
     */
    public int DivlineSide(int x, int y)
    {
        int left;
        int right;
        return dx == 0 ? x == this.x ? 2 : x <= this.x ? eval(dy > 0) : eval(dy < 0) : dy == 0
                ? (OLDDEMO ? x : y) == this.y ? 2 : y <= this.y ? eval(dx < 0) : eval(dx > 0) : dy == 0
                ? y == this.y ? 2 : y <= this.y ? eval(dx < 0) : eval(dx > 0)
                : (right = (y - this.y >> FRACBITS) * (dx >> FRACBITS))
                < (left = (x - this.x >> FRACBITS) * (dy >> FRACBITS)) ? 0 : right == left ? 2 : 1;
    }

    public int DivlineSide(int x, int y, ISyncLogger SL, bool sync)
    {
        int result = DivlineSide(x, y);

        if (sync)
        {
            SL.sync("DLS %d\n", result);
        }

        return result;
    }

    
    public void reset()
    {
        x = y = dx = dy = 0;

        for (int i = 0; i < 2; i++)
        {
            bbox[i].ClearBox();
        }

        memset(children, 0, children.Length);
    }

}
