using System;

namespace data
{
//import m.define;

    import defines.ammotype_t;
    import defines.card_t;
    import doom.weapontype_t;
    import g.Signals;

    import static data.Limits.MAXINT;
    import static data.Limits.MININT;
    import static m.fixed_t.*;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: Defines.java,v 1.48 2012/09/24 17:16:22 velktron Exp $
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
//  Internally used data structures for virtually everything,
//   key definitions, lots of other stuff.
//
//-----------------------------------------------------------------------------

//#ifndef __DOOMDEF__
//#define __DOOMDEF__

//#include <stdio.h>
//#include <string.h>

//
// Global parameters/defines.
//
// DOOM version
    public class Defines
    {

        /**
     * Seems to be 109 for shareware 1.9, wtf is this
     */
        public static int VERSION = 109;

        public static int JAVARANDOM_MASK = 0x80;

        /**
     * Some parts of the code may actually be better used as if in a UNIX environment
     */

        public static bool NORMALUNIX = false;


        /**
     * If rangecheck is undefined,  ost parameter validation debugging code will not be compiled
     */
        public static bool RANGECHECK = false;

// Do or do not use external soundserver.
// The sndserver binary to be run separately
//  has been introduced by Dave Taylor.
// The integrated sound support is experimental,
//  and unfinished. Default is synchronous.
// Experimental asynchronous timer based is
//  handled by SNDINTR. 
//#define SNDSERV  1
//#define SNDINTR  1

// Defines suck. C sucks.
// C++ might sucks for OOP, but it sure is a better C.
// So there.

// MAES: moved static defines out of here and into VideoScaleInfo.

        // State updates, number of tics / second.
        public static int BASETICRATE = 35;
        public static int TIC_MUL = 1;
        public static int TICRATE = BASETICRATE * TIC_MUL;

//
// Difficulty/skill settings/filters.
//
        //Maes: this makes it a bit less retarded.
        public static int NUMCARDS = card_t.NUMCARDS.ordinal();

        //Maes: this makes it a bit less retarded.
        public static int NUMWEAPONS = weapontype_t.NUMWEAPONS.ordinal();

        //Maes: this makes it a bit less retarded.
        public static int NUMAMMO = ammotype_t.NUMAMMO.ordinal();

        // Power up artifacts.
        public static int pw_invulnerability = 0;
        public static int pw_strength = 1;
        public static int pw_invisibility = 2;
        public static int pw_ironfeet = 3;
        public static int pw_allmap = 4;
        public static int pw_infrared = 5;
        public static int NUMPOWERS = 6;

        /**
     * Power up durations,
     * how many seconds till expiration,
     * assuming TICRATE is 35 ticks/second.
     */
        public static int INVULNTICS = 30 * TICRATE;
        public static int INVISTICS = 60 * TICRATE;
        public static int INFRATICS = 120 * TICRATE;
        public static int IRONTICS = 60 * TICRATE;

        // Center command from Heretic
        public static int TOCENTER = -8;

        //Silhouette, needed for clipping Segs (mainly)
//and sprites representing things.
        public static int SIL_NONE = 0;
        public static int SIL_BOTTOM = 1;
        public static int SIL_TOP = 2;
        public static int SIL_BOTH = 3;

// from r_defs.h:
        //SKY, store the number for name.
        static public String SKYFLATNAME = "F_SKY1";

        // The sky map is 256*128*4 maps.
        public static int ANGLETOSKYSHIFT = 22;

        // status bar height at bottom of screen
        public static int SBARHEIGHT = 32;

        //
//Different vetween registered DOOM (1994) and
//Ultimate DOOM - edition (retail, 1995?).
//This is supposedly ignored for commercial
//release (aka DOOM II), which had 34 maps
//in one episode. So there.
        public static int NUMEPISODES = 4;
        public static int NUMMAPS = 9;
        public static int BACKUPTICS = 12;

// From r_draw.c
        //
//ZONE MEMORY
//PU - purge tags.
//Tags < 100 are not overwritten until freed.
        public static int PU_STATIC = 1; // static entire execution time
        public static int PU_SOUND = 2; // static while playing
        public static int PU_MUSIC = 3; // static while playing


//in tics
//U #define PAUSELEN        (TICRATE*2) 
//U #define SCORESTEP       100
//U #define ANIMPERIOD      32
//pixel distance from "(YOU)" to "PLAYER N"
//U #define STARDIST        10 
//U #define WK 1


// MAES 23/5/2011: moved SP_... stuff to EndLevel
        public static int PU_DAVE = 4; // anything else Dave wants static


// From Zone:
        public static int PU_LEVEL = 50; // static until level exited
        public static int PU_LEVSPEC = 51; // a special thinker in a level

