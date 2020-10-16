package rr;

import p.Resettable;
import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static m.fixed_t.FRACBITS;

/**
 * This is the vertex structure used IN MEMORY with fixed-point arithmetic.
 * It's DIFFERENT than the one used on disk, which has 16-bit signed shorts.
 * However, it must be parsed.
 */

public class vertex_t implements CacheableDoomObject, Resettable
{

    /**
     * treat as (fixed_t)
     */
    public int x, y;

    public vertex_t()
    {

    }

    public static int sizeOf()
    {
        return 4;
    }

    /**
     * Notice how we auto-expand to fixed_t
     */
    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        x = buf.getShort() << FRACBITS;
        y = buf.getShort() << FRACBITS;

    }

    @Override
    public void reset()
    {
        x = 0;
        y = 0;
    }

}