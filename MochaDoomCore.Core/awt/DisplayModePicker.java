namespace awt {  

using java.awt.*;
using java.util.ArrayList;
using java.util.Collections;
using java.util.Comparator;
using java.util.List;

public class DisplayModePicker
{

    protected GraphicsDevice device;
    protected DisplayMode default_mode;

    public DisplayModePicker(GraphicsDevice device)
    {
        this.device = device;
        default_mode = device.getDisplayMode();
    }

    public DisplayMode pickClosest(int width, int height)
    {

        DisplayMode[] modes = device.getDisplayModes();
        List<DisplayMode> picks = new ArrayList<>();

        WidthComparator wc = new WidthComparator();
        HeightComparator hc = new HeightComparator();

        // Filter out those with too small dimensions.
        for (DisplayMode dm : modes)
        {
            if (dm.getWidth() >= width && dm.getHeight() >= height)
            {
                picks.add(dm);
            }
        }

        if (picks.size() > 0)
        {
            Collections.sort(picks, wc.thenComparing(hc));
        }

        // First one is the minimum that satisfies the desired criteria.
        return picks.get(0);
    }

    /**
     * Return offsets to center rasters too oddly shaped to fit entirely into
     * a standard display mode (unfortunately, this means most stuff > 640 x 400),
     * with doom's standard 8:5 ratio.
     *
     * @param width
     * @param height
     * @param dm
     * @return array, x-offset and y-offset.
     */
    public int[] getCentering(int width, int height, DisplayMode dm)
    {
        int[] xy = new int[2];

        xy[0] = (dm.getWidth() - width) / 2;
        xy[1] = (dm.getHeight() - height) / 2;

        return xy;
    }

    class WidthComparator : Comparator<DisplayMode>
    {

        
        public int compare(DisplayMode arg0, DisplayMode arg1)
        {
            if (arg0.getWidth() > arg1.getWidth())
            {
                return 1;
            }
            if (arg0.getWidth() < arg1.getWidth())
            {
                return -1;
            }
            return 0;
        }
    }

    class HeightComparator : Comparator<DisplayMode>
    {

        
        public int compare(DisplayMode arg0, DisplayMode arg1)
        {
            if (arg0.getHeight() > arg1.getHeight())
            {
                return 1;
            }
            if (arg0.getHeight() < arg1.getHeight())
            {
                return -1;
            }
            return 0;
        }
    }

}
