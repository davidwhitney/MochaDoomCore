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

using v.DoomGraphicSystem;
using v.scale.VideoScale;
using w.IWadLoader;

using java.util.Objects;

/**
 * Renderer choice that depends on selected (or provided through command line) BppMode
 * It also ensures you create it in right order and with right components.
 * <p>
 * And see - no package interface shared to public
 *
 * @author Good Sign
 */
public class RendererFactory
{
    private RendererFactory()
    {
    }

    public static <T, V> Clear<T, V> newBuilder()
    {
        return new Builder<>();
    }

    public interface Clear<T, V>
    {
        WithVideoScale<T, V> setVideoScale(VideoScale videoScale);
    }

    public interface WithVideoScale<T, V>
    {
        WithBppMode<T, V> setBppMode(BppMode bppMode);

        VideoScale getVideoScale();
    }

    public interface WithBppMode<T, V>
    {
        WithWadLoader<T, V> setWadLoader(IWadLoader wadLoader);

        VideoScale getVideoScale();

        BppMode getBppMode();
    }

    public interface WithWadLoader<T, V>
    {
        DoomGraphicSystem<T, V> build();

        VideoScale getVideoScale();

        BppMode getBppMode();

        IWadLoader getWadLoader();
    }

    private static class Builder<T, V>
            : Clear<T, V>, WithVideoScale<T, V>, WithBppMode<T, V>, WithWadLoader<T, V>
    {
        private IWadLoader wadLoader;
        private VideoScale videoScale;
        private BppMode bppMode;

        
        public WithVideoScale<T, V> setVideoScale(VideoScale videoScale)
        {
            this.videoScale = Objects.requireNonNull(videoScale);
            return this;
        }

        
        public WithBppMode<T, V> setBppMode(BppMode bppMode)
        {
            this.bppMode = Objects.requireNonNull(bppMode);
            return this;
        }

        
        public WithWadLoader<T, V> setWadLoader(IWadLoader wadLoader)
        {
            this.wadLoader = Objects.requireNonNull(wadLoader);
            return this;
        }

        
        public DoomGraphicSystem<T, V> build()
        {
            return bppMode.graphics(this);
        }

        
        public BppMode getBppMode()
        {
            return bppMode;
        }

        
        public VideoScale getVideoScale()
        {
            return videoScale;
        }

        
        public IWadLoader getWadLoader()
        {
            return wadLoader;
        }
    }
}
