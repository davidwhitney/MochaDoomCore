namespace p {  

using rr.line_t;
using s.degenmobj_t;

public class button_t : Resettable
{

    public line_t line;
    public bwhere_e where;
    public int btexture;
    public int btimer;
    public degenmobj_t soundorg;

    public button_t()
    {
        btexture = 0;
        btimer = 0;
        where = bwhere_e.top;
    }

    public void reset()
    {
        line = null;
        where = bwhere_e.top;
        btexture = 0;
        btimer = 0;
        soundorg = null;

    }

}