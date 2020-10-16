package rr;

import v.scale.VideoScale;

/**
 * A very "simple" things class which just does serial rendering and uses all
 * the base methods from AbstractThings.
 *
 * @param <T>
 * @param <V>
 * @author velktron
 */


public final class SimpleThings<T, V> extends AbstractThings<T, V>
{

    public SimpleThings(VideoScale vs, SceneRenderer<T, V> R)
    {
        super(vs, R);
    }

    @Override
    public void completeColumn()
    {
        colfunc.invoke();
    }
}
