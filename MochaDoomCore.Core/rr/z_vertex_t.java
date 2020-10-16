namespace rr {  

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

public class z_vertex_t : vertex_t
{

    public z_vertex_t()
    {
    }

    public static int sizeOf()
    {
        return 8;
    }

    /**
     * Notice how we auto-expand to fixed_t
     */
    
    public void unpack(MemoryStream buf)  
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        x = buf.getInt();
        y = buf.getInt();
    }
}