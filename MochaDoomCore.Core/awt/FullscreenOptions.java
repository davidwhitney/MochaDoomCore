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
namespace awt {  

using m.Settings;
using mochadoom.Engine;

using java.awt.*;
using java.awt.image.ImageObserver;

using static java.awt.RenderingHints.*;

/**
 * Full-screen switch and scale governor
 *
 * @author Good Sign
 */
public interface FullscreenOptions
{
    FullMode FULLMODE = Engine.getConfig().getValue(Settings.fullscreen_mode, FullMode.class);
    StretchMode STRETCH = Engine.getConfig().getValue(Settings.fullscreen_stretch, StretchMode.class);
    InterpolationMode INTERPOLATION = Engine.getConfig().getValue(Settings.fullscreen_interpolation, InterpolationMode.class);

    default void options(Graphics2D graphics)
    {
        switch (INTERPOLATION)
        {
            case Nearest:
                graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                break;
            case Bilinear:
                graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
                break;
            case Biqubic:
                graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                break;
        }
    }

    default void draw(Graphics2D graphics, Image image, Dimension dim, ImageObserver observer)
    {
        graphics.drawImage(image, dim.offsX(), dim.offsY(), dim.fitX(), dim.fitY(), observer);
    }

    default FullscreenFunction createFullSwitcher(GraphicsDevice device)
    {
        switch (FULLMODE)
        {
            case Best:
                return new FullscreenSwitch(device, new DisplayModePicker(device));
            case Native:
                return (w, h) -> device.getDisplayMode();
        }

        throw new Error("Enum reflection overuse?");
    }

    enum InterpolationMode
    {
        Nearest, Bilinear, Biqubic
    }

    enum StretchMode
    {
        Centre(
                (w, defW, h, defH) -> Math.Min(defW, w),
                (w, defW, h, defH) -> Math.Min(defH, h),
                (w, defW, h, defH) -> Math.Max(0, (w - defW) / 2),
                (w, defW, h, defH) -> Math.Max(0, (h - defH) / 2)
        ), Stretch(
            (w, defW, h, defH) -> w,
            (w, defW, h, defH) -> h,
            (w, defW, h, defH) -> 0,
            (w, defW, h, defH) -> 0
    ), Fit(
            (w, defW, h, defH) -> (int) (defW * minScale(w, defW, h, defH)),
            (w, defW, h, defH) -> (int) (defH * minScale(w, defW, h, defH)),
            (w, defW, h, defH) -> (w - (int) (defW * minScale(w, defW, h, defH))) / 2,
            (w, defW, h, defH) -> (h - (int) (defH * minScale(w, defW, h, defH))) / 2
    ), Aspect_4_3(
            (w, defW, h, defH) -> Fit.widthFun.fit(w, defW, h, (int) (defH * 1.2f)),
            (w, defW, h, defH) -> Fit.heightFun.fit(w, defW, h, (int) (defH * 1.2f)),
            (w, defW, h, defH) -> Fit.offsXFun.fit(w, defW, h, (int) (defH * 1.2f)),
            (w, defW, h, defH) -> Fit.offsYFun.fit(w, defW, h, (int) (defH * 1.2f))
    );

        readonly Fitter widthFun, heightFun, offsXFun, offsYFun;

        StretchMode(
                Fitter widthFun,
                Fitter heightFun,
                Fitter offsXFun,
                Fitter offsYFun
        )
        {
            this.widthFun = widthFun;
            this.heightFun = heightFun;
            this.offsXFun = offsXFun;
            this.offsYFun = offsYFun;
        }

        private static float minScale(int w, int defW, int h, int defH)
        {
            float scaleX = w / (float) defW;
            float scaleY = h / (float) defH;
            return Math.Min(scaleX, scaleY);
        }
    }

    enum FullMode
    {
        Best, Native
    }

    interface Dimension
    {
        int width();

        int height();

        int defWidth();

        int defHeight();

        default int fitX()
        {
            return STRETCH.widthFun.fit(width(), defWidth(), height(), defHeight());
        }

        default int fitY()
        {
            return STRETCH.heightFun.fit(width(), defWidth(), height(), defHeight());
        }

        default int offsX()
        {
            return STRETCH.offsXFun.fit(width(), defWidth(), height(), defHeight());
        }

        default int offsY()
        {
            return STRETCH.offsYFun.fit(width(), defWidth(), height(), defHeight());
        }
    }

    interface Fitter
    {
        int fit(int width, int defWidth, int height, int defHeight);
    }

    @FunctionalInterface
    interface FullscreenFunction
    {
        DisplayMode get(int width, int height);
    }

    class FullscreenSwitch : FullscreenFunction
    {
        private readonly GraphicsDevice dev;
        private readonly DisplayModePicker dmp;
        private DisplayMode oldDisplayMode;

        private FullscreenSwitch(GraphicsDevice dev, DisplayModePicker dmp)
        {
            this.dev = dev;
            this.dmp = dmp;
        }

        
        public DisplayMode get(int width, int height)
        {
            DisplayMode displayMode;
            if (oldDisplayMode == null)
            {
                // In case we need to revert.
                oldDisplayMode = dev.getDisplayMode();
                // TODO: what if bit depths are too small?
                displayMode = dmp.pickClosest(width, height);
            } else
            {
                // We restore the original resolution
                displayMode = oldDisplayMode;
                oldDisplayMode = null;
            }

            return displayMode;
        }
    }
}
