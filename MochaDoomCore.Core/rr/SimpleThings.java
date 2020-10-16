namespace rr {  

using v.scale.VideoScale;

/**
 * A very "simple" things class which just does serial rendering and uses all
 * the base methods from AbstractThings.
 *
 * @param <T>
 * @param <V>
 * @author velktron
 */


public  class SimpleThings<T, V> : AbstractThings<T, V>
{

    public SimpleThings(VideoScale vs, SceneRenderer<T, V> R)
    {
        super(vs, R);
    }

    
    public void completeColumn()
    {
        colfunc.invoke();
    }
}
