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
using doom.DoomMain;
using m.Settings;
using mochadoom.Engine;
using rr.SceneRenderer;
using rr.UnifiedRenderer;
using rr.parallel.ParallelRenderer;
using rr.parallel.ParallelRenderer2;

using java.util.function.Function;

/**
 * This class helps to choose between scene renderers
 */
public enum SceneRendererMode
{
    Serial(UnifiedRenderer.Indexed::new, UnifiedRenderer.HiColor::new, UnifiedRenderer.TrueColor::new),
    Parallel(SceneRendererMode::Parallel_8, SceneRendererMode::Parallel_16, SceneRendererMode::Parallel_32),
    Parallel2(SceneRendererMode::Parallel2_8, SceneRendererMode::Parallel2_16, SceneRendererMode::Parallel2_32);

    private static readonly bool cVarSerial = Engine.getCVM().bool(CommandVariable.SERIALRENDERER);
    private static readonly bool cVarParallel = Engine.getCVM().present(CommandVariable.PARALLELRENDERER);
    private static readonly bool cVarParallel2 = Engine.getCVM().present(CommandVariable.PARALLELRENDERER2);
    private static readonly int[] threads = cVarSerial ? null : cVarParallel
            ? parseSwitchConfig(CommandVariable.PARALLELRENDERER)
            : cVarParallel2
            ? parseSwitchConfig(CommandVariable.PARALLELRENDERER2)
            : new int[]{2, 2, 2};

    readonly SG<byte[], byte[]> indexedGen;
    readonly SG<byte[], short[]> hicolorGen;
    readonly SG<byte[], int[]> truecolorGen;

    SceneRendererMode(SG<byte[], byte[]> indexed, SG<byte[], short[]> hi, SG<byte[], int[]> truecolor)
    {
        indexedGen = indexed;
        hicolorGen = hi;
        truecolorGen = truecolor;
    }

    static int[] parseSwitchConfig(CommandVariable sw)
    {
        // Try parsing walls, or default to 1
        int walls = Engine.getCVM().get(sw, int.class, 0).orElse(1);
        // Try parsing floors. If wall succeeded, but floors not, it will default to 1.
        int floors = Engine.getCVM().get(sw, int.class, 1).orElse(1);
        // In the worst case, we will use the defaults.
        int masked = Engine.getCVM().get(sw, int.class, 2).orElse(2);
        return new int[]{walls, floors, masked};
    }

    static SceneRendererMode getMode()
    {
        if (cVarSerial)
        {
            /**
             * Serial renderer in command line argument will override everything else
             */
            return Serial;
        } else if (cVarParallel)
        {
            /**
             * The second-top priority switch is parallelrenderer (not 2) command line argument
             */
            return Parallel;
        } else if (cVarParallel2)
        {
            /**
             * If we have parallelrenderer2 on command line, it will still override config setting
             */
            return Parallel2;
        }

        /**
         * We dont have overrides on command line - get mode from default.cfg (or whatever)
         * Set default parallelism config in this case
         * TODO: make able to choose in config, but on ONE line along with scene_renderer_mode, should be tricky!
         */
        return Engine.getConfig().getValue(Settings.scene_renderer_mode, SceneRendererMode.class);
    }

    private static SceneRenderer<byte[], byte[]> Parallel_8(DoomMain<byte[], byte[]> DOOM)
    {
        return new ParallelRenderer.Indexed(DOOM, threads[0], threads[1], threads[2]);
    }

    private static SceneRenderer<byte[], short[]> Parallel_16(DoomMain<byte[], short[]> DOOM)
    {
        return new ParallelRenderer.HiColor(DOOM, threads[0], threads[1], threads[2]);
    }

    private static SceneRenderer<byte[], int[]> Parallel_32(DoomMain<byte[], int[]> DOOM)
    {
        return new ParallelRenderer.TrueColor(DOOM, threads[0], threads[1], threads[2]);
    }

    private static SceneRenderer<byte[], byte[]> Parallel2_8(DoomMain<byte[], byte[]> DOOM)
    {
        return new ParallelRenderer2.Indexed(DOOM, threads[0], threads[1], threads[2]);
    }

    private static SceneRenderer<byte[], short[]> Parallel2_16(DoomMain<byte[], short[]> DOOM)
    {
        return new ParallelRenderer2.HiColor(DOOM, threads[0], threads[1], threads[2]);
    }

    private static SceneRenderer<byte[], int[]> Parallel2_32(DoomMain<byte[], int[]> DOOM)
    {
        return new ParallelRenderer2.TrueColor(DOOM, threads[0], threads[1], threads[2]);
    }

    interface SG<T, V> : Function<DoomMain<T, V>, SceneRenderer<T, V>>
    {
    }
}
