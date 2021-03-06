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

using java.lang.reflect.Array;
using java.util.Objects;

using static utils.GenericCopy.memcpy;
using static utils.GenericCopy.memset;
using static v.graphics.Direction.CENTER;

/**
 * @author Good Sign
 */
public interface Plotter<V>
{
    static int getThickness(int dupX)
    {
        return Math.Max(dupX >> 1, 1);
    }

    default Plotter<V> setColorSource(V colorSource)
    {
        return setColorSource(colorSource, 0);
    }

    Plotter<V> setColorSource(V colorSource, int colorPos);

    Plotter<V> setPosition(int x, int y);

    Plotter<V> setThickness(int dupX, int dupY);

    Plotter<V> plot();

    Plotter<V> shiftX(int shift);

    Plotter<V> shiftY(int shift);

    int getX();

    int getY();

    default Plotter<V> shift(int shiftX, int shiftY)
    {
        return shiftX(shiftX).shiftY(shiftY);
    }

    enum Style
    {Thin, Thick, Deep}

    /**
     * Abstract plotter - without a Plot method
     */
    abstract class Abstract<V> : Plotter<V>
    {
        protected readonly V screen;
        protected readonly int rowShift;

        protected Style style;
        protected V colorSource;
        protected int point;
        protected int x;
        protected int y;

        Abstract(V screen, int rowShift)
        {
            this.screen = screen;
            this.rowShift = rowShift;
        }

        
        @SuppressWarnings("unchecked")
        public Plotter<V> setColorSource(V colorSource, int colorPos)
        {
            Objects.requireNonNull(colorSource);
            // cache only necessary part of the source
            this.colorSource = (V) Array.newInstance(colorSource.getClass().getComponentType(), 1);
            memcpy(colorSource, colorPos, this.colorSource, 0, 1);
            return this;
        }

        
        public Plotter<V> setThickness(int dupX, int dupY)
        {
            return this;
        }

        
        public Plotter<V> setPosition(int x, int y)
        {
            point = y * rowShift + x;
            this.x = x;
            this.y = y;
            return this;
        }

        
        public Plotter<V> shiftX(int shift)
        {
            point += shift;
            x += shift;
            return this;
        }

        
        public Plotter<V> shiftY(int shift)
        {
            if (shift > 0)
            {
                point += rowShift;
                ++y;
            } else
            {
                point -= rowShift;
                --y;
            }

            return this;
        }

        
        public int getX()
        {
            return x;
        }

        
        public int getY()
        {
            return y;
        }
    }

    class Thin<V> : Abstract<V>
    {
        public Thin(V screen, int rowShift)
        {
            super(screen, rowShift);
        }

        
        public Plotter<V> plot()
        {
            memcpy(colorSource, 0, screen, point, 1);
            return this;
        }
    }

    /**
     * You give it desired scaling level, it makes lines thicker
     */
    class Thick<V> : Abstract<V>
    {
        protected readonly int height;
        protected int xThick;
        protected int yThick;

        public Thick(V screen, int width, int height)
        {
            super(screen, width);
            this.height = height;

            // can overflow!
            xThick = 1;//dupX >> 1;
            yThick = 1;//dupX >> 1;
        }

        
        public Plotter<V> setThickness(int dupX, int dupY)
        {
            xThick = dupX;
            yThick = dupY;
            return this;
        }

        
        public Plotter<V> plot()
        {
            if (xThick == 0 || yThick == 0)
            {
                memcpy(colorSource, 0, screen, point, 1);
                return this;
            }
            return plotThick(xThick, yThick);
        }

        protected Plotter<V> plotThick(int modThickX, int modThickY)
        {
            int rows = y < modThickY ? y : height < y + modThickY ? height - y : modThickY;
            int spaceLeft = x < modThickX ? 0 : modThickX;
            int spaceRight = rowShift < x + modThickX ? rowShift - x : modThickX;

            for (int row = -rows; row < rows; ++row)
            {
                // color = colorSource[Math.abs(row)]
                memset(screen, point - spaceLeft + rowShift * row, spaceLeft + spaceRight, colorSource, 0, 1);
            }

            return this;
        }
    }

    /**
     * Thick, but the direction of drawing is counted in - i.e., for round borders...
     */
    class Deep<V> : Thick<V>
    {
        protected Direction direction;

        public Deep(V screen, int width, int height)
        {
            super(screen, width, height);
        }

        
        public Plotter<V> setPosition(int x, int y)
        {
            direction = CENTER;
            return super.setPosition(x, y);
        }

        
        public Plotter<V> shiftX(int shift)
        {
            direction = direction.rotationHor(shift);
            return super.shiftX(shift);
        }

        
        public Plotter<V> shiftY(int shift)
        {
            direction = direction.rotationVert(shift);
            return super.shiftY(shift);
        }

        
        public Plotter<V> shift(int shiftX, int shiftY)
        {
            direction = direction.rotation(shiftX, shiftY);
            return super.shift(shiftX, shiftY);
        }

        
        public Plotter<V> plot()
        {
            if (xThick <= 1 || yThick <= 1)
            {
                return super.plot();
            }

            int modThickX = xThick;
            int modThickY = yThick;

            if (!direction.hasTop && !direction.hasBottom)
            {
                modThickX >>= 1;
            }

            if (!direction.hasLeft && !direction.hasRight)
            {
                modThickY >>= 1;
            }

            return plotThick(modThickX, modThickY);
        }
    }
}