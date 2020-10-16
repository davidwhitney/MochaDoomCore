using System;

namespace doom {  

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: englsh.java,v 1.5 2011/05/31 21:46:20 velktron Exp $
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
//  Printed strings for translation.
//  English language support (default).
//
//-----------------------------------------------------------------------------

//
//  Printed strings for translation
//

//
// D_Main.C
//

public class englsh
{
    public readonly static string D_DEVSTR = "Development mode ON.\n";
    public readonly static String D_CDROM = "CD-ROM Version: default.cfg from c:\\doomdata\n";

//
// M_Misc.C
//

    public readonly static String SCREENSHOT = "screen shot";

    //
//  M_Menu.C
//
    public readonly static String PRESSKEY = "press a key.";
    public readonly static String PRESSYN = "press y or n.";
    public readonly static String QUITMSG = "are you sure you want to\nquit this great game?";
    public readonly static String LOADNET = "you can't do load while in a net game!\n\n" + PRESSKEY;
    public readonly static String QLOADNET = "you can't quickload during a netgame!\n\n" + PRESSKEY;
    public readonly static String QSAVESPOT = "you haven't picked a quicksave slot yet!\n\n" + PRESSKEY;
    public readonly static String SAVEDEAD = "you can't save if you aren't playing!\n\n" + PRESSKEY;
    public readonly static String QSPROMPT = "quicksave over your game named\n\n'%s'?\n\n" + PRESSYN;
    public readonly static String QLPROMPT = "do you want to quickload the game named\n\n'%s'?\n\n" + PRESSYN;

    public readonly static String NEWGAME = "you can't start a new game\nwhile in a network game.\n\n" + PRESSKEY;

    public readonly static String NIGHTMARE = "are you sure? this skill level\nisn't even remotely fair.\n\n" + PRESSYN;

    public readonly static String SWSTRING = "this is the shareware version of doom.\n\nyou need to order the entire trilogy.\n\n" + PRESSKEY;

    public readonly static String MSGOFF = "Messages OFF";
    public readonly static String MSGON = "Messages ON";
    public readonly static String NETEND = "you can't end a netgame!\n\n" + PRESSKEY;
    public readonly static String ENDGAME = "are you sure you want to end the game?\n\n" + PRESSYN;

    public readonly static String DOSY = "(press y to quit)";

    public readonly static String DETAILHI = "High detail";
    public readonly static String DETAILLO = "Low detail";
    public readonly static String GAMMALVL0 = "Gamma correction OFF";
    public readonly static String GAMMALVL1 = "Gamma correction level 1";
    public readonly static String GAMMALVL2 = "Gamma correction level 2";
    public readonly static String GAMMALVL3 = "Gamma correction level 3";
    public readonly static String GAMMALVL4 = "Gamma correction level 4";
    public readonly static String EMPTYSTRING = "empty slot";

    //
//  P_inter.C
//
    public readonly static String GOTARMOR = "Picked up the armor.";
    public readonly static String GOTMEGA = "Picked up the MegaArmor!";
    public readonly static String GOTHTHBONUS = "Picked up a health bonus.";
    public readonly static String GOTARMBONUS = "Picked up an armor bonus.";
    public readonly static String GOTSTIM = "Picked up a stimpack.";
    public readonly static String GOTMEDINEED = "Picked up a medikit that you REALLY need!";
    public readonly static String GOTMEDIKIT = "Picked up a medikit.";
    public readonly static String GOTSUPER = "Supercharge!";

    public readonly static String GOTBLUECARD = "Picked up a blue keycard.";
    public readonly static String GOTYELWCARD = "Picked up a yellow keycard.";
    public readonly static String GOTREDCARD = "Picked up a red keycard.";
    public readonly static String GOTBLUESKUL = "Picked up a blue skull key.";
    public readonly static String GOTYELWSKUL = "Picked up a yellow skull key.";
    public readonly static String GOTREDSKULL = "Picked up a red skull key.";

    public readonly static String GOTINVUL = "Invulnerability!";
    public readonly static String GOTBERSERK = "Berserk!";
    public readonly static String GOTINVIS = "Partial Invisibility";
    public readonly static String GOTSUIT = "Radiation Shielding Suit";
    public readonly static String GOTMAP = "Computer Area Map";
    public readonly static String GOTVISOR = "Light Amplification Visor";
    public readonly static String GOTMSPHERE = "MegaSphere!";

