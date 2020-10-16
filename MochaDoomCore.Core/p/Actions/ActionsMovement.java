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

using defines.slopetype_t;
using defines.statenum_t;
using doom.SourceCode;
using doom.SourceCode.fixed_t;
using doom.player_t;
using p.intercept_t;
using p.mobj_t;
using rr.SceneRenderer;
using rr.line_t;
using utils.TraitFactory.ContextKey;

using static data.Defines.FLOATSPEED;
using static data.Defines.PT_ADDLINES;
using static data.Limits.MAXMOVE;
using static data.Tables.*;
using static doom.SourceCode.P_Map.PTR_SlideTraverse;
using static m.fixed_t.FRACUNIT;
using static m.fixed_t.FixedMul;
using static p.ChaseDirections.*;
using static p.MapUtils.AproxDistance;
using static p.mobj_t.*;
using static rr.line_t.ML_TWOSIDED;
using static utils.C2JUtils.eval;

public interface ActionsMovement extends ActionsPathTraverse
{

    ContextKey<DirType> KEY_DIRTYPE = ACTION_KEY_CHAIN.newKey(ActionsMovement.class, DirType::new);

    //
    // P_XYMovement
    //
    int STOPSPEED = 4096;
    int FRICTION = 59392;
    int FUDGE = 2048; ///(FRACUNIT/MAPFRACUNIT);

    void UnsetThingPosition(mobj_t thing);

    void ExplodeMissile(mobj_t mo);

    /**
     * If "floatok" true, move would be ok if within "tmfloorz - tmceilingz".
     */
    //
    // P_Move
    // Move in the current direction,
    // returns false if the move is blocked.
    //
    default bool Move(mobj_t actor)
    {
        var mov = contextRequire(KEY_MOVEMENT);
        var sp = contextRequire(KEY_SPECHITS);

        @fixed_t
        int tryx;
        @fixed_t int tryy;
        line_t ld;

        // warning: 'catch', 'throw', and 'try'
        // are all C++ reserved words
        bool try_ok;
        bool good;

        if (actor.movedir == DI_NODIR)
        {
            return false;
        }

        if (actor.movedir >= 8)
        {
            doomSystem().Error("Weird actor.movedir!");
        }

        tryx = actor.x + actor.info.speed * xspeed[actor.movedir];
        tryy = actor.y + actor.info.speed * yspeed[actor.movedir];

        try_ok = TryMove(actor, tryx, tryy);

        if (!try_ok)
        {
            // open any specials
            if (eval(actor.flags & MF_FLOAT) && mov.floatok)
            {
                // must adjust height
                if (actor.z < mov.tmfloorz)
                {
                    actor.z += FLOATSPEED;
                } else
                {
                    actor.z -= FLOATSPEED;
                }

                actor.flags |= MF_INFLOAT;
                return true;
            }

            if (sp.numspechit == 0)
            {
                return false;
            }

            actor.movedir = DI_NODIR;
            good = false;
            while (sp.numspechit-- > 0)
            {
                ld = sp.spechit[sp.numspechit];
                // if the special is not a door
                // that can be opened,
                // return false
                if (UseSpecialLine(actor, ld, false))
                {
                    good = true;
                }
            }
            return good;
        } else
        {
            actor.flags &= ~MF_INFLOAT;
        }

        if (!eval(actor.flags & MF_FLOAT))
        {
            actor.z = actor.floorz;
        }
        return true;
    }

    ///////////////// MOVEMENT'S ACTIONS ////////////////////////

