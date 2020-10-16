namespace pooling {  

public class RoguePatchMap extends GenericIntMap<byte[][]>
{

    public RoguePatchMap()
    {
        patches = new byte[DEFAULT_CAPACITY][][];
    }
}
