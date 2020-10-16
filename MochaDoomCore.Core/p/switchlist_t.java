package p;

import w.CacheableDoomObject;
import w.DoomBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//
// P_SWITCH
//

public class switchlist_t
        implements CacheableDoomObject
{

    // Were char[9]
    public String name1;
    public String name2;
    public short episode;

    switchlist_t(String name1, String name2, int episode)
    {
        this.name1 = name1;
        this.name2 = name2;
        this.episode = (short) episode;
    }

    public static int size()
    {
        return 20;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        // Like most Doom structs...
        buf.order(ByteOrder.LITTLE_ENDIAN);
        name1 = DoomBuffer.getNullTerminatedString(buf, 9);
        name2 = DoomBuffer.getNullTerminatedString(buf, 9);
        episode = buf.getShort();
    }

    public String toString()
    {
        return String.format("%s %s %d", name1, name2, episode);
    }
}