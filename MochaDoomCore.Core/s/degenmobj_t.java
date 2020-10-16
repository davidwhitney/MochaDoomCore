namespace s {  

public  class degenmobj_t
        : ISoundOrigin
{

    private readonly int x;
    private readonly int y;
    private readonly int z;

    public degenmobj_t(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public degenmobj_t(int x, int y)
    {
        this.x = x;
        this.y = y;
        z = 0;
    }

    @Override
    public  int getX()
    {
        return x;
    }

    @Override
    public  int getY()
    {
        return y;
    }

    @Override
    public  int getZ()
    {
        return z;
    }

}
