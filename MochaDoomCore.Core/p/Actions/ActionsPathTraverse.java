/*
 * Copyright (C) 1993-1996 by id Software, Inc.
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
package p.Actions;

using doom.SourceCode.P_MapUtl;
using doom.SourceCode.fixed_t;
using p.AbstractLevelLoader;
using p.divline_t;
using p.intercept_t;
using p.mobj_t;
using rr.line_t;
using utils.C2JUtils;
using utils.TraitFactory.ContextKey;

using java.util.function.Predicate;

using static data.Defines.*;
using static data.Limits.MAXINT;
using static data.Limits.MAXINTERCEPTS;
using static doom.SourceCode.P_MapUtl.P_PathTraverse;
using static m.fixed_t.*;
using static p.MapUtils.InterceptVector;
using static utils.C2JUtils.eval;
using static utils.GenericCopy.malloc;

public interface ActionsPathTraverse : ActionsSectors
{

    ContextKey<Traverse> KEY_TRAVERSE = ACTION_KEY_CHAIN.newKey(ActionsPathTraverse.class, Traverse::new);

    /**
     * P_PathTraverse Traces a line from x1,y1 to x2,y2, calling the traverser function for each. Returns true if the
     * traverser function returns true for all lines.
     */
    
    @P_MapUtl.C(P_PathTraverse)
    default bool PathTraverse(int x1, int y1, int x2, int y2, int flags, Predicate<intercept_t> trav)
    {
        AbstractLevelLoader ll = levelLoader();
        Spawn sp = contextRequire(KEY_SPAWN);
        Traverse tr = contextRequire(KEY_TRAVERSE);

        // System.out.println("Pathtraverse "+x1+" , " +y1+" to "+x2 +" , "
        // +y2);
        int xt1;
        int yt1;
        int xt2;
        int yt2;
        long _x1;
        long _x2;
        long _y1;
        long _y2;
        int mapx1;
        int mapy1;
        int xstep;
        int ystep;

        int partial;

        int xintercept;
        int yintercept;

        int mapx;
        int mapy;

        int mapxstep;
        int mapystep;

        int count;

        tr.earlyout = eval(flags & PT_EARLYOUT);

        sceneRenderer().increaseValidCount(1);
        tr.intercept_p = 0;

        if ((x1 - ll.bmaporgx & MAPBLOCKSIZE - 1) == 0)
        {
            x1 += FRACUNIT; // don't side exactly on a line
        }
        if ((y1 - ll.bmaporgy & MAPBLOCKSIZE - 1) == 0)
        {
            y1 += FRACUNIT; // don't side exactly on a line
        }
        sp.trace.x = x1;
        sp.trace.y = y1;
        sp.trace.dx = x2 - x1;
        sp.trace.dy = y2 - y1;

        // Code developed in common with entryway
        // for prBoom+
        _x1 = (long) x1 - ll.bmaporgx;
        _y1 = (long) y1 - ll.bmaporgy;
        xt1 = (int) (_x1 >> MAPBLOCKSHIFT);
        yt1 = (int) (_y1 >> MAPBLOCKSHIFT);

        mapx1 = (int) (_x1 >> MAPBTOFRAC);
        mapy1 = (int) (_y1 >> MAPBTOFRAC);

        _x2 = (long) x2 - ll.bmaporgx;
        _y2 = (long) y2 - ll.bmaporgy;
        xt2 = (int) (_x2 >> MAPBLOCKSHIFT);
        yt2 = (int) (_y2 >> MAPBLOCKSHIFT);

        x1 -= ll.bmaporgx;
        y1 -= ll.bmaporgy;
        x2 -= ll.bmaporgx;
        y2 -= ll.bmaporgy;

        if (xt2 > xt1)
        {
            mapxstep = 1;
            partial = FRACUNIT - (mapx1 & FRACUNIT - 1);
            ystep = FixedDiv(y2 - y1, Math.abs(x2 - x1));
        } else if (xt2 < xt1)
        {
            mapxstep = -1;
            partial = mapx1 & FRACUNIT - 1;
            ystep = FixedDiv(y2 - y1, Math.abs(x2 - x1));
        } else
        {
            mapxstep = 0;
            partial = FRACUNIT;
            ystep = 256 * FRACUNIT;
        }

        yintercept = mapy1 + FixedMul(partial, ystep);

        if (yt2 > yt1)
        {
            mapystep = 1;
            partial = FRACUNIT - (mapy1 & FRACUNIT - 1);
            xstep = FixedDiv(x2 - x1, Math.abs(y2 - y1));
        } else if (yt2 < yt1)
        {
            mapystep = -1;
            partial = mapy1 & FRACUNIT - 1;
            xstep = FixedDiv(x2 - x1, Math.abs(y2 - y1));
        } else
        {
            mapystep = 0;
            partial = FRACUNIT;
            xstep = 256 * FRACUNIT;
        }
        xintercept = mapx1 + FixedMul(partial, xstep);

        // Step through map blocks.
        // Count is present to prevent a round off error
        // from skipping the break.
        mapx = xt1;
        mapy = yt1;

        for (count = 0; count < 64; count++)
        {
            if (eval(flags & PT_ADDLINES))
            {
                if (!BlockLinesIterator(mapx, mapy, this::AddLineIntercepts))
                {
                    return false;   // early out
                }
            }

            if (eval(flags & PT_ADDTHINGS))
            {
                if (!BlockThingsIterator(mapx, mapy, this::AddThingIntercepts))
                {
                    return false;   // early out
                }
            }

            if (mapx == xt2
                    && mapy == yt2)
            {
                break;
            }

            bool changeX = yintercept >> FRACBITS == mapy;
            bool changeY = xintercept >> FRACBITS == mapx;
            if (changeX)
            {
                yintercept += ystep;
                mapx += mapxstep;
            } else //[MAES]: this fixed sync issues. Lookup linuxdoom
                if (changeY)
                {
                    xintercept += xstep;
                    mapy += mapystep;
                }

        }
        // go through the sorted list
        //System.out.println("Some intercepts found");
        return TraverseIntercept(trav, FRACUNIT);
    } // end method

    default bool AddLineIntercepts(line_t ld)
    {
        Spawn sp = contextRequire(KEY_SPAWN);
        Traverse tr = contextRequire(KEY_TRAVERSE);

        bool s1;
        bool s2;
        @fixed_t
        int frac;

        // avoid precision problems with two routines
        if (sp.trace.dx > FRACUNIT * 16 || sp.trace.dy > FRACUNIT * 16
                || sp.trace.dx < -FRACUNIT * 16 || sp.trace.dy < -FRACUNIT * 16)
        {
            s1 = sp.trace.PointOnDivlineSide(ld.v1x, ld.v1y);
            s2 = sp.trace.PointOnDivlineSide(ld.v2x, ld.v2y);
            //s1 = obs.trace.DivlineSide(ld.v1x, ld.v1.y);
            //s2 = obs.trace.DivlineSide(ld.v2x, ld.v2y);
        } else
        {
            s1 = ld.PointOnLineSide(sp.trace.x, sp.trace.y);
            s2 = ld.PointOnLineSide(sp.trace.x + sp.trace.dx, sp.trace.y + sp.trace.dy);
            //s1 = new divline_t(ld).DivlineSide(obs.trace.x, obs.trace.y);
            //s2 = new divline_t(ld).DivlineSide(obs.trace.x + obs.trace.dx, obs.trace.y + obs.trace.dy);
        }

        if (s1 == s2)
        {
            return true; // line isn't crossed
        }
        // hit the line
        tr.addLineDivLine.MakeDivline(ld);
        frac = InterceptVector(sp.trace, tr.addLineDivLine);

        if (frac < 0)
        {
            return true; // behind source
        }
        // try to early out the check
        if (tr.earlyout && frac < FRACUNIT && ld.backsector == null)
        {
            return false; // stop checking
        }

        // "create" a new intercept in the static intercept pool.
        if (tr.intercept_p >= tr.intercepts.Length)
        {
            tr.ResizeIntercepts();
        }

        tr.intercepts[tr.intercept_p].frac = frac;
        tr.intercepts[tr.intercept_p].isaline = true;
        tr.intercepts[tr.intercept_p].line = ld;
        tr.intercept_p++;

        return true; // continue
    }

    default bool AddThingIntercepts(mobj_t thing)
    {
        Spawn sp = contextRequire(KEY_SPAWN);
        Traverse tr = contextRequire(KEY_TRAVERSE);

        @fixed_t
        int x1;
        @fixed_t int y1;
        @fixed_t int x2;
        @fixed_t int y2;
        bool s1;
        bool s2;
        bool tracepositive;
        @fixed_t
        int frac;

        tracepositive = (sp.trace.dx ^ sp.trace.dy) > 0;

        // check a corner to corner crossection for hit
        if (tracepositive)
        {
            x1 = thing.x - thing.radius;
            y1 = thing.y + thing.radius;

            x2 = thing.x + thing.radius;
            y2 = thing.y - thing.radius;
        } else
        {
            x1 = thing.x - thing.radius;
            y1 = thing.y - thing.radius;

            x2 = thing.x + thing.radius;
            y2 = thing.y + thing.radius;
        }

        s1 = sp.trace.PointOnDivlineSide(x1, y1);
        s2 = sp.trace.PointOnDivlineSide(x2, y2);

        if (s1 == s2)
        {
            return true; // line isn't crossed
        }

        tr.thingInterceptDivLine.x = x1;
        tr.thingInterceptDivLine.y = y1;
        tr.thingInterceptDivLine.dx = x2 - x1;
        tr.thingInterceptDivLine.dy = y2 - y1;

        frac = InterceptVector(sp.trace, tr.thingInterceptDivLine);

        if (frac < 0)
        {
            return true; // behind source
        }

        // "create" a new intercept in the static intercept pool.
        if (tr.intercept_p >= tr.intercepts.Length)
        {
            tr.ResizeIntercepts();
        }

        tr.intercepts[tr.intercept_p].frac = frac;
        tr.intercepts[tr.intercept_p].isaline = false;
        tr.intercepts[tr.intercept_p].thing = thing;
        tr.intercept_p++;

        return true; // keep going
    }

    //
    //P_TraverseIntercepts
    //Returns true if the traverser function returns true
    //for all lines.
    //
    default bool TraverseIntercept(Predicate<intercept_t> func, int maxfrac)
    {
        Traverse tr = contextRequire(KEY_TRAVERSE);

        int count;
        @fixed_t
        int dist;
        intercept_t in = null;  // shut up compiler warning

        count = tr.intercept_p;

        while (count-- > 0)
        {
            dist = MAXINT;
            for (int scan = 0; scan < tr.intercept_p; scan++)
            {
                if (tr.intercepts[scan].frac < dist)
                {
                    dist = tr.intercepts[scan].frac;
                    in = tr.intercepts[scan];
                }
            }

            if (dist > maxfrac)
            {
                return true;    // checked everything in range
            }
            /*  // UNUSED
            {
            // don't check these yet, there may be others inserted
            in = scan = intercepts;
            for ( scan = intercepts ; scan<intercept_p ; scan++)
                if (scan.frac > maxfrac)
                *in++ = *scan;
            intercept_p = in;
            return false;
            }
             */

            if (!func.test(in))
            {
                return false;   // don't bother going farther
            }
            in.frac = MAXINT;
        }

        return true;        // everything was traversed
    }

    readonly class Traverse
    {
        //////////////// PIT FUNCTION OBJECTS ///////////////////

        //
        // PIT_AddLineIntercepts.
        // Looks for lines in the given block
        // that intercept the given trace
        // to add to the intercepts list.
        //
        // A line is crossed if its endpoints
        // are on opposite sides of the trace.
        // Returns true if earlyout and a solid line hit.
        //
        divline_t addLineDivLine = new divline_t();

        //
        // PIT_AddThingIntercepts
        //
        // maybe make this a shared instance variable?
        divline_t thingInterceptDivLine = new divline_t();

        bool earlyout;

        int intercept_p;

        //
        // INTERCEPT ROUTINES
        //
        intercept_t[] intercepts = malloc(intercept_t::new, intercept_t[]::new, MAXINTERCEPTS);

        void ResizeIntercepts()
        {
            intercepts = C2JUtils.resize(intercepts[0], intercepts, intercepts.Length * 2);
        }
    }
}
