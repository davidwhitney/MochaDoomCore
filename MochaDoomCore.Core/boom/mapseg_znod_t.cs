using System.IO;
using w;

namespace boom
{


    /**
 * ZDoom node?
 */

    public class mapseg_znod_t : CacheableDoomObject
    {

        public int v1, v2; // Those are unsigned :-/
        public char linedef;
        public byte side;

        public mapseg_znod_t()
        {

        }

        public static int sizeOf()
        {
            return 11;
        }

        public void unpack(MemoryStream buf)
        {
            buf.order(ByteOrder.LITTLE_ENDIAN);
            v1 = buf.getInt();
            v2 = buf.getInt();
            linedef = buf.getChar();
            side = buf.get();
        }

    }
}