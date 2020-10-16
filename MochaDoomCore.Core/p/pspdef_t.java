package p;

import data.state_t;
import w.DoomIO;
import w.IPackableDoomObject;
import w.IReadableDoomObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class pspdef_t implements IReadableDoomObject, IPackableDoomObject
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
    public void read(DataInputStream f) throws IOException
    {
        //state=data.info.states[f.readLEInt()];
        readstate = DoomIO.readLEInt(f);
        tics = DoomIO.readLEInt(f);
        sx = DoomIO.readLEInt(f);
        sy = DoomIO.readLEInt(f);
    }

    @Override
    public void pack(ByteBuffer f) throws IOException
    {
        if (state == null) f.putInt(0);
        else f.putInt(state.id);
        f.putInt(tics);
        f.putInt(sx);
        f.putInt(sy);
    }

}
