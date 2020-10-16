namespace timing {  

public class FastTicker : ITicker
{

    protected volatile int fasttic = 0;

    /**
     * I_GetTime
     * returns time in 1/70th second tics
     */
    @Override
    public int GetTime()
    {
        return fasttic++;
    }
}