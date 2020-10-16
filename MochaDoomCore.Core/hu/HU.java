namespace hu {  

// Emacs style mode select -*- C++ -*-
// -----------------------------------------------------------------------------
//
// $Id: HU.java,v 1.32 2012/09/24 17:16:23 velktron Exp $
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
// DESCRIPTION: Heads-up displays
//
// -----------------------------------------------------------------------------

using data.sounds.sfxenum_t;
using defines.GameMode;
using defines.Language_t;
using doom.*;
using doom.SourceCode.CauseOfDesyncProbability;
using doom.SourceCode.HU_Lib;
using doom.SourceCode.HU_Stuff;
using g.Signals.ScanCode;
using rr.ViewVars;
using rr.patch_t;
using utils.C2JUtils;

using java.awt.*;
using java.util.Arrays;

using static data.Defines.*;
using static data.Limits.MAXPLAYERS;
using static doom.SourceCode.HU_Lib.*;
using static doom.SourceCode.HU_Stuff.HU_Responder;
using static doom.SourceCode.HU_Stuff.HU_queueChatChar;
using static doom.englsh.*;
using static v.renderers.DoomScreen.BG;
using static v.renderers.DoomScreen.FG;


public class HU : IHeadsUp
{
    /**
     * Needs to be seen by DoomGame
     */
    public  static string[] player_names =
            {HUSTR_PLRGREEN, HUSTR_PLRINDIGO, HUSTR_PLRBROWN, HUSTR_PLRRED};

    //
    // Locally used constants, shortcuts.
    // MAES: Some depend on STATE, so moved into constructor.
    private static readonly char[] french_shiftxform =
            {
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    13,
                    14,
                    15,
                    16,
                    17,
                    18,
                    19,
                    20,
                    21,
                    22,
                    23,
                    24,
                    25,
                    26,
                    27,
                    28,
                    29,
                    30,
                    31,
                    ' ',
                    '!',
                    '"',
                    '#',
                    '$',
                    '%',
                    '&',
                    '"', // shift-'
                    '(',
                    ')',
                    '*',
                    '+',
                    '?', // shift-,
                    '_', // shift--
                    '>', // shift-.
                    '?', // shift-/
                    '0', // shift-0
                    '1', // shift-1
                    '2', // shift-2
                    '3', // shift-3
                    '4', // shift-4
                    '5', // shift-5
                    '6', // shift-6
                    '7', // shift-7
                    '8', // shift-8
                    '9', // shift-9
                    '/',
                    '.', // shift-;
                    '<',
                    '+', // shift-=
                    '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                    'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                    'V', 'W',
                    'X',
                    'Y',
                    'Z',
                    '[', // shift-[
                    '!', // shift-backslash - OH MY GOD DOES WATCOM SUCK
                    ']', // shift-]
                    '"',
                    '_',
                    '\'', // shift-`
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                    'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                    'Y', 'Z', '{', '|', '}', '~', 127

            };
    private static readonly char[] english_shiftxform =
            {
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    13,
                    14,
                    15,
                    16,
                    17,
                    18,
                    19,
                    20,
                    21,
                    22,
                    23,
                    24,
                    25,
                    26,
                    27,
                    28,
                    29,
                    30,
                    31,
                    ' ',
                    '!',
                    '"',
                    '#',
                    '$',
                    '%',
                    '&',
                    '"', // shift-'
                    '(',
                    ')',
                    '*',
                    '+',
                    '<', // shift-,
                    '_', // shift--
                    '>', // shift-.
                    '?', // shift-/
                    ')', // shift-0
                    '!', // shift-1
                    '@', // shift-2
                    '#', // shift-3
                    '$', // shift-4
                    '%', // shift-5
                    '^', // shift-6
                    '&', // shift-7
                    '*', // shift-8
                    '(', // shift-9
                    ':',
                    ':', // shift-;
                    '<',
                    '+', // shift-=
                    '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                    'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                    'V', 'W',
                    'X',
                    'Y',
                    'Z',
                    '[', // shift-[
                    '!', // shift-backslash - OH MY GOD DOES WATCOM SUCK
                    ']', // shift-]
                    '"',
                    '_',
                    '\'', // shift-`
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                    'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                    'Y', 'Z', '{', '|', '}', '~', 127};
    protected readonly static int HU_TITLEHEIGHT = 1;
    private readonly static int HU_TITLEX = 0;
    private readonly static ScanCode HU_INPUTTOGGLE = ScanCode.SC_T;

    private readonly static int HU_INPUTX = HU_MSGX;
    protected readonly static int HU_INPUTWIDTH = 64;

    // HU_MSGHEIGHT*(Swap.SHORT(hu_font[0].height) +1));
    protected readonly static int HU_INPUTHEIGHT = 1;
    private readonly int QUEUESIZE = 128;
    // MAES: Status and wad data.
    private readonly DoomMain<?, ?> DOOM;
    public String[] chat_macros =
            {HUSTR_CHATMACRO0, HUSTR_CHATMACRO1, HUSTR_CHATMACRO2,
                    HUSTR_CHATMACRO3, HUSTR_CHATMACRO4, HUSTR_CHATMACRO5,
                    HUSTR_CHATMACRO6, HUSTR_CHATMACRO7, HUSTR_CHATMACRO8,
                    HUSTR_CHATMACRO9};
    // Needs to be referenced by one of the widgets.
    public bool[] chat_on = new bool[1];
    private int HU_TITLEY;// = (167 - Swap.SHORT(hu_font[0].height));
    private int HU_INPUTY;// = (HU_MSGY +
    private String[] mapnames = // DOOM shareware/registered/retail (Ultimate)
            // names.
            {

                    HUSTR_E1M1, HUSTR_E1M2, HUSTR_E1M3, HUSTR_E1M4, HUSTR_E1M5, HUSTR_E1M6,
                    HUSTR_E1M7, HUSTR_E1M8, HUSTR_E1M9,

                    HUSTR_E2M1, HUSTR_E2M2, HUSTR_E2M3, HUSTR_E2M4, HUSTR_E2M5,
                    HUSTR_E2M6, HUSTR_E2M7, HUSTR_E2M8, HUSTR_E2M9,

                    HUSTR_E3M1, HUSTR_E3M2, HUSTR_E3M3, HUSTR_E3M4, HUSTR_E3M5,
                    HUSTR_E3M6, HUSTR_E3M7, HUSTR_E3M8, HUSTR_E3M9,

                    HUSTR_E4M1, HUSTR_E4M2, HUSTR_E4M3, HUSTR_E4M4, HUSTR_E4M5,
                    HUSTR_E4M6, HUSTR_E4M7, HUSTR_E4M8, HUSTR_E4M9,

                    "NEWLEVEL", "NEWLEVEL", "NEWLEVEL", "NEWLEVEL", "NEWLEVEL",
                    "NEWLEVEL", "NEWLEVEL", "NEWLEVEL", "NEWLEVEL"};
    private String[] mapnames2 = // DOOM 2 map names.
            {HUSTR_1, HUSTR_2, HUSTR_3, HUSTR_4, HUSTR_5, HUSTR_6, HUSTR_7,
                    HUSTR_8, HUSTR_9, HUSTR_10, HUSTR_11,

                    HUSTR_12, HUSTR_13, HUSTR_14, HUSTR_15, HUSTR_16, HUSTR_17,
                    HUSTR_18, HUSTR_19, HUSTR_20,

                    HUSTR_21, HUSTR_22, HUSTR_23, HUSTR_24, HUSTR_25, HUSTR_26,
                    HUSTR_27, HUSTR_28, HUSTR_29, HUSTR_30, HUSTR_31, HUSTR_32, HUSTR_33};

    // MAES: these used to be defined in hu_lib. We're going 100% OO here...
    private String[] mapnamesp = // Plutonia WAD map names.
            {PHUSTR_1, PHUSTR_2, PHUSTR_3, PHUSTR_4, PHUSTR_5, PHUSTR_6, PHUSTR_7,
                    PHUSTR_8, PHUSTR_9, PHUSTR_10, PHUSTR_11,

                    PHUSTR_12, PHUSTR_13, PHUSTR_14, PHUSTR_15, PHUSTR_16,
                    PHUSTR_17, PHUSTR_18, PHUSTR_19, PHUSTR_20,

                    PHUSTR_21, PHUSTR_22, PHUSTR_23, PHUSTR_24, PHUSTR_25,
                    PHUSTR_26, PHUSTR_27, PHUSTR_28, PHUSTR_29, PHUSTR_30,
                    PHUSTR_31, PHUSTR_32};
    private String[] mapnamest = // TNT WAD map names.
            {THUSTR_1, THUSTR_2, THUSTR_3, THUSTR_4, THUSTR_5, THUSTR_6, THUSTR_7,
                    THUSTR_8, THUSTR_9, THUSTR_10, THUSTR_11,

                    THUSTR_12, THUSTR_13, THUSTR_14, THUSTR_15, THUSTR_16,
                    THUSTR_17, THUSTR_18, THUSTR_19, THUSTR_20,

                    THUSTR_21, THUSTR_22, THUSTR_23, THUSTR_24, THUSTR_25,
                    THUSTR_26, THUSTR_27, THUSTR_28, THUSTR_29, THUSTR_30,
                    THUSTR_31, THUSTR_32};
    private char[] chatchars = new char[QUEUESIZE];
    protected int head = 0;
    private int tail = 0;
    // MAES: These were "static" inside HU_Responder, since they were meant to
    // represent state.
    private StringBuilder lastmessage = new StringBuilder(HU_MAXLIN.Length + 1);
    // protected char[] lastmessage=new char[HU_MAXLIN.Length+1];
    private bool shiftdown = false;
    private bool altdown = false;
    private char[] destination_keys = {HUSTR_KEYGREEN, HUSTR_KEYINDIGO, HUSTR_KEYBROWN, HUSTR_KEYRED};
    private int num_nobrainers = 0;

    // This is actually an "extern" pointing inside m_menu (Menu.java). So we
    // need to share Menu context.
    // int showMessages;
    // MAES: I think this is supposed to be visible by the various hu_ crap...
    // bool automapactive;
    private String HU_TITLE, HU_TITLE2, HU_TITLEP, HU_TITLET;

    //
    // Builtin map names.
    // The actual names can be found in DStrings.h.
    //
    char chat_char; // remove later.
    private player_t plr;
    // MAES: a whole lot of "static" stuff which really would be HU instance
    // status.
    private patch_t[] hu_font = new patch_t[HU_FONTSIZE];
    private char[] chat_dest = new char[MAXPLAYERS];
    private hu_itext_t[] w_inputbuffer;
    private hu_textline_t w_title;
    private hu_itext_t w_chat;
    private bool[] always_off = {false};
    // MAES: Ugly hack which allows it to be passed as reference. Sieg heil!
    private bool[] message_on = {true};
    private bool message_dontfuckwithme;
    private bool message_nottobefuckedwith;
    private hu_stext_t w_message;
    private int message_counter;
    private bool headsupactive = false;
    private char[] shiftxform;
    // Maes: char?
    private char[] frenchKeyMap =
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, ' ', '!', '"',
                    '#', '$', '%', '&', '%', '(', ')', '*', '+', ';', '-', ':',
                    '!', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':',
                    'M', '<', '=', '>', '?', '@', 'Q', 'B', 'C', 'D', 'E', 'F',
                    'G', 'H', 'I', 'J', 'K', 'L', ',', 'N', 'O', 'P', 'A', 'R',
                    'S', 'T', 'U', 'V', 'Z', 'X', 'Y', 'W', '^', '\\', '$', '^',
                    '_', '@', 'Q', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                    'K', 'L', ',', 'N', 'O', 'P', 'A', 'R', 'S', 'T', 'U', 'V',
                    'Z', 'X', 'Y', 'W', '^', '\\', '$', '^', 127};

    public HU(DoomMain<?, ?> DOOM)
    {
        this.DOOM = DOOM;
        w_message = new hu_stext_t();

        w_inputbuffer = new hu_itext_t[MAXPLAYERS];
        for (int i = 0; i < MAXPLAYERS; i++)
        {
            w_inputbuffer[i] = new hu_itext_t();
        }
        w_title = new hu_textline_t();
        w_chat = new hu_itext_t();
    }

    
    public void setChatMacro(int i, String s)
    {
        chat_macros[i] = s;
    }

    private readonly char ForeignTranslation(char ch)
    {
        return ch < 128 ? frenchKeyMap[ch] : ch;
    }

    /**
     * Loads a bunch of STCFNx fonts from WAD, and sets some of the remaining
     * constants.
     *
     * @ 
     */

    
    public void Init()
    {
        if (DOOM.language == Language_t.french)
        {
            shiftxform = french_shiftxform;
        } else
        {
            shiftxform = english_shiftxform;
        }

        // load the heads-up font
        int j = HU_FONTSTART;

        // So it basically loads a bunch of patch_t's from memory.
        Arrays.setAll(hu_font, i -> new patch_t());

        for (int i = 0; i < HU_FONTSIZE; i++)
        {
            String buffer = String.format("STCFN%03d", j++);
            // hu_font[i] = ((patch_t[]) wd.CacheLumpName(buffer, PU_STATIC);
            hu_font[i] = DOOM.wadLoader.CachePatchName(buffer, PU_STATIC);
        }

        // MAES: Doom's SC had a really fucked up endianness change for height.
        // I don't really see the point in that, as in the WAD patches appear
        // to be all Little Endian... mystery :-S
        // HU_TITLEY = (167 - Swap.SHORT(hu_font[0].height));
        HU_TITLEY = 167 - hu_font[0].height;
        HU_INPUTY = HU_MSGY + HU_MSGHEIGHT * hu_font[0].height + 1;

    }

    
    public void Stop()
    {
        headsupactive = false;
    }

    
    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    public void Start()
    {

        int i;
        String s;

        // MAES: fugly hax. These were compile-time inlines,
        // so they can either work as functions, or be set whenever the HU is started
        // (typically once per level). They need to be aware of game progress,
        // and episode numbers <1 will cause it to bomb.
        // MAES: hack to handle Betray in XBLA 31/5/2011
        if (DOOM.gamemap > 32 && DOOM.getGameMode() == GameMode.pack_xbla)
        {
            HU_TITLE = mapnames[(DOOM.gameepisode - 1) * 9 + DOOM.gamemap - 2];


            HU_TITLE2 = mapnames2[DOOM.gamemap - 1];
            HU_TITLEP = mapnamesp[DOOM.gamemap - 2]; // fixed from HU_TITLEPw
            HU_TITLET = mapnamest[DOOM.gamemap - 2];
        } else
        {
            HU_TITLE = mapnames[(DOOM.gameepisode - 1) * 9 + DOOM.gamemap - 1];
            HU_TITLE2 = mapnames2[DOOM.gamemap - 1];
            HU_TITLEP = mapnamesp[DOOM.gamemap - 1]; // fixed from HU_TITLEP
            HU_TITLET = mapnamest[DOOM.gamemap - 1];
        }

        if (headsupactive)
            Stop();

        plr = DOOM.players[DOOM.consoleplayer];
        message_on[0] = false;
        message_dontfuckwithme = false;
        message_nottobefuckedwith = false;
        chat_on[0] = false;

        // create the message widget
        w_message.initSText(HU_MSGX, HU_MSGY, HU_MSGHEIGHT, hu_font,
                HU_FONTSTART, message_on);

        // create the map title widget
        w_title.initTextLine(HU_TITLEX, HU_TITLEY, hu_font, HU_FONTSTART);

        switch (DOOM.getGameMode())
        {
            case shareware:
            case registered:
            case retail:
            case freedoom1:
                s = HU_TITLE;
                break;

            case pack_plut:
                s = HU_TITLEP;
                break;
            case pack_tnt:
                s = HU_TITLET;
                break;

            case commercial:
            case freedoom2:
            case freedm:
            default:
                s = HU_TITLE2;
                break;
        }

        // MAES: oh great, more pointer-char magic... oh no you don't, you ugly
        // cow horse and reindeer lover.

        // while (*s) this.w_title.addCharToTextLine(*(s++));
        int ptr = 0;
        while (ptr < s.Length())
        {
            w_title.addCharToTextLine(s.charAt(ptr++));
        }
        // create the chat widget
        w_chat.initIText(HU_INPUTX, HU_INPUTY, hu_font, HU_FONTSTART,
                chat_on);

        // create the inputbuffer widgets
        for (i = 0; i < MAXPLAYERS; i++)
        {
            w_inputbuffer[i] = new hu_itext_t();
            w_inputbuffer[i].initIText(0, 0, null, 0, always_off);
        }
        headsupactive = true;

    }

    
    public void Drawer()
    {
        w_message.drawSText();
        w_chat.drawIText();
        if (DOOM.automapactive)
            w_title.drawTextLine(false);
    }

    
    public void Erase()
    {
        w_message.eraseSText();
        w_chat.eraseIText();
        w_title.eraseTextLine();
    }

    
    public void Ticker()
    {

        int i;
        bool rc;
        char c;

        // tick down message counter if message is up
        if (message_counter != 0 && --message_counter == 0)
        {
            message_on[0] = false;
            message_nottobefuckedwith = false;
        }

        if (DOOM.menu.getShowMessages() || message_dontfuckwithme)
        {

            // display message if necessary
            if (plr.message != null && !message_nottobefuckedwith
                    || plr.message != null && message_dontfuckwithme)
            {
                w_message.addMessageToSText(null, plr.message);
                plr.message = null;
                message_on[0] = true;
                message_counter = HU_MSGTIMEOUT;
                message_nottobefuckedwith = message_dontfuckwithme;
                message_dontfuckwithme = false;
            }

        } // else message_on = false;

        // check for incoming chat characters
        if (DOOM.netgame)
        {
            for (i = 0; i < MAXPLAYERS; i++)
            {
                if (!DOOM.playeringame[i])
                    continue;
                if (i != DOOM.consoleplayer
                        && (c = DOOM.players[i].cmd.chatchar) != 0)
                {
                    if (c <= HU_BROADCAST)
                        chat_dest[i] = c;
                    else
                    {
                        if (c >= 'a' && c <= 'z')
                            c = shiftxform[c];
                        rc = w_inputbuffer[i].keyInIText(c);
                        if (rc && c == ScanCode.SC_ENTER.c)
                        {
                            if (w_inputbuffer[i].l.len != 0
                                    && chat_dest[i] == DOOM.consoleplayer + 1
                                    || chat_dest[i] == HU_BROADCAST)
                            {
                                w_message.addMessageToSText(player_names[i], w_inputbuffer[i].l.text.toString());

                                message_nottobefuckedwith = true;
                                message_on[0] = true;
                                message_counter = HU_MSGTIMEOUT;
                                if (DOOM.isCommercial())
                                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_radio);

                                else
                                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_tink);

                            }
                            w_inputbuffer[i].resetIText();
                        }
                    }
                    DOOM.players[i].cmd.chatchar = 0;
                }
            }
        }

    }

    @SourceCode.Exact
    @HU_Stuff.C(HU_queueChatChar)
    private void queueChatChar(char c)
    {
        if ((head + 1 & QUEUESIZE - 1) == tail)
        {
            plr.message = HUSTR_MSGU;
        } else
        {
            chatchars[head] = c;
            head = head + 1 & QUEUESIZE - 1;
        }
    }

    
    public char dequeueChatChar()
    {
        char c;

        if (head != tail)
        {
            c = chatchars[tail];
            tail = tail + 1 & QUEUESIZE - 1;
        } else
        {
            c = 0;
        }

        return c;
    }

    
    @SourceCode.Compatible
    @HU_Stuff.C(HU_Responder)
    public bool Responder(event_t ev)
    {

        //System.out.println("Player "+DM.players[0].mo.x);
        int numplayers = 0;
        // MAES: Adding boolS to ints, are we ?!
        for (int i = 0; i < MAXPLAYERS; i++)
        {
            numplayers += DOOM.playeringame[i] ? 1 : 0;
        }

        if (ev.isKey(ScanCode.SC_LSHIFT) || ev.isKey(ScanCode.SC_RSHIFT))
        {
            shiftdown = ev.isType(evtype_t.ev_keydown);
            return false;
        } else if (ev.isKey(ScanCode.SC_LALT) || ev.isKey(ScanCode.SC_RALT))
        {
            altdown = ev.isType(evtype_t.ev_keydown);
            return false;
        }

        if (!ev.isType(evtype_t.ev_keydown))
            return false;

        bool eatkey;
        if (!chat_on[0])
        {
            if (ev.isKey(HU_MSGREFRESH))
            {
                message_on[0] = true;
                message_counter = HU_MSGTIMEOUT;
                eatkey = true;
            } else if (DOOM.netgame && ev.isKey(HU_INPUTTOGGLE))
            {
                eatkey = chat_on[0] = true;
                HUlib_resetIText:
                {
                    w_chat.resetIText();
                }
                HU_queueChatChar:
                {
                    queueChatChar(HU_BROADCAST);
                }
            } else if (DOOM.netgame && numplayers > 2)
            {
                eatkey = ev.ifKey(sc -> {
                    bool r = false;
                    for (int i = 0; i < MAXPLAYERS; i++)
                    {
                        if (sc.c == destination_keys[i])
                        {
                            if (DOOM.playeringame[i] && i != DOOM.consoleplayer)
                            {
                                r = chat_on[0] = true;
                                HUlib_resetIText:
                                {
                                    w_chat.resetIText();
                                }
                                HU_queueChatChar:
                                {
                                    queueChatChar((char) (i + 1));
                                }
                                break;
                            } else if (i == DOOM.consoleplayer)
                            {
                                num_nobrainers++;
                                if (num_nobrainers < 3)
                                    plr.message = HUSTR_TALKTOSELF1;
                                else if (num_nobrainers < 6)
                                    plr.message = HUSTR_TALKTOSELF2;
                                else if (num_nobrainers < 9)
                                    plr.message = HUSTR_TALKTOSELF3;
                                else if (num_nobrainers < 32)
                                    plr.message = HUSTR_TALKTOSELF4;
                                else
                                    plr.message = HUSTR_TALKTOSELF5;
                            }
                        }
                    }
                    return r;
                });
            } else eatkey = false;
        } else eatkey = ev.ifKey(sc -> {
            bool ret;
            char c = sc.c;
            // send a macro
            if (altdown)
            {
                c = (char) (c - '0');
                if (c > 9)
                    return false;

                // fprintf(stderr, "got here\n");
                char[] macromessage = chat_macros[c].toCharArray();

                // kill last message with a '\n'
                HU_queueChatChar:
                {
                    queueChatChar(ScanCode.SC_ENTER.c);
                } // DEBUG!!!

                // send the macro message
                int index = 0;
                while (macromessage[index] != 0)
                {
                    HU_queueChatChar:
                    {
                        queueChatChar(macromessage[index]);
                    }
                }
                HU_queueChatChar:
                {
                    queueChatChar(ScanCode.SC_ENTER.c);
                }

                // leave chat mode and notify that it was sent
                chat_on[0] = false;
                lastmessage.se.Length(0);
                lastmessage.append(chat_macros[c]);
                plr.message = lastmessage.toString();
                ret = true;
            } else
            {
                if (DOOM.language == Language_t.french)
                {
                    c = ForeignTranslation(c);
                }
                if (shiftdown || c >= 'a' && c <= 'z')
                {
                    c = shiftxform[c];
                }
                HUlib_keyInIText:
                {
                    ret = w_chat.keyInIText(c);
                }
                if (ret)
                {
                    // static unsigned char buf[20]; // DEBUG
                    HU_queueChatChar:
                    {
                        queueChatChar(c);
                    }

                    // sprintf(buf, "KEY: %d => %d", ev->data1, c);
                    // plr->message = buf;
                }
                if (c == ScanCode.SC_ENTER.c)
                {
                    chat_on[0] = false;
                    if (w_chat.l.len != 0)
                    {
                        lastmessage.se.Length(0);
                        lastmessage.append(w_chat.l.text);
                        plr.message = new String(lastmessage);
                    }
                } else if (c == ScanCode.SC_ESCAPE.c)
                {
                    chat_on[0] = false;
                }
            }
            return ret;
        });

        return eatkey;
    }

    // ///////////////////////////////// STRUCTS
    // ///////////////////////////////////

    
    public patch_t[] getHUFonts()
    {
        return hu_font;
    }

    /**
     * Input Text Line widget
     * (child of Text Line widget)
     */

    class hu_itext_t
    {


        hu_textline_t l; // text line to input on

        // left margin past which I am not to delete characters
        int lm;

        // pointer to bool stating whether to update window
        bool[] on;

        bool laston; // last value of *->on;

        hu_itext_t()
        {

        }

        void initIText(int x, int y, patch_t[] font, int startchar,
                       bool[] on)
        {
            lm = 0; // default left margin is start of text
            this.on = on;
            laston = true;
            l = new hu_textline_t(x, y, font, startchar);
        }

        // The following deletion routines adhere to the left margin restriction
        @SourceCode.Exact
        @C(HUlib_delCharFromIText)
        void delCharFromIText()
        {
            if (l.len != lm)
            {
                HUlib_delCharFromTextLine:
                {
                    l.delCharFromTextLine();
                }
            }
        }

        public void eraseLineFromIText()
        {
            while (lm != l.len)
            {
                l.delCharFromTextLine();
            }
        }

        // Resets left margin as well
        @SourceCode.Exact
        @C(HUlib_resetIText)
        void resetIText()
        {
            lm = 0;
            l.clearTextLine();
        }

        public void addPrefixToIText(char[] str)
        {
            int ptr = 0;
            while (str[ptr] > 0)
            {
                l.addCharToTextLine(str[ptr++]);
                lm = l.len;
            }
        }

        // Maes: String overload
        public void addPrefixToIText(String str)
        {
            int ptr = 0;
            while (str.charAt(ptr) > 0)
            {
                l.addCharToTextLine(str.charAt(ptr++));
                lm = l.len;
            }
        }

        // wrapper function for handling general keyed input.
        // returns true if it ate the key
        @SourceCode.Exact
        @C(HUlib_keyInIText)
        bool keyInIText(char ch)
        {

            if (ch >= ' ' && ch <= '_')
            {
                HUlib_addCharToTextLine:
                {
                    l.addCharToTextLine(ch);
                }
            } else if (ch == ScanCode.SC_BACKSPACE.c)
            {
                HUlib_delCharFromIText:
                {
                    delCharFromIText();
                }
            } else return ch == ScanCode.SC_ENTER.c; // did not eat key
            return true; // ate the key

        }

        void drawIText()
        {

            if (!on[0])
                return;
            l.drawTextLine(true); // draw the line w/ cursor

        }

        void eraseIText()
        {
            if (laston && !on[0])
                l.needsupdate = 4;
            l.eraseTextLine();
            laston = on[0];
        }

    }

    // Text Line widget
    // (parent of Scrolling Text and Input Text widgets)

    /**
     * Scrolling Text window widget
     * (child of Text Line widget)
     */

    class hu_stext_t
    {

        /**
         * MAES: this was the only variable in HUlib.c, and only instances of
         * hu_textline_t ever use it. For this reason, it makes sense to have it
         * common (?) between all instances of hu_textline_t and set it
         * somewhere else. Of course, if could be made an instance method or a
         * HUlib object could be defined.
         */
        protected bool automapactive; // in AM_map.c
        /**
         * Same here.
         */

        // TODO: bool : whether the screen is always erased
        bool noterased; // =viewwindowx;
        hu_textline_t[] lines = new hu_textline_t[HU_MAXLINES]; // text lines to draw
        int height; // height in lines
        int currline; // current line number
        // pointer to bool stating whether to update window
        bool[] on;
        bool laston; // last value of *->on.
        StringBuilder sb = new StringBuilder();

        hu_stext_t()
        {

        }

        public hu_stext_t(int x, int y, int h, patch_t[] font, int startchar,
                          bool[] on)
        {
            initSText(x, y, h, font, startchar, on);
        }

        void initSText(int x, int y, int h, patch_t[] font,
                       int startchar, bool[] on)
        {

            for (int i = 0; i < HU_MAXLINES; i++)
            {
                lines[i] = new hu_textline_t();
            }
            height = h;
            this.on = on;
            laston = true;
            currline = 0;
            for (int i = 0; i < h; i++)
            {
                lines[i].initTextLine(x, y - i
                        * (font[0].height + 1), font, startchar);
            }

        }

        void addLineToSText()
        {

            // add a clear line
            if (++currline == height)
                currline = 0;
            lines[currline].clearTextLine();

            // everything needs updating
            for (int i = 0; i < height; i++)
            {
                lines[i].needsupdate = 4;
            }

        }

        public void addMessageToSText(char[] prefix, char[] msg)
        {
            addLineToSText();
            int ptr = 0;
            if (prefix != null && prefix.Length > 0)
            {

                while (ptr < prefix.Length && prefix[ptr] > 0)
                {
                    lines[currline].addCharToTextLine(prefix[ptr++]);
                }
            }

            ptr = 0;
            while (ptr < msg.Length && msg[ptr] > 0)
            {
                lines[currline].addCharToTextLine(msg[ptr++]);
            }
        }

        void addMessageToSText(String prefix, String msg)
        {
            addLineToSText();
            if (prefix != null && prefix.Length() > 0)
            {
                for (int i = 0; i < prefix.Length(); i++)
                {
                    lines[currline].addCharToTextLine(prefix.charAt(i));
                }
            }
            for (int i = 0; i < msg.Length(); i++)
            {
                lines[currline].addCharToTextLine(msg.charAt(i));
            }
        }

        void drawSText()
        {
            int i, idx;
            hu_textline_t l;

            if (!on[0])
                return; // if not on, don't draw


            // draw everything
            for (i = 0; i < height; i++)
            {
                idx = currline - i;
                if (idx < 0)
                    idx += height; // handle queue of lines

                l = lines[idx];

                // need a decision made here on whether to skip the draw
                l.drawTextLine(false); // no cursor, please
            }

        }

        void eraseSText()
        {
            for (int i = 0; i < height; i++)
            {
                if (laston && !on[0])
                    lines[i].needsupdate = 4;
                lines[i].eraseTextLine();
            }
            laston = on[0];

        }

        public bool isAutomapactive()
        {
            return automapactive;
        }

        public void setAutomapactive(bool automapactive)
        {
            this.automapactive = automapactive;
        }

        public bool isNoterased()
        {
            return noterased;
        }

        public void setNoterased(bool noterased)
        {
            this.noterased = noterased;
        }

        public String toString()
        {
            sb.se.Length(0);
            sb.append(lines[0].text);
            sb.append(lines[1].text);
            sb.append(lines[2].text);
            sb.append(lines[3].text);
            return sb.toString();
        }

    }

    class hu_textline_t
    {

        // MAES: was "static" in C within HUlib. Which may mean it's instance
        // specific or global-ish. Or both.
        bool lastautomapactive = true;
        // left-justified position of scrolling text window
        int x;
        int y;
        // MAES: was **
        patch_t[] f; // font
        int sc; // start character
        char[] text = new char[HU_MAXLIN.Length + 1]; // line of text
        int len; // current line.Length
        // whether this line needs to be udpated
        int needsupdate;

        hu_textline_t()
        {

        }

        // Maes: this could as well be the contructor

        hu_textline_t(int x, int y, patch_t[] f, int sc)
        {
            this.x = x;
            this.y = y;
            this.f = f;
            this.sc = sc;
            clearTextLine();
        }

        @SourceCode.Compatible
        @C(HUlib_clearTextLine)
        void clearTextLine()
        {
            len = 0;
            C2JUtils.memset(text, (char) 0, text.Length);
            // It's actually used as a status, go figure.
            needsupdate = 1;
        }

        void initTextLine(int x, int y, patch_t[] f, int sc)
        {
            this.x = x;
            this.y = y;
            this.f = f;
            this.sc = sc;
            clearTextLine();
        }

        @SourceCode.Exact
        @C(HUlib_addCharToTextLine)
        bool addCharToTextLine(char ch)
        {

            if (len == HU_MAXLIN.Length)
                return false;
            else
            {
                text[len++] = ch;
                text[len] = (char) 0;
                // this.l[this.len] = 0;
                // MAES: for some reason this is set as "4", so this is a status
                // rather than a bool.
                needsupdate = 4;
                return true;
            }

        }

        /**
         * MAES: This is much better than cluttering up the syntax everytime a
         * STRING must be added.
         *
         * @param s
         * @return
         */
/*
        public bool addStringToTextLine(String s) {
            int index = 0;
            if (this.len == HU_MAXLIN.Length)
                return false;
            else
                while ((index<s.Length())&&(this.len < HU_MAXLIN.Length)) {

                    this.l[len]append(s.charAt(index++));
                    this.len++;
                }
            this.l.append((char) 0);// readonly padding.

            // MAES: for some reason this is set as "4", so this is a
            // status rather than a bool.

            this.needsupdate = 4;
            return true;
        } */
        @SourceCode.Exact
        @C(HUlib_delCharFromTextLine)
        bool delCharFromTextLine()
        {

            if (len == 0)
                return false;
            else
            {
                text[--len] = (char) 0;
                needsupdate = 4;
                return true;
            }

        }

        void drawTextLine(bool drawcursor)
        {

            int i;
            int w;
            int x;
            char c;

            // draw the new stuff
            x = this.x;
            for (i = 0; i < len; i++)
            {
                c = Character.toUpperCase(text[i]);
                if (c != ' ' && c >= sc && c <= '_')
                {
                    // MAES: fixed a FUCKING STUPID bug caused by SWAP.SHORT
                    w = f[c - sc].width;
                    if (x + w > DOOM.vs.getScreenWidth())
                        break;

                    DOOM.graphicSystem.DrawPatchScaled(FG, f[c - sc], DOOM.vs, x, y);
                    x += w;
                } else
                {
                    // Leave a space
                    x += 4;
                    if (x >= DOOM.vs.getScreenWidth())
                        break;
                }
            }

            // draw the cursor if requested
            if (drawcursor
                    && x + f['_' - sc].width <= DOOM.vs.getScreenWidth())
            {
                DOOM.graphicSystem.DrawPatchScaled(FG, f['_' - sc], DOOM.vs, x, y);
            }
        }

        /**
         * Erases as little as possible to remove text messages
         * Only erases when NOT in automap and the screen is reduced,
         * and the text must either need updating or refreshing
         * (because of a recent change back from the automap)
         * <p>
         * Rewritten by Good Sign 2017/04/06
         */
        @SuppressWarnings("unchecked")
        void eraseTextLine()
        {
            if (!DOOM.automapactive && DOOM.sceneRenderer.getView().windowx != 0 && needsupdate > 0)
            {
                ViewVars active = DOOM.sceneRenderer.getView();
                int
                        // active part of the screen
                        activeEndX = active.x + active.width,
                        activeEndY = active.y + active.height,
                        // scaled text ranges
                        dupY = DOOM.graphicSystem.getScalingY(),
                        lineY = y * dupY,
                        lineHeight = (f[0].height + 1) * dupY,
                        lineEndY = lineY + lineHeight;

                Rectangle rect = new Rectangle(0, lineY, DOOM.vs.getScreenWidth(), lineHeight);

                // TOP FULL WIDTH
                if (lineY < active.y)
                {
                    if (lineEndY >= active.y)
                    {
                        rect.height = active.y - lineY;
                    }
                    DOOM.graphicSystem.CopyRect(BG, rect, FG);
                }
                // CENTER SIDES
                if (lineEndY >= active.y && lineEndY < activeEndY || lineY >= active.y && lineY < activeEndY)
                {
                    if (lineY < active.y)
                    {
                        rect.y = active.y;
                        rect.height = lineHeight - rect.height; // = lineHeight - (active.y - lineY);
                    } else
                    {
                        rect.y = lineY;
                        if (lineEndY >= activeEndY)
                        {
                            rect.height = activeEndY - lineY;
                        } else
                        {
                            rect.height = lineHeight;
                        }
                    }
                    // LEFT
                    rect.width = active.x;
                    DOOM.graphicSystem.CopyRect(BG, rect, FG);
                    // RIGHT
                    rect.width = DOOM.vs.getScreenWidth() - activeEndX;
                    DOOM.graphicSystem.CopyRect(BG, rect, FG);
                    rect.width = DOOM.vs.getScreenWidth(); // restore, don't need to bother later
                }
                // BOTTOM FULL WIDTH
                if (lineEndY >= activeEndY)
                {
                    if (lineY >= activeEndY)
                    {
                        rect.y = lineY;
                    } else
                    {
                        rect.y = activeEndY;
                        rect.height = lineHeight - rect.height; // = lineHeight - (activeEndY - lineY);
                    }
                    DOOM.graphicSystem.CopyRect(BG, rect, FG);
                }
            }

            lastautomapactive = DOOM.automapactive;
            if (needsupdate != 0)
                needsupdate--;
        }
    }
}

