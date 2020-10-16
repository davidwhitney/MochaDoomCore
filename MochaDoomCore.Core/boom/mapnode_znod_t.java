package boom;

import w.CacheableDoomObject;
import w.DoomBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class mapnode_znod_t implements CacheableDoomObject
{


    public short x;  // Partition line from (x,y) to x+dx,y+dy)
    public short y;
    public short dx;
    public short dy;
    // Bounding box for each child, clip against view frustum.
    public short[][] bbox;
    // If NF_SUBSECTOR its a subsector, else it's a node of another subtree.
    public int[] children;

    public mapnode_znod_t()
    {
        bbox = new short[2][4];
        children = new int[2];
    }

    public static int sizeOf()
    {
        return 8 + 16 + 8;
    }

    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        x = buf.getShort();
        y = buf.getShort();
        dx = buf.getShort();
        dy = buf.getShort();
        DoomBuffer.readShortArray(buf, bbox[0], 4);
        DoomBuffer.readShortArray(buf, bbox[1], 4);
        DoomBuffer.readIntArray(buf, children, 2);

    }

}
