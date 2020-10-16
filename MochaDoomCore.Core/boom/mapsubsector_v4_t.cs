using System.IO;
using w;

namespace boom
{
    public class mapsubsector_v4_t : CacheableDoomObject
    {

        public char numsegs;

        /**
     * Index of first one, segs are stored sequentially.
     */
        public int firstseg;

        public static int sizeOf()
        {
            return 6;
        }

        public void unpack(MemoryStream buf)
        {
            buf.order(ByteOrder.LITTLE_ENDIAN);
            numsegs = buf.getChar();
            firstseg = buf.getInt();
        }

    }
}