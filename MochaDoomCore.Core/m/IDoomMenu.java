namespace m {  

using doom.SourceCode.M_Menu;
using doom.event_t;

using static doom.SourceCode.M_Menu.*;

// Emacs style mode select -*- C++ -*-
// -----------------------------------------------------------------------------
//
// $Id: IDoomMenu.java,v 1.5 2011/09/29 15:16:23 velktron Exp $
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// DESCRIPTION:
// Menu widget stuff, episode selection and such.
//    
// -----------------------------------------------------------------------------

/**
 *
 */

public interface IDoomMenu
{

    //
    // MENUS
    //

    /**
     * Called by main loop, saves config file and calls I_Quit when user exits.
     * Even when the menu is not displayed, this can resize the view and change
     * game parameters. Does all the real work of the menu interaction.
     */
    @C(M_Responder)
    bool Responder(event_t ev);

    /**
     * Called by main loop, only used for menu (skull cursor) animation.
     */
    @C(M_Ticker)
    void Ticker();

    /**
     * Called by main loop, draws the menus directly into the screen buffer.
     */
    @C(M_Drawer)
    void Drawer();

    /**
     * Called by D_DoomMain, loads the config file.
     */
    @C(M_Init)
    void Init();

    /**
     * Called by intro code to force menu up upon a keypress, does nothing if
     * menu is already up.
     */
    @C(M_StartControlPanel)
    void StartControlPanel();

    bool getShowMessages();

    void setShowMessages(bool val);

    int getScreenBlocks();

    void setScreenBlocks(int val);

    int getDetailLevel();

    void ClearMenus();
}
    