    public readonly static String GOTCLIP = "Picked up a clip.";
    public readonly static String GOTCLIPBOX = "Picked up a box of bullets.";
    public readonly static String GOTROCKET = "Picked up a rocket.";
    public readonly static String GOTROCKBOX = "Picked up a box of rockets.";
    public readonly static String GOTCELL = "Picked up an energy cell.";
    public readonly static String GOTCELLBOX = "Picked up an energy cell pack.";
    public readonly static String GOTSHELLS = "Picked up 4 shotgun shells.";
    public readonly static String GOTSHELLBOX = "Picked up a box of shotgun shells.";
    public readonly static String GOTBACKPACK = "Picked up a backpack full of ammo!";

    public readonly static String GOTBFG9000 = "You got the BFG9000!  Oh, yes.";
    public readonly static String GOTCHAINGUN = "You got the chaingun!";
    public readonly static String GOTCHAINSAW = "A chainsaw!  Find some meat!";
    public readonly static String GOTLAUNCHER = "You got the rocket launcher!";
    public readonly static String GOTPLASMA = "You got the plasma gun!";
    public readonly static String GOTSHOTGUN = "You got the shotgun!";
    public readonly static String GOTSHOTGUN2 = "You got the super shotgun!";

    //
// P_Doors.C
//
    public readonly static String PD_BLUEO = "You need a blue key to activate this object";
    public readonly static String PD_REDO = "You need a red key to activate this object";
    public readonly static String PD_YELLOWO = "You need a yellow key to activate this object";
    public readonly static String PD_BLUEK = "You need a blue key to open this door";
    public readonly static String PD_REDK = "You need a red key to open this door";
    public readonly static String PD_YELLOWK = "You need a yellow key to open this door";

    //
//  G_game.C
//
    public readonly static String GGSAVED = "game saved.";

    //
//  HU_stuff.C
//
    public readonly static String HUSTR_MSGU = "[Message unsent]";

    public readonly static String HUSTR_E1M1 = "E1M1: Hangar";
    public readonly static String HUSTR_E1M2 = "E1M2: Nuclear Plant";
    public readonly static String HUSTR_E1M3 = "E1M3: Toxin Refinery";
    public readonly static String HUSTR_E1M4 = "E1M4: Command Control";
    public readonly static String HUSTR_E1M5 = "E1M5: Phobos Lab";
    public readonly static String HUSTR_E1M6 = "E1M6: Central Processing";
    public readonly static String HUSTR_E1M7 = "E1M7: Computer Station";
    public readonly static String HUSTR_E1M8 = "E1M8: Phobos Anomaly";
    public readonly static String HUSTR_E1M9 = "E1M9: Military Base";

    public readonly static String HUSTR_E2M1 = "E2M1: Deimos Anomaly";
    public readonly static String HUSTR_E2M2 = "E2M2: Containment Area";
    public readonly static String HUSTR_E2M3 = "E2M3: Refinery";
    public readonly static String HUSTR_E2M4 = "E2M4: Deimos Lab";
    public readonly static String HUSTR_E2M5 = "E2M5: Command Center";
    public readonly static String HUSTR_E2M6 = "E2M6: Halls of the Damned";
    public readonly static String HUSTR_E2M7 = "E2M7: Spawning Vats";
    public readonly static String HUSTR_E2M8 = "E2M8: Tower of Babel";
    public readonly static String HUSTR_E2M9 = "E2M9: Fortress of Mystery";

    public readonly static String HUSTR_E3M1 = "E3M1: Hell Keep";
    public readonly static String HUSTR_E3M2 = "E3M2: Slough of Despair";
    public readonly static String HUSTR_E3M3 = "E3M3: Pandemonium";
    public readonly static String HUSTR_E3M4 = "E3M4: House of Pain";
    public readonly static String HUSTR_E3M5 = "E3M5: Unholy Cathedral";
    public readonly static String HUSTR_E3M6 = "E3M6: Mt. Erebus";
    public readonly static String HUSTR_E3M7 = "E3M7: Limbo";
    public readonly static String HUSTR_E3M8 = "E3M8: Dis";
    public readonly static String HUSTR_E3M9 = "E3M9: Warrens";

