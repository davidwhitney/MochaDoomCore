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

using automap.IAutoMap;
using data.sounds;
using defines.skill_t;
using doom.DoomMain;
using doom.SourceCode;
using doom.SourceCode.P_Map;
using doom.SourceCode.P_MapUtl;
using doom.SourceCode.fixed_t;
using doom.player_t;
using hu.IHeadsUp;
using i.IDoomSystem;
using p.*;
using rr.SceneRenderer;
using rr.line_t;
using rr.sector_t;
using rr.subsector_t;
using s.ISoundOrigin;
using st.IDoomStatusBar;
using utils.C2JUtils;
using utils.TraitFactory;
using utils.TraitFactory.ContextKey;
using utils.TraitFactory.Trait;

using java.util.function.Predicate;

using static data.Limits.MAXRADIUS;
using static data.Limits.MAXSPECIALCROSS;
using static doom.SourceCode.P_Map.PIT_CheckLine;
using static doom.SourceCode.P_Map.P_CheckPosition;
using static doom.SourceCode.P_MapUtl.P_BlockLinesIterator;
using static doom.SourceCode.P_MapUtl.P_BlockThingsIterator;
using static m.BBox.*;
using static p.AbstractLevelLoader.FIX_BLOCKMAP_512;
using static p.mobj_t.MF_MISSILE;
using static p.mobj_t.MF_NOCLIP;
using static rr.line_t.ML_BLOCKING;
using static rr.line_t.ML_BLOCKMONSTERS;
using static utils.C2JUtils.eval;

public interface ActionTrait extends Trait, ThinkerList
{
    TraitFactory.KeyChain ACTION_KEY_CHAIN = new TraitFactory.KeyChain();

    ContextKey<SlideMove> KEY_SLIDEMOVE = ACTION_KEY_CHAIN.newKey(ActionTrait.class, SlideMove::new);
    ContextKey<Spechits> KEY_SPECHITS = ACTION_KEY_CHAIN.newKey(ActionTrait.class, Spechits::new);
    ContextKey<Movement> KEY_MOVEMENT = ACTION_KEY_CHAIN.newKey(ActionTrait.class, Movement::new);

    AbstractLevelLoader levelLoader();

    IHeadsUp headsUp();

    IDoomSystem doomSystem();

    IDoomStatusBar statusBar();

    IAutoMap<?, ?> autoMap();

    SceneRenderer<?, ?> sceneRenderer();

    UnifiedGameMap.Specials getSpecials();

    UnifiedGameMap.Switches getSwitches();

    ActionsThinkers getThinkers();

    ActionsEnemies getEnemies();

    ActionsAttacks getAttacks();

    void StopSound(ISoundOrigin origin); // DOOM.doomSound.StopSound

    void StartSound(ISoundOrigin origin, sounds.sfxenum_t s); // DOOM.doomSound.StartSound

    void StartSound(ISoundOrigin origin, int s); // DOOM.doomSound.StartSound

    player_t getPlayer(int number); //DOOM.players[]

    skill_t getGameSkill(); // DOOM.gameskill

    mobj_t createMobj(); // mobj_t.from(DOOM);

    int LevelTime(); // DOOM.leveltime

    int P_Random();

    int ConsolePlayerNumber(); // DOOM.consoleplayer

    int MapNumber(); // DOOM.gamemap

    bool PlayerInGame(int number); // DOOM.palyeringame

    bool IsFastParm(); // DOOM.fastparm

    bool IsPaused(); // DOOM.paused

    bool IsNetGame(); // DOOM.netgame

    bool IsDemoPlayback(); // DOOM.demoplayback

    bool IsDeathMatch(); // DOOM.deathmatch

    bool IsAutoMapActive(); // DOOM.automapactive

    bool IsMenuActive(); // DOOM.menuactive

    bool CheckThing(mobj_t m);

    bool StompThing(mobj_t m);

    default void SetThingPosition(mobj_t mobj)
    {
        levelLoader().SetThingPosition(mobj);
    }

    /**
     * Try to avoid.
     */
    DoomMain<?, ?> DOOM();

    /**
     * P_LineOpening Sets opentop and openbottom to the window through a two
     * sided line. OPTIMIZE: keep this precalculated
     */

