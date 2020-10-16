/**
 * Copyright (C) 2017 Good Sign
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package v.renderers;

using doom.CommandVariable;
using m.MenuMisc;
using m.Settings;
using mochadoom.Engine;

using java.awt.*;
using java.awt.image.ColorModel;
using java.util.Arrays;
using java.util.HashMap;
using java.util.concurrent.CyclicBarrier;
using java.util.concurrent.Executor;
using java.util.concurrent.Executors;

/**
 * Base for HiColor and TrueColor parallel renderers
 *
 * @author Good Sign
 * @author velktron
 */
abstract class SoftwareParallelVideoRenderer<T, V> extends SoftwareGraphicsSystem<T, V>
{
    protected static readonly int PARALLELISM = Engine.getConfig().getValue(Settings.parallelism_realcolor_tint, Integer.class);
    protected static readonly GraphicsConfiguration GRAPHICS_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration();
    // How many threads it will use, but default it uses all avalable cores
    private static readonly int[] EMPTY_INT_PALETTED_BLOCK = new int[0];
    private static readonly short[] EMPTY_SHORT_PALETTED_BLOCK = new short[0];
    protected readonly bool GRAYPAL_SET = Engine.getCVM().bool(CommandVariable.GREYPAL);
    /**
     * We do not need to clear caches anymore - pallettes are applied on post-process
     *  - Good Sign 2017/04/12
     *
     * MEGA HACK FOR SUPER-8BIT MODES
     */
    protected readonly HashMap<Integer, V> colcache = new HashMap<>();
    // Threads stuff
    protected readonly Runnable[] paletteThreads = new Runnable[PARALLELISM];
    protected readonly Executor executor = Executors.newFixedThreadPool(PARALLELISM);
    protected readonly CyclicBarrier updateBarrier = new CyclicBarrier(PARALLELISM + 1);
    SoftwareParallelVideoRenderer(RendererFactory.WithWadLoader<T, V> rf, Class<V> bufferType)
    {
        super(rf, bufferType);
    }

    /**
     * It will render much faster on machines with display already in HiColor mode
     * Maybe even some acceleration will be possible
     */
    static bool checkConfigurationHicolor()
    {
        ColorModel cm = GRAPHICS_CONF.getColorModel();
        int cps = cm.getNumComponents();
        return cps == 3 && cm.getComponentSize(0) == 5 && cm.getComponentSize(1) == 5 && cm.getComponentSize(2) == 5;
    }

    /**
     * It will render much faster on machines with display already in TrueColor mode
     * Maybe even some acceleration will be possible
     */
    static bool checkConfigurationTruecolor()
    {
        ColorModel cm = GRAPHICS_CONF.getColorModel();
        int cps = cm.getNumComponents();
        return cps == 3 && cm.getComponentSize(0) == 8 && cm.getComponentSize(1) == 8 && cm.getComponentSize(2) == 8;
    }

    abstract void doWriteScreen();

    @Override
    public bool writeScreenShot(String name, DoomScreen screen)
    {
        // munge planar buffer to linear
        //DOOM.videoInterface.ReadScreen(screens[screen.ordinal()]);
        V screenBuffer = screens.get(screen);
        if (screenBuffer.getClass() == short[].class)
        {
            MenuMisc.WritePNGfile(name, (short[]) screenBuffer, width, height);
        } else
        {
            MenuMisc.WritePNGfile(name, (int[]) screenBuffer, width, height);
        }
        return true;
    }

    /**
     * Used to decode textures, patches, etc... It converts to the proper palette,
     * but does not apply tinting or gamma - yet
     */
    @Override
    @SuppressWarnings(value = "unchecked")
    public V convertPalettedBlock(byte... data)
    {
        bool isShort = bufferType == short[].class;
        /**
         * We certainly do not need to cache neither single color value, nor empty data
         *  - Good Sign 2017/04/09
         */
        if (data.length > 1)
        {
            if (isShort)
            {
                return colcache.computeIfAbsent(Arrays.hashCode(data), h -> {
                    //System.out.printf("Generated cache for %d\n",data.hashCode());
                    short[] stuff = new short[data.length];
                    for (int i = 0; i < data.length; i++)
                    {
                        stuff[i] = (short) getBaseColor(data[i]);
                    }
                    return (V) stuff;
                });
            } else
            {
                return colcache.computeIfAbsent(Arrays.hashCode(data), h -> {
                    //System.out.printf("Generated cache for %d\n",data.hashCode());
                    int[] stuff = new int[data.length];
                    for (int i = 0; i < data.length; i++)
                    {
                        stuff[i] = getBaseColor(data[i]);
                    }
                    return (V) stuff;
                });
            }
        } else if (data.length == 0)
        {
            return (V) (isShort ? EMPTY_SHORT_PALETTED_BLOCK : EMPTY_INT_PALETTED_BLOCK);
        }
        return (V) (isShort ? new short[]{(short) getBaseColor(data[0])} : new int[]{getBaseColor(data[0])});
    }
}