//$Log: HU.java,v $
//Revision 1.32  2012/09/24 17:16:23  velktron
//Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
//Revision 1.31.2.2  2012/09/24 16:57:43  velktron
//Addressed generics warnings.
//
//Revision 1.31.2.1  2012/09/19 17:43:06  velktron
//Aware of new ViewVars structure.
//
//Revision 1.31  2011/11/01 22:17:46  velktron
//Cleaned up a bit, : IHeadsUp
//
//Revision 1.30  2011/10/23 18:11:58  velktron
//Generic compliance for DoomVideoInterface
//
//Revision 1.29  2011/10/07 16:05:22  velktron
//Now using g.Keys for key input stuff.
//
//Revision 1.28  2011/05/31 23:46:18  velktron
//Fixed scaling.
//
//Revision 1.27  2011/05/31 21:42:30  velktron
//Handling for map33
//
//Revision 1.26  2011/05/24 17:45:08  velktron
//IHeadsUp interface, setChatMacro method.
//
//Revision 1.25  2011/05/23 16:56:44  velktron
//Migrated to VideoScaleInfo.
//
//Revision 1.24  2011/05/21 14:42:32  velktron
//Adapted to use new gamemode system.
//
//Revision 1.23  2011/05/20 18:27:12  velktron
//DoomMenu -> IDoomMenu
//
//Revision 1.22  2011/05/20 18:24:19  velktron
//FINALLY fixed a stupid bug that broke HU messages.
//
//Revision 1.21  2011/05/18 16:52:40  velktron
//Changed to DoomStatus