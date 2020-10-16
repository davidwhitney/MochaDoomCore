package rr;

import w.CacheableDoomObject;

import java.io.IOException;
import java.nio.ByteBuffer;

public class flat_t
        implements CacheableDoomObject
{

    public static final int FLAT_SIZE = 4096;

    public byte[] data;

    public flat_t()
    {
        data = new byte[FLAT_SIZE];
    }

    public flat_t(int size)
    {
        data = new byte[size];
    }


    @Override
    public void unpack(ByteBuffer buf)
            throws IOException
    {

        //buf.get(this.data);
        data = buf.array();

    }

}
