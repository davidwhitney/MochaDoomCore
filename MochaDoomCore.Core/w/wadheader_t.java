namespace w {  

using java.io.Stream;
using java.io.Stream;
using java.io.IOException;

public class wadheader_t : IReadableDoomObject, IWritableDoomObject
{
    public String type;
    public int numentries;
    public int tablepos;

    public bool big_endian = false;

    public static int sizeof()
    {
        return 16;
    }

    public void read(Stream f)  
    {

        type = DoomIO.readNullTerminatedString(f, 4);

        if (!big_endian)
        {
            numentries = (int) DoomIO.readUnsignedLEInt(f);
            tablepos = (int) DoomIO.readUnsignedLEInt(f);

        } else
        {
            numentries = f.readInt();
            tablepos = f.readInt();
        }

    }

    
    public void write(Stream dos)
             
    {
        DoomIO.writeString(dos, type, 4);

        if (!big_endian)
        {
            DoomIO.writeLEInt(dos, numentries);
            DoomIO.writeLEInt(dos, tablepos);
        } else
        {
            dos.writeInt(numentries);
            dos.writeInt(tablepos);
        }


    }

}
