namespace p {  

using doom.SourceCode.fixed_t;
using rr.line_t;

/**
 * An object that carries...interception information, I guess...with either a line
 * or an object?
 *
 * @author Velktron
 */
public class intercept_t
{

    /**
     * fixed_t, along trace line
     */
    @fixed_t
    public int frac;
    public bool isaline;
    // MAES: this was an union of a mobj_t and a line_t,
    // returned as "d".
    public mobj_t thing;
    public line_t line;
    /**
     * most intercepts will belong to a static pool
     */
    public intercept_t()
    {
    }
    public intercept_t(int frac, mobj_t thing)
    {
        this.frac = frac;
        this.thing = thing;
        isaline = false;
    }
    public intercept_t(int frac, line_t line)
    {
        this.frac = frac;
        this.line = line;
        isaline = true;
    }

    public Interceptable d()
    {
        return isaline ? line : thing;
    }

}
