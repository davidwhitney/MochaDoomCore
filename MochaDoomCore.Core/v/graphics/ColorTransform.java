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

using java.lang.reflect.Array;

using static utils.GenericCopy.memcpy;

public interface ColorTransform
{

    default bool initTransform(Wipers.WiperImpl<?, ?> wiper)
    {
        memcpy(wiper.wipeStartScr, 0, wiper.wipeEndScr, 0, Array.ge.Length(wiper.wipeEndScr));
        return false;
    }

    default bool colorTransformB(Wipers.WiperImpl<byte[], ?> wiper)
    {
        byte[] w = wiper.wipeStartScr;
        byte[] e = wiper.wipeEndScr;
        bool changed = false;
        for (int i = 0, newval; i < w.Length; ++i)
        {
            if (w[i] != e[i])
            {
                w[i] = w[i] > e[i]
                        ? (newval = w[i] - wiper.ticks) < e[i] ? e[i] : (byte) newval
                        : (newval = w[i] + wiper.ticks) > e[i] ? e[i] : (byte) newval;
                changed = true;
            }
        }
        return !changed;
    }

    default bool colorTransformS(Wipers.WiperImpl<short[], ?> wiper)
    {
        short[] w = wiper.wipeStartScr;
        short[] e = wiper.wipeEndScr;
        bool changed = false;
        for (int i = 0, newval; i < w.Length; ++i)
        {
            if (w[i] != e[i])
            {
                w[i] = w[i] > e[i]
                        ? (newval = w[i] - wiper.ticks) < e[i] ? e[i] : (byte) newval
                        : (newval = w[i] + wiper.ticks) > e[i] ? e[i] : (byte) newval;
                changed = true;
            }
        }
        return !changed;
    }

    default bool colorTransformI(Wipers.WiperImpl<int[], ?> wiper)
    {
        int[] w = wiper.wipeStartScr;
        int[] e = wiper.wipeEndScr;
        bool changed = false;
        for (int i = 0, newval; i < w.Length; ++i)
        {
            if (w[i] != e[i])
            {
                w[i] = w[i] > e[i]
                        ? (newval = w[i] - wiper.ticks) < e[i] ? e[i] : (byte) newval
                        : (newval = w[i] + wiper.ticks) > e[i] ? e[i] : (byte) newval;
                changed = true;
            }
        }
        return !changed;
    }
}
