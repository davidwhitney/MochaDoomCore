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

using data.mobjtype_t;
using defines.statenum_t;
using doom.SourceCode.fixed_t;
using doom.player_t;
using p.mobj_t;
using rr.SceneRenderer;
using rr.line_t;
using rr.sector_t;
using rr.side_t;
using utils.TraitFactory.ContextKey;

using static data.Defines.MELEERANGE;
using static data.Limits.MAXSPECIALCROSS;
using static data.Tables.*;
using static doom.items.weaponinfo;
using static m.fixed_t.FRACUNIT;
using static p.MapUtils.AproxDistance;
using static p.MobjFlags.MF_JUSTHIT;
using static rr.line_t.ML_SOUNDBLOCK;
using static rr.line_t.ML_TWOSIDED;

public interface ActionsEnemies extends ActionsSight, ActionsSpawns
{

    ContextKey<Enemies> KEY_ENEMIES = ACTION_KEY_CHAIN.newKey(ActionsEnemies.class, Enemies::new);

    /**
     * P_CheckMeleeRange
     */
    default bool CheckMeleeRange(mobj_t actor)
    {
        mobj_t pl;
        @fixed_t
        int dist;

        if (actor.target == null)
        {
            return false;
        }

        pl = actor.target;
        dist = AproxDistance(pl.x - actor.x, pl.y - actor.y);

        if (dist >= MELEERANGE - 20 * FRACUNIT + pl.info.radius)
        {
            return false;
        }

        return CheckSight(actor, actor.target);
    }

    //
    // ENEMY THINKING
    // Enemies are allways spawned
    // with targetplayer = -1, threshold = 0
    // Most monsters are spawned unaware of all players,
    // but some can be made preaware
    //

    /**
     * P_CheckMissileRange
     */
    default bool CheckMissileRange(mobj_t actor)
    {
        @fixed_t
        int dist;

        if (!CheckSight(actor, actor.target))
        {
            return false;
        }

        if ((actor.flags & MF_JUSTHIT) != 0)
        {
            // the target just hit the enemy,
            // so fight back!
            actor.flags &= ~MF_JUSTHIT;
            return true;
        }

        if (actor.reactiontime != 0)
        {
            return false; // do not attack yet
        }

        // OPTIMIZE: get this from a global checksight
        dist = AproxDistance(actor.x - actor.target.x, actor.y - actor.target.y) - 64 * FRACUNIT;

        // [SYNC}: Major desync cause of desyncs.
        // DO NOT compare with null!
        if (actor.info.meleestate == statenum_t.S_NULL)
        {
            dist -= 128 * FRACUNIT; // no melee attack, so fire more
        }

        dist >>= 16;

        if (actor.type == mobjtype_t.MT_VILE)
        {
            if (dist > 14 * 64)
            {
                return false; // too far away
            }
        }

        if (actor.type == mobjtype_t.MT_UNDEAD)
        {
            if (dist < 196)
            {
                return false; // close for fist attack
            }
            dist >>= 1;
        }

        if (actor.type == mobjtype_t.MT_CYBORG || actor.type == mobjtype_t.MT_SPIDER || actor.type == mobjtype_t.MT_SKULL)
        {
            dist >>= 1;
        }

        if (dist > 200)
        {
            dist = 200;
        }

        if (actor.type == mobjtype_t.MT_CYBORG && dist > 160)
        {
            dist = 160;
        }

        return P_Random() >= dist;
    }

    //
    // Called by P_NoiseAlert.
    // Recursively traverse adjacent sectors,
    // sound blocking lines cut off traversal.
    //
    default void RecursiveSound(sector_t sec, int soundblocks)
    {
        SceneRenderer<?, ?> sr = sceneRenderer();
        Enemies en = contextRequire(KEY_ENEMIES);
        Movement mov = contextRequire(KEY_MOVEMENT);
        int i;
        line_t check;
        sector_t other;

        // wake up all monsters in this sector
        if (sec.validcount == sr.getValidCount() && sec.soundtraversed <= soundblocks + 1)
        {
            return; // already flooded
        }

        sec.validcount = sr.getValidCount();
        sec.soundtraversed = soundblocks + 1;
        sec.soundtarget = en.soundtarget;

        // "peg" to the level loader for syntactic sugar
        side_t[] sides = levelLoader().sides;

        for (i = 0; i < sec.linecount; i++)
        {
            check = sec.lines[i];

            if ((check.flags & ML_TWOSIDED) == 0)
            {
                continue;
            }

            LineOpening(check);

            if (mov.openrange <= 0)
            {
                continue; // closed door
            }

            if (sides[check.sidenum[0]].sector == sec)
            {
                other = sides[check.sidenum[1]].sector;
            } else
            {
                other = sides[check.sidenum[0]].sector;
            }

            if ((check.flags & ML_SOUNDBLOCK) != 0)
            {
                if (soundblocks == 0)
                {
                    RecursiveSound(other, 1);
                }
            } else
            {
                RecursiveSound(other, soundblocks);
            }
        }
    }

    /**
     * P_NoiseAlert
     * If a monster yells at a player,
     * it will alert other monsters to the player.
     */
    default void NoiseAlert(mobj_t target, mobj_t emmiter)
    {
        Enemies en = contextRequire(KEY_ENEMIES);
        en.soundtarget = target;
        sceneRenderer().increaseValidCount(1);
        RecursiveSound(emmiter.subsector.sector, 0);
    }

    /**
     * P_FireWeapon. Originally in pspr
     */
    default void FireWeapon(player_t player)
    {
        statenum_t newstate;

        if (!player.CheckAmmo())
        {
            return;
        }

        player.mo.SetMobjState(statenum_t.S_PLAY_ATK1);
        newstate = weaponinfo[player.readyweapon.ordinal()].atkstate;
        player.SetPsprite(player_t.ps_weapon, newstate);
        NoiseAlert(player.mo, player.mo);
    }

    /**
     * P_LookForPlayers If allaround is false, only look 180 degrees in
     * front. Returns true if a player is targeted.
     */
    default bool LookForPlayers(mobj_t actor, bool allaround)
    {
        SceneRenderer<?, ?> sr = sceneRenderer();

        int c;
        int stop;
        player_t player;
        // sector_t sector;
        long an; // angle
        int dist; // fixed

        // sector = actor.subsector.sector;
        c = 0;
        stop = actor.lastlook - 1 & 3;

        for (; ; actor.lastlook = actor.lastlook + 1 & 3)
        {
            if (!PlayerInGame(actor.lastlook))
            {
                continue;
            }

            if (c++ == 2 || actor.lastlook == stop)
            {
                // done looking
                return false;
            }

            player = getPlayer(actor.lastlook);

            if (player.health[0] <= 0)
            {
                continue; // dead
            }

            if (!CheckSight(actor, player.mo))
            {
                continue; // out of sight
            }

            if (!allaround)
            {
                an = sr.PointToAngle2(actor.x, actor.y, player.mo.x, player.mo.y) - actor.angle & BITS32;

                if (an > ANG90 && an < ANG270)
                {
                    dist = AproxDistance(player.mo.x - actor.x, player.mo.y - actor.y);

                    // if real close, react anyway
                    if (dist > MELEERANGE)
                    {
                        continue; // behind back
                    }
                }
            }

            actor.target = player.mo;
            return true;
        }
        // The compiler complains that this is unreachable
        // return false;
    }

    class Enemies
    {

        mobj_t soundtarget;
        // Peg to map movement
        line_t[] spechitp = new line_t[MAXSPECIALCROSS];
        int numspechit;
    }

}
