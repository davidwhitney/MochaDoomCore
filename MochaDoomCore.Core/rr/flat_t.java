namespace rr {  

using w.CacheableDoomObject;

using java.io.IOException;
using java.nio.MemoryStream;

public class flat_t
        : CacheableDoomObject
{

    public static readonly int FLAT_SIZE = 4096;

    public byte[] data;

    public flat_t()
    {
        data = new byte[FLAT_SIZE];
    }

    public flat_t(int size)
    {
        data = new byte[size];
    }


    
    public void unpack(MemoryStream buf)
             
    {

        //buf.get(this.data);
        data = buf.array();

    }

}
