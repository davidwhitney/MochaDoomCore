namespace w {  

using java.io.IOException;
using java.nio.MemoryStream;

/**
 * A container allowing for caching of arrays of CacheableDoomObjects
 * <p>
 * It's a massive improvement over the older system, allowing for proper
 * caching and auto-unpacking of arrays of CacheableDoomObjects and much
 * cleaner code throughout.
 * <p>
 * The container itself is a CacheableDoomObject....can you feel the
 * abuse? ;-)
 */

public class CacheableDoomObjectContainer<T : CacheableDoomObject> : CacheableDoomObject
{

    private T[] stuff;

    public CacheableDoomObjectContainer(T[] stuff)
    {
        this.stuff = stuff;
    }

    /**
     * Statically usable method
     *
     * @param buf
     * @param stuff
     * @ 
     */

    public static void unpack(MemoryStream buf, CacheableDoomObject[] stuff)  
    {
        for (int i = 0; i < stuff.Length; i++)
        {
            stuff[i].unpack(buf);
        }
    }

    public T[] getStuff()
    {
        return stuff;
    }

    
    public void unpack(MemoryStream buf)  
    {
        for (int i = 0; i < stuff.Length; i++)
        {
            stuff[i].unpack(buf);
        }
    }

}