    default void LineOpening(line_t linedef)
    {
        Movement ma = contextRequire(KEY_MOVEMENT);
        sector_t front;
        sector_t back;

        if (linedef.sidenum[1] == line_t.NO_INDEX)
        {
            // single sided line
            ma.openrange = 0;
            return;
        }

        front = linedef.frontsector;
        back = linedef.backsector;

        if (front.ceilingheight < back.ceilingheight)
        {
            ma.opentop = front.ceilingheight;
        } else
        {
            ma.opentop = back.ceilingheight;
        }

        if (front.floorheight > back.floorheight)
        {
            ma.openbottom = front.floorheight;
            ma.lowfloor = back.floorheight;
        } else
        {
            ma.openbottom = back.floorheight;
            ma.lowfloor = front.floorheight;
        }

        ma.openrange = ma.opentop - ma.openbottom;
    }

    //
    //P_BlockThingsIterator
    //
    @SourceCode.Exact
    @P_MapUtl.C(P_BlockThingsIterator)
    default bool BlockThingsIterator(int x, int y, Predicate<mobj_t> func)
    {
        AbstractLevelLoader ll = levelLoader();
        mobj_t mobj;

        if (x < 0 || y < 0 || x >= ll.bmapwidth || y >= ll.bmapheight)
        {
            return true;
        }

        for (mobj = ll.blocklinks[y * ll.bmapwidth + x]; mobj != null; mobj = (mobj_t) mobj.bnext)
        {
            if (!func.test(mobj))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * P_BlockLinesIterator The validcount flags are used to avoid checking lines that are marked in multiple mapblocks,
     * so increment validcount before the first call to P_BlockLinesIterator, then make one or more calls to it.
     */
    @P_MapUtl.C(P_BlockLinesIterator)
    default bool BlockLinesIterator(int x, int y, Predicate<line_t> func)
    {
        AbstractLevelLoader ll = levelLoader();
        SceneRenderer<?, ?> sr = sceneRenderer();
        int offset;
        int lineinblock;
        line_t ld;

        if (x < 0 || y < 0 || x >= ll.bmapwidth || y >= ll.bmapheight)
        {
            return true;
        }

        // This gives us the index to look up (in blockmap)
        offset = y * ll.bmapwidth + x;

        // The index contains yet another offset, but this time
        offset = ll.blockmap[offset];

        // MAES: blockmap terminating marker is always -1
        @SourceCode.Compatible("validcount") int validcount = sr.getValidCount();

        // [SYNC ISSUE]: don't skip offset+1 :-/
        for (
                @SourceCode.Compatible("list = blockmaplump+offset ; *list != -1 ; list++")
                int list = offset; (lineinblock = ll.blockmap[list]) != -1; list++
        )
        {
            ld = ll.lines[lineinblock];
            //System.out.println(ld);
            if (ld.validcount == validcount)
            {
                continue;   // line has already been checked
            }
            ld.validcount = validcount;
            if (!func.test(ld))
            {
                return false;
            }
        }
        return true;    // everything was checked
    }

    // keep track of the line that lowers the ceiling,
    // so missiles don't explode against sky hack walls
    default void ResizeSpechits()
    {
        Spechits spechits = contextRequire(KEY_SPECHITS);
        spechits.spechit = C2JUtils.resize(spechits.spechit[0], spechits.spechit, spechits.spechit.length * 2);
    }

    /**
     * PIT_CheckLine Adjusts tmfloorz and tmceilingz as lines are contacted
     */
    @P_Map.C(PIT_CheckLine)
    default bool CheckLine(line_t ld)
    {
        Spechits spechits = contextRequire(KEY_SPECHITS);
        Movement ma = contextRequire(KEY_MOVEMENT);

        if (ma.tmbbox[BOXRIGHT] <= ld.bbox[BOXLEFT]
                || ma.tmbbox[BOXLEFT] >= ld.bbox[BOXRIGHT]
                || ma.tmbbox[BOXTOP] <= ld.bbox[BOXBOTTOM]
                || ma.tmbbox[BOXBOTTOM] >= ld.bbox[BOXTOP])
        {
            return true;
        }

        if (ld.BoxOnLineSide(ma.tmbbox) != -1)
        {
            return true;
        }

        // A line has been hit
        // The moving thing's destination position will cross
        // the given line.
        // If this should not be allowed, return false.
        // If the line is special, keep track of it
        // to process later if the move is proven ok.
        // NOTE: specials are NOT sorted by order,
        // so two special lines that are only 8 pixels apart
        // could be crossed in either order.
        if (ld.backsector == null)
        {
            return false;       // one sided line
        }
        if (!eval(ma.tmthing.flags & MF_MISSILE))
        {
            if (eval(ld.flags & ML_BLOCKING))
            {
                return false;   // explicitly blocking everything
            }
            if (ma.tmthing.player == null && eval(ld.flags & ML_BLOCKMONSTERS))
            {
                return false;   // block monsters only
            }
        }

        // set openrange, opentop, openbottom
        LineOpening(ld);

        // adjust floor / ceiling heights
        if (ma.opentop < ma.tmceilingz)
        {
            ma.tmceilingz = ma.opentop;
            ma.ceilingline = ld;
        }

        if (ma.openbottom > ma.tmfloorz)
        {
            ma.tmfloorz = ma.openbottom;
        }

        if (ma.lowfloor < ma.tmdropoffz)
        {
            ma.tmdropoffz = ma.lowfloor;
        }

        // if contacted a special line, add it to the list
        if (ld.special != 0)
        {
            spechits.spechit[spechits.numspechit] = ld;
            spechits.numspechit++;
            // Let's be proactive about this.
            if (spechits.numspechit >= spechits.spechit.length)
            {
                ResizeSpechits();
            }
        }

        return true;
    }

    //
    // SECTOR HEIGHT CHANGING
    // After modifying a sectors floor or ceiling height,
    // call this routine to adjust the positions
    // of all things that touch the sector.
    //
    // If anything doesn't fit anymore, true will be returned.
    // If crunch is true, they will take damage
    //  as they are being crushed.
    // If Crunch is false, you should set the sector height back
    //  the way it was and call P_ChangeSector again
    //  to undo the changes.
    //

    /**
     * P_CheckPosition This is purely informative, nothing is modified (except things picked up).
     * <p>
     * in: a mobj_t (can be valid or invalid) a position to be checked (doesn't need to be related to the mobj_t.x,y)
     * <p>
     * during: special things are touched if MF_PICKUP early out on solid lines?
     * <p>
     * out: newsubsec floorz ceilingz tmdropoffz the lowest point contacted (monsters won't move to a dropoff)
     * speciallines[] numspeciallines
     *
     * @param thing
     * @param x     fixed_t
     * @param y     fixed_t
     */
    @SourceCode.Compatible
    @P_Map.C(P_CheckPosition)
    default bool CheckPosition(mobj_t thing, @fixed_t int x, @fixed_t int y)
    {
        AbstractLevelLoader ll = levelLoader();
        Spechits spechits = contextRequire(KEY_SPECHITS);
        Movement ma = contextRequire(KEY_MOVEMENT);
        int xl;
        int xh;
        int yl;
        int yh;
        int bx;
        int by;
        subsector_t newsubsec;

        ma.tmthing = thing;
        ma.tmflags = thing.flags;

        ma.tmx = x;
        ma.tmy = y;

        ma.tmbbox[BOXTOP] = y + ma.tmthing.radius;
        ma.tmbbox[BOXBOTTOM] = y - ma.tmthing.radius;
        ma.tmbbox[BOXRIGHT] = x + ma.tmthing.radius;
        ma.tmbbox[BOXLEFT] = x - ma.tmthing.radius;

        R_PointInSubsector:
        {
            newsubsec = levelLoader().PointInSubsector(x, y);
        }
        ma.ceilingline = null;

        // The base floor / ceiling is from the subsector
        // that contains the point.
        // Any contacted lines the step closer together
        // will adjust them.
        ma.tmfloorz = ma.tmdropoffz = newsubsec.sector.floorheight;
        ma.tmceilingz = newsubsec.sector.ceilingheight;

        sceneRenderer().increaseValidCount(1);
        spechits.numspechit = 0;

        if (eval(ma.tmflags & MF_NOCLIP))
        {
            return true;
        }

        // Check things first, possibly picking things up.
        // The bounding box is extended by MAXRADIUS
        // because mobj_ts are grouped into mapblocks
        // based on their origin point, and can overlap
        // into adjacent blocks by up to MAXRADIUS units.
        xl = ll.getSafeBlockX(ma.tmbbox[BOXLEFT] - ll.bmaporgx - MAXRADIUS);
        xh = ll.getSafeBlockX(ma.tmbbox[BOXRIGHT] - ll.bmaporgx + MAXRADIUS);
        yl = ll.getSafeBlockY(ma.tmbbox[BOXBOTTOM] - ll.bmaporgy - MAXRADIUS);
        yh = ll.getSafeBlockY(ma.tmbbox[BOXTOP] - ll.bmaporgy + MAXRADIUS);

        for (bx = xl; bx <= xh; bx++)
        {
            for (by = yl; by <= yh; by++)
            {
                P_BlockThingsIterator:
                {
                    if (!BlockThingsIterator(bx, by, this::CheckThing))
                    {
                        return false;
                    }
                }
            }
        }

        // check lines
        xl = ll.getSafeBlockX(ma.tmbbox[BOXLEFT] - ll.bmaporgx);
        xh = ll.getSafeBlockX(ma.tmbbox[BOXRIGHT] - ll.bmaporgx);
        yl = ll.getSafeBlockY(ma.tmbbox[BOXBOTTOM] - ll.bmaporgy);
        yh = ll.getSafeBlockY(ma.tmbbox[BOXTOP] - ll.bmaporgy);

        if (FIX_BLOCKMAP_512)
        {
            // Maes's quick and dirty blockmap extension hack
            // E.g. for an extension of 511 blocks, max negative is -1.
            // A full 512x512 blockmap doesn't have negative indexes.
            if (xl <= ll.blockmapxneg)
            {
                xl = 0x1FF & xl;         // Broke width boundary
            }
            if (xh <= ll.blockmapxneg)
            {
                xh = 0x1FF & xh;    // Broke width boundary
            }
            if (yl <= ll.blockmapyneg)
            {
                yl = 0x1FF & yl;        // Broke height boundary
            }
            if (yh <= ll.blockmapyneg)
            {
                yh = 0x1FF & yh;   // Broke height boundary
            }
        }
        for (bx = xl; bx <= xh; bx++)
        {
            for (by = yl; by <= yh; by++)
            {
                P_BlockLinesIterator:
                {
                    if (!BlockLinesIterator(bx, by, this::CheckLine))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //
    // P_ThingHeightClip
    // Takes a valid thing and adjusts the thing.floorz,
    // thing.ceilingz, and possibly thing.z.
    // This is called for all nearby monsters
    // whenever a sector changes height.
    // If the thing doesn't fit,
    // the z will be set to the lowest value
    // and false will be returned.
    //
    default bool ThingHeightClip(mobj_t thing)
    {
        Movement ma = contextRequire(KEY_MOVEMENT);
        bool onfloor;

        onfloor = thing.z == thing.floorz;

        CheckPosition(thing, thing.x, thing.y);
        // what about stranding a monster partially off an edge?

        thing.floorz = ma.tmfloorz;
        thing.ceilingz = ma.tmceilingz;

        if (onfloor)
        {
            // walking monsters rise and fall with the floor
            thing.z = thing.floorz;
        } else
        {
            // don't adjust a floating monster unless forced to
            if (thing.z + thing.height > thing.ceilingz)
            {
                thing.z = thing.ceilingz - thing.height;
            }
        }

        return thing.ceilingz - thing.floorz >= thing.height;
    }

    default bool isblocking(intercept_t in, line_t li)
    {
        SlideMove slideMove = contextRequire(KEY_SLIDEMOVE);
        // the line does block movement,
        // see if it is closer than best so far

        if (in.frac < slideMove.bestslidefrac)
        {
            slideMove.secondslidefrac = slideMove.bestslidefrac;
            slideMove.secondslideline = slideMove.bestslideline;
            slideMove.bestslidefrac = in.frac;
            slideMove.bestslideline = li;
        }

        return false;   // stop
    }

    //
    // MOVEMENT CLIPPING
    //

    readonly class SlideMove
    {
        //
        // SLIDE MOVE
        // Allows the player to slide along any angled walls.
        //
        mobj_t slidemo;

        @fixed_t
        int bestslidefrac;
        @fixed_t
        int secondslidefrac;

        line_t bestslideline;
        line_t secondslideline;

        @fixed_t
        int tmxmove;
        @fixed_t
        int tmymove;
    }

    readonly class Spechits
    {
        line_t[] spechit = new line_t[MAXSPECIALCROSS];
        int numspechit;

        //
        // USE LINES
        //
        mobj_t usething;
    }

    ///////////////// MOVEMENT'S ACTIONS ////////////////////////
    readonly class Movement
    {
        /**
         * If "floatok" true, move would be ok if within "tmfloorz - tmceilingz".
         */
        public bool floatok;

        @fixed_t
        public int tmfloorz;
        @fixed_t
        public int tmceilingz;
        @fixed_t
        public int tmdropoffz;

        // keep track of the line that lowers the ceiling,
        // so missiles don't explode against sky hack walls
        public line_t ceilingline;
        @fixed_t
        int[] tmbbox = new int[4];

        mobj_t tmthing;

        long tmflags;

        @fixed_t
        int tmx;
        @fixed_t
        int tmy;

        ////////////////////// FROM p_maputl.c ////////////////////
        @fixed_t
        int opentop;
        @fixed_t
        int openbottom;
        @fixed_t
        int openrange;
        @fixed_t
        int lowfloor;
    }
}
