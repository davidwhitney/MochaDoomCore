namespace p {  

using w.CacheableDoomObject;
using w.DoomBuffer;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;
//
// P_SWITCH
//

public class switchlist_t
        : CacheableDoomObject
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
    public void unpack(MemoryStream buf)
             
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