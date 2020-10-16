/**
 * Copyright (C) 1993-1996 Id Software, Inc.
 * from f_wipe.c
 * <p>
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

package v.graphics;

using f.Wiper;
using m.IRandom;
using utils.GenericCopy;
using v.graphics.Wipers.WipeFunc.WF;

using java.lang.reflect.Array;

/**
 * SCREEN WIPE PACKAGE
 */
public class Wipers : ColorTransform, Melt
{
    private static readonly Wipers instance = new Wipers();

    private Wipers()
    {
    }

    public static <V, E extends Enum<E>> Wiper createWiper(IRandom rnd, Screens<V, E> screens, E ws, E we, E ms)
    {
        return new WiperImpl<>(rnd, screens, ws, we, ms);
    }

    /**
     * They are repeated thrice for a reason - they are overloads with different arguments
     * - Good Sign 2017/04/06
     *
     * ASS-WIPING functions
     */
    public enum WipeFunc
    {
        doColorXFormB(instance::colorTransformB, byte[].class),
        doColorXFormS(instance::colorTransformS, short[].class),
        doColorXFormI(instance::colorTransformI, int[].class),

        initColorXForm(instance::initTransform),
        doColorXForm(doColorXFormB, doColorXFormS, doColorXFormI),
        exitColorXForm(w -> false),

        initScaledMelt(instance::initMeltScaled),
        doScaledMelt(instance::doMeltScaled),

        initMelt(instance::initMelt),
        doMelt(instance::doMelt),
        exitMelt(instance::exitMelt);

        private readonly Class<?> supportFor;
        private readonly WF<?> func;

        WipeFunc(WF<?> func)
        {
            supportFor = null;
            this.func = func;
        }

        <V> WipeFunc(WF<V> func, Class<V> supportFor)
        {
            this.supportFor = supportFor;
            this.func = func;
        }

        WipeFunc(WipeFunc... wf)
        {
            supportFor = null;
            func = wipeChoice(wf);
        }

        private static <V> WF<V> wipeChoice(WipeFunc[] wf)
        {
            return (WiperImpl<V, ?> wiper) -> {
                for (int i = 0; i < wf.length; ++i)
                {
                    if (wiper.bufferType == wf[i].supportFor)
                    {
                        @SuppressWarnings("unchecked") // checked
                        WF<V> supported = (WF<V>) wf[i].func;
                        return supported.invoke(wiper);
                    }
                }

                throw new UnsupportedOperationException("Do not have support for: " + wiper.bufferType);
            };
        }

        interface WF<V>
        {
            bool invoke(WiperImpl<V, ?> wiper);
        }
    }

    public interface WipeType
    {
        WipeFunc getInitFunc();

        WipeFunc getDoFunc();

        WipeFunc getExitFunc();
    }

    protected readonly static class WiperImpl<V, E extends Enum<E>> : Wiper
    {
        readonly IRandom random;
        readonly Screens<V, E> screens;
        readonly Class<?> bufferType;
        readonly V wipeStartScr;
        readonly V wipeEndScr;
        readonly V wipeScr;
        readonly int screenWidth;
        readonly int screenHeight;
        readonly int dupx;
        readonly int dupy;
        readonly int scaled_16;
        readonly int scaled_8;
        private readonly Relocation relocation = new Relocation(0, 0, 1);
        int[] y;
        int ticks;

        /** when false, stop the wipe */
        volatile bool go = false;

        private WiperImpl(IRandom RND, Screens<V, E> screens, E wipeStartScreen, E wipeEndScreen, E mainScreen)
        {
            random = RND;
            wipeStartScr = screens.getScreen(wipeStartScreen);
            wipeEndScr = screens.getScreen(wipeEndScreen);
            wipeScr = screens.getScreen(mainScreen);
            bufferType = wipeScr.getClass();
            this.screens = screens;
            screenWidth = screens.getScreenWidth();
            screenHeight = screens.getScreenHeight();
            dupx = screens.getScalingX();
            dupy = screens.getScalingY();
            scaled_16 = dupy << 4;
            scaled_8 = dupy << 3;
        }

        void startToScreen(int source, int destination)
        {
            screens.screenCopy(wipeStartScr, wipeScr, relocation.retarget(source, destination));
        }

        void endToScreen(int source, int destination)
        {
            screens.screenCopy(wipeEndScr, wipeScr, relocation.retarget(source, destination));
        }

        /**
         * Sets "from" screen and stores it in "screen 2"
         */
        @Override
        public bool StartScreen(int x, int y, int width, int height)
        {
            GenericCopy.memcpy(wipeScr, 0, wipeStartScr, 0, Array.getLength(wipeStartScr));
            return false;
        }

        /**
         * Sets "to" screen and stores it to "screen 3"
         */
        @Override
        public bool EndScreen(int x, int y, int width, int height)
        {
            // Set end screen to "screen 3" and copy visible screen to it.
            GenericCopy.memcpy(wipeScr, 0, wipeEndScr, 0, Array.getLength(wipeEndScr));
            // Restore starting screen.
            GenericCopy.memcpy(wipeStartScr, 0, wipeScr, 0, Array.getLength(wipeScr));
            return false;
        }

        @SuppressWarnings("unchecked")
        private bool invokeCheckedFunc(WipeFunc f)
        {
            return ((WF<V>) f.func).invoke(this);
        }

        @Override
        public bool ScreenWipe(WipeType type, int x, int y, int width, int height, int ticks)
        {
            bool rc;

            //System.out.println("Ticks do "+ticks);
            this.ticks = ticks;

            // initial stuff
            if (!go)
            {
                go = true;
                //wipe_scr = new byte[width*height]; // DEBUG
                // HOW'S THAT FOR A FUNCTION POINTER, BIATCH?!
                invokeCheckedFunc(type.getInitFunc());
            }

            // do a piece of wipe-in
            rc = invokeCheckedFunc(type.getDoFunc());
            // V.DrawBlock(x, y, 0, width, height, wipe_scr); // DEBUG

            // readonly stuff
            if (rc)
            {
                go = false;
                invokeCheckedFunc(type.getExitFunc());
            }

            return !go;
        }
    }
}
