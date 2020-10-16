package w;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IPackableDoomObject
{
    void pack(ByteBuffer buf) throws IOException;
}