    public readonly static String HUSTR_E4M1 = "E4M1: Hell Beneath";
    public readonly static String HUSTR_E4M2 = "E4M2: Perfect Hatred";
    public readonly static String HUSTR_E4M3 = "E4M3: Sever The Wicked";
    public readonly static String HUSTR_E4M4 = "E4M4: Unruly Evil";
    public readonly static String HUSTR_E4M5 = "E4M5: They Will Repent";
    public readonly static String HUSTR_E4M6 = "E4M6: Against Thee Wickedly";
    public readonly static String HUSTR_E4M7 = "E4M7: And Hell Followed";
    public readonly static String HUSTR_E4M8 = "E4M8: Unto The Cruel";
    public readonly static String HUSTR_E4M9 = "E4M9: Fear";

    public readonly static String HUSTR_1 = "level 1: entryway";
    public readonly static String HUSTR_2 = "level 2: underhalls";
    public readonly static String HUSTR_3 = "level 3: the gantlet";
    public readonly static String HUSTR_4 = "level 4: the focus";
    public readonly static String HUSTR_5 = "level 5: the waste tunnels";
    public readonly static String HUSTR_6 = "level 6: the crusher";
    public readonly static String HUSTR_7 = "level 7: dead simple";
    public readonly static String HUSTR_8 = "level 8: tricks and traps";
    public readonly static String HUSTR_9 = "level 9: the pit";
    public readonly static String HUSTR_10 = "level 10: refueling base";
    public readonly static String HUSTR_11 = "level 11: 'o' of destruction!";

    public readonly static String HUSTR_12 = "level 12: the factory";
    public readonly static String HUSTR_13 = "level 13: downtown";
    public readonly static String HUSTR_14 = "level 14: the inmost dens";
    public readonly static String HUSTR_15 = "level 15: industrial zone";
    public readonly static String HUSTR_16 = "level 16: suburbs";
    public readonly static String HUSTR_17 = "level 17: tenements";
    public readonly static String HUSTR_18 = "level 18: the courtyard";
    public readonly static String HUSTR_19 = "level 19: the citadel";
    public readonly static String HUSTR_20 = "level 20: gotcha!";

    public readonly static String HUSTR_21 = "level 21: nirvana";
    public readonly static String HUSTR_22 = "level 22: the catacombs";
    public readonly static String HUSTR_23 = "level 23: barrels o' fun";
    public readonly static String HUSTR_24 = "level 24: the chasm";
    public readonly static String HUSTR_25 = "level 25: bloodfalls";
    public readonly static String HUSTR_26 = "level 26: the abandoned mines";
    public readonly static String HUSTR_27 = "level 27: monster condo";
    public readonly static String HUSTR_28 = "level 28: the spirit world";
    public readonly static String HUSTR_29 = "level 29: the living end";
    public readonly static String HUSTR_30 = "level 30: icon of sin";

    public readonly static String HUSTR_31 = "level 31: wolfenstein";
    public readonly static String HUSTR_32 = "level 32: grosse";
    public readonly static String HUSTR_33 = "level 33: betray";

    public readonly static String PHUSTR_1 = "level 1: congo";
    public readonly static String PHUSTR_2 = "level 2: well of souls";
    public readonly static String PHUSTR_3 = "level 3: aztec";
    public readonly static String PHUSTR_4 = "level 4: caged";
    public readonly static String PHUSTR_5 = "level 5: ghost town";
    public readonly static String PHUSTR_6 = "level 6: baron's lair";
    public readonly static String PHUSTR_7 = "level 7: caughtyard";
    public readonly static String PHUSTR_8 = "level 8: realm";
    public readonly static String PHUSTR_9 = "level 9: abattoire";
    public readonly static String PHUSTR_10 = "level 10: onslaught";
    public readonly static String PHUSTR_11 = "level 11: hunted";

    public readonly static String PHUSTR_12 = "level 12: speed";
    public readonly static String PHUSTR_13 = "level 13: the crypt";
    public readonly static String PHUSTR_14 = "level 14: genesis";
    public readonly static String PHUSTR_15 = "level 15: the twilight";
    public readonly static String PHUSTR_16 = "level 16: the omen";
    public readonly static String PHUSTR_17 = "level 17: compound";
    public readonly static String PHUSTR_18 = "level 18: neurosphere";
    public readonly static String PHUSTR_19 = "level 19: nme";
    public readonly static String PHUSTR_20 = "level 20: the death domain";

