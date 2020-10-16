using System.IO;
using w;

namespace boom
{


    /**
 * fixed 32 bit gl_vert format v2.0+ (glBsp 1.91)
 */

    public class mapglvertex_t : CacheableDoomObject
    {
        public int x, y; // fixed_t

        public static int sizeOf()
        {
            return 8;
        }

        public void unpack(MemoryStream buf)
        {
            buf.order(ByteOrder.LITTLE_ENDIAN);
            x = buf.getInt();
            y = buf.getInt();
        }
    }
}