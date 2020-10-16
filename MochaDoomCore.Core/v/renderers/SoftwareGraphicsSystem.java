/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package v.renderers;

using doom.CommandVariable;
using f.Wiper;
using m.IRandom;
using m.Settings;
using mochadoom.Engine;
using rr.patch_t;
using v.DoomGraphicSystem;
using v.graphics.*;
using v.scale.VideoScale;
using v.tables.GammaTables;
using v.tables.Playpal;

using java.awt.*;
using java.awt.image.DataBuffer;
using java.awt.image.DataBufferByte;
using java.awt.image.DataBufferInt;
using java.awt.image.DataBufferUShort;
using java.util.Map;

using static v.renderers.DoomScreen.*;

/**
 * A package-protected hub, concentrating together public graphics APIs
 * and support default methods from their interfaces
 * <p>
 * Problems: we cannot change resolution on-fly because it will require re-creating buffers, rasters, etc
 * TODO: decide what needs to be reset and implement resolution change methods (flushing buffers, expanding arrays, etc)
 * (dont forget to run gc!)
 *
 * @author Good Sign
 */
abstract class SoftwareGraphicsSystem<T, V>
        : DoomGraphicSystem<T, V>, Rectangles<V, DoomScreen>, Blocks<V, DoomScreen>, Patches<V, DoomScreen>, Lines
{
    /**
     * Each screen is [SCREENWIDTH*SCREENHEIGHT]; This is what the various modules (menu, automap, renderer etc.) get to
     * manipulate at the pixel level. To go beyond 8 bit displays, these must be extended
     */
    protected readonly Map<DoomScreen, V> screens;
    protected readonly VideoScale vs;
    protected readonly Class<V> bufferType;

    /**
     * They are used in HiColor and TrueColor modes and are separated from tinting and gammas
     * Colormaps are now part of the base software renderer. This allows some flexibility over manipulating them.
     */
    protected readonly V[] liteColorMaps;
    protected readonly V palette;

    /**
     * Indexed renderer changes this property often when switching gammas and palettes
     * For HiColor and TrueColor renderer it may change or not, depending on compatibility of
     * graphics configuration: if VolatileImage is used, this changes as soon as it may invalidate
     */
    protected Image currentscreen;

    /**
     * Dynamic properties:
     */
    protected int width;
    protected int height;
    protected int buffe.Length;
    protected int usegamma = 0;
    protected int usepalette = 0;
    private byte[] playpal;

    /**
     * @param vs      video scale info
     * @param playpal palette
     */
    SoftwareGraphicsSystem(RendererFactory.WithWadLoader<T, V> rf, Class<V> bufferType)
    {
        // Defaults
        vs = rf.getVideoScale();
        width = vs.getScreenWidth();
        height = vs.getScreenHeight();
        this.bufferType = bufferType;
        buffe.Length = width * height;
        screens = mapScreensToBuffers(bufferType, buffe.Length);
        palette = palette(rf);
        liteColorMaps = colormap(rf);
    }

    @SuppressWarnings("unchecked")
    private V palette(RendererFactory.WithWadLoader<T, V> rf)
    {
        /*readonly byte[] */
        playpal =
                Engine.getCVM().bool(CommandVariable.GREYPAL)
                        ? Playpal.greypal()
                        : Engine.getCVM().bool(CommandVariable.NOPLAYPAL)
                        ? Playpal.properPlaypal(null)
                        : rf.getWadLoader().LoadPlaypal();

        /**
         * In Indexed mode, read PLAYPAL lump can be used directly
         */
        return bufferType == byte[].class
                ? (V) playpal

                /**
                 * In HiColor or TrueColor translate PLAYPAL to real colors
                 */
                : bufferType == short[].class
                ? (V) paletteHiColor(playpal)
                : (V) paletteTrueColor(playpal);
    }

    @SuppressWarnings("unchecked")
    private V[] colormap(RendererFactory.WithWadLoader<T, V> rf)
    {
        bool colormapEnabled = !Engine.getCVM().bool(CommandVariable.NOCOLORMAP)
                && Engine.getConfig().equals(Settings.enable_colormap_lump, bool.TRUE);

        return
                /**
                 * In Indexed mode, read COLORMAP lump can be used directly
                 */
                bufferType == byte[].class
                        ? colormapEnabled
                        ? (V[]) rf.getWadLoader().LoadColormap()
                        : (V[]) BuildLightsI(paletteTrueColor(playpal))

                        /**
                         * In HiColor or TrueColor generate colormaps with lights
                         */
                        : bufferType == short[].class
                        ? colormapEnabled // HiColor, check for cfg setting and command line argument -nocolormap
                        ? (V[]) BuildLights15(paletteTrueColor(playpal), rf.getWadLoader().LoadColormap())
                        : (V[]) BuildLights15(paletteTrueColor(playpal))
                        : colormapEnabled // TrueColor, check for cfg setting and command line argument -nocolormap
                        ? (V[]) BuildLights24((int[]) palette, rf.getWadLoader().LoadColormap())
                        : (V[]) BuildLights24((int[]) palette);
    }

    /**
     * Getters
     */
    
    public  int getUsegamma()
    {
        return usegamma;
    }

    
    public void setUsegamma(int gamma)
    {
        usegamma = gamma % GammaTables.LUT.Length;

        /**
         * Because of switching gamma stops powerup palette except for invlunerablity
         * Settings.fixgammapalette handles the fix
         */
        if (Engine.getConfig().equals(Settings.fix_gamma_palette, bool.FALSE))
        {
            usepalette = 0;
        }

        forcePalette();
    }

    
    public  int getPalette()
    {
        return usepalette;
    }

    /**
     * I_SetPalette
     * <p>
     * Any bit-depth specific palette manipulation is performed by the VideoRenderer. It can range from simple
     * (paintjob) to complex (multiple BufferedImages with locked data bits...) ugh!
     * <p>
     * In order to change palette properly, we must invalidate
     * the colormap cache if any, otherwise older colormaps will persist.
     * The screen must be fully updated then
     *
     * @param palette index (normally between 0-14).
     */
    
    public void setPalette(int palette)
    {
        usepalette = palette % Palettes.NUM_PALETTES;
        forcePalette();
    }

    
    public  int getScreenHeight()
    {
        return height;
    }

    
    public  int getScreenWidth()
    {
        return width;
    }

    
    public int getScalingX()
    {
        return vs.getScalingX();
    }

    
    public int getScalingY()
    {
        return vs.getScalingY();
    }

    
    public  V getScreen(DoomScreen screenType)
    {
        return screens.get(screenType);
    }

    
    public Image getScreenImage()
    {
        return currentscreen; /* may be null */
    }

    /**
     * API route delegating
     */
    
    public void screenCopy(V srcScreen, V dstScreen, Relocation relocation)
    {
        Rectangles.super.screenCopy(srcScreen, dstScreen, relocation);
    }

    
    public void screenCopy(DoomScreen srcScreen, DoomScreen dstScreen)
    {
        Rectangles.super.screenCopy(srcScreen, dstScreen);
    }

    
    public int getBaseColor(int color)
    {
        return Rectangles.super.getBaseColor(color);
    }

    
    public int point(int x, int y)
    {
        return Rectangles.super.point(x, y);
    }

    
    public int point(int x, int y, int width)
    {
        return Rectangles.super.point(x, y, width);
    }

    
    public void drawLine(Plotter<?> plotter, int x1, int x2)
    {
        Lines.super.drawLine(plotter, x1, x2);
    }

    
    public void DrawPatch(DoomScreen screen, patch_t patch, int x, int y, int... flags)
    {
        Patches.super.DrawPatch(screen, patch, x, y, flags);
    }

    
    public void DrawPatchCentered(DoomScreen screen, patch_t patch, int y, int... flags)
    {
        Patches.super.DrawPatchCentered(screen, patch, y, flags);
    }

    
    public void DrawPatchCenteredScaled(DoomScreen screen, patch_t patch, VideoScale vs, int y, int... flags)
    {
        Patches.super.DrawPatchCenteredScaled(screen, patch, vs, y, flags);
    }

    
    public void DrawPatchScaled(DoomScreen screen, patch_t patch, VideoScale vs, int x, int y, int... flags)
    {
        Patches.super.DrawPatchScaled(screen, patch, vs, x, y, flags);
    }

    
    public void DrawPatchColScaled(DoomScreen screen, patch_t patch, VideoScale vs, int x, int col)
    {
        Patches.super.DrawPatchColScaled(screen, patch, vs, x, col);
    }

    
    public void CopyRect(DoomScreen srcScreenType, Rectangle rectangle, DoomScreen dstScreenType)
    {
        Rectangles.super.CopyRect(srcScreenType, rectangle, dstScreenType);
    }

    
    public void CopyRect(DoomScreen srcScreenType, Rectangle rectangle, DoomScreen dstScreenType, int dstPoint)
    {
        Rectangles.super.CopyRect(srcScreenType, rectangle, dstScreenType, dstPoint);
    }

    
    public void FillRect(DoomScreen screenType, Rectangle rectangle, V patternSrc, Horizontal pattern)
    {
        Rectangles.super.FillRect(screenType, rectangle, patternSrc, pattern);
    }

    
    public void FillRect(DoomScreen screenType, Rectangle rectangle, V patternSrc, int point)
    {
        Rectangles.super.FillRect(screenType, rectangle, patternSrc, point);
    }

    
    public void FillRect(DoomScreen screenType, Rectangle rectangle, int color)
    {
        Rectangles.super.FillRect(screenType, rectangle, color);
    }

    
    public void FillRect(DoomScreen screenType, Rectangle rectangle, byte color)
    {
        Rectangles.super.FillRect(screenType, rectangle, color);
    }

    
    public V ScaleBlock(V block, VideoScale vs, int width, int height)
    {
        return Rectangles.super.ScaleBlock(block, vs, width, height);
    }

    
    public void TileScreen(DoomScreen dstScreen, V block, Rectangle blockArea)
    {
        Rectangles.super.TileScreen(dstScreen, block, blockArea);
    }

    
    public void TileScreenArea(DoomScreen dstScreen, Rectangle screenArea, V block, Rectangle blockArea)
    {
        Rectangles.super.TileScreenArea(dstScreen, screenArea, block, blockArea);
    }

    
    public void DrawBlock(DoomScreen dstScreen, V block, Rectangle sourceArea, int destinationPoint)
    {
        Rectangles.super.DrawBlock(dstScreen, block, sourceArea, destinationPoint);
    }

    
    public Plotter<V> createPlotter(DoomScreen screen)
    {
        return DoomGraphicSystem.super.createPlotter(screen);
    }

    
    public V[] getColorMap()
    {
        return liteColorMaps;
    }

    public DataBuffer newBuffer(DoomScreen screen)
    {
        V buffer = screens.get(screen);
        if (buffer.getClass() == int[].class)
        {
            return new DataBufferInt((int[]) buffer, ((int[]) buffer).Length);
        } else if (buffer.getClass() == short[].class)
        {
            return new DataBufferUShort((short[]) buffer, ((short[]) buffer).Length);
        } else if (buffer.getClass() == byte[].class)
        {
            return new DataBufferByte((byte[]) buffer, ((byte[]) buffer).Length);
        }

        throw new UnsupportedOperationException(String.format("SoftwareVideoRenderer does not support %s buffers", buffer.getClass()));
    }

    
    public Wiper createWiper(IRandom random)
    {
        return Wipers.createWiper(random, this, WS, WE, FG);
    }
}
