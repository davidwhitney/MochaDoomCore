namespace pooling {  

public class RoguePatchMap : GenericIntMap<byte[][]>
{

    public RoguePatchMap()
    {
        patches = new byte[DEFAULT_CAPACITY][][];
    }
}
