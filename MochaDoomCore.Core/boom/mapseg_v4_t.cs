using System.IO;
using w;

namespace boom
{
    /**
 * LineSeg, generated by splitting LineDefs
 * using partition lines selected by BSP builder.
 * MAES: this is the ON-DISK structure. The corresponding memory structure,
 * segs_t, has fixed_t members.
 */

    public class mapseg_v4_t : CacheableDoomObject
    {
        public int v1;
        public int v2;
        public char angle;
        public char linedef;
        public char side;
        public char offset;

        public mapseg_v4_t()
        {
        }

        public static int sizeOf()
        {
            return 16;
        }

        public void unpack(MemoryStream buf)
        {
            buf.order(ByteOrder.LITTLE_ENDIAN);
            v1 = buf.getInt();
            v2 = buf.getInt();
            angle = buf.getChar();
            linedef = buf.getChar();
            side = buf.getChar();
            offset = buf.getChar();

        }
    }
}