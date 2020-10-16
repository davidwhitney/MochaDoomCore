namespace automap {  

using doom.SourceCode.AM_Map;
using doom.event_t;

using static doom.SourceCode.AM_Map.AM_Responder;
using static doom.SourceCode.AM_Map.AM_Stop;

public interface IAutoMap<T, V>
{
    // Used by ST StatusBar stuff.
    int AM_MSGHEADER = ('a' << 24) + ('m' << 16);
    int AM_MSGENTERED = AM_MSGHEADER | 'e' << 8;
    int AM_MSGEXITED = AM_MSGHEADER | 'x' << 8;

    // Color ranges for automap. Actual colors are bit-depth dependent.

    int REDRANGE = 16;
    int BLUERANGE = 8;
    int GREENRANGE = 16;
    int GRAYSRANGE = 16;
    int BROWNRANGE = 16;
    int YELLOWRANGE = 1;

    int YOURRANGE = 0;
    int WALLRANGE = REDRANGE;
    int TSWALLRANGE = GRAYSRANGE;
    int FDWALLRANGE = BROWNRANGE;
    int CDWALLRANGE = YELLOWRANGE;
    int THINGRANGE = GREENRANGE;
    int SECRETWALLRANGE = WALLRANGE;
    int GRIDRANGE = 0;

    // Called by main loop.
    @AM_Map.C(AM_Responder)
    bool Responder(event_t ev);

    // Called by main loop.
    void Ticker();

    // Called by main loop,
    // called instead of view drawer if automap active.
    void Drawer();

    // Added to be informed of gamma changes - Good Sign 2017/04/05
    void Repalette();

    // Called to force the automap to quit
    // if the level is completed while it is up.
    @AM_Map.C(AM_Stop)
    void Stop();

    void Start();
}