    public readonly static String PHUSTR_21 = "level 21: slayer";
    public readonly static String PHUSTR_22 = "level 22: impossible mission";
    public readonly static String PHUSTR_23 = "level 23: tombstone";
    public readonly static String PHUSTR_24 = "level 24: the readonly frontier";
    public readonly static String PHUSTR_25 = "level 25: the temple of darkness";
    public readonly static String PHUSTR_26 = "level 26: bunker";
    public readonly static String PHUSTR_27 = "level 27: anti-christ";
    public readonly static String PHUSTR_28 = "level 28: the sewers";
    public readonly static String PHUSTR_29 = "level 29: odyssey of noises";
    public readonly static String PHUSTR_30 = "level 30: the gateway of hell";

    public readonly static String PHUSTR_31 = "level 31: cyberden";
    public readonly static String PHUSTR_32 = "level 32: go 2 it";

    public readonly static String THUSTR_1 = "level 1: system control";
    public readonly static String THUSTR_2 = "level 2: human bbq";
    public readonly static String THUSTR_3 = "level 3: power control";
    public readonly static String THUSTR_4 = "level 4: wormhole";
    public readonly static String THUSTR_5 = "level 5: hanger";
    public readonly static String THUSTR_6 = "level 6: open season";
    public readonly static String THUSTR_7 = "level 7: prison";
    public readonly static String THUSTR_8 = "level 8: metal";
    public readonly static String THUSTR_9 = "level 9: stronghold";
    public readonly static String THUSTR_10 = "level 10: redemption";
    public readonly static String THUSTR_11 = "level 11: storage facility";

    public readonly static String THUSTR_12 = "level 12: crater";
    public readonly static String THUSTR_13 = "level 13: nukage processing";
    public readonly static String THUSTR_14 = "level 14: steel works";
    public readonly static String THUSTR_15 = "level 15: dead zone";
    public readonly static String THUSTR_16 = "level 16: deepest reaches";
    public readonly static String THUSTR_17 = "level 17: processing area";
    public readonly static String THUSTR_18 = "level 18: mill";
    public readonly static String THUSTR_19 = "level 19: shipping/respawning";
    public readonly static String THUSTR_20 = "level 20: central processing";

    public readonly static String THUSTR_21 = "level 21: administration center";
    public readonly static String THUSTR_22 = "level 22: habitat";
    public readonly static String THUSTR_23 = "level 23: lunar mining project";
    public readonly static String THUSTR_24 = "level 24: quarry";
    public readonly static String THUSTR_25 = "level 25: baron's den";
    public readonly static String THUSTR_26 = "level 26: ballistyx";
    public readonly static String THUSTR_27 = "level 27: mount pain";
    public readonly static String THUSTR_28 = "level 28: heck";
    public readonly static String THUSTR_29 = "level 29: river styx";
    public readonly static String THUSTR_30 = "level 30: last call";

    public readonly static String THUSTR_31 = "level 31: pharaoh";
    public readonly static String THUSTR_32 = "level 32: caribbean";

    public readonly static String HUSTR_CHATMACRO1 = "I'm ready to kick butt!";
    public readonly static String HUSTR_CHATMACRO2 = "I'm OK.";
    public readonly static String HUSTR_CHATMACRO3 = "I'm not looking too good!";
    public readonly static String HUSTR_CHATMACRO4 = "Help!";
    public readonly static String HUSTR_CHATMACRO5 = "You suck!";
    public readonly static String HUSTR_CHATMACRO6 = "Next time, scumbag...";
    public readonly static String HUSTR_CHATMACRO7 = "Come here!";
    public readonly static String HUSTR_CHATMACRO8 = "I'll take care of it.";
    public readonly static String HUSTR_CHATMACRO9 = "Yes";
    public readonly static String HUSTR_CHATMACRO0 = "No";

    public readonly static String HUSTR_TALKTOSELF1 = "You mumble to yourself";
    public readonly static String HUSTR_TALKTOSELF2 = "Who's there?";
    public readonly static String HUSTR_TALKTOSELF3 = "You scare yourself";
    public readonly static String HUSTR_TALKTOSELF4 = "You start to rave";
    public readonly static String HUSTR_TALKTOSELF5 = "You've lost it...";

