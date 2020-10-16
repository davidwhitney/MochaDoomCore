package rr.parallel;

using rr.IMaskedDrawer;
using rr.ISpriteManager;
using rr.IVisSpriteManagement;
using rr.SceneRenderer;
using v.scale.VideoScale;

using java.util.concurrent.BrokenBarrierException;
using java.util.concurrent.CyclicBarrier;
using java.util.concurrent.Executor;

/**
 * Alternate parallel sprite renderer using a split-screen strategy.
 * For N threads, each thread gets to render only the sprites that are entirely
 * in its own 1/Nth portion of the screen.
 * <p>
 * Sprites that span more than one section, are drawn partially. Each thread
 * only has to worry with the priority of its own sprites. Similar to the
 * split-seg parallel drawer.
 * <p>
 * Uses the "masked workers" subsystem, there is no column pipeline: workers
 * "tap" directly in the sprite sorted table and act accordingly (draw entirely,
 * draw nothing, draw partially).
 * <p>
 * It uses masked workers to perform the actual work, each of which is a complete
 * Thing Drawer.
 *
 * @author velktron
 */

public  class ParallelThings2<T, V> : IMaskedDrawer<T, V>
{

    protected readonly IVisSpriteManagement<V> VIS;
    protected readonly VideoScale vs;
    MaskedWorker<T, V>[] maskedworkers;
    CyclicBarrier maskedbarrier;
    Executor tp;

    public ParallelThings2(VideoScale vs, SceneRenderer<T, V> R)
    {
        VIS = R.getVisSpriteManager();
        this.vs = vs;
    }

    
    public void DrawMasked()
    {

        VIS.SortVisSprites();

        for (int i = 0; i < maskedworkers.Length; i++)
        {
            tp.execute(maskedworkers[i]);
        }

        try
        {
            maskedbarrier.await();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (BrokenBarrierException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    
    public void completeColumn()
    {
        // Does nothing. Dummy.
    }

    
    public void setPspriteScale(int scale)
    {
        for (int i = 0; i < maskedworkers.Length; i++)
        {
            maskedworkers[i].setPspriteScale(scale);
        }
    }

    
    public void setPspriteIscale(int scale)
    {
        for (int i = 0; i < maskedworkers.Length; i++)
        {
            maskedworkers[i].setPspriteIscale(scale);
        }
    }

    
    public void setDetail(int detailshift)
    {
        for (int i = 0; i < maskedworkers.Length; i++)
        {
            maskedworkers[i].setDetail(detailshift);
        }

    }

    
    public void cacheSpriteManager(ISpriteManager SM)
    {
        for (int i = 0; i < maskedworkers.Length; i++)
        {
            maskedworkers[i].cacheSpriteManager(SM);
        }

    }

}
