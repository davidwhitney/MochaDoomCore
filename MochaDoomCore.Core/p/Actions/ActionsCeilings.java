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
using doom.SourceCode.P_Ceiling;
using doom.thinker_t;
using p.ActiveStates;
using p.ceiling_e;
using p.ceiling_t;
using p.result_e;
using rr.line_t;
using rr.sector_t;
using utils.C2JUtils;
using utils.TraitFactory.ContextKey;

using static data.Limits.CEILSPEED;
using static data.Limits.MAXCEILINGS;
using static doom.SourceCode.P_Ceiling.EV_DoCeiling;
using static m.fixed_t.FRACUNIT;
using static utils.C2JUtils.eval;

public interface ActionsCeilings : ActionsMoveEvents, ActionsUseEvents
{

    ContextKey<Ceilings> KEY_CEILINGS = ACTION_KEY_CHAIN.newKey(ActionsCeilings.class, Ceilings::new);

    void RemoveThinker(thinker_t activeCeiling);

    result_e MovePlane(sector_t sector, int speed, int bottomheight, bool crush, int i, int direction);

    int FindSectorFromLineTag(line_t line, int secnum);

    /**
     * This needs to be called before loading, otherwise crushers won't be able to be restarted.
     */
    default void ClearCeilingsBeforeLoading()
    {
        contextRequire(KEY_CEILINGS).activeceilings = new ceiling_t[MAXCEILINGS];
    }

