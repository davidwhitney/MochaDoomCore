namespace w {  

/*
Copyright (C) 1997-2001 Id Software, Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

//Created on 24.07.2004 by RST.

//$Id: DoomIO.java,v 1.3 2013/06/03 10:30:20 velktron Exp $

using m.Swap;

using java.io.DataInputStream;
using java.io.DataOutputStream;
using java.io.IOException;
using java.io.InputStream;
using java.nio.ByteOrder;
using java.nio.charset.Charset;
using java.nio.charset.StandardCharsets;
using java.util.List;

/**
 * An extension of RandomAccessFile, which handles readString/WriteString specially
 * and offers several Doom related (and cross-OS) helper functions for reading/writing
 * arrays of multiple objects or fixed-length strings from/to disk.
 * <p>
 * TO DEVELOPERS: this is the preferrered method of I/O for anything implemented.
 * In addition, Doomfiles can be passed to objects implementing the IReadableDoomObject
 * and IWritableDoomObject interfaces, which will "autoread" or "autowrite" themselves
 * to the implied stream.
 * <p>
 * TODO: in the light of greater future portabililty and compatibility in certain
 * environments, PERHAPS this should have been implemented using Streams. Perhaps
 * it's possible to change the underlying implementation without (?) changing too
 * much of the exposed interface, but it's not a priority for me right now.
 */
public class DoomIO
{

    private DoomIO()
    {

    }

    /**
     * Writes a Vector to a RandomAccessFile.
     */
    public static void writeVector(DataOutputStream dos, float[] v)  
    {
        for (int n = 0; n < 3; n++)
        {
            dos.writeFloat(v[n]);
        }
    }

    /**
     * Writes a Vector to a RandomAccessFile.
     */
    public static float[] readVector(DataInputStream dis)  
    {
        float[] res = {0, 0, 0};
        for (int n = 0; n < 3; n++)
        {
            res[n] = dis.readFloat();
        }

        return res;
    }

    /**
     * Reads a length specified string from a file.
     */
    public static string readString(DataInputStream dis)  
    {
        int len = dis.readInt();

        if (len == -1)
            return null;

        if (len == 0)
            return "";

        byte[] bb = new byte[len];

        dis.read(bb, 0, len);

        return new String(bb, 0, len, Charset.forName("ISO-8859-1"));
    }

    /**
     * MAES: Reads a specified number of bytes from a file into a new String.
     * With many lengths being implicit, we need to actually take the loader by the hand.
     *
     * @param len
     * @return
     * @ 
     */

    public static string readString(DataInputStream dis, int len)  
    {

        if (len == -1)
            return null;

        if (len == 0)
            return "";

        byte[] bb = new byte[len];

        dis.read(bb, 0, len);

        return new String(bb, 0, len);
    }

    public static string readString(InputStream f, int len)  
    {

        if (len == -1)
            return null;

        if (len == 0)
            return "";

        byte[] bb = new byte[len];

        f.read(bb, 0, len);

        return new String(bb, 0, len, Charset.forName("ISO-8859-1"));
    }

    /**
     * MAES: Reads a specified number of bytes from a file into a new, NULL TERMINATED String.
     * With many lengths being implicit, we need to actually take the loader by the hand.
     *
     * @param len
     * @return
     * @ 
     */

    public static string readNullTerminatedString(InputStream dis, int len)  
    {

        if (len == -1)
            return null;

        if (len == 0)
            return "";

        byte[] bb = new byte[len];
        int terminator = len;

        dis.read(bb, 0, len);

        for (int i = 0; i < bb.length; i++)
        {
            if (bb[i] == 0)
            {
                terminator = i;
                break; // stop on first null
            }

        }

        // This is the One True Encoding for Doom.
        return new String(bb, 0, terminator, Charset.forName("ISO-8859-1"));
    }

    /**
     * MAES: Reads multiple strings with a specified number of bytes from a file.
     * If the array is not large enough, only partial reads will occur.
     *
     * @param len
     * @return
     * @ 
     */

    public static string[] readMultipleFixedLengthStrings(DataInputStream dis, String[] dest, int num, int len)  
    {

        // Some sanity checks...
        if (num <= 0 || len < 0)
            return null;

        if (len == 0)
        {
            for (int i = 0; i < dest.length; i++)
            {
                dest[i] = "";
            }
            return dest;
        }

        for (int i = 0; i < num; i++)
        {
            dest[i] = readString(dis, len);
        }
        return dest;
    }


    /**
     * Writes a length specified string (Pascal style) to a file.
     */
    public static void writeString(DataOutputStream dos, String s)
    {
        try
        {
            if (s == null)
            {
                dos.writeInt(-1);
                return;
            }

            dos.writeInt(s.length());
            if (s.length() != 0)
                dos.writeBytes(s);
        }
        catch (Exception e)
        {
            System.err.println("writeString " + s + " to DoomFile failed!");
        }
    }

    /**
     * Writes a String with a specified len to a file.
     * This is useful for fixed-size String fields in
     * files. Any leftover space will be filled with 0x00s.
     *
     * @param s
     * @param len
     * @ 
     */

    public static void writeString(DataOutputStream dos, String s, int len)  
    {

        if (s == null) return;

        if (s.length() != 0)
        {
            byte[] dest = s.getBytes(StandardCharsets.ISO_8859_1);
            dos.write(dest, 0, Math.min(len, dest.length));
            // Fill in with 0s if something's left.
            if (dest.length < len)
            {
                for (int i = 0; i < len - dest.length; i++)
                {
                    dos.write((byte) 0x00);
                }
            }
        }
    }

    public static void readObjectArray(DataInputStream dis, IReadableDoomObject[] s, int len)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            s[i].read(dis);
        }
    }

    public static void readObjectArrayWithReflection(DataInputStream dis, IReadableDoomObject[] s, int len)  
    {

        if (len == 0) return;
        Class<?> c = s.getClass().getComponentType();

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            if (s[i] == null) s[i] = (IReadableDoomObject) c.newInstance();
            s[i].read(dis);
        }
    }

    public static void readObjectArray(DataInputStream dis, IReadableDoomObject[] s, int len, Class<?> c)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            if (s[i] == null)
            {
                s[i] = (IReadableDoomObject) c.newInstance();
            }
            s[i].read(dis);
        }
    }

    public static void readIntArray(DataInputStream dis, int[] s, int len, ByteOrder bo)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            s[i] = dis.readInt();
            if (bo == ByteOrder.LITTLE_ENDIAN)
            {
                s[i] = Swap.LONG(s[i]);
            }
        }
    }

    public static void readShortArray(DataInputStream dis, short[] s, int len, ByteOrder bo)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            s[i] = dis.readShort();
            if (bo == ByteOrder.LITTLE_ENDIAN)
            {
                s[i] = Swap.SHORT(s[i]);
            }
        }
    }

    public static void readIntArray(DataInputStream dis, int[] s, ByteOrder bo)  
    {
        readIntArray(dis, s, s.length, bo);
    }

    public static void readShortArray(DataInputStream dis, short[] s, ByteOrder bo)  
    {
        readShortArray(dis, s, s.length, bo);
    }

    public static void readboolArray(DataInputStream dis, bool[] s, int len)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            s[i] = dis.readbool();
        }
    }


    /**
     * Reads an array of "int bools" into an array or
     * proper bools. 4 bytes per bool are used!
     *
     * @param s
     * @param len
     * @ 
     */

    public static void readboolIntArray(DataInputStream dis, bool[] s, int len)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            s[i] = readIntbool(dis);
        }
    }

    public static void readboolIntArray(DataInputStream dis, bool[] s)  
    {
        readboolIntArray(dis, s, s.length);
    }

    public static void writebool(DataOutputStream dos, bool[] s, int len)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            dos.writebool(s[i]);
        }
    }

    public static void writeObjectArray(DataOutputStream dos, IWritableDoomObject[] s, int len)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.length); i++)
        {
            s[i].write(dos);
        }
    }

    public static void writeListOfObjects(DataOutputStream dos, List<IWritableDoomObject> s, int len)  
    {

        if (s == null || len == 0) return;

        for (int i = 0; i < Math.min(len, s.size()); i++)
        {
            s.get(i).write(dos);
        }
    }

    public static void readboolArray(DataInputStream dis, bool[] s)  
    {
        readboolArray(dis, s, s.length);
    }

    public static void readIntboolArray(DataInputStream dis, bool[] s)  
    {
        readboolIntArray(dis, s, s.length);
    }

    public static void writeCharArray(DataOutputStream dos, char[] charr, int len)  
    {

        if (charr == null || len == 0) return;

        for (int i = 0; i < Math.min(len, charr.length); i++)
        {
            dos.writeChar(charr[i]);
        }
    }

    /**
     * Will read an array of proper Unicode chars.
     *
     * @param charr
     * @param len
     * @ 
     */

    public static void readCharArray(DataInputStream dis, char[] charr, int len)  
    {

        if (charr == null || len == 0) return;

        for (int i = 0; i < Math.min(len, charr.length); i++)
        {
            charr[i] = dis.readChar();
        }
    }

    /**
     * Will read a bunch of non-unicode chars into a char array.
     * Useful when dealing with legacy text files.
     *
     * @param charr
     * @param len
     * @ 
     */

    public static void readNonUnicodeCharArray(DataInputStream dis, char[] charr, int len)  
    {

        if (charr == null || len == 0) return;

        for (int i = 0; i < Math.min(len, charr.length); i++)
        {
            charr[i] = (char) dis.readUnsignedByte();
        }
    }

    /** Writes an item reference.
     public void writeItem(gitem_t item)   {
     if (item == null)
     writeInt(-1);
     else
     writeInt(item.index);
     }
     */
    /**
     * Reads the item index and returns the game item.
     * public gitem_t readItem()   {
     * int ndx = readInt();
     * if (ndx == -1)
     * return null;
     * else
     * return GameItemList.itemlist[ndx];
     * }
     *
     * @ 
     */

    public static long readUnsignedLEInt(DataInputStream dis)  
    {
        int tmp = dis.readInt();
        return 0xFFFFFFFFL & Swap.LONG(tmp);
    }

    public static int readLEInt(DataInputStream dis)  
    {
        int tmp = dis.readInt();
        return Swap.LONG(tmp);
    }

    public static int readLEInt(InputStream dis)  
    {
        int tmp = new DataInputStream(dis).readInt();
        return Swap.LONG(tmp);
    }

    public static void writeLEInt(DataOutputStream dos, int value)  
    {
        dos.writeInt(Swap.LONG(value));
    }

    // 2-byte number
    public static int SHORT_little_endian_TO_big_endian(int i)
    {
        return (i >> 8 & 0xff) + (i << 8 & 0xff00);
    }

    // 4-byte number
    public static int INT_little_endian_TO_big_endian(int i)
    {
        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8) + (i >> 24 & 0xff);
    }

    public static short readLEShort(DataInputStream dis)  
    {
        short tmp = dis.readShort();
        return Swap.SHORT(tmp);
    }

    /**
     * Reads a "big bool" using 4 bytes.
     *
     * @return
     * @ 
     */
    public static bool readIntbool(DataInputStream dis)  
    {
        return dis.readInt() != 0;

    }


}
