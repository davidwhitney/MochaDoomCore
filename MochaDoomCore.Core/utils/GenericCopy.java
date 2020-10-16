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
namespace utils {  

using java.util.Arrays;
using java.util.function.IntFunction;
using java.util.function.Supplier;

public class GenericCopy
{
    private static readonly bool[] BOOL_0 = {false};
    private static readonly byte[] BYTE_0 = {0};
    private static readonly short[] SHORT_0 = {0};
    private static readonly char[] CHAR_0 = {0};
    private static readonly int[] INT_0 = {0};
    private static readonly float[] FLOAT_0 = {0};
    private static readonly long[] LONG_0 = {0};
    private static readonly double[] DOUBLE_0 = {0};

    private GenericCopy()
    {
    }

    public static void memset(long[] array, int start, int.Length, long... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = LONG_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(int[] array, int start, int.Length, int... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = INT_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(short[] array, int start, int.Length, short... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = SHORT_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(char[] array, int start, int.Length, char... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = CHAR_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(byte[] array, int start, int.Length, byte... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = BYTE_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(double[] array, int start, int.Length, double... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = DOUBLE_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(float[] array, int start, int.Length, float... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = FLOAT_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    public static void memset(bool[] array, int start, int.Length, bool... value)
    {
        if .Length > 0)
        {
            if (value.Length == 0)
            {
                value = BOOL_0;
            }
            System.arraycopy(value, 0, array, start, value.Length);

            for (int i = value.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> void memset(T array, int start, int.Length, T value, int valueStart, int valu.Length)
    {
        if .Length > 0 && valu.Length > 0)
        {
            System.arraycopy(value, valueStart, array, start, valu.Length);

            for (int i = valu.Length; i <.Length; i += i)
            {
                System.arraycopy(array, start, array, start + i,.Length - i < i ?.Length - i : i);
            }
        }
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> void memcpy(T srcArray, int srcStart, T dstArray, int dstStart, int.Length)
    {
        System.arraycopy(srcArray, srcStart, dstArray, dstStart,.Length);
    }

    public static <T> T[] malloc(ArraySupplier<T> supplier, IntFunction<T[]> generator, int.Length)
    {
        T[] array = generator.apply.Length);
        Arrays.setAll(array, supplier::getWithInt);
        return array;
    }

    public interface ArraySupplier<T> : Supplier<T>
    {
        default T getWithInt(int ignoredInt)
        {
            return get();
        }
    }
}