    /**
     * T_MoveCeiling
     */
    default void MoveCeiling(ceiling_t ceiling)
    {
        result_e res;

        switch (ceiling.direction)
        {
            case 0:
                // IN STASIS
                break;
            case 1:
                // UP
                res = MovePlane(ceiling.sector, ceiling.speed, ceiling.topheight, false, 1, ceiling.direction);

                if (!eval(LevelTime() & 7))
                {
                    switch (ceiling.type)
                    {
                        case silentCrushAndRaise:
                            break;
                        default:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    }
                }

                if (res == result_e.pastdest)
                {
                    switch (ceiling.type)
                    {
                        case raiseToHighest:
                            RemoveActiveCeiling(ceiling);
                            break;
                        case silentCrushAndRaise:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_pstop);
                        case fastCrushAndRaise:
                        case crushAndRaise:
                            ceiling.direction = -1;
                        default:
                            break;
                    }
                }
                break;

            case -1:
                // DOWN
                res = MovePlane(ceiling.sector, ceiling.speed, ceiling.bottomheight, ceiling.crush, 1, ceiling.direction);

                if (!eval(LevelTime() & 7))
                {
                    switch (ceiling.type)
                    {
                        case silentCrushAndRaise:
                            break;
                        default:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    }
                }

                if (res == result_e.pastdest)
                {
                    switch (ceiling.type)
                    {
                        case silentCrushAndRaise:
                            StartSound(ceiling.sector.soundorg, sounds.sfxenum_t.sfx_pstop);
                        case crushAndRaise:
                            ceiling.speed = CEILSPEED;
                        case fastCrushAndRaise:
                            ceiling.direction = 1;
                            break;
                        case lowerAndCrush:
                        case lowerToFloor:
                            RemoveActiveCeiling(ceiling);
                            break;
                        default:
                            break;
                    }
                } else
                { // ( res != result_e.pastdest )
                    if (res == result_e.crushed)
                    {
                        switch (ceiling.type)
                        {
                            case silentCrushAndRaise:
                            case crushAndRaise:
                            case lowerAndCrush:
                                ceiling.speed = CEILSPEED / 8;
                                break;
                            default:
                                break;
                        }
                    }
                }
        }
    }

    //
    // EV.DoCeiling
    // Move a ceiling up/down and all around!
    //
    
    @P_Ceiling.C(EV_DoCeiling)
    default bool DoCeiling(line_t line, ceiling_e type)
    {
        int secnum = -1;
        bool rtn = false;
        sector_t sec;
        ceiling_t ceiling;

        //  Reactivate in-stasis ceilings...for certain types.
        switch (type)
        {
            case fastCrushAndRaise:
            case silentCrushAndRaise:
            case crushAndRaise:
                ActivateInStasisCeiling(line);
            default:
                break;
        }

        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0)
        {
            sec = levelLoader().sectors[secnum];
            if (sec.specialdata != null)
            {
                continue;
            }

            // new door thinker
            rtn = true;
            ceiling = new ceiling_t();
            sec.specialdata = ceiling;
            ceiling.thinkerFunction = ActiveStates.T_MoveCeiling;
            AddThinker(ceiling);
            ceiling.sector = sec;
            ceiling.crush = false;

            switch (type)
            {
                case fastCrushAndRaise:
                    ceiling.crush = true;
                    ceiling.topheight = sec.ceilingheight;
                    ceiling.bottomheight = sec.floorheight + 8 * FRACUNIT;
                    ceiling.direction = -1;
                    ceiling.speed = CEILSPEED * 2;
                    break;

                case silentCrushAndRaise:
                case crushAndRaise:
                    ceiling.crush = true;
                    ceiling.topheight = sec.ceilingheight;
                case lowerAndCrush:
                case lowerToFloor:
                    ceiling.bottomheight = sec.floorheight;
                    if (type != ceiling_e.lowerToFloor)
                    {
                        ceiling.bottomheight += 8 * FRACUNIT;
                    }
                    ceiling.direction = -1;
                    ceiling.speed = CEILSPEED;
                    break;

                case raiseToHighest:
                    ceiling.topheight = sec.FindHighestCeilingSurrounding();
                    ceiling.direction = 1;
                    ceiling.speed = CEILSPEED;
                    break;
            }

            ceiling.tag = sec.tag;
            ceiling.type = type;
            AddActiveCeiling(ceiling);
        }
        return rtn;
    }

    //
    // Add an active ceiling
    //
    default void AddActiveCeiling(ceiling_t c)
    {
        ceiling_t[] activeCeilings = getActiveCeilings();
        for (int i = 0; i < activeCeilings.Length; ++i)
        {
            if (activeCeilings[i] == null)
            {
                activeCeilings[i] = c;
                return;
            }
        }
        // Needs rezising
        setActiveceilings(C2JUtils.resize(c, activeCeilings, 2 * activeCeilings.Length));
    }

    //
    // Remove a ceiling's thinker
    //
    default void RemoveActiveCeiling(ceiling_t c)
    {
        ceiling_t[] activeCeilings = getActiveCeilings();
        for (int i = 0; i < activeCeilings.Length; ++i)
        {
            if (activeCeilings[i] == c)
            {
                activeCeilings[i].sector.specialdata = null;
                RemoveThinker(activeCeilings[i]);
                activeCeilings[i] = null;
                break;
            }
        }
    }

    //
    // Restart a ceiling that's in-stasis
    //
    default void ActivateInStasisCeiling(line_t line)
    {
        ceiling_t[] activeCeilings = getActiveCeilings();
        for (int i = 0; i < activeCeilings.Length; ++i)
        {
            if (activeCeilings[i] != null
                    && activeCeilings[i].tag == line.tag
                    && activeCeilings[i].direction == 0)
            {
                activeCeilings[i].direction = activeCeilings[i].olddirection;
                activeCeilings[i].thinkerFunction = ActiveStates.T_MoveCeiling;
            }
        }
    }

    //
    // EV_CeilingCrushStop
    // Stop a ceiling from crushing!
    //
    
    default int CeilingCrushStop(line_t line)
    {
        int i;
        int rtn;

        rtn = 0;
        ceiling_t[] activeCeilings = getActiveCeilings();
        for (i = 0; i < activeCeilings.Length; ++i)
        {
            if (activeCeilings[i] != null
                    && activeCeilings[i].tag == line.tag
                    && activeCeilings[i].direction != 0)
            {
                activeCeilings[i].olddirection = activeCeilings[i].direction;
                // MAES: don't set it to NOP here, otherwise its thinker will be
                // removed and it won't be possible to restart it.
                activeCeilings[i].thinkerFunction = null;
                activeCeilings[i].direction = 0;       // in-stasis
                rtn = 1;
            }
        }

        return rtn;
    }

    default void setActiveceilings(ceiling_t[] activeceilings)
    {
        contextRequire(KEY_CEILINGS).activeceilings = activeceilings;
    }

    default ceiling_t[] getActiveCeilings()
    {
        return contextRequire(KEY_CEILINGS).activeceilings;
    }

    default int getMaxCeilings()
    {
        return contextRequire(KEY_CEILINGS).activeceilings.Length;
    }

    readonly class Ceilings
    {

        ceiling_t[] activeceilings = new ceiling_t[MAXCEILINGS];
    }
}
