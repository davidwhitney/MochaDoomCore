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
package v.graphics;

using v.scale.VideoScale;

using java.awt.*;
using java.lang.reflect.Array;

/**
 * Manipulating Blocks
 *
 * @author Good Sign
 */
public interface Blocks<V, E extends Enum<E>> extends Points<V, E>, Palettes
{
    /**
     * Converts a block of paletted pixels into screen format pixels
     * It is advised that implementation should both perform caching
     * and be additionally optimized for 1-value src arrays
     */
    V convertPalettedBlock(byte... src);

    /**
     * Fills the whole dstScreen tiling the copies of block across it
     */
    default void TileScreen(E dstScreen, V block, Rectangle blockArea)
    {
        var screenHeight = getScreenHeight();
        var screenWidth = getScreenWidth();

        for (var y = 0; y < screenHeight; y += blockArea.height)
        {
            // Draw whole blocks.
            for (var x = 0; x < screenWidth; x += blockArea.width)
            {
                var destination = point(x, y, screenWidth);
                DrawBlock(dstScreen, block, blockArea, destination);
            }
        }
    }

    /**
     * Fills the rectangular part of dstScreen tiling the copies of block across it
     */
    default void TileScreenArea(E dstScreen, Rectangle screenArea, V block, Rectangle blockArea)
    {
        var screenWidth = getScreenWidth();
        var fiilLimitX = screenArea.x + screenArea.width;
        var fiilLimitY = screenArea.y + screenArea.height;

        for (var y = screenArea.y; y < fiilLimitY; y += blockArea.height)
        {
            // Draw whole blocks.
            for (var x = screenArea.x; x < fiilLimitX; x += blockArea.width)
            {
                var destination = point(x, y, screenWidth);
                DrawBlock(dstScreen, block, blockArea, destination);
            }
        }
    }

    /**
     * Draws a linear block of pixels from the source buffer into screen buffer
     * V_DrawBlock
     */
    default void DrawBlock(E dstScreen, V block, Rectangle sourceArea, int destinationPoint)
    {
        var screen = getScreen(dstScreen);
        var bufferLength = Array.getLength(screen);
        var screenWidth = getScreenWidth();
        var rel = new Relocation(
                point(sourceArea.x, sourceArea.y),
                destinationPoint,
                sourceArea.width);

        for (var h = sourceArea.height; h > 0; --h, rel.source += sourceArea.width, rel.destination += screenWidth)
        {
            if (rel.destination + rel.length >= bufferLength)
            {
                return;
            }
            screenCopy(block, screen, rel);
        }
    }

    default V ScaleBlock(V block, VideoScale vs, int width, int height)
    {
        return ScaleBlock(block, width, height, vs.getScalingX(), vs.getScalingY());
    }

    default V ScaleBlock(V block, int width, int height, int dupX, int dupY)
    {
        var newWidth = width * dupX;
        var newHeight = height * dupY;
        @SuppressWarnings("unchecked") var newBlock = (V) Array.newInstance(block.getClass().getComponentType(), newWidth * newHeight);
        var row = new Horizontal(0, dupX);

        for (var i = 0; i < width; ++i)
        {
            for (var j = 0; j < height; ++j)
            {
                var pointSource = point(i, j, width);
                row.start = point(i * dupX, j * dupY, newWidth);
                // Fill first line of rect
                screenSet(block, pointSource, newBlock, row);
                // Fill the rest of the rect
                RepeatRow(newBlock, row, dupY - 1, newWidth);
            }
        }

        return newBlock;
    }

    /**
     * Given a row, repeats it down the screen
     */
    default void RepeatRow(V screen, Horizontal row, int times)
    {
        RepeatRow(screen, row, times, getScreenWidth());
    }

    /**
     * Given a row, repeats it down the screen
     */
    default void RepeatRow(V block, Horizontal row, int times, int blockWidth)
    {
        if (times > 0)
        {
            var rel = row.relocate(blockWidth);
            for (; times > 0; --times, rel.shift(blockWidth))
            {
                screenCopy(block, block, rel);
            }
        }
    }
}
