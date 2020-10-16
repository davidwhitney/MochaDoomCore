using System.IO;
using w;

namespace boom
{
    public class ZNodeSegs : CacheableDoomObject
    {

        private static readonly byte[] DeepBSPHeader =
        {
            (int)'x', (int)'N', (int)'d', (int)'4', 0, 0, 0, 0
        };

        byte[] header;
        mapseg_znod_t[] nodes;
        int numnodes;

        public bool formatOK()
        {
            return Arrays.equals(header, DeepBSPHeader);
        }

        public mapseg_znod_t[] getNodes()
        {
            return nodes;
        }


        public void unpack(MemoryStream buf)
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

            nodes = malloc(mapseg_znod_t::new, mapseg_znod_t[]::new, length);

            for (int i = 0; i < length; i++)
            {
                nodes[i].unpack(buf);
            }
        }
    }
}