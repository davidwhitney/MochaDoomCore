using w;

namespace boom {

public class DeepBSPNodesV4 : CacheableDoomObject
{

    public static readonly byte[] DeepBSPHeader = {
            'x', 'N', 'd', '4', 0, 0, 0, 0
    };

    byte[] header = new byte[8];
    mapnode_v4_t[] nodes;
    int numnodes;

    public boolean formatOK()
    {
        return Arrays.equals(header, DeepBSPHeader);
    }

    public mapnode_v4_t[] getNodes()
    {
        return nodes;
    }

    public void unpack(ByteBuffer buf) throws IOException
    {
        int length = buf.capacity();

        // Too short, not even header.
        if (length < 8)
        {
            return;
        }

        numnodes = (length - 8) / mapnode_v4_t.sizeOf();

        if (length < 1)
        {
            return;
        }

        buf.get(header); // read header

        nodes = malloc(mapnode_v4_t::new, mapnode_v4_t[]::new, length);

        for (int i = 0; i < length; i++)
        {
            nodes[i].unpack(buf);
        }
    }
}
    }