    public readonly static String HUSTR_MESSAGESENT = "[Message Sent]";

// The following should NOT be changed unless it seems
// just AWFULLY necessary

    public readonly static String HUSTR_PLRGREEN = "Green: ";
    public readonly static String HUSTR_PLRINDIGO = "Indigo: ";
    public readonly static String HUSTR_PLRBROWN = "Brown: ";
    public readonly static String HUSTR_PLRRED = "Red: ";

    public readonly static char HUSTR_KEYGREEN = 'g';
    public readonly static char HUSTR_KEYINDIGO = 'i';
    public readonly static char HUSTR_KEYBROWN = 'b';
    public readonly static char HUSTR_KEYRED = 'r';

//
//  AM_map.C
//

    public readonly static String AMSTR_FOLLOWON = "Follow Mode ON";
    public readonly static String AMSTR_FOLLOWOFF = "Follow Mode OFF";

    public readonly static String AMSTR_GRIDON = "Grid ON";
    public readonly static String AMSTR_GRIDOFF = "Grid OFF";

    public readonly static String AMSTR_MARKEDSPOT = "Marked Spot";
    public readonly static String AMSTR_MARKSCLEARED = "All Marks Cleared";

//
//  ST_stuff.C
//

    public readonly static String STSTR_MUS = "Music Change";
    public readonly static String STSTR_NOMUS = "IMPOSSIBLE SELECTION";
    public readonly static String STSTR_DQDON = "Degreelessness Mode On";
    public readonly static String STSTR_DQDOFF = "Degreelessness Mode Off";

    public readonly static String STSTR_KFAADDED = "Very Happy Ammo Added";
    public readonly static String STSTR_FAADDED = "Ammo (no keys) Added";

    public readonly static String STSTR_NCON = "No Clipping Mode ON";
    public readonly static String STSTR_NCOFF = "No Clipping Mode OFF";

    public readonly static String STSTR_BEHOLD = "inVuln, Str, Inviso, Rad, Allmap, or Lite-amp";
    public readonly static String STSTR_BEHOLDX = "Power-up Toggled";

    public readonly static String STSTR_CHOPPERS = "... doesn't suck - GM";
    public readonly static String STSTR_CLEV = "Changing Level...";

    //
//  F_Finale.C
//
    public readonly static String E1TEXT = "Once you beat the big badasses and\n" +
            "clean out the moon base you're supposed\n" +
            "to win, aren't you? Aren't you? Where's\n" +
            "your fat reward and ticket home? What\n" +
            "the hell is this? It's not supposed to\n" +
            "end this way!\n" +
            "\n" +
            "It stinks like rotten meat, but looks\n" +
            "like the lost Deimos base.  Looks like\n" +
            "you're stuck on The Shores of Hell.\n" +
            "The only way out is through.\n" +
            "\n" +
            "To continue the DOOM experience, play\n" +
            "The Shores of Hell and its amazing\n" +
            "sequel, Inferno!\n";


    public readonly static String E2TEXT = "You've done it! The hideous cyber-\n" +
            "demon lord that ruled the lost Deimos\n" +
            "moon base has been slain and you\n" +
            "are triumphant! But ... where are\n" +
            "you? You clamber to the edge of the\n" +
            "moon and look down to see the awful\n" +
            "truth.\n" +
            "\n" +
            "Deimos floats above Hell itself!\n" +
            "You've never heard of anyone escaping\n" +
            "from Hell, but you'll make the bastards\n" +
            "sorry they ever heard of you! Quickly,\n" +
            "you rappel down to  the surface of\n" +
            "Hell.\n" +
            "\n" +
            "Now, it's on to the readonly chapter of\n" +
            "DOOM! -- Inferno.";


