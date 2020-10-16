package s;

public class AudioChunk
{
    public int chunk;
    public int time;
    public byte[] buffer;
    public boolean free;
    public AudioChunk()
    {
        buffer = new byte[ISoundDriver.MIXBUFFERSIZE];
        setStuff(0, 0);
        free = true;
    }

    public void setStuff(int chunk, int time)
    {
        this.chunk = chunk;
        this.time = time;
    }


}