using System;
using System.IO;

namespace w
{
    /**
 * filelumps are on-disk structures. lumpinfos are almost the same, but are memory only.
 *
 * @author Maes
 */

    public class filelump_t : IReadableDoomObject, IWritableDoomObject
    {
        public long filepos;
        public long size; // Is INT 32-bit in file!
        public String name; // Whatever appears inside the wadfile
        public String actualname; // Sanitized name, e.g. after compression markers

        public bool big_endian = false; // E.g. Jaguar
        public bool compressed = false; // Compressed lump

        public static int @sizeof()
        {
            return 4 + 4 + 8;
        }

        public void read(Stream f)
        {
            // MAES: Byte Buffers actually make it convenient changing byte order on-the-fly.
            // But RandomAccessFiles (and inputsteams) don't :-S

            if (!big_endian)
            {
                filepos = DoomIO.readUnsignedLEInt(f);
                size = DoomIO.readUnsignedLEInt(f);

            }
            else
            {
                filepos = f.readInt();
                size = f.readInt();

            }

            // Names used in the reading subsystem should be upper case,
            // but check for compressed status first
            name = DoomIO.readNullTerminatedString(f, 8);


            char[] stuff = name.toCharArray();

            // It's a compressed lump
            if (stuff[0] > 0x7F)
            {
                compressed = true;
                stuff[0] &= 0x7F;
            }

            actualname = new String(stuff).ToUpper();


        }


        public void write(Stream dos)
        {
            if (!big_endian)
            {
                DoomIO.writeLEInt(dos, (int) filepos);
                DoomIO.writeLEInt(dos, (int) size);
            }
            else
            {
                dos.writeInt((int) filepos);
                dos.writeInt((int) size);
            }

            DoomIO.writeString(dos, name, 8);

        }

    }
}