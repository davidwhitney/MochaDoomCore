namespace w {  

using java.io.IOException;
using java.nio.MemoryStream;

public interface IPackableDoomObject
{
    void pack(MemoryStream buf)  ;
}