    public readonly static String E3TEXT = "The loathsome spiderdemon that\n" +
            "masterminded the invasion of the moon\n" +
            "bases and caused so much death has had\n" +
            "its ass kicked for all time.\n" +
            "\n" +
            "A hidden doorway opens and you enter.\n" +
            "You've proven too tough for Hell to\n" +
            "contain, and now Hell at last plays\n" +
            "fair -- for you emerge from the door\n" +
            "to see the green fields of Earth!\n" +
            "Home at last.\n" +
            "\n" +
            "You wonder what's been happening on\n" +
            "Earth while you were battling evil\n" +
            "unleashed. It's good that no Hell-\n" +
            "spawn could have come through that\n" +
            "door with you ...";


    public readonly static String E4TEXT = "the spider mastermind must have sent forth\n" +
            "its legions of hellspawn before your\n" +
            "readonly confrontation with that terrible\n" +
            "beast from hell.  but you stepped forward\n" +
            "and brought forth eternal damnation and\n" +
            "suffering upon the horde as a true hero\n" +
            "would in the face of something so evil.\n" +
            "\n" +
            "besides, someone was gonna pay for what\n" +
            "happened to daisy, your pet rabbit.\n" +
            "\n" +
            "but now, you see spread before you more\n" +
            "potential pain and gibbitude as a nation\n" +
            "of demons run amok among our cities.\n" +
            "\n" +
            "next stop, hell on earth!";


// after level 6, put this:

    public readonly static String C1TEXT = "YOU HAVE ENTERED DEEPLY INTO THE INFESTED\n" +
            "STARPORT. BUT SOMETHING IS WRONG. THE\n" +
            "MONSTERS HAVE BROUGHT THEIR OWN REALITY\n" +
            "WITH THEM, AND THE STARPORT'S TECHNOLOGY\n" +
            "IS BEING SUBVERTED BY THEIR PRESENCE.\n" +
            "\n" +
            "AHEAD, YOU SEE AN OUTPOST OF HELL, A\n" +
            "FORTIFIED ZONE. IF YOU CAN GET PAST IT,\n" +
            "YOU CAN PENETRATE INTO THE HAUNTED HEART\n" +
            "OF THE STARBASE AND FIND THE CONTROLLING\n" +
            "SWITCH WHICH HOLDS EARTH'S POPULATION\n" +
            "HOSTAGE.";

// After level 11, put this:

    public readonly static String C2TEXT = "YOU HAVE WON! YOUR VICTORY HAS ENABLED\n" +
            "HUMANKIND TO EVACUATE EARTH AND ESCAPE\n" +
            "THE NIGHTMARE.  NOW YOU ARE THE ONLY\n" +
            "HUMAN LEFT ON THE FACE OF THE PLANET.\n" +
            "CANNIBAL MUTATIONS, CARNIVOROUS ALIENS,\n" +
            "AND EVIL SPIRITS ARE YOUR ONLY NEIGHBORS.\n" +
            "YOU SIT BACK AND WAIT FOR DEATH, CONTENT\n" +
            "THAT YOU HAVE SAVED YOUR SPECIES.\n" +
            "\n" +
            "BUT THEN, EARTH CONTROL BEAMS DOWN A\n" +
            "MESSAGE FROM SPACE: \"SENSORS HAVE LOCATED\n" +
            "THE SOURCE OF THE ALIEN INVASION. IF YOU\n" +
            "GO THERE, YOU MAY BE ABLE TO BLOCK THEIR\n" +
            "ENTRY.  THE ALIEN BASE IS IN THE HEART OF\n" +
            "YOUR OWN HOME CITY, NOT FAR FROM THE\n" +
            "STARPORT.\" SLOWLY AND PAINFULLY YOU GET\n" +
            "UP AND RETURN TO THE FRAY.";


// After level 20, put this:

    public readonly static String C3TEXT = "YOU ARE AT THE CORRUPT HEART OF THE CITY,\n" +
            "SURROUNDED BY THE CORPSES OF YOUR ENEMIES.\n" +
            "YOU SEE NO WAY TO DESTROY THE CREATURES'\n" +
            "ENTRYWAY ON THIS SIDE, SO YOU CLENCH YOUR\n" +
            "TEETH AND PLUNGE THROUGH IT.\n" +
            "\n" +
            "THERE MUST BE A WAY TO CLOSE IT ON THE\n" +
            "OTHER SIDE. WHAT DO YOU CARE IF YOU'VE\n" +
            "GOT TO GO THROUGH HELL TO GET TO IT?";


// After level 30, put this:

