namespace automap {  

using m.fixed_t;

public class mpoint_t
{
    /**
     * fixed_t
     */
    public int x, y;

    public mpoint_t(fixed_t x, fixed_t y)
    {
        this.x = x.val;
        this.y = y.val;
    }

    public mpoint_t(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public mpoint_t(double x, double y)
    {
        this.x = (int) x;
        this.y = (int) y;
    }

    public mpoint_t()
    {
        x = 0;
        y = 0;
    }

    public String toString()
    {
        return Integer.toHexString(x) + " , " + Integer.toHexString(y);
    }
}
