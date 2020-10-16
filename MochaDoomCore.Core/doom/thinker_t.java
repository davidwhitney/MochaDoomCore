namespace doom {  

using p.ActiveStates;
using w.CacheableDoomObject;
using w.IPackableDoomObject;
using w.IReadableDoomObject;

using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

using static utils.C2JUtils.pointer;

public class thinker_t : CacheableDoomObject, IReadableDoomObject, IPackableDoomObject
{

    private static readonly MemoryStream readbuffer = MemoryStream.allocate(12);
    public thinker_t prev;
    public thinker_t next;

    /**
     * killough's code for thinkers seems to be totally broken in M.D,
     * so commented it out and will not probably restore, but may invent
     * something new in future
     * - Good Sign 2017/05/1
     *
     * killough 8/29/98: we maintain thinkers in several equivalence classes,
     * according to various criteria, so as to allow quicker searches.
     */
    /**
     * Next, previous thinkers in same class
     */
    //public thinker_t cnext, cprev;
    public ActiveStates thinkerFunction;
    /**
     * extra fields, to use when archiving/unarchiving for
     * identification. Also in blocklinks, etc.
     */
    public int id, previd, nextid, functionid;

    
    public void read(Stream f)
             
    {
        readbuffer.position(0);
        readbuffer.order(ByteOrder.LITTLE_ENDIAN);
        f.read(readbuffer.array());
        unpack(readbuffer);
    }

    /**
     * This adds 12 bytes
     */
    
    public void pack(MemoryStream b)
             
    {
        // It's possible to reconstruct even by hashcodes.
        // As for the function, that should be implied by the mobj_t type.
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(pointer(prev));
        b.putInt(pointer(next));
        b.putInt(pointer(thinkerFunction.ordinal()));
        //System.out.printf("Packed thinker %d %d %d\n",pointer(prev),pointer(next),pointer(function));
    }

    
    public void unpack(MemoryStream b)
             
    {
        // We are supposed to archive pointers to other thinkers,
        // but they are rather useless once on disk.
        b.order(ByteOrder.LITTLE_ENDIAN);
        previd = b.getInt();
        nextid = b.getInt();
        functionid = b.getInt();
        //System.out.printf("Unpacked thinker %d %d %d\n",pointer(previd),pointer(nextid),pointer(functionid));
    }

}
