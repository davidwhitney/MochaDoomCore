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

using data.mapthing_t;
using data.mobjtype_t;
using data.sounds;
using doom.CommandVariable;
using doom.DoomMain;
using doom.SourceCode;
using doom.SourceCode.CauseOfDesyncProbability;
using doom.SourceCode.P_Spec;
using doom.thinker_t;
using p.*;
using p.ActiveStates.MobjConsumer;
using p.ActiveStates.ThinkerConsumer;
using rr.sector_t;
using rr.subsector_t;

using static data.Defines.*;
using static data.Limits.MAXPLAYERS;
using static data.Tables.ANG45;
using static data.info.mobjinfo;
using static doom.SourceCode.P_Spec.P_SpawnSpecials;
using static doom.SourceCode.P_Tick.P_RemoveThinker;
using static m.fixed_t.FRACBITS;
using static p.ActiveStates.NOP;
using static p.DoorDefines.FASTDARK;
using static p.DoorDefines.SLOWDARK;
using static p.mobj_t.MF_SPAWNCEILING;
using static utils.C2JUtils.eval;

public interface ActionsThinkers : ActionsSectors, ThinkerList
{

    //
    // P_RemoveThinker
    // Deallocation is lazy -- it will not actually be freed
    // until its thinking turn comes up.
    //
    //
    // killough 4/25/98:
    //
    // Instead of marking the function with -1 value cast to a function pointer,
    // set the function to P_RemoveThinkerDelayed(), so that later, it will be
    // removed automatically as part of the thinker process.
    //
    
    @SourceCode.Compatible("thinker->function.acv = (actionf_v)(-1)")
    @SourceCode.P_Tick.C(P_RemoveThinker)
    default void RemoveThinker(thinker_t thinker)
    {
        thinker.thinkerFunction = NOP;
    }

    /**
     * P_SpawnSpecials After the map has been loaded, scan for specials that spawn thinkers
     */
    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @P_Spec.C(P_SpawnSpecials)
    default void SpawnSpecials()
    {
        DoomMain<?, ?> D = DOOM();
        AbstractLevelLoader ll = levelLoader();
        UnifiedGameMap.Specials sp = getSpecials();
        sector_t sector;

        /*int     episode;

        episode = 1;
        if (W.CheckNumForName("texture2") >= 0)
        episode = 2;
         */
        // See if -TIMER needs to be used.
        sp.levelTimer = false;

        if (D.cVarManager.bool(CommandVariable.AVG) && IsDeathMatch())
        {
            sp.levelTimer = true;
            sp.levelTimeCount = 20 * 60 * 35;
        }

        if (IsDeathMatch())
        {
            D.cVarManager.with(CommandVariable.TIMER, 0, (int.i) -> {
                sp.levelTimer = true;
                sp.levelTimeCount = i * 60 * 35;
            });
        }

        //  Init special SECTORs.
        //sector = LL.sectors;
        for (int i = 0; i < ll.numsectors; i++)
        {
            sector = ll.sectors[i];
            if (!eval(sector.special))
            {
                continue;
            }

            switch (sector.special)
            {
                case 1:
                    // FLICKERING LIGHTS
                    P_SpawnLightFlash:
                    {
                        SpawnLightFlash(sector);
                    }
                    break;

                case 2:
                    // STROBE FAST
                    P_SpawnStrobeFlash:
                    {
                        SpawnStrobeFlash(sector, FASTDARK, 0);
                    }
                    break;

                case 3:
                    // STROBE SLOW
                    P_SpawnStrobeFlash:
                    {
                        SpawnStrobeFlash(sector, SLOWDARK, 0);
                    }
                    break;

                case 4:
                    // STROBE FAST/DEATH SLIME
                    P_SpawnStrobeFlash:
                    {
                        SpawnStrobeFlash(sector, FASTDARK, 0);
                    }
                    sector.special = 4;
                    break;

                case 8:
                    // GLOWING LIGHT
                    P_SpawnGlowingLight:
                    {
                        SpawnGlowingLight(sector);
                    }
                    break;
                case 9:
                    // SECRET SECTOR
                    D.totalsecret++;
                    break;

                case 10:
                    // DOOR CLOSE IN 30 SECONDS
                    SpawnDoorCloseIn30:
                    {
                        SpawnDoorCloseIn30(sector);
                    }
                    break;

                case 12:
                    // SYNC STROBE SLOW
                    P_SpawnStrobeFlash:
                    {
                        SpawnStrobeFlash(sector, SLOWDARK, 1);
                    }
                    break;

                case 13:
                    // SYNC STROBE FAST
                    P_SpawnStrobeFlash:
                    {
                        SpawnStrobeFlash(sector, FASTDARK, 1);
                    }
                    break;

                case 14:
                    // DOOR RAISE IN 5 MINUTES
                    P_SpawnDoorRaiseIn5Mins:
                    {
                        SpawnDoorRaiseIn5Mins(sector, i);
                    }
                    break;

                case 17:
                    P_SpawnFireFlicker:
                    {
                        SpawnFireFlicker(sector);
                    }
                    break;
            }
        }

        //  Init line EFFECTs
        sp.numlinespecials = 0;
        for (int i = 0; i < ll.numlines; i++)
        {
            switch (ll.lines[i].special)
            {
                case 48:
                    // EFFECT FIRSTCOL SCROLL+
                    // Maes 6/4/2012: removed.Length limit.
                    if (sp.numlinespecials == sp.linespeciallist.Length)
                    {
                        sp.resizeLinesSpecialList();
                    }
                    sp.linespeciallist[sp.numlinespecials] = ll.lines[i];
                    sp.numlinespecials++;
                    break;
            }
        }

        //  Init other misc stuff
        for (int i = 0; i < getMaxCeilings(); i++)
        {
            getActiveCeilings()[i] = null;
        }

        getSwitches().initButtonList();

        // UNUSED: no horizonal sliders.
        // if (SL!=null) {
        // SL.updateStatus(DM);
        //  SL.P_InitSlidingDoorFrames();
        //}
    }

