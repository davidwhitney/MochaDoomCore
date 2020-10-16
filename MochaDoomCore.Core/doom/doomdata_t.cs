using System;
using System.IO;
using System.Text;
using data.Defines;
using w.DoomBuffer;

namespace doom
{
    public class doomdata_t : IDatagramSerializable
    {

        public static int DOOMDATALEN = 8 + Defines.BACKUPTICS * ticcmd_t.TICCMDLEN;

        // High bit is retransmit request.
        /**
     * MAES: was "unsigned"
     */
        public int checksum;

        /*
         * CAREFUL!!! Those "bytes" are actually unsigned
         */

        /**
     * Only valid if NCMD_RETRANSMIT.
     */
        public byte retransmitfrom;

        public byte starttic;
        public byte player;
        public byte numtics;
        public ticcmd_t[] cmds;

        StringBuilder sb = new StringBuilder();

        // Used for datagram serialization.
        private byte[] buffer;
        private MemoryStream bbuf;

        public doomdata_t()
        {
            cmds = malloc(ticcmd_t::new, ticcmd_t[]::new, Defines.BACKUPTICS);
            // Enough space for its own header + the ticcmds;
            buffer = new byte[DOOMDATALEN];
            // This "pegs" the ByteBuffer to this particular array.
            // Separate updates are not necessary.
            bbuf = new MemoryStream(buffer);
        }

        public byte[] pack()
        {
            bbuf.Position = 0;

            // Why making it harder?
            bbuf.putInt(checksum);
            bbuf.put(retransmitfrom);
            bbuf.put(starttic);
            bbuf.put(player);
            bbuf.put(numtics);

            // FIXME: it's probably more efficient to use System.arraycopy ?
            // Or are the packets too small anyway? At most we'll be sending "doomdata_t's"

            for (int i = 0; i < cmds.length; i++)
            {
                bbuf.put(cmds[i].pack());
            }

            return bbuf.array();
        }

        public void pack(byte[] buf, int offset)
        {
            // No need to make it harder...just pack it and slap it in.
            byte[] tmp = pack();
            System.arraycopy(tmp, 0, buf, offset, tmp.length);
        }

        public void unpack(byte[] buf)
        {
            unpack(buf, 0);
        }

        public void unpack(byte[] buf, int offset)
        {
            checksum = DoomBuffer.getBEInt(buf);
            offset = +4;
            retransmitfrom = buf[offset++];
            starttic = buf[offset++];
            player = buf[offset++];
            numtics = buf[offset++];

            for (int i = 0; i < cmds.length; i++)
            {
                cmds[i].unpack(buf, offset);
                offset += ticcmd_t.TICCMDLEN;
            }
        }

        public void selfUnpack()
        {
            unpack(buffer);
        }

        public void copyFrom(doomdata_t source)
        {
            checksum = source.checksum;
            numtics = source.numtics;
            player = source.player;
            retransmitfrom = source.retransmitfrom;
            starttic = source.starttic;

            // MAES: this was buggy as hell, and didn't work at all, which
            // in turn prevented other subsystems such as speed throttling and
            // networking to work.
            //
            // This should be enough to alter the ByteBuffer too.
            // System.arraycopy(source.cached(), 0, this.buffer, 0, DOOMDATALEN);
            // This should set all fields
            // selfUnpack();
        }

        public byte[] cached()
        {
            return buffer;
        }

        public String toString()
        {
            sb.Clear();
            sb.Append("doomdata_t ");
            sb.Append(retransmitfrom);
            sb.Append(" starttic ");
            sb.Append(starttic);
            sb.Append(" player ");
            sb.Append(player);
            sb.Append(" numtics ");
            sb.Append(numtics);
            return sb.ToString();
        }

    }
}