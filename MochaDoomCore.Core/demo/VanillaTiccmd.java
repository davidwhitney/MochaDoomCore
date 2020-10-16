namespace demo {  

using doom.ticcmd_t;
using utils.C2JUtils;
using w.CacheableDoomObject;
using w.IWritableDoomObject;

using java.io.DataOutputStream;
using java.io.IOException;
using java.nio.MemoryStream;

/**
 * A more lightweight version of ticcmd_t, which contains
 * too much crap and redundant data. In order to keep demo
 * loading and recording easier, this class contains only the
 * necessary stuff to read/write from/to disk during VANILLA
 * demos. It can be converted from/to ticcmd_t, if needed.
 *
 * @author admin
 */
public class VanillaTiccmd : CacheableDoomObject, IDemoTicCmd, IWritableDoomObject
{

    private static stringBuilder sb = new StringBuilder();
    /**
     * 2048 for move
     */
    public byte forwardmove;
    /**
     * 2048 for move
     */
    public byte sidemove;
    /**
     * <<16 for angle delta
     */
    public byte angleturn;
    public byte buttons;

    /**
     * Special note: the only occasion where we'd ever be interested
     * in reading ticcmd_t's from a lump is when playing back demos.
     * Therefore, we use this specialized reading method which does NOT,
     * I repeat, DOES NOT set all fields and some are read differently.
     * NOT 1:1 intercangeable with the Datagram methods!
     */
    @Override
    public void unpack(MemoryStream f)
             
    {

        // MAES: the original ID code for reference.
        // demo_p++ is a pointer inside a raw byte buffer.

        //cmd->forwardmove = ((signed char)*demo_p++);
        //cmd->sidemove = ((signed char)*demo_p++);
        //cmd->angleturn = ((unsigned char)*demo_p++)<<8;
        //cmd->buttons = (unsigned char)*demo_p++;

        forwardmove = f.get();
        sidemove = f.get();
        // Even if they use the "unsigned char" syntax, angleturn is signed.
        angleturn = f.get();
        buttons = f.get();

    }

    /**
     * Ditto, we only pack some of the fields.
     *
     * @param f
     * @ 
     */
    public void pack(MemoryStream f)
             
    {

        f.put(forwardmove);
        f.put(sidemove);
        f.put(angleturn);
        f.put(buttons);
    }

    public String toString()
    {
        sb.setLength(0);
        sb.append(" forwardmove ");
        sb.append(forwardmove);
        sb.append(" sidemove ");
        sb.append(sidemove);
        sb.append(" angleturn ");
        sb.append(angleturn);
        sb.append(" buttons ");
        sb.append(Integer.toHexString(buttons));
        return sb.toString();
    }

    @Override
    public void decode(ticcmd_t dest)
    {
        // Decode
        dest.forwardmove = forwardmove;
        dest.sidemove = sidemove;
        dest.angleturn = (short) (angleturn << 8);
        dest.buttons = (char) C2JUtils.toUnsignedByte(buttons);
    }

    @Override
    public void encode(ticcmd_t source)
    {
        // Note: we can get away with a simple copy because
        // Demoticcmds have already been "decoded".
        forwardmove = source.forwardmove;
        sidemove = source.sidemove;
        // OUCH!!! NASTY PRECISION REDUCTION... but it's the
        // One True Vanilla way.
        angleturn = (byte) (source.angleturn >>> 8);
        buttons = (byte) (source.buttons & 0x00FF);
    }

    @Override
    public void write(DataOutputStream f)
             
    {
        f.writeByte(forwardmove);
        f.writeByte(sidemove);
        f.writeByte(angleturn);
        f.writeByte(buttons);
    }

}
