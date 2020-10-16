namespace p {  

using data.state_t;
using w.DoomIO;
using w.IPackableDoomObject;
using w.IReadableDoomObject;

using java.io.DataInputStream;
using java.io.IOException;
using java.nio.MemoryStream;

public class pspdef_t : IReadableDoomObject, IPackableDoomObject
{

    /**
     * a NULL state means not active
     */
    public state_t state;
    public int tics;
    /**
     * fixed_t
     */
    public int sx, sy;
    // When read from disk.
    public int readstate;
    public pspdef_t()
    {
        state = new state_t();
    }

    @Override
    public void read(DataInputStream f)  
    {
        //state=data.info.states[f.readLEInt()];
        readstate = DoomIO.readLEInt(f);
        tics = DoomIO.readLEInt(f);
        sx = DoomIO.readLEInt(f);
        sy = DoomIO.readLEInt(f);
    }

    @Override
    public void pack(MemoryStream f)  
    {
        if (state == null) f.putInt(0);
        else f.putInt(state.id);
        f.putInt(tics);
        f.putInt(sx);
        f.putInt(sy);
    }

}