    /**
     * P_RespawnSpecials
     */
    default void RespawnSpecials()
    {
        RespawnQueue resp = contextRequire(KEY_RESP_QUEUE);
        int x;  // fixed
        int y;
        int z;

        subsector_t ss;
        mobj_t mo;
        mapthing_t mthing;

        int i;

        // only respawn items in deathmatch (deathmatch!=2)
        if (!DOOM().altdeath)
        {
            return; // 
        }
        // nothing left to respawn?
        if (resp.iquehead == resp.iquetail)
        {
            return;
        }

        // wait at least 30 seconds
        if (LevelTime() - resp.itemrespawntime[resp.iquetail] < 30 * 35)
        {
            return;
        }

        mthing = resp.itemrespawnque[resp.iquetail];

        x = mthing.x << FRACBITS;
        y = mthing.y << FRACBITS;

        // spawn a teleport fog at the new spot
        ss = levelLoader().PointInSubsector(x, y);
        mo = SpawnMobj(x, y, ss.sector.floorheight, mobjtype_t.MT_IFOG);
        StartSound(mo, sounds.sfxenum_t.sfx_itmbk);

        // find which type to spawn
        for (i = 0; i < mobjtype_t.NUMMOBJTYPES.ordinal(); i++)
        {
            if (mthing.type == mobjinfo[i].doomednum)
            {
                break;
            }
        }

        // spawn it
        if (eval(mobjinfo[i].flags & MF_SPAWNCEILING))
        {
            z = ONCEILINGZ;
        } else
        {
            z = ONFLOORZ;
        }

        mo = SpawnMobj(x, y, z, mobjtype_t.values()[i]);
        mo.spawnpoint = mthing;
        mo.angle = ANG45 * (mthing.angle / 45);

        // pull it from the que
        resp.iquetail = resp.iquetail + 1 & ITEMQUESIZE - 1;
    }

    //
    // P_AllocateThinker
    // Allocates memory and adds a new thinker at the end of the list.
    //
    //public void AllocateThinker(thinker_t thinker) {;
    // UNUSED
    //}
    //
    // P_RunThinkers
    //
    default void RunThinkers()
    {
        thinker_t thinker = getThinkerCap().next;
        while (thinker != getThinkerCap())
        {
            if (thinker.thinkerFunction == NOP)
            {
                // time to remove it
                thinker.next.prev = thinker.prev;
                thinker.prev.next = thinker.next;
                // Z_Free (currentthinker);
            } else
            {
                if (thinker.thinkerFunction.isParamType(MobjConsumer.class))
                {
                    thinker.thinkerFunction.fun(MobjConsumer.class).accept(DOOM().actions, (mobj_t) thinker);
                } else if (thinker.thinkerFunction.isParamType(ThinkerConsumer.class))
                {
                    thinker.thinkerFunction.fun(ThinkerConsumer.class).accept(DOOM().actions, thinker);
                }
            }
            thinker = thinker.next;
        }
    }

    //
    //P_Ticker
    //
    default void Ticker()
    {
        // run the tic
        if (IsPaused())
        {
            return;
        }

        // pause if in menu and at least one tic has been run
        if (!IsNetGame() && IsMenuActive() && !IsDemoPlayback() && getPlayer(ConsolePlayerNumber()).viewz != 1)
        {
            return;
        }

        for (int i = 0; i < MAXPLAYERS; i++)
        {
            if (PlayerInGame(i))
            {
                getPlayer(i).PlayerThink();
            }
        }

        RunThinkers();
        getSpecials().UpdateSpecials(); // In specials. Merge?
        RespawnSpecials();

        // for par times
        DOOM().leveltime++;
    }
}