        //Tags >= 100 are purgable whenever needed.
        public static int PU_PURGELEVEL = 100;
        public static int PU_CACHE = 101;

        //font stuff
        static public Signals.ScanCode HU_CHARERASE = Signals.ScanCode.SC_BACKSPACE;
        public static int HU_MAXLINES = 4;
        public static int HU_MAXLINELENGTH = 80;

        //
//Globally visible constants.
//
        static public byte HU_FONTSTART = '!'; // the first font characters


// From hu_lib.h:
        static public byte HU_FONTEND = '_'; // the last font characters

        //Calculate # of glyphs in font.
        public static int HU_FONTSIZE = HU_FONTEND - HU_FONTSTART + 1;
        static public char HU_BROADCAST = 5;

// From hu_stuff.h
        static public Signals.ScanCode HU_MSGREFRESH = Signals.ScanCode.SC_ENTER;
        static public char HU_MSGX = 0;
        static public char HU_MSGY = 0;
        static public char HU_MSGWIDTH = 64; // in characters
        static public char HU_MSGHEIGHT = 1; // in lines
        public static int HU_MSGTIMEOUT = 4 * TICRATE;
        public static int SAVESTRINGSIZE = 24;

        // Press "Fire".
        public static int BT_ATTACK = 1;

        // Use button, to open doors, activate switches.
        public static int BT_USE = 2;

        // Flag: game events, not really buttons.
        public static int BT_SPECIAL = 128;
        public static int BT_SPECIALMASK = 3;

//
// Button/action code definitions.
// From d_event.h
        // Flag, weapon change pending.
        // If true, the next 3 bits hold weapon num.
        public static int BT_CHANGE = 4;

        // The 3bit weapon mask and shift, convenience.
        public static int BT_WEAPONMASK = 8 + 16 + 32;
        public static int BT_WEAPONSHIFT = 3;

        // Pause the game.
        public static int BTS_PAUSE = 1;

        // Save the game at each console.
        public static int BTS_SAVEGAME = 2;

        // Savegame slot numbers
        //  occupy the second byte of buttons.
        public static int BTS_SAVEMASK = 4 + 8 + 16;
        public static int BTS_SAVESHIFT = 2;
        public static int FLOATSPEED = FRACUNIT * 4;
        public static int VIEWHEIGHT = 41 * FRACUNIT;

        // mapblocks are used to check movement
        // against lines and things
        public static int MAPBLOCKUNITS = 128;
        public static int MAPBLOCKSIZE = MAPBLOCKUNITS * FRACUNIT;


        //==================== Stuff from r_local.c =========================================
        public static int MAPBLOCKSHIFT = FRACBITS + 7;
        public static int MAPBMASK = MAPBLOCKSIZE - 1;
        public static int MAPBTOFRAC = MAPBLOCKSHIFT - FRACBITS;
        public static int BLOCKMAPPADDING = 8 * FRACUNIT;

        // player radius for movement checking
        public static int PLAYERRADIUS = 16 * FRACUNIT;
        public static int GRAVITY = MAPFRACUNIT;

        /**
     * Indicate a leaf. e6y: support for extended nodes
     */
        public static int NF_SUBSECTOR = 0x80000000;

        /**
     * This is the regular leaf indicator. Use for reference/conversions
     */
        public static int NF_SUBSECTOR_CLASSIC = 0x8000;

        /**
     * Player states.
     */

        public static int PST_LIVE = 0; // Playing or camping.
        public static int PST_DEAD = 1; // Dead on the ground, view follows killer.

        public static int PST_REBORN = 2; // Ready to restart/respawn???
        public static int FF_FULLBRIGHT = 0x8000; // flag in thing->frame
        public static int FF_FRAMEMASK = 0x7fff;
        static String
        rcsid = "$Id: Defines.java,v 1.48 2012/09/24 17:16:22 velktron Exp $";

        // Skill flags.
        public static int MTF_EASY = 1;
        public static int MTF_NORMAL = 2;

        public static int MTF_HARD = 4;

        // Deaf monsters/do not react to sound.
        public static int MTF_AMBUSH = 8;
        public static int USERANGE = 64 * FRACUNIT;
        public static int MELEERANGE = 64 * FRACUNIT;

        public static int MISSILERANGE = 32 * 64 * FRACUNIT;

        // follow a player exlusively for 3 seconds
        public static int BASETHRESHOLD = 100;
        public static int PT_ADDLINES = 1;
        public static int PT_ADDTHINGS = 2;

        public static int PT_EARLYOUT = 4;

        //
        // P_MOBJ
        //
        public static int ONFLOORZ = MININT;

        public static int ONCEILINGZ = MAXINT;

        // Time interval for item respawning.
        public static int ITEMQUESIZE = 128;
    }

}