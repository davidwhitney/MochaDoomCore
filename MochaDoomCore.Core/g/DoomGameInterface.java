namespace g {  

using defines.skill_t;
using doom.event_t;
using doom.gameaction_t;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: DoomGameInterface.java,v 1.4 2010/12/20 17:15:08 velktron Exp $
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
// DESCRIPTION:
//   Duh.
// 
//-----------------------------------------------------------------------------

public interface DoomGameInterface
{


    //
// GAME
//
    void DeathMatchSpawnPlayer(int playernum);

    void InitNew(skill_t skill, int episode, int map);

    /**
     * Can be called by the startup code or M_Responder.
     * A normal game starts at map 1,
     * but a warp test can start elsewhere
     */
    void DeferedInitNew(skill_t skill, int episode, int map);

    void DeferedPlayDemo(String demo);

    /**
     * Can be called by the startup code or M_Responder,
     * calls P_SetupLevel or W_EnterWorld.
     */
    void LoadGame(String name);

    void DoLoadGame();

    /**
     * Called by M_Responder.
     */
    void SaveGame(int slot, String description);

    /**
     * Only called by startup code.
     */
    void RecordDemo(String name);

    void BeginRecording();

    void PlayDemo(String name);

    void TimeDemo(String name);

    bool CheckDemoStatus();

    void ExitLevel();

    void SecretExitLevel();

    void WorldDone();

    void Ticker();

    bool Responder(event_t ev);

    void ScreenShot();

    gameaction_t getGameAction();

    void setGameAction(gameaction_t ga);

    bool getPaused();

    void setPaused(bool on);

}
