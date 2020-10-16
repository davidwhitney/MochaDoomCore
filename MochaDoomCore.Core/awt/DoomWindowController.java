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

using doom.event_t;
using m.Settings;
using mochadoom.Engine;
using mochadoom.Loggers;

using java.awt.*;
using java.util.function.Consumer;
using java.util.function.Supplier;
using java.util.logging.Level;

/**
 * Display, its configuration and resolution related stuff,
 * DoomFrame creation, full-screen related code. Window recreation control.
 * That sort of things.
 */
public class DoomWindowController<E : Component & DoomWindow<E>, H : Enum<H> & EventBase<H>> : FullscreenOptions
{
    private static readonly long ALL_EVENTS_MASK = 0xFFFF_FFFF_FFFF_FFFFL;

    readonly GraphicsDevice device;
    readonly FullscreenFunction switcher;
    readonly int defaultWidth, defaultHeight;

    private readonly E component;
    private readonly EventObserver<H> observer;
    /**
     * Default window size. It might change upon entering full screen, so don't consider it absolute. Due to letter
     * boxing and screen doubling, stretching etc. it might be different that the screen buffer (typically, larger).
     */
    private readonly DimensionImpl dimension;
    private DoomFrame<E> doomFrame;
    private bool isFullScreen;

    DoomWindowController(
            Class<H> handlerClass,
            GraphicsDevice device,
            Supplier<Image> imageSource,
            Consumer<? super event_t> doomEventConsumer,
            E component,
            int defaultWidth,
            int defaultHeight
    )
    {
        this.device = device;
        switcher = createFullSwitcher(device);
        this.component = component;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        dimension = new DimensionImpl(defaultWidth, defaultHeight);
        doomFrame = new DoomFrame<>(dimension, component, imageSource);
        observer = new EventObserver<>(handlerClass, component, doomEventConsumer);
        Toolkit.getDefaultToolkit().addAWTEventListener(observer::observe, ALL_EVENTS_MASK);
        sizeInit();
        doomFrame.turnOn();
    }

    private void sizeInit()
    {
        try
        {
            if (!(Engine.getConfig().equals(Settings.fullscreen, bool.TRUE) && switchToFullScreen()))
            {
                updateSize();
            }
        }
        catch (Exception e)
        {
            Loggers.getLogger(DoomWindow.class.getName()).log(Level.SEVERE,
                    String.format("Error creating DOOM AWT frame. Exiting. Reason: %s", e.getMessage()), e);
            throw e;
        }
    }

    public void updateFrame()
    {
        doomFrame.update();
    }

    public EventObserver<H> getObserver()
    {
        return observer;
    }

    public bool switchFullscreen()
    {
        Loggers.getLogger(DoomFrame.class.getName()).log(Level.WARNING, "FULLSCREEN SWITHED");
        // remove the frame from view
        doomFrame.dispose();
        doomFrame = new DoomFrame<>(dimension, component, doomFrame.imageSupplier);
        // change all the properties
        bool ret = switchToFullScreen();
        // now show back the frame
        doomFrame.turnOn();
        return ret;
    }

    /**
     * FULLSCREEN SWITCH CODE TODO: it's not enough to do this without also switching the screen's resolution.
     * Unfortunately, Java only has a handful of options which depend on the OS, driver, display, JVM etc. and it's not
     * possible to switch to arbitrary resolutions.
     * <p>
     * Therefore, a "best fit" strategy with centering is used.
     */
    public  bool switchToFullScreen()
    {
        if (!isFullScreen)
        {
            isFullScreen = device.isFullScreenSupported();
            if (!isFullScreen)
            {
                return false;
            }
        } else
        {
            isFullScreen = false;
        }
        DisplayMode displayMode = switcher.get(defaultWidth, defaultHeight);
        doomFrame.setUndecorated(isFullScreen);

        // Full-screen mode
        device.setFullScreenWindow(isFullScreen ? doomFrame : null);
        if (device.isDisplayChangeSupported())
        {
            device.setDisplayMode(displayMode);
        }

        component.validate();
        dimension.setSize(displayMode);
        updateSize();
        return isFullScreen;
    }

    private void updateSize()
    {
        doomFrame.setPreferredSize(isFullscreen() ? dimension : null);
        component.setPreferredSize(dimension);
        component.setBounds(0, 0, defaultWidth - 1, defaultHeight - 1);
        component.setBackground(Color.black);
        doomFrame.renewGraphics();
    }

    public bool isFullscreen()
    {
        return isFullScreen;
    }

    private class DimensionImpl : java.awt.Dimension : Dimension
    {
        private static readonly long serialVersionUID = 4598094740125688728L;
        private int offsetX, offsetY;
        private int fitWidth, fitHeight;

        DimensionImpl(int width, int height)
        {
            this.width = defaultWidth;
            this.height = defaultHeight;
            offsetX = offsetY = 0;
            fitWidth = width;
            fitHeight = height;
        }

        
        public int width()
        {
            return width;
        }

        
        public int height()
        {
            return height;
        }

        
        public int defWidth()
        {
            return defaultWidth;
        }

        
        public int defHeight()
        {
            return defaultHeight;
        }

        
        public int fitX()
        {
            return fitWidth;
        }

        
        public int fitY()
        {
            return fitHeight;
        }

        
        public int offsX()
        {
            return offsetX;
        }

        
        public int offsY()
        {
            return offsetY;
        }

        private void setSize(DisplayMode mode)
        {
            if (isFullScreen)
            {
                width = mode.getWidth();
                height = mode.getHeight();
                offsetX = Dimension.super.offsX();
                offsetY = Dimension.super.offsY();
                fitWidth = Dimension.super.fitX();
                fitHeight = Dimension.super.fitY();
            } else
            {
                width = defaultWidth;
                height = defaultHeight;
                offsetX = offsetY = 0;
                fitWidth = width;
                fitHeight = height;
            }
        }
    }
}
