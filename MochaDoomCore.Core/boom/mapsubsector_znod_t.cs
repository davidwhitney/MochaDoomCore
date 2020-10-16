using System.IO;
using w;

namespace boom
{

    public class mapsubsector_znod_t : CacheableDoomObject
    {

        public long numsegs;

        public static int sizeOf()
        {
            return 4;
        }

        public void unpack(MemoryStream buf)
        {
            buf.order(ByteOrder.LITTLE_ENDIAN);
            numsegs = C2JUtils.unsigned(buf.getInt());
        }

    }
}   