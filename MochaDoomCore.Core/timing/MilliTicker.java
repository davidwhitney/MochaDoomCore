namespace timing {  

using static data.Defines.TICRATE;

public class MilliTicker
        : ITicker
{

    protected volatile long basetime = 0;
    protected volatile int oldtics = 0;
    protected volatile int discrepancies;

    /**
     * I_GetTime
     * returns time in 1/70th second tics
     */

    
    public int GetTime()
    {
        long tp;
        //struct timezone   tzp;
        int newtics;

        tp = System.currentTimeMillis();
        if (basetime == 0)
        {
            basetime = tp;
        }
        newtics = (int) ((tp - basetime) * TICRATE / 1000);
        return newtics;
    }

}
