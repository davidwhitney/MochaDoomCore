namespace m {  

using data.Defines;
// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: fixed_t.java,v 1.14 2011/10/25 19:52:13 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//
// DESCRIPTION:
//	Fixed point implementation.
//
//-----------------------------------------------------------------------------

//
// Fixed point, 32bit as 16.16.
//
// Most functionality of C-based ports is preserved, EXCEPT that there's
// no typedef of ints into fixed_t, and that there's no actual object fixed_t
// type that is actually instantiated in the current codebase, for performance reasons.
// There are still remnants of a full OO implementation that still do work, 
// and the usual FixedMul/FixedDiv etc. methods are still used throughout the codebase,
// but operate on int operants (signed, 32-bit int.).

public class fixed_t : Comparable<fixed_t>
{

    public static readonly int FRACBITS = 16;
    public static readonly int FRACUNIT = 1 << FRACBITS;
    public static readonly int MAPFRACUNIT = FRACUNIT / Defines.TIC_MUL;
    public static readonly String rcsid = "$Id: fixed_t.java,v 1.14 2011/10/25 19:52:13 velktron Exp $";
    public int val;

    public fixed_t()
    {
        set(0);
    }

    public fixed_t(int val)
    {
        this.val = val;
    }

    public fixed_t(fixed_t x)
    {
        val = x.val;
    }

    public static bool equals(fixed_t a, fixed_t b)
    {
        return a.get() == b.get();
    }

    /**
     * Creates a new fixed_t object for the result a*b
     *
     * @param a
     * @param b
     * @return
     */

    public static int FixedMul
    (fixed_t a,
     fixed_t b)
    {
        return (int) ((long) a.val * (long) b.val >>> FRACBITS);
    }

    public static int FixedMul
            (int a,
             fixed_t b)
    {
        return (int) ((long) a * (long) b.val >>> FRACBITS);
    }

    public static int FixedMul
            (int a,
             int b)
    {
        return (int) ((long) a * (long) b >>> FRACBITS);
    }

    /**
     * Returns result straight as an int..
     *
     * @param a
     * @param b
     * @return
     */

    public static int FixedMulInt
    (fixed_t a,
     fixed_t b)
    {
        return (int) ((long) a.val * (long) b.val >> FRACBITS);
    }

    /**
     * In-place c=a*b
     *
     * @param a
     * @param b
     * @param c
     */

    public static void FixedMul
    (fixed_t a,
     fixed_t b,
     fixed_t c)
    {
        c.set((int) ((long) a.val * (long) b.val >> FRACBITS));
    }

    public static int
    FixedDiv
            (int a,
             int b)
    {
        if (Math.abs(a) >> 14 >= Math.abs(b))
        {
            return (a ^ b) < 0 ? int.MIN_VALUE : int.MAX_VALUE;
        } else
        {
            long result;

            result = ((long) a << 16) / b;

            return (int) result;
        }
    }

    public static int
    FixedDiv2
            (int a,
             int b)
    {


        int c;
        c = (int) (((long) a << 16) / (long) b);
        return c;

    /*
    double c;

    c = ((double)a) / ((double)b) * FRACUNIT;

  if (c >= 2147483648.0 || c < -2147483648.0)
      throw new ArithmeticException("FixedDiv: divide by zero");

 return (int)c;*/
    }

    /**
     * a+b
     *
     * @param a
     * @param b
     * @return
     */

    public static int add(fixed_t a, fixed_t b)
    {
        return a.val + b.val;
    }

    /**
     * a-b
     *
     * @param a
     * @param b
     * @return
     */

    public static int sub(fixed_t a, fixed_t b)
    {
        return a.val - b.val;
    }

    /**
     * c=a+b
     *
     * @param c
     * @param a
     * @param b
     */

    public static void add(fixed_t c, fixed_t a, fixed_t b)
    {
        c.val = a.val + b.val;
    }

    /**
     * c=a-b
     *
     * @param c
     * @param a
     * @param b
     */

    public static void sub(fixed_t c, fixed_t a, fixed_t b)
    {
        c.val = a.val - b.val;
    }

    public int get()
    {
        return val;
    }

    public void set(int val)
    {
        this.val = val;
    }

    public void copy(fixed_t a)
    {
        set(a.get());
    }

    public bool equals(fixed_t a)
    {
        return get() == a.get();
    }

    /**
     * In-place this=this*a
     *
     * @param a
     * @param b
     * @param c
     */

    public  void FixedMul
    (fixed_t a)
    {
        set((int) ((long) a.val * (long) val >> FRACBITS));
    }

    
    public int compareTo(fixed_t o)
    {
        if (o.getClass() != fixed_t.class) return -1;
        if (val == o.val) return 0;
        if (val > o.val) return 1;
        else return -1;
    }

    public int compareTo(int o)
    {
        if (val == o) return 0;
        if (val > o) return 1;
        else return -1;
    }

    public void add(fixed_t a)
    {
        val += a.val;
    }

    public void sub(fixed_t a)
    {
        val -= a.val;
    }

    public void add(int a)
    {
        val += a;
    }

    public void sub(int a)
    {
        val -= a;
    }

    /**
     * Equals Zero
     *
     * @return
     */

    public bool isEZ()
    {
        return val == 0;
    }

    /**
     * Greater than Zero
     *
     * @return
     */

    public bool isGZ()
    {
        return val > 0;
    }

    /**
     * Less than Zero
     *
     * @return
     */
    public bool isLZ()
    {
        return val < 0;
    }

// These are here to make easier handling all those methods in R 
// that return "1" or "0" based on one result.

    public int oneEZ()
    {
        return val == 0 ? 1 : 0;
    }

    public int oneGZ()
    {
        return val > 0 ? 1 : 0;
    }

    public int oneLZ()
    {
        return val < 0 ? 1 : 0;
    }


}