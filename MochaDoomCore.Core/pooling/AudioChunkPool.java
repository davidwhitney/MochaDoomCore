namespace pooling {  

using s.AudioChunk;

// Referenced classes of package pooling:
//            ObjectPool

public class AudioChunkPool : ObjectQueuePool<AudioChunk>
{

    public AudioChunkPool()
    {
        // A reasonable time limit for Audio chunks
        super(10000L);
    }

    protected AudioChunk create()
    {
        return new AudioChunk();
    }

    public void expire(AudioChunk o)
    {
        o.free = true;
    }

    public bool validate(AudioChunk o)
    {
        return o.free;
    }

}
