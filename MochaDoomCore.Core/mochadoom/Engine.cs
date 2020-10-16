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

using System;
using System.IO;
using awt.DoomWindow;
using awt.DoomWindowController;
using awt.EventBase.KeyStateInterest;
using awt.EventHandler;
using doom.CVarManager;
using doom.CommandVariable;
using doom.ConfigManager;
using doom.DoomMain;
using i.Strings;

using java.io.IOException;
using java.util.Arrays;
using java.util.logging.Level;
using java.util.logging.Logger;

using static awt.EventBase.KeyStateSatisfaction.WANTS_MORE_ATE;
using static awt.EventBase.KeyStateSatisfaction.WANTS_MORE_PASS;
using static g.Signals.ScanCode.*;

namespace mochadoom { 


public class Engine
{
    private static volatile Engine instance;
    private readonly CVarManager cvm;
    private readonly ConfigManager cm;
    private readonly DoomWindowController<?, EventHandler> windowController;
    private readonly DoomMain<?, ?> DOOM;

    private Engine(string[] argv)
    {
        instance = this;

        // reads command line arguments
        cvm = new CVarManager(Arrays.asList(argv));

        // reads default.cfg and mochadoom.cfg
        cm = new ConfigManager();

        // intiializes stuff
        DOOM = new DoomMain<>();

        // opens a window
        windowController = DoomWindow.createCanvasWindowController(
                DOOM.graphicSystem::getScreenImage,
                DOOM::PostEvent,
                DOOM.graphicSystem.getScreenWidth(),
                DOOM.graphicSystem.getScreenHeight()
        );

        windowController.getObserver()
                .addInterest(
                        new KeyStateInterest<>(obs -> {
                            EventHandler.fullscreenChanges(windowController.getObserver(), windowController.switchFullscreen());
                            return WANTS_MORE_ATE;
                        }, SC_LALT, SC_ENTER)
                ).addInterest(
                new KeyStateInterest<>(obs -> {
                    if (!windowController.isFullscreen())
                    {
                        if (DOOM.menuactive || DOOM.paused || DOOM.demoplayback)
                        {
                            EventHandler.menuCaptureChanges(obs, DOOM.mousecaptured = !DOOM.mousecaptured);
                        } else
                        { // can also work when not DOOM.mousecaptured
                            EventHandler.menuCaptureChanges(obs, DOOM.mousecaptured = true);
                        }
                    }
                    return WANTS_MORE_PASS;
                }, SC_LALT)
        ).addInterest(
                new KeyStateInterest<>(obs -> {
                    if (!windowController.isFullscreen() && !DOOM.mousecaptured && DOOM.menuactive)
                    {
                        EventHandler.menuCaptureChanges(obs, DOOM.mousecaptured = true);
                    }

                    return WANTS_MORE_PASS;
                }, SC_ESCAPE)
        ).addInterest(
                new KeyStateInterest<>(obs -> {
                    if (!windowController.isFullscreen() && !DOOM.mousecaptured && DOOM.paused)
                    {
                        EventHandler.menuCaptureChanges(obs, DOOM.mousecaptured = true);
                    }
                    return WANTS_MORE_PASS;
                }, SC_PAUSE)
        );
    }

    /**
     * Mocha Doom engine entry point
     */
    public static void main(String[] argv)
    {
        Engine local;
        lock (typeof(Engine))
        {
            local = new Engine(argv);
        }

        local.DOOM.setupLoop();
    }

    public static void updateFrame()
    {
        instance.windowController.updateFrame();
    }

    public static Engine getEngine()
    {
        var local = instance;
        if (local == null)
        {
            synchronized (Engine.class)
            {
                local = instance;
                if (local == null)
                {
                    try
                    {
                        instance = local = new Engine();
                    }
                    catch (IOException ex)
                    {
                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                        throw new Error("This launch is DOOMed");
                    }
                }
            }
        }

        return local;
    }

    public static CVarManager getCVM()
    {
        return getEngine().cvm;
    }

    public static ConfigManager getConfig()
    {
        return getEngine().cm;
    }

    public String getWindowTitle(double frames)
    {
        return cvm.bool(CommandVariable.SHOWFPS)
                ? String.format("%s - %s FPS: %.2f", Strings.MOCHA_DOOM_TITLE, DOOM.bppMode, frames)
                : String.format("%s - %s", Strings.MOCHA_DOOM_TITLE, DOOM.bppMode);
    }
}
}