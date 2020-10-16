package pooling;

import p.mobj_t;

import java.util.Stack;

/**
 * A convenient object pooling class, derived from the stock ObjectPool.
 * <p>
 * It's about 50% faster than calling new, and MUCH faster than ObjectPool
 * because it doesn't do that bullshit object cleanup every so often.
 */


public abstract class ObjectQueuePool<K>
{

    private static final boolean D = false;
    protected Stack<K> locked;

    public ObjectQueuePool(long expirationTime)
    {
        locked = new Stack<K>();

    }

    protected abstract K create();

    public abstract boolean validate(K obj);

    public abstract void expire(K obj);

    public void drain()
    {
        locked.clear();
    }

    public K checkOut()
    {

        K t;
        if (!locked.isEmpty())
        {
            return locked.pop();

        }

        t = create();
        return t;
    }

    public void checkIn(K t)
    {
        if (D) if (t instanceof mobj_t)
            System.out.printf("Object %s returned to the pool\n", t.toString());
        locked.push(t);
    }
    // private Hashtable<K,Long> unlocked;
}
