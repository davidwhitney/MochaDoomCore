namespace w {  

using java.io.DataInputStream;
using java.io.IOException;

/**
 * This is for objects that can be read from disk, but cannot
 * self-determine their own length for some reason.
 *
 * @author Maes
 */

public interface AidedReadableDoomObject
{

    void read(DataInputStream f, int len)  ;
}
