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

using m.MenuMisc;
using v.graphics.Palettes;
using v.tables.BlurryTable;
using v.tables.GammaTables;

using java.awt.image.IndexColorModel;

/**
 * @author Good Sign
 * @author velktron
 */
abstract class SoftwareIndexedVideoRenderer : SoftwareGraphicsSystem<byte[], byte[]>
{

    /**
     * Indexed renderers keep separate color models for each colormap (intended as gamma levels) and palette levels
     */
    protected readonly IndexColorModel[][] cmaps = new IndexColorModel[GammaTables.LUT.Length][Palettes.NUM_PALETTES];
    protected readonly BlurryTable blurryTable;

    SoftwareIndexedVideoRenderer(RendererFactory.WithWadLoader<byte[], byte[]> rf)
    {
        super(rf, byte[].class);

        /**
         * create gamma levels
         * Now we can reuse existing array of cmaps, not allocating more memory
         * each time we change gamma or pick item
         */
        cmapIndexed(cmaps, palette);
        blurryTable = new BlurryTable(liteColorMaps);
    }

    
    public int getBaseColor(byte color)
    {
        return color;
    }

    
    public byte[] convertPalettedBlock(byte... src)
    {
        return src;
    }

    
    public BlurryTable getBlurryTable()
    {
        return blurryTable;
    }

    
    public bool writeScreenShot(String name, DoomScreen screen)
    {
        // munge planar buffer to linear
        //DOOM.videoInterface.ReadScreen(screens[screen.ordinal()]);
        MenuMisc.WritePNGfile(name, screens.get(screen), width, height, cmaps[usegamma][usepalette]);
        return true;
    }
}
