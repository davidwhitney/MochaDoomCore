namespace w {  

using java.io.Stream;
using java.io.IOException;

public class wadinfo_t : IReadableDoomObject
{
    // Should be "IWAD" or "PWAD".
    String identification;
    long numlumps;
    long infotableofs;

    /**
     * Reads the wadinfo_t from the file.
     */
    public void read(Stream f)  
    {
        identification = DoomIO.readString(f, 4);
        numlumps = DoomIO.readUnsignedLEInt(f);
        infotableofs = DoomIO.readUnsignedLEInt(f);
    }

}