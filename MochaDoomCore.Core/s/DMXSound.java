namespace s {  

using w.CacheableDoomObject;

using java.io.IOException;
using java.nio.BufferUnderflowException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

/**
 * An object representation of Doom's sound format
 */

public class DMXSound : CacheableDoomObject
{

    /**
     * ushort, all Doom samples are "type 3". No idea how
     */
    public int type;
    /**
     * ushort, speed in Hz.
     */
    public int speed;
    /**
     * uint
     */
    public int datasize;

    public byte[] data;

    @Override
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        type = buf.getChar();
        speed = buf.getChar();
        try
        {
            datasize = buf.getInt();
        }
        catch (BufferUnderflowException e)
        {
            datasize = buf.capacity() - buf.position();
        }
        data = new byte[Math.min(buf.remaining(), datasize)];
        buf.get(data);
    }

}