    public readonly static String C4TEXT = "THE HORRENDOUS VISAGE OF THE BIGGEST\n" +
            "DEMON YOU'VE EVER SEEN CRUMBLES BEFORE\n" +
            "YOU, AFTER YOU PUMP YOUR ROCKETS INTO\n" +
            "HIS EXPOSED BRAIN. THE MONSTER SHRIVELS\n" +
            "UP AND DIES, ITS THRASHING LIMBS\n" +
            "DEVASTATING UNTOLD MILES OF HELL'S\n" +
            "SURFACE.\n" +
            "\n" +
            "YOU'VE DONE IT. THE INVASION IS OVER.\n" +
            "EARTH IS SAVED. HELL IS A WRECK. YOU\n" +
            "WONDER WHERE BAD FOLKS WILL GO WHEN THEY\n" +
            "DIE, NOW. WIPING THE SWEAT FROM YOUR\n" +
            "FOREHEAD YOU BEGIN THE LONG TREK BACK\n" +
            "HOME. REBUILDING EARTH OUGHT TO BE A\n" +
            "LOT MORE FUN THAN RUINING IT WAS.\n";


// Before level 31, put this:

    public readonly static String C5TEXT = "CONGRATULATIONS, YOU'VE FOUND THE SECRET\n" +
            "LEVEL! LOOKS LIKE IT'S BEEN BUILT BY\n" +
            "HUMANS, RATHER THAN DEMONS. YOU WONDER\n" +
            "WHO THE INMATES OF THIS CORNER OF HELL\n" +
            "WILL BE.";


// Before level 32, put this:

    public readonly static String C6TEXT = "CONGRATULATIONS, YOU'VE FOUND THE\n" +
            "SUPER SECRET LEVEL!  YOU'D BETTER\n" +
            "BLAZE THROUGH THIS ONE!\n";


// after map 06 

    public readonly static String P1TEXT = "You gloat over the steaming carcass of the\n" +
            "Guardian.  With its death, you've wrested\n" +
            "the Accelerator from the stinking claws\n" +
            "of Hell.  You relax and glance around the\n" +
            "room.  Damn!  There was supposed to be at\n" +
            "least one working prototype, but you can't\n" +
            "see it. The demons must have taken it.\n" +
            "\n" +
            "You must find the prototype, or all your\n" +
            "struggles will have been wasted. Keep\n" +
            "moving, keep fighting, keep killing.\n" +
            "Oh yes, keep living, too.";


// after map 11

    public readonly static String P2TEXT = "Even the deadly Arch-Vile labyrinth could\n" +
            "not stop you, and you've gotten to the\n" +
            "prototype Accelerator which is soon\n" +
            "efficiently and permanently deactivated.\n" +
            "\n" +
            "You're good at that kind of thing.";


// after map 20

    public readonly static String P3TEXT = "You've bashed and battered your way into\n" +
            "the heart of the devil-hive.  Time for a\n" +
            "Search-and-Destroy mission, aimed at the\n" +
            "Gatekeeper, whose foul offspring is\n" +
            "cascading to Earth.  Yeah, he's bad. But\n" +
            "you know who's worse!\n" +
            "\n" +
            "Grinning evilly, you check your gear, and\n" +
            "get ready to give the bastard a little Hell\n" +
            "of your own making!";

// after map 30

    public readonly static String P4TEXT = "The Gatekeeper's evil face is splattered\n" +
            "all over the place.  As its tattered corpse\n" +
            "collapses, an inverted Gate forms and\n" +
            "sucks down the shards of the last\n" +
            "prototype Accelerator, not to mention the\n" +
            "few remaining demons.  You're done. Hell\n" +
            "has gone back to pounding bad dead folks \n" +
            "instead of good live ones.  Remember to\n" +
            "tell your grandkids to put a rocket\n" +
            "launcher in your coffin. If you go to Hell\n" +
            "when you die, you'll need it for some\n" +
            "readonly cleaning-up ...";

// before map 31

    public readonly static String P5TEXT = "You've found the second-hardest level we\n" +
            "got. Hope you have a saved game a level or\n" +
            "two previous.  If not, be prepared to die\n" +
            "aplenty. For master marines only.";

// before map 32

    public readonly static String P6TEXT = "Betcha wondered just what WAS the hardest\n" +
            "level we had ready for ya?  Now you know.\n" +
            "No one gets out alive.";


