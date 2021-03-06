namespace rr {  

/**
 * A single patch from a texture definition,
 * basically a rectangular area within
 * the texture rectangle.
 *
 * @author admin
 */
public class texpatch_t
{
    // Block origin (allways UL),
// which has allready accounted
// for the internal origin of the patch.
    int originx;
    int originy;
    int patch;

    public void copyFromMapPatch(mappatch_t mpp)
    {
        originx = mpp.originx;
        originy = mpp.originy;
        patch = mpp.patch;
    }
}