    /**
     * // P_TryMove // Attempt to move to a new position, // crossing special lines unless MF_TELEPORT is set.
     *
     * @param x fixed_t
     * @param y fixed_t
     */
    default bool TryMove(mobj_t thing, @fixed_t int x, @fixed_t int y)
    {
        var mov = contextRequire(KEY_MOVEMENT);
        var sp = contextRequire(KEY_SPECHITS);

        @fixed_t
        int oldx;
        @fixed_t int oldy;
        bool side;  // both were int
        bool oldside;
        line_t ld;

        mov.floatok = false;
        if (!CheckPosition(thing, x, y))
        {
            return false;       // solid wall or thing
        }
        if (!eval(thing.flags & MF_NOCLIP))
        {
            if (mov.tmceilingz - mov.tmfloorz < thing.height)
            {
                return false;   // doesn't fit
            }
            mov.floatok = true;

            if (!eval(thing.flags & MF_TELEPORT) && mov.tmceilingz - thing.z < thing.height)
            {
                return false;   // mobj must lower itself to fit
            }
            if (!eval(thing.flags & MF_TELEPORT) && mov.tmfloorz - thing.z > 24 * FRACUNIT)
            {
                return false;   // too big a step up
            }
            if (!eval(thing.flags & (MF_DROPOFF | MF_FLOAT)) && mov.tmfloorz - mov.tmdropoffz > 24 * FRACUNIT)
            {
                return false;   // don't stand over a dropoff
            }
        }

        // the move is ok,
        // so link the thing into its new position
        UnsetThingPosition(thing);

        oldx = thing.x;
        oldy = thing.y;
        thing.floorz = mov.tmfloorz;
        thing.ceilingz = mov.tmceilingz;
        thing.x = x;
        thing.y = y;

        levelLoader().SetThingPosition(thing);

        // if any special lines were hit, do the effect
        if (!eval(thing.flags & (MF_TELEPORT | MF_NOCLIP)))
        {
            while (sp.numspechit-- > 0)
            {
                // see if the line was crossed
                ld = sp.spechit[sp.numspechit];
                side = ld.PointOnLineSide(thing.x, thing.y);
                oldside = ld.PointOnLineSide(oldx, oldy);
                if (side != oldside)
                {
                    if (ld.special != 0)
                    {
                        CrossSpecialLine(ld, oldside ? 1 : 0, thing);
                    }
                }
            }
        }

        return true;
    }

    default void NewChaseDir(mobj_t actor)
    {
        var dirtype = contextRequire(KEY_DIRTYPE);

        @fixed_t
        int deltax;
        @fixed_t int deltay;

        int tdir;
        int olddir;
        // dirtypes
        int turnaround;

        if (actor.target == null)
        {
            doomSystem().Error("P_NewChaseDir: called with no target");
        }

        olddir = actor.movedir;
        turnaround = opposite[olddir];

        deltax = actor.target.x - actor.x;
        deltay = actor.target.y - actor.y;

        if (deltax > 10 * FRACUNIT)
        {
            dirtype.d1 = DI_EAST;
        } else if (deltax < -10 * FRACUNIT)
        {
            dirtype.d1 = DI_WEST;
        } else
        {
            dirtype.d1 = DI_NODIR;
        }

        if (deltay < -10 * FRACUNIT)
        {
            dirtype.d2 = DI_SOUTH;
        } else if (deltay > 10 * FRACUNIT)
        {
            dirtype.d2 = DI_NORTH;
        } else
        {
            dirtype.d2 = DI_NODIR;
        }

        // try direct route
        if (dirtype.d1 != DI_NODIR && dirtype.d2 != DI_NODIR)
        {
            actor.movedir = diags[(eval(deltay < 0) << 1) + eval(deltax > 0)];
            if (actor.movedir != turnaround && TryWalk(actor))
            {
                return;
            }
        }

        // try other directions
        if (P_Random() > 200 || Math.abs(deltay) > Math.abs(deltax))
        {
            tdir = dirtype.d1;
            dirtype.d1 = dirtype.d2;
            dirtype.d2 = tdir;
        }

        if (dirtype.d1 == turnaround)
        {
            dirtype.d1 = DI_NODIR;
        }

        if (dirtype.d2 == turnaround)
        {
            dirtype.d2 = DI_NODIR;
        }

        if (dirtype.d1 != DI_NODIR)
        {
            actor.movedir = dirtype.d1;
            if (TryWalk(actor))
            {
                // either moved forward or attacked
                return;
            }
        }

        if (dirtype.d2 != DI_NODIR)
        {
            actor.movedir = dirtype.d2;

            if (TryWalk(actor))
            {
                return;
            }
        }

        // there is no direct path to the player,
        // so pick another direction.
        if (olddir != DI_NODIR)
        {
            actor.movedir = olddir;

            if (TryWalk(actor))
            {
                return;
            }
        }

        // randomly determine direction of search
        if (eval(P_Random() & 1))
        {
            for (tdir = DI_EAST; tdir <= DI_SOUTHEAST; tdir++)
            {
                if (tdir != turnaround)
                {
                    actor.movedir = tdir;

                    if (TryWalk(actor))
                    {
                        return;
                    }
                }
            }
        } else
        {
            for (tdir = DI_SOUTHEAST; tdir != DI_EAST - 1; tdir--)
            {
                if (tdir != turnaround)
                {
                    actor.movedir = tdir;

                    if (TryWalk(actor))
                    {
                        return;
                    }
                }
            }
        }

        if (turnaround != DI_NODIR)
        {
            actor.movedir = turnaround;
            if (TryWalk(actor))
            {
                return;
            }
        }

        actor.movedir = DI_NODIR;  // can not move
    }

    /**
     * TryWalk Attempts to move actor on in its current (ob.moveangle) direction. If blocked by either a wall or an
     * actor returns FALSE If move is either clear or blocked only by a door, returns TRUE and sets... If a door is in
     * the way, an OpenDoor call is made to start it opening.
     */
    default bool TryWalk(mobj_t actor)
    {
        if (!Move(actor))
        {
            return false;
        }

        actor.movecount = P_Random() & 15;
        return true;
    }

    //
    // P_HitSlideLine
    // Adjusts the xmove / ymove
    // so that the next move will slide along the wall.
    //
    default void HitSlideLine(line_t ld)
    {
        var sr = sceneRenderer();
        var slideMove = contextRequire(KEY_SLIDEMOVE);
        bool side;

        // all angles
        long lineangle;
        long moveangle;
        long deltaangle;

        @fixed_t
        int movelen;
        @fixed_t int newlen;

        if (ld.slopetype == slopetype_t.ST_HORIZONTAL)
        {
            slideMove.tmymove = 0;
            return;
        }

        if (ld.slopetype == slopetype_t.ST_VERTICAL)
        {
            slideMove.tmxmove = 0;
            return;
        }

        side = ld.PointOnLineSide(slideMove.slidemo.x, slideMove.slidemo.y);

        lineangle = sr.PointToAngle2(0, 0, ld.dx, ld.dy);

        if (side)
        {
            lineangle += ANG180;
        }

        moveangle = sr.PointToAngle2(0, 0, slideMove.tmxmove, slideMove.tmymove);
        deltaangle = moveangle - lineangle & BITS32;

        if (deltaangle > ANG180)
        {
            deltaangle += ANG180;
        }
        //  system.Error ("SlideLine: ang>ANG180");

        //lineangle >>>= ANGLETOFINESHIFT;
        //deltaangle >>>= ANGLETOFINESHIFT;
        movelen = AproxDistance(slideMove.tmxmove, slideMove.tmymove);
        newlen = FixedMul(movelen, finecosine(deltaangle));

        slideMove.tmxmove = FixedMul(newlen, finecosine(lineangle));
        slideMove.tmymove = FixedMul(newlen, finesine(lineangle));
    }

    ///(FRACUNIT/MAPFRACUNIT);
    //
    // P_SlideMove
    // The momx / momy move is bad, so try to slide
    // along a wall.
    // Find the first line hit, move flush to it,
    // and slide along it
    //
    // This is a kludgy mess.
    //
    default void SlideMove(mobj_t mo)
    {
        var slideMove = contextRequire(KEY_SLIDEMOVE);
        @fixed_t
        int leadx;
        @fixed_t int leady;
        @fixed_t int trailx;
        @fixed_t int traily;
        @fixed_t int newx;
        @fixed_t int newy;
        int hitcount;

        slideMove.slidemo = mo;
        hitcount = 0;

        do
        {
            if (++hitcount == 3)
            {
                // goto stairstep
                stairstep(mo);
                return;
            }     // don't loop forever

            // trace along the three leading corners
            if (mo.momx > 0)
            {
                leadx = mo.x + mo.radius;
                trailx = mo.x - mo.radius;
            } else
            {
                leadx = mo.x - mo.radius;
                trailx = mo.x + mo.radius;
            }

            if (mo.momy > 0)
            {
                leady = mo.y + mo.radius;
                traily = mo.y - mo.radius;
            } else
            {
                leady = mo.y - mo.radius;
                traily = mo.y + mo.radius;
            }

            slideMove.bestslidefrac = FRACUNIT + 1;

            PathTraverse(leadx, leady, leadx + mo.momx, leady + mo.momy, PT_ADDLINES, this::SlideTraverse);
            PathTraverse(trailx, leady, trailx + mo.momx, leady + mo.momy, PT_ADDLINES, this::SlideTraverse);
            PathTraverse(leadx, traily, leadx + mo.momx, traily + mo.momy, PT_ADDLINES, this::SlideTraverse);

            // move up to the wall
            if (slideMove.bestslidefrac == FRACUNIT + 1)
            {
                // the move most have hit the middle, so stairstep
                stairstep(mo);
                return;
            }     // don't loop forever

            // fudge a bit to make sure it doesn't hit
            slideMove.bestslidefrac -= FUDGE;
            if (slideMove.bestslidefrac > 0)
            {
                newx = FixedMul(mo.momx, slideMove.bestslidefrac);
                newy = FixedMul(mo.momy, slideMove.bestslidefrac);

                if (!TryMove(mo, mo.x + newx, mo.y + newy))
                {
                    // goto stairstep
                    stairstep(mo);
                    return;
                }     // don't loop forever
            }

            // Now continue along the wall.
            // First calculate remainder.
            slideMove.bestslidefrac = FRACUNIT - (slideMove.bestslidefrac + FUDGE);

            if (slideMove.bestslidefrac > FRACUNIT)
            {
                slideMove.bestslidefrac = FRACUNIT;
            }

            if (slideMove.bestslidefrac <= 0)
            {
                return;
            }

            slideMove.tmxmove = FixedMul(mo.momx, slideMove.bestslidefrac);
            slideMove.tmymove = FixedMul(mo.momy, slideMove.bestslidefrac);

            HitSlideLine(slideMove.bestslideline); // clip the moves

            mo.momx = slideMove.tmxmove;
            mo.momy = slideMove.tmymove;

        } // goto retry
        while (!TryMove(mo, mo.x + slideMove.tmxmove, mo.y + slideMove.tmymove));
    }

    /**
     * Fugly "goto stairstep" simulation
     *
     * @param mo
     */
    default void stairstep(mobj_t mo)
    {
        if (!TryMove(mo, mo.x, mo.y + mo.momy))
        {
            TryMove(mo, mo.x + mo.momx, mo.y);
        }
    }

    //
    // P_XYMovement
    //
    default void XYMovement(mobj_t mo)
    {
        var mv = contextRequire(KEY_MOVEMENT);

        @fixed_t
        int ptryx;  // pointers to fixed_t ???
        @fixed_t int ptryy;
        @fixed_t
        int xmove;
        @fixed_t int ymove;
        player_t player;

        if (mo.momx == 0 && mo.momy == 0)
        {
            if ((mo.flags & MF_SKULLFLY) != 0)
            {
                // the skull slammed into something
                mo.flags &= ~MF_SKULLFLY;
                mo.momx = mo.momy = mo.momz = 0;

                mo.SetMobjState(mo.info.spawnstate);
            }
            return;
        }

        player = mo.player;

        if (mo.momx > MAXMOVE)
        {
            mo.momx = MAXMOVE;
        } else if (mo.momx < -MAXMOVE)
        {
            mo.momx = -MAXMOVE;
        }

        if (mo.momy > MAXMOVE)
        {
            mo.momy = MAXMOVE;
        } else if (mo.momy < -MAXMOVE)
        {
            mo.momy = -MAXMOVE;
        }

        xmove = mo.momx;
        ymove = mo.momy;

        do
        {
            if (xmove > MAXMOVE / 2 || ymove > MAXMOVE / 2)
            {
                ptryx = mo.x + xmove / 2;
                ptryy = mo.y + ymove / 2;
                xmove >>= 1;
                ymove >>= 1;
            } else
            {
                ptryx = mo.x + xmove;
                ptryy = mo.y + ymove;
                xmove = ymove = 0;
            }

            if (!TryMove(mo, ptryx, ptryy))
            {
                // blocked move
                if (mo.player != null)
                {   // try to slide along it
                    SlideMove(mo);
                } else if (eval(mo.flags & MF_MISSILE))
                {
                    // explode a missile
                    if (mv.ceilingline != null && mv.ceilingline.backsector != null
                            && mv.ceilingline.backsector.ceilingpic == DOOM().textureManager.getSkyFlatNum())
                    {
                        // Hack to prevent missiles exploding
                        // against the sky.
                        // Does not handle sky floors.
                        RemoveMobj(mo);
                        return;
                    }
                    ExplodeMissile(mo);
                } else
                {
                    mo.momx = mo.momy = 0;
                }
            }
        }
        while ((xmove | ymove) != 0);

        // slow down
        if (player != null && eval(player.cheats & player_t.CF_NOMOMENTUM))
        {
            // debug option for no sliding at all
            mo.momx = mo.momy = 0;
            return;
        }

        if (eval(mo.flags & (MF_MISSILE | MF_SKULLFLY)))
        {
            return;     // no friction for missiles ever
        }
        if (mo.z > mo.floorz)
        {
            return;     // no friction when airborne
        }
        if (eval(mo.flags & MF_CORPSE))
        {
            // do not stop sliding
            //  if halfway off a step with some momentum
            if (mo.momx > FRACUNIT / 4
                    || mo.momx < -FRACUNIT / 4
                    || mo.momy > FRACUNIT / 4
                    || mo.momy < -FRACUNIT / 4)
            {
                if (mo.floorz != mo.subsector.sector.floorheight)
                {
                    return;
                }
            }
        }

        if (mo.momx > -STOPSPEED && mo.momx < STOPSPEED && mo.momy > -STOPSPEED && mo.momy < STOPSPEED
                && (player == null || player.cmd.forwardmove == 0 && player.cmd.sidemove == 0))
        {
            // if in a walking frame, stop moving
            // TODO: we need a way to get state indexed inside of states[], to sim pointer arithmetic.
            // FIX: added an "id" field.
            if (player != null && player.mo.mobj_state.id - statenum_t.S_PLAY_RUN1.ordinal() < 4)
            {
                player.mo.SetMobjState(statenum_t.S_PLAY);
            }

            mo.momx = 0;
            mo.momy = 0;
        } else
        {
            mo.momx = FixedMul(mo.momx, FRICTION);
            mo.momy = FixedMul(mo.momy, FRICTION);
        }
    }

    //
    // SLIDE MOVE
    // Allows the player to slide along any angled walls.
    //
    // fixed
    //
    // PTR_SlideTraverse
    //
    @SourceCode.P_Map.C(PTR_SlideTraverse)
    default bool SlideTraverse(intercept_t in)
    {
        var slideMove = contextRequire(KEY_SLIDEMOVE);
        var ma = contextRequire(KEY_MOVEMENT);
        line_t li;

        if (!in.isaline)
        {
            doomSystem().Error("PTR_SlideTraverse: not a line?");
        }

        li = (line_t) in.d();

        if (!eval(li.flags & ML_TWOSIDED))
        {
            if (li.PointOnLineSide(slideMove.slidemo.x, slideMove.slidemo.y))
            {
                // don't hit the back side
                return true;
            }
            return isblocking(in, li);
        }

        // set openrange, opentop, openbottom
        LineOpening(li);

        if (ma.openrange < slideMove.slidemo.height
                || // doesn't fit
                ma.opentop - slideMove.slidemo.z < slideMove.slidemo.height
                || // mobj is too high
                ma.openbottom - slideMove.slidemo.z > 24 * FRACUNIT) // too big a step up
        {
            if (in.frac < slideMove.bestslidefrac)
            {
                slideMove.secondslidefrac = slideMove.bestslidefrac;
                slideMove.secondslideline = slideMove.bestslideline;
                slideMove.bestslidefrac = in.frac;
                slideMove.bestslideline = li;
            }

            return false;   // stop
        } else
        { // this line doesn't block movement
            return true;
        }
    }

    readonly class DirType
    {

        //dirtype
        int d1;
        int d2;
    }

}