    public readonly static String T1TEXT = "You've fought your way out of the infested\n" +
            "experimental labs.   It seems that UAC has\n" +
            "once again gulped it down.  With their\n" +
            "high turnover, it must be hard for poor\n" +
            "old UAC to buy corporate health insurance\n" +
            "nowadays..\n" +
            "\n" +
            "Ahead lies the military complex, now\n" +
            "swarming with diseased horrors hot to get\n" +
            "their teeth into you. With luck, the\n" +
            "complex still has some warlike ordnance\n" +
            "laying around.";


    public readonly static String T2TEXT = "You hear the grinding of heavy machinery\n" +
            "ahead.  You sure hope they're not stamping\n" +
            "out new hellspawn, but you're ready to\n" +
            "ream out a whole herd if you have to.\n" +
            "They might be planning a blood feast, but\n" +
            "you feel about as mean as two thousand\n" +
            "maniacs packed into one mad killer.\n" +
            "\n" +
            "You don't plan to go down easy.";


    public readonly static String T3TEXT = "The vista opening ahead looks real damn\n" +
            "familiar. Smells familiar, too -- like\n" +
            "fried excrement. You didn't like this\n" +
            "place before, and you sure as hell ain't\n" +
            "planning to like it now. The more you\n" +
            "brood on it, the madder you get.\n" +
            "Hefting your gun, an evil grin trickles\n" +
            "onto your face. Time to take some names.";

    public readonly static String T4TEXT = "Suddenly, all is silent, from one horizon\n" +
            "to the other. The agonizing echo of Hell\n" +
            "fades away, the nightmare sky turns to\n" +
            "blue, the heaps of monster corpses start \n" +
            "to evaporate along with the evil stench \n" +
            "that filled the air. Jeeze, maybe you've\n" +
            "done it. Have you really won?\n" +
            "\n" +
            "Something rumbles in the distance.\n" +
            "A blue light begins to glow inside the\n" +
            "ruined skull of the demon-spitter.";


    public readonly static String T5TEXT = "What now? Looks totally different. Kind\n" +
            "of like King Tut's condo. Well,\n" +
            "whatever's here can't be any worse\n" +
            "than usual. Can it?  Or maybe it's best\n" +
            "to let sleeping gods lie..";


    public readonly static String T6TEXT = "Time for a vacation. You've burst the\n" +
            "bowels of hell and by golly you're ready\n" +
            "for a break. You mutter to yourself,\n" +
            "Maybe someone else can kick Hell's ass\n" +
            "next time around. Ahead lies a quiet town,\n" +
            "with peaceful flowing water, quaint\n" +
            "buildings, and presumably no Hellspawn.\n" +
            "\n" +
            "As you step off the transport, you hear\n" +
            "the stomp of a cyberdemon's iron shoe.";


    //
// Character cast strings F_FINALE.C
//
    public readonly static String CC_ZOMBIE = "ZOMBIEMAN";
    public readonly static String CC_SHOTGUN = "SHOTGUN GUY";
    public readonly static String CC_HEAVY = "HEAVY WEAPON DUDE";
    public readonly static String CC_IMP = "IMP";
    public readonly static String CC_DEMON = "DEMON";
    public readonly static String CC_LOST = "LOST SOUL";
    public readonly static String CC_CACO = "CACODEMON";
    public readonly static String CC_HELL = "HELL KNIGHT";
    public readonly static String CC_BARON = "BARON OF HELL";
    public readonly static String CC_ARACH = "ARACHNOTRON";
    public readonly static String CC_PAIN = "PAIN ELEMENTAL";
    public readonly static String CC_REVEN = "REVENANT";
    public readonly static String CC_MANCU = "MANCUBUS";
    public readonly static String CC_ARCH = "ARCH-VILE";
    public readonly static String CC_SPIDER = "THE SPIDER MASTERMIND";
    public readonly static String CC_CYBER = "THE CYBERDEMON";
    public readonly static String CC_NAZI = "WAFFEN SS. SIEG HEIL!";
    public readonly static String CC_KEEN = "COMMANDER KEEN";
    public readonly static String CC_BARREL = "EXPLODING BARREL";
    public readonly static String CC_HERO = "OUR HERO";

}