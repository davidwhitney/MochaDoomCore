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
namespace p {  

using automap.IAutoMap;
using data.sounds;
using defines.skill_t;
using doom.DoomMain;
using doom.player_t;
using hu.IHeadsUp;
using i.IDoomSystem;
using p.Actions.ActionsAttacks;
using p.Actions.ActionsEnemies;
using p.Actions.ActionsThinkers;
using p.Actions.ActiveStates.Ai;
using p.Actions.ActiveStates.Attacks;
using p.Actions.ActiveStates.Thinkers;
using p.Actions.ActiveStates.Weapons;
using rr.SceneRenderer;
using s.ISoundOrigin;
using st.IDoomStatusBar;
using utils.TraitFactory;
using utils.TraitFactory.SharedContext;

using java.util.logging.Level;
using java.util.logging.Logger;

public class ActionFunctions : UnifiedGameMap :
        ActionsThinkers, ActionsEnemies, ActionsAttacks, Ai, Attacks, Thinkers, Weapons
{
    private readonly SharedContext traitsSharedContext;

    public ActionFunctions(DoomMain<?, ?> DOOM)
    {
        super(DOOM);
        traitsSharedContext = buildContext();
    }

    private SharedContext buildContext()
    {
        try
        {
            return TraitFactory.build(this, ACTION_KEY_CHAIN);
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            Logger.getLogger(ActionFunctions.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    
    public AbstractLevelLoader levelLoader()
    {
        return DOOM.levelLoader;
    }

    
    public IHeadsUp headsUp()
    {
        return DOOM.headsUp;
    }

    
    public IDoomSystem doomSystem()
    {
        return DOOM.doomSystem;
    }

    
    public IDoomStatusBar statusBar()
    {
        return DOOM.statusBar;
    }

    
    public IAutoMap<?, ?> autoMap()
    {
        return DOOM.autoMap;
    }

    
    public SceneRenderer<?, ?> sceneRenderer()
    {
        return DOOM.sceneRenderer;
    }

    
    public UnifiedGameMap.Specials getSpecials()
    {
        return SPECS;
    }

    
    public UnifiedGameMap.Switches getSwitches()
    {
        return SW;
    }

    
    public void StopSound(ISoundOrigin origin)
    {
        DOOM.doomSound.StopSound(origin);
    }

    
    public void StartSound(ISoundOrigin origin, sounds.sfxenum_t s)
    {
        DOOM.doomSound.StartSound(origin, s);
    }

    
    public void StartSound(ISoundOrigin origin, int s)
    {
        DOOM.doomSound.StartSound(origin, s);
    }

    
    public player_t getPlayer(int number)
    {
        return DOOM.players[number];
    }

    
    public skill_t getGameSkill()
    {
        return DOOM.gameskill;
    }

    
    public mobj_t createMobj()
    {
        return mobj_t.createOn(DOOM);
    }

    
    public int LevelTime()
    {
        return DOOM.leveltime;
    }

    
    public int P_Random()
    {
        return DOOM.random.P_Random();
    }

    
    public int ConsolePlayerNumber()
    {
        return DOOM.consoleplayer;
    }

    
    public int MapNumber()
    {
        return DOOM.gamemap;
    }

    
    public bool PlayerInGame(int number)
    {
        return DOOM.playeringame[number];
    }

    
    public bool IsFastParm()
    {
        return DOOM.fastparm;
    }

    
    public bool IsPaused()
    {
        return DOOM.paused;
    }

    
    public bool IsNetGame()
    {
        return DOOM.netgame;
    }

    
    public bool IsDemoPlayback()
    {
        return DOOM.demoplayback;
    }

    
    public bool IsDeathMatch()
    {
        return DOOM.deathmatch;
    }

    
    public bool IsAutoMapActive()
    {
        return DOOM.automapactive;
    }

    
    public bool IsMenuActive()
    {
        return DOOM.menuactive;
    }

    /**
     * TODO: avoid, deprecate
     */
    
    public DoomMain<?, ?> DOOM()
    {
        return DOOM;
    }

    
    public SharedContext getContext()
    {
        return traitsSharedContext;
    }

    
    public ActionsThinkers getThinkers()
    {
        return this;
    }

    
    public ActionsEnemies getEnemies()
    {
        return this;
    }

    
    public ActionsAttacks getAttacks()
    {
        return this;
    }
}
