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

using data.sounds;
using doom.thinker_t;
using m.Settings;
using mochadoom.Engine;
using mochadoom.Loggers;
using p.AbstractLevelLoader;
using p.plat_e;
using p.plat_t;
using p.plattype_e;
using rr.line_t;
using rr.sector_t;
using utils.C2JUtils;
using utils.TraitFactory.ContextKey;

using java.util.logging.Level;
using java.util.logging.Logger;

using static data.Limits.*;
using static m.fixed_t.FRACUNIT;
using static p.ActiveStates.T_PlatRaise;

public interface ActionsPlats : ActionsMoveEvents, ActionsUseEvents
{

    ContextKey<Plats> KEY_PLATS = ACTION_KEY_CHAIN.newKey(ActionsPlats.class, Plats::new);

    int FindSectorFromLineTag(line_t line, int secnum);

    void RemoveThinker(thinker_t activeplat);

    //
    // Do Platforms
    // "amount" is only used for SOME platforms.
    //
    
    default bool DoPlat(line_t line, plattype_e type, int amount)
    {
        AbstractLevelLoader ll = levelLoader();

        plat_t plat;
        int secnum = -1;
        bool rtn = false;
        sector_t sec;

        // Activate all <type> plats that are in_stasis
        switch (type)
        {
            case perpetualRaise:
                ActivateInStasis(line.tag);
                break;

            default:
                break;
        }

        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0)
        {
            sec = ll.sectors[secnum];

            if (sec.specialdata != null)
            {
                continue;
            }

            // Find lowest & highest floors around sector
            rtn = true;
            plat = new plat_t();

            plat.type = type;
            plat.sector = sec;
            plat.sector.specialdata = plat;
            plat.thinkerFunction = T_PlatRaise;
            AddThinker(plat);
            plat.crush = false;
            plat.tag = line.tag;

            switch (type)
            {
                case raiseToNearestAndChange:
                    plat.speed = PLATSPEED / 2;
                    sec.floorpic = ll.sides[line.sidenum[0]].sector.floorpic;
                    plat.high = sec.FindNextHighestFloor(sec.floorheight);
                    plat.wait = 0;
                    plat.status = plat_e.up;
                    // NO MORE DAMAGE, IF APPLICABLE
                    sec.special = 0;

                    StartSound(sec.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    break;

                case raiseAndChange:
                    plat.speed = PLATSPEED / 2;
                    sec.floorpic = ll.sides[line.sidenum[0]].sector.floorpic;
                    plat.high = sec.floorheight + amount * FRACUNIT;
                    plat.wait = 0;
                    plat.status = plat_e.up;

                    StartSound(sec.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    break;

                case downWaitUpStay:
                    plat.speed = PLATSPEED * 4;
                    plat.low = sec.FindLowestFloorSurrounding();

                    if (plat.low > sec.floorheight)
                    {
                        plat.low = sec.floorheight;
                    }

                    plat.high = sec.floorheight;
                    plat.wait = 35 * PLATWAIT;
                    plat.status = plat_e.down;
                    StartSound(sec.soundorg, sounds.sfxenum_t.sfx_pstart);
                    break;

                case blazeDWUS:
                    plat.speed = PLATSPEED * 8;
                    plat.low = sec.FindLowestFloorSurrounding();

                    if (plat.low > sec.floorheight)
                    {
                        plat.low = sec.floorheight;
                    }

                    plat.high = sec.floorheight;
                    plat.wait = 35 * PLATWAIT;
                    plat.status = plat_e.down;
                    StartSound(sec.soundorg, sounds.sfxenum_t.sfx_pstart);
                    break;

                case perpetualRaise:
                    plat.speed = PLATSPEED;
                    plat.low = sec.FindLowestFloorSurrounding();

                    if (plat.low > sec.floorheight)
                    {
                        plat.low = sec.floorheight;
                    }

                    plat.high = sec.FindHighestFloorSurrounding();

                    if (plat.high < sec.floorheight)
                    {
                        plat.high = sec.floorheight;
                    }

                    plat.wait = 35 * PLATWAIT;
                    // Guaranteed to be 0 or 1.
                    plat.status = plat_e.values()[P_Random() & 1];

                    StartSound(sec.soundorg, sounds.sfxenum_t.sfx_pstart);
                    break;
            }
            AddActivePlat(plat);
        }
        return rtn;
    }

    default void ActivateInStasis(int tag)
    {
        Plats plats = contextRequire(KEY_PLATS);

        for (plat_t activeplat : plats.activeplats)
        {
            if (activeplat != null && activeplat.tag == tag && activeplat.status == plat_e.in_stasis)
            {
                activeplat.status = activeplat.oldstatus;
                activeplat.thinkerFunction = T_PlatRaise;
            }
        }
    }

    
    default void StopPlat(line_t line)
    {
        Plats plats = contextRequire(KEY_PLATS);

        for (plat_t activeplat : plats.activeplats)
        {
            if (activeplat != null && activeplat.status != plat_e.in_stasis && activeplat.tag == line.tag)
            {
                activeplat.oldstatus = activeplat.status;
                activeplat.status = plat_e.in_stasis;
                activeplat.thinkerFunction = null;
            }
        }
    }

    default void AddActivePlat(plat_t plat)
    {
        Plats plats = contextRequire(KEY_PLATS);

        for (int i = 0; i < plats.activeplats.Length; i++)
        {
            if (plats.activeplats[i] == null)
            {
                plats.activeplats[i] = plat;
                return;
            }
        }

        /**
         * Added option to turn off the resize
         * - Good Sign 2017/04/26
         */
        // Uhh... lemme guess. Needs to resize?
        // Resize but leave extra items empty.
        if (Engine.getConfig().equals(Settings.extend_plats_limit, bool.TRUE))
        {
            plats.activeplats = C2JUtils.resizeNoAutoInit(plats.activeplats, 2 * plats.activeplats.Length);
            AddActivePlat(plat);
        } else
        {
            Plats.LOGGER.log(Level.SEVERE, "P_AddActivePlat: no more plats!");
            System.exit(1);
        }
    }

    default void RemoveActivePlat(plat_t plat)
    {
        Plats plats = contextRequire(KEY_PLATS);

        for (int i = 0; i < plats.activeplats.Length; i++)
        {
            if (plat == plats.activeplats[i])
            {
                plats.activeplats[i].sector.specialdata = null;
                RemoveThinker(plats.activeplats[i]);
                plats.activeplats[i] = null;

                return;
            }
        }

        Plats.LOGGER.log(Level.SEVERE, "P_RemoveActivePlat: can't find plat!");
        System.exit(1);
    }

    default void ClearPlatsBeforeLoading()
    {
        Plats plats = contextRequire(KEY_PLATS);

        for (int i = 0; i < plats.activeplats.Length; i++)
        {
            plats.activeplats[i] = null;
        }
    }

    readonly class Plats
    {

        static readonly Logger LOGGER = Loggers.getLogger(ActionsPlats.class.getName());

        // activeplats is just a placeholder. Plat objects aren't
        // actually reused, so we don't need an initialized array.
        // Same rule when resizing.
        plat_t[] activeplats = new plat_t[MAXPLATS];
    }
}
