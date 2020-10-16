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

import doom.CommandVariable;
import f.Wiper;
import m.IRandom;
import m.Settings;
import mochadoom.Engine;
import rr.patch_t;
import v.DoomGraphicSystem;
import v.graphics.*;
import v.scale.VideoScale;
import v.tables.GammaTables;
import v.tables.Playpal;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.util.Map;

import static v.renderers.DoomScreen.*;

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
        implements DoomGraphicSystem<T, V>, Rectangles<V, DoomScreen>, Blocks<V, DoomScreen>, Patches<V, DoomScreen>, Lines
{
    /**
     * Each screen is [SCREENWIDTH*SCREENHEIGHT]; This is what the various modules (menu, automap, renderer etc.) get to
     * manipulate at the pixel level. To go beyond 8 bit displays, these must be extended
     */
    protected final Map<DoomScreen, V> screens;
    protected final VideoScale vs;
    protected final Class<V> bufferType;

    /**
     * They are used in HiColor and TrueColor modes and are separated from tinting and gammas
     * Colormaps are now part of the base software renderer. This allows some flexibility over manipulating them.
     */
    protected final V[] liteColorMaps;
    protected final V palette;

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
    protected int bufferLength;
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
        bufferLength = width * height;
        screens = mapScreensToBuffers(bufferType, bufferLength);
        palette = palette(rf);
        liteColorMaps = colormap(rf);
    }

    @SuppressWarnings("unchecked")
    private V palette(RendererFactory.WithWadLoader<T, V> rf)
    {
        /*final byte[] */
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
        boolean colormapEnabled = !Engine.getCVM().bool(CommandVariable.NOCOLORMAP)
                && Engine.getConfig().equals(Settings.enable_colormap_lump, Boolean.TRUE);

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
    @Override
    public final int getUsegamma()
    {
        return usegamma;
    }

    @Override
    public void setUsegamma(int gamma)
    {
        usegamma = gamma % GammaTables.LUT.length;

        /**
         * Because of switching gamma stops powerup palette except for invlunerablity
         * Settings.fixgammapalette handles the fix
         */
        if (Engine.getConfig().equals(Settings.fix_gamma_palette, Boolean.FALSE))
        {
            usepalette = 0;
        }

        forcePalette();
    }

    @Override
    public final int getPalette()
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
    @Override
    public void setPalette(int palette)
    {
        usepalette = palette % Palettes.NUM_PALETTES;
        forcePalette();
    }

    @Override
    public final int getScreenHeight()
    {
        return height;
    }

    @Override
    public final int getScreenWidth()
    {
        return width;
    }

    @Override
    public int getScalingX()
    {
        return vs.getScalingX();
    }

    @Override
    public int getScalingY()
    {
        return vs.getScalingY();
    }

    @Override
    public final V getScreen(DoomScreen screenType)
    {
        return screens.get(screenType);
    }

    @Override
    public Image getScreenImage()
    {
        return currentscreen; /* may be null */
    }

    /**
     * API route delegating
     */
    @Override
    public void screenCopy(V srcScreen, V dstScreen, Relocation relocation)
    {
        Rectangles.super.screenCopy(srcScreen, dstScreen, relocation);
    }

    @Override
    public void screenCopy(DoomScreen srcScreen, DoomScreen dstScreen)
    {
        Rectangles.super.screenCopy(srcScreen, dstScreen);
    }

    @Override
    public int getBaseColor(int color)
    {
        return Rectangles.super.getBaseColor(color);
    }

    @Override
    public int point(int x, int y)
    {
        return Rectangles.super.point(x, y);
    }

    @Override
    public int point(int x, int y, int width)
    {
        return Rectangles.super.point(x, y, width);
    }

    @Override
    public void drawLine(Plotter<?> plotter, int x1, int x2)
    {
        Lines.super.drawLine(plotter, x1, x2);
    }

    @Override
    public void DrawPatch(DoomScreen screen, patch_t patch, int x, int y, int... flags)
    {
        Patches.super.DrawPatch(screen, patch, x, y, flags);
    }

    @Override
    public void DrawPatchCentered(DoomScreen screen, patch_t patch, int y, int... flags)
    {
        Patches.super.DrawPatchCentered(screen, patch, y, flags);
    }

    @Override
    public void DrawPatchCenteredScaled(DoomScreen screen, patch_t patch, VideoScale vs, int y, int... flags)
    {
        Patches.super.DrawPatchCenteredScaled(screen, patch, vs, y, flags);
    }

    @Override
    public void DrawPatchScaled(DoomScreen screen, patch_t patch, VideoScale vs, int x, int y, int... flags)
    {
        Patches.super.DrawPatchScaled(screen, patch, vs, x, y, flags);
    }

    @Override
    public void DrawPatchColScaled(DoomScreen screen, patch_t patch, VideoScale vs, int x, int col)
    {
        Patches.super.DrawPatchColScaled(screen, patch, vs, x, col);
    }

    @Override
    public void CopyRect(DoomScreen srcScreenType, Rectangle rectangle, DoomScreen dstScreenType)
    {
        Rectangles.super.CopyRect(srcScreenType, rectangle, dstScreenType);
    }

    @Override
    public void CopyRect(DoomScreen srcScreenType, Rectangle rectangle, DoomScreen dstScreenType, int dstPoint)
    {
        Rectangles.super.CopyRect(srcScreenType, rectangle, dstScreenType, dstPoint);
    }

    @Override
    public void FillRect(DoomScreen screenType, Rectangle rectangle, V patternSrc, Horizontal pattern)
    {
        Rectangles.super.FillRect(screenType, rectangle, patternSrc, pattern);
    }

    @Override
    public void FillRect(DoomScreen screenType, Rectangle rectangle, V patternSrc, int point)
    {
        Rectangles.super.FillRect(screenType, rectangle, patternSrc, point);
    }

    @Override
    public void FillRect(DoomScreen screenType, Rectangle rectangle, int color)
    {
        Rectangles.super.FillRect(screenType, rectangle, color);
    }

    @Override
    public void FillRect(DoomScreen screenType, Rectangle rectangle, byte color)
    {
        Rectangles.super.FillRect(screenType, rectangle, color);
    }

    @Override
    public V ScaleBlock(V block, VideoScale vs, int width, int height)
    {
        return Rectangles.super.ScaleBlock(block, vs, width, height);
    }

    @Override
    public void TileScreen(DoomScreen dstScreen, V block, Rectangle blockArea)
    {
        Rectangles.super.TileScreen(dstScreen, block, blockArea);
    }

    @Override
    public void TileScreenArea(DoomScreen dstScreen, Rectangle screenArea, V block, Rectangle blockArea)
    {
        Rectangles.super.TileScreenArea(dstScreen, screenArea, block, blockArea);
    }

    @Override
    public void DrawBlock(DoomScreen dstScreen, V block, Rectangle sourceArea, int destinationPoint)
    {
        Rectangles.super.DrawBlock(dstScreen, block, sourceArea, destinationPoint);
    }

    @Override
    public Plotter<V> createPlotter(DoomScreen screen)
    {
        return DoomGraphicSystem.super.createPlotter(screen);
    }

    @Override
    public V[] getColorMap()
    {
        return liteColorMaps;
    }

    public DataBuffer newBuffer(DoomScreen screen)
    {
        V buffer = screens.get(screen);
        if (buffer.getClass() == int[].class)
        {
            return new DataBufferInt((int[]) buffer, ((int[]) buffer).length);
        } else if (buffer.getClass() == short[].class)
        {
            return new DataBufferUShort((short[]) buffer, ((short[]) buffer).length);
        } else if (buffer.getClass() == byte[].class)
        {
            return new DataBufferByte((byte[]) buffer, ((byte[]) buffer).length);
        }

        throw new UnsupportedOperationException(String.format("SoftwareVideoRenderer does not support %s buffers", buffer.getClass()));
    }

    @Override
    public Wiper createWiper(IRandom random)
    {
        return Wipers.createWiper(random, this, WS, WE, FG);
    }
}
