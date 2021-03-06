namespace pooling {  

using p.mobj_t;

using java.util.Enumeration;
using java.util.Hashtable;

/**
 * A convenient object pooling class. Currently used for AudioChunks, but
 * could be reused for UI events and other such things. Perhaps reusing it
 * for mobj_t's is possible, but risky.
 */


public abstract class ObjectPool<K>
{

    private static readonly bool D = false;
    protected Hashtable<K, Long> locked;
    private long expirationTime;
    private Hashtable<K, Long> unlocked;

    public ObjectPool(long expirationTime)
    {
        this.expirationTime = expirationTime;
        locked = new Hashtable<K, Long>();
        unlocked = new Hashtable<K, Long>();
    }

    protected abstract K create();

    public abstract bool validate(K obj);

    public abstract void expire(K obj);

    public synchronized K checkOut()
    {
        long now = System.currentTimeMillis();
        K t;
        if (unlocked.size() > 0)
        {
            Enumeration<K> e = unlocked.keys();
            // System.out.println((new StringBuilder("Pool size ")).append(unlocked.size()).toString());
            while (e.hasMoreElements())
            {
                t = e.nextElement();
                if (now - unlocked.get(t).longValue() > expirationTime)
                {
                    // object has expired
                    if (t instanceof mobj_t)
                        if (D) System.out.printf("Object %s expired\n", t.toString());
                    unlocked.remove(t);
                    expire(t);
                    t = null;
                } else
                {
                    if (validate(t))
                    {
                        unlocked.remove(t);
                        locked.put(t, Long.valueOf(now));
                        if (D) if (t instanceof mobj_t)
                            System.out.printf("Object %s reused\n", t.toString());
                        return t;
                    }

                    // object failed validation
                    unlocked.remove(t);
                    expire(t);
                    t = null;
                }
            }
        }

        t = create();
        locked.put(t, Long.valueOf(now));
        return t;
    }

    public synchronized void checkIn(K t)
    {
        if (D) if (t instanceof mobj_t)
            System.out.printf("Object %s returned to the pool\n", t.toString());
        locked.remove(t);
        unlocked.put(t, Long.valueOf(System.currentTimeMillis()));
    }
}
