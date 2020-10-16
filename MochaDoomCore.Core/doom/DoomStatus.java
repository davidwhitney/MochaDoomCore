package doom;

import data.mapthing_t;
import defines.*;
import demo.IDoomDemo;
import f.Finale;
import m.Settings;
import mochadoom.Engine;
import p.mobj_t;

import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.stream.Stream;

import static data.Defines.BACKUPTICS;
import static data.Limits.*;
import static g.Signals.ScanCode.*;

/**
 * We need globally shared data structures, for defining the global state
 * variables. MAES: in pure OO style, this should be a global "Doom state"
 * object to be passed along various modules. No ugly globals here!!! Now, some
 * of the variables that appear here were actually defined in separate modules.
 * Pretty much, whatever needs to be shared with other modules was placed here,
 * either as a local definition, or as an extern share. The very least, I'll
 * document where everything is supposed to come from/reside.
 */

public abstract class DoomStatus<T, V>
{

    public static final int BGCOLOR = 7;
    public static final int FGCOLOR = 8;
    /**
     * LUT of ammunition limits for each kind.
     * This doubles with BackPack powerup item.
     * NOTE: this "maxammo" is treated like a global.
     */
    final static int[] maxammo = {200, 50, 300, 50};
    static final int TURBOTHRESHOLD = 0x32;
    static final int SLOWTURNTICS = 6;
    static final int BODYQUESIZE = 32;
    private static final int NUMKEYS = 256;
    /**
     * More prBoom+ stuff. Used mostly for code uhm..reuse, rather
     * than to actually change the way stuff works.
     */

    public static int compatibility_level;
    static int RESENDCOUNT = 10;
    static int PL_DRONE = 0x80;  // bit flag in doomdata->player
    public final ConfigManager CM = Engine.getConfig();
    /**
     * fixed_t
     */
    final int[] forwardmove = {0x19, 0x32}; // + slow turn

    /////////// Local to doomstat.c ////////////
    // TODO: hide those behind getters
    final int[] sidemove = {0x18, 0x28};
    final int[] angleturn = {640, 1280, 320};
    /**
     * Command line parametersm, actually defined in d_main.c
     */
    public boolean nomonsters; // checkparm of -nomonsters
    public boolean fastparm; // checkparm of -fast
    public boolean devparm; // DEBUG: launched with -devparm
    /**
     * Language.
     */
    public Language_t language;
    /**
     * Selected by user
     */
    public skill_t gameskill;
    public int gameepisode;
    public int gamemap;

    // /////////// Normally found in d_main.c ///////////////

    // Selected skill type, map etc.
    /**
     * Nightmare mode flag, single player.
     */
    public boolean respawnmonsters;
    /**
     * Netgame? Only true if >1 player.
     */
    public boolean netgame;
    /**
     * Flag: true only if started as net deathmatch. An enum might handle
     * altdeath/cooperative better. Use altdeath for the "2" value
     */
    public boolean deathmatch;
    /**
     * Use this instead of "deathmatch=2" which is bullshit.
     */
    public boolean altdeath;
    public boolean viewactive;
    // Player taking events, and displaying.
    public int consoleplayer;
    public int displayplayer;
    // Depending on view size - no status bar?
    // Note that there is no way to disable the
    // status bar explicitely.
    public boolean statusbaractive;
    public boolean automapactive; // In AutoMap mode?
    public boolean menuactive; // Menu overlayed?
    public boolean mousecaptured = true;

    //////////// STUFF SHARED WITH THE RENDERER ///////////////

    // -------------------------
    // Status flags for refresh.
    //
    public boolean paused; // Game Pause?
    /**
     * maximum volume for sound
     */
    public int snd_SfxVolume;
    /**
     * maximum volume for music
     */
    public int snd_MusicVolume;
    /**
     * Maximum number of sound channels
     */
    public int numChannels;
    // Current music/sfx card - index useless
    // w/o a reference LUT in a sound module.
    // Ideally, this would use indices found
    // in: /usr/include/linux/soundcard.h
    public int snd_MusicDevice;
    public int snd_SfxDevice;
    // Config file? Same disclaimer as above.
    public int snd_DesiredMusicDevice;
    public int snd_DesiredSfxDevice;
    // -------------------------------------
    // Scores, rating.
    // Statistics on a given map, for intermission.
    //
    public int totalkills;
    public int totalitems;

    // -------------------------
    // Internal parameters for sound rendering.
    // These have been taken from the DOS version,
    // but are not (yet) supported with Linux
    // (e.g. no sound volume adjustment with menu.

    // These are not used, but should be (menu).
    // From m_menu.c:
    // Sound FX volume has default, 0 - 15
    // Music volume has default, 0 - 15
    // These are multiplied by 8.
    public int totalsecret;
    /**
     * TNTHOM "cheat" for flashing HOM-detecting BG
     */
    public boolean flashing_hom;
    // Added for prBoom+ code
    public int totallive;
    public int leveltime; // tics in game play for par
    // --------------------------------------
    // DEMO playback/recording related stuff.
    // No demo, there is a human player in charge?
    // Disable save/end game?
    public boolean usergame;
    // ?
    public boolean demoplayback;
    public boolean demorecording;
    // Quit after playing a demo from cmdline.
    public boolean singledemo;
    public boolean mapstrobe;
    /**
     * Set this to GS_DEMOSCREEN upon init, else it will be null
     * Good Sign at 2017/03/21: I hope it is no longer true info, since I've checked its assignment by NetBeans
     */
    public gamestate_t gamestate = gamestate_t.GS_DEMOSCREEN;
    public int gametic;
    // Alive? Disconnected?
    public boolean[] playeringame = new boolean[MAXPLAYERS];
    public mapthing_t[] deathmatchstarts = new mapthing_t[MAX_DM_STARTS];
    /**
     * pointer into deathmatchstarts
     */
    public int deathmatch_p;
    /**
     * Player spawn spots.
     */
    public mapthing_t[] playerstarts = new mapthing_t[MAXPLAYERS];
    /**
     * Intermission stats.
     * Parameters for world map / intermission.
     */
    public wbstartstruct_t wminfo;
    // if true, load all graphics at level load
    public boolean precache;
    // wipegamestate can be set to -1
    // to force a wipe on the next draw
    // wipegamestate can be set to -1 to force a wipe on the next draw
    public gamestate_t wipegamestate = gamestate_t.GS_DEMOSCREEN;
    public int mouseSensitivity = 5;    // AX: Fix wrong defaut mouseSensitivity
    public int bodyqueslot;

    // -----------------------------
    // Internal parameters, fixed.
    // These are set by the engine, and not changed
    // according to user inputs. Partly load from
    // WAD, partly set at startup time.
    // TODO: This is ???
    public doomcom_t doomcom;
    // TODO: This points inside doomcom.
    public doomdata_t netbuffer;
    public int rndindex;
    protected byte[] savebuffer;
    String[] wadfiles = new String[MAXWADFILES];
    boolean drone;
    boolean respawnparm; // checkparm of -respawn

    // -----------------------------------------
    // Internal parameters, used for engine.
    //

    // File handling stuff.
    // MAES: declared as "extern", shared with Menu.java
    boolean inhelpscreens;
    boolean advancedemo;
    GameMission_t gamemission;
    /**
     * Defaults for menu, methinks.
     */
    skill_t startskill;
    int startepisode;
    int startmap;
    boolean autostart;
    boolean nodrawers;
    boolean noblit;
    // Timer, for scores.
    int levelstarttic; // gametic at level start

    // Needed to store the number of the dummy sky flat.
    // Used for rendering,
    // as well as tracking projectiles etc.
    //public int skyflatnum;

    // TODO: Netgame stuff (buffers and pointers, i.e. indices).
    OutputStreamWriter debugfile;
    /**
     * Set if homebrew PWAD stuff has been added.
     */
    boolean modifiedgame = false;
    /**
     * debug flag to cancel adaptiveness set to true during timedemos.
     */
    boolean singletics = false;
    /* A "fastdemo" is a demo with a clock that tics as
     * fast as possible, yet it maintains adaptiveness and doesn't
     * try to render everything at all costs.
     */
    boolean fastdemo;
    boolean normaldemo;
    String loaddemo = null;

    // Fields used for selecting variable BPP implementations.
    ticcmd_t[] localcmds = new ticcmd_t[BACKUPTICS];
    ticcmd_t[][] netcmds;// [MAXPLAYERS][BACKUPTICS];
    // MAES: Fields specific to DoomGame. A lot of them were
    // duplicated/externalized
    // in d_game.c and d_game.h, so it makes sense adopting a more unified
    // approach.
    gameaction_t gameaction = gameaction_t.ga_nothing;
    boolean sendpause; // send a pause event next tic
    boolean sendsave; // send a save event next tic
    int starttime;
    boolean timingdemo; // if true, exit with report on completion
    String demoname;

    // ////////// DEMO SPECIFIC STUFF/////////////
    boolean netdemo;
    IDoomDemo demobuffer;

    //protected IDemoTicCmd[] demobuffer;
    /**
     * pointers
     */
    // USELESS protected int demo_p;

    // USELESS protected int demoend;

    short[][] consistancy = new short[MAXPLAYERS][BACKUPTICS];
    /* TODO Proper reconfigurable controls. Defaults hardcoded for now. T3h h4x, d00d. */
    int key_right = SC_NUMKEY6.ordinal();
    int key_left = SC_NUMKEY4.ordinal();
    int key_up = SC_W.ordinal();
    int key_down = SC_S.ordinal();
    int key_strafeleft = SC_A.ordinal();
    int key_straferight = SC_D.ordinal();
    int key_fire = SC_LCTRL.ordinal();
    int key_use = SC_SPACE.ordinal();
    int key_strafe = SC_LALT.ordinal();
    int key_speed = SC_RSHIFT.ordinal();
    boolean vanillaKeyBehavior;
    int key_recordstop = SC_Q.ordinal();
    int[] key_numbers = Stream.of(SC_1, SC_2, SC_3, SC_4, SC_5, SC_6, SC_7, SC_8, SC_9, SC_0)
            .mapToInt(Enum::ordinal).toArray();
    // Heretic stuff
    int key_lookup = SC_PGUP.ordinal();
    int key_lookdown = SC_PGDOWN.ordinal();
    int key_lookcenter = SC_END.ordinal();
    int mousebfire = 0;
    int mousebstrafe = 2;    // AX: Fixed - Now we use the right mouse buttons
    int mousebforward = 1;    // AX: Fixed - Now we use the right mouse buttons
    int joybfire;
    int joybstrafe;
    int joybuse;
    int joybspeed;
    /**
     * Cancel vertical mouse movement by default
     */
    boolean novert = false;    // AX: The good default
    boolean[] gamekeydown = new boolean[NUMKEYS];
    boolean keysCleared;
    boolean alwaysrun;
    int turnheld; // for accelerative turning
    int lookheld; // for accelerative looking?
    boolean[] mousearray = new boolean[4];
    /**
     * mouse values are used once
     */
    int mousex;
    int mousey;
    int dclicktime;
    int dclickstate;
    int dclicks;
    int dclicktime2;
    int dclickstate2;
    int dclicks2;
    /**
     * joystick values are repeated
     */
    int joyxmove;
    int joyymove;
    boolean[] joyarray = new boolean[5];
    int savegameslot;
    String savedescription;
    mobj_t[] bodyque = new mobj_t[BODYQUESIZE];
    String statcopy; // for statistics driver
    /**
     * Not documented/used in linuxdoom. I supposed it could be used to
     * ignore mouse input?
     */

    boolean use_mouse;
    boolean use_joystick;
    /**
     * Game Mode - identify IWAD as shareware, retail etc.
     * This is now hidden behind getters so some cases like plutonia
     * etc. can be handled more cleanly.
     */

    private GameMode gamemode;

    public DoomStatus()
    {
        wminfo = new wbstartstruct_t();
        initNetGameStuff();
    }

    public GameMode getGameMode()
    {
        return gamemode;
    }

    public void setGameMode(GameMode mode)
    {
        gamemode = mode;
    }

    public boolean isShareware()
    {
        return gamemode == GameMode.shareware;
    }

    /**
     * Commercial means Doom 2, Plutonia, TNT, and possibly others like XBLA.
     *
     * @return
     */
    public boolean isCommercial()
    {
        return gamemode == GameMode.commercial ||
                gamemode == GameMode.pack_plut ||
                gamemode == GameMode.pack_tnt ||
                gamemode == GameMode.pack_xbla ||
                gamemode == GameMode.freedoom2 ||
                gamemode == GameMode.freedm;
    }

    /**
     * Retail means Ultimate.
     *
     * @return
     */
    public boolean isRetail()
    {
        return gamemode == GameMode.retail || gamemode == GameMode.freedoom1;
    }

    /**
     * Registered is a subset of Ultimate
     *
     * @return
     */

    public boolean isRegistered()
    {
        return gamemode == GameMode.registered || gamemode == GameMode.retail || gamemode == GameMode.freedoom1;
    }

    /**
     * MAES: this WAS NOT in the original.
     * Remember to call it!
     */
    private void initNetGameStuff()
    {
        //this.netbuffer = new doomdata_t();
        doomcom = new doomcom_t();
        netcmds = new ticcmd_t[MAXPLAYERS][BACKUPTICS];

        Arrays.setAll(localcmds, i -> new ticcmd_t());
        for (var i = 0; i < MAXPLAYERS; i++)
        {
            Arrays.setAll(netcmds[i], j -> new ticcmd_t());
        }
    }

    protected abstract Finale<T> selectFinale();

    public boolean getPaused()
    {
        return paused;
    }

    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }

    int MAXPLMOVE()
    {
        return forwardmove[1];
    }

    /**
     * This is an alias for mousearray [1+i]
     */
    boolean mousebuttons(int i)
    {
        return mousearray[1 + i]; // allow [-1]
    }

    void mousebuttons(int i, boolean value)
    {
        mousearray[1 + i] = value; // allow [-1]
    }

    protected void mousebuttons(int i, int value)
    {
        mousearray[1 + i] = value != 0; // allow [-1]
    }

    boolean joybuttons(int i)
    {
        return joyarray[1 + i]; // allow [-1]
    }

    void joybuttons(int i, boolean value)
    {
        joyarray[1 + i] = value; // allow [-1]
    }

    protected void joybuttons(int i, int value)
    {
        joyarray[1 + i] = value != 0; // allow [-1]
    }

    public void update()
    {

        snd_SfxVolume = CM.getValue(Settings.sfx_volume, Integer.class);
        snd_MusicVolume = CM.getValue(Settings.music_volume, Integer.class);
        alwaysrun = CM.equals(Settings.alwaysrun, Boolean.TRUE);

        // Keys...
        key_right = CM.getValue(Settings.key_right, Integer.class);
        key_left = CM.getValue(Settings.key_left, Integer.class);
        key_up = CM.getValue(Settings.key_up, Integer.class);
        key_down = CM.getValue(Settings.key_down, Integer.class);
        key_strafeleft = CM.getValue(Settings.key_strafeleft, Integer.class);
        key_straferight = CM.getValue(Settings.key_straferight, Integer.class);
        key_fire = CM.getValue(Settings.key_fire, Integer.class);
        key_use = CM.getValue(Settings.key_use, Integer.class);
        key_strafe = CM.getValue(Settings.key_strafe, Integer.class);
        key_speed = CM.getValue(Settings.key_speed, Integer.class);

        // Mouse buttons
        use_mouse = CM.equals(Settings.use_mouse, 1);
        mousebfire = CM.getValue(Settings.mouseb_fire, Integer.class);
        mousebstrafe = CM.getValue(Settings.mouseb_strafe, Integer.class);
        mousebforward = CM.getValue(Settings.mouseb_forward, Integer.class);

        // Joystick
        use_joystick = CM.equals(Settings.use_joystick, 1);
        joybfire = CM.getValue(Settings.joyb_fire, Integer.class);
        joybstrafe = CM.getValue(Settings.joyb_strafe, Integer.class);
        joybuse = CM.getValue(Settings.joyb_use, Integer.class);
        joybspeed = CM.getValue(Settings.joyb_speed, Integer.class);

        // Sound
        numChannels = CM.getValue(Settings.snd_channels, Integer.class);

        // Map strobe
        mapstrobe = CM.equals(Settings.vestrobe, Boolean.TRUE);

        // Mouse sensitivity
        mouseSensitivity = CM.getValue(Settings.mouse_sensitivity, Integer.class);

        // This should indicate keyboard behavior should be as close as possible to vanilla
        vanillaKeyBehavior = CM.equals(Settings.vanilla_key_behavior, Boolean.TRUE);
    }

    public void commit()
    {
        CM.update(Settings.sfx_volume, snd_SfxVolume);
        CM.update(Settings.music_volume, snd_MusicVolume);
        CM.update(Settings.alwaysrun, alwaysrun);

        // Keys...
        CM.update(Settings.key_right, key_right);
        CM.update(Settings.key_left, key_left);
        CM.update(Settings.key_up, key_up);
        CM.update(Settings.key_down, key_down);
        CM.update(Settings.key_strafeleft, key_strafeleft);
        CM.update(Settings.key_straferight, key_straferight);
        CM.update(Settings.key_fire, key_fire);
        CM.update(Settings.key_use, key_use);
        CM.update(Settings.key_strafe, key_strafe);
        CM.update(Settings.key_speed, key_speed);

        // Mouse buttons
        CM.update(Settings.use_mouse, use_mouse ? 1 : 0);
        CM.update(Settings.mouseb_fire, mousebfire);
        CM.update(Settings.mouseb_strafe, mousebstrafe);
        CM.update(Settings.mouseb_forward, mousebforward);

        // Joystick
        CM.update(Settings.use_joystick, use_joystick ? 1 : 0);
        CM.update(Settings.joyb_fire, joybfire);
        CM.update(Settings.joyb_strafe, joybstrafe);
        CM.update(Settings.joyb_use, joybuse);
        CM.update(Settings.joyb_speed, joybspeed);

        // Sound
        CM.update(Settings.snd_channels, numChannels);

        // Map strobe
        CM.update(Settings.vestrobe, mapstrobe);

        // Mouse sensitivity
        CM.update(Settings.mouse_sensitivity, mouseSensitivity);
    }
}

// $Log: DoomStatus.java,v $
// Revision 1.36  2012/11/06 16:04:58  velktron
// Variables manager less tightly integrated.
//
// Revision 1.35  2012/09/24 17:16:22  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.34.2.3  2012/09/24 16:58:06  velktron
// TrueColor, Generics.
//
// Revision 1.34.2.2  2012/09/20 14:25:13  velktron
// Unified DOOM!!!
//
// Revision 1.34.2.1  2012/09/17 16:06:52  velktron
// Now handling updates of all variables, though those specific to some subsystems should probably be moved???
//
// Revision 1.34  2011/11/01 23:48:10  velktron
// Added tnthom stuff.
//
// Revision 1.33  2011/10/24 02:11:27  velktron
// Stream compliancy
//
// Revision 1.32  2011/10/07 16:01:16  velktron
// Added freelook stuff, using Keys.
//
// Revision 1.31  2011/09/27 16:01:41  velktron
// -complevel_t
//
// Revision 1.30  2011/09/27 15:54:51  velktron
// Added some more prBoom+ stuff.
//
// Revision 1.29  2011/07/28 17:07:04  velktron
// Added always run hack.
//
// Revision 1.28  2011/07/16 10:57:50  velktron
// Merged finnw's changes for enabling polling of ?_LOCK keys.
//
// Revision 1.27  2011/06/14 20:59:47  velktron
// Channel settings now read from default.cfg. Changes in sound creation order.
//
// Revision 1.26  2011/06/04 11:04:25  velktron
// Fixed registered/ultimate identification.
//
// Revision 1.25  2011/06/01 17:35:56  velktron
// Techdemo v1.4a level. Default novert and experimental mochaevents interface.
//
// Revision 1.24  2011/06/01 00:37:58  velktron
// Changed default keys to WASD.
//
// Revision 1.23  2011/05/31 21:45:51  velktron
// Added XBLA version as explicitly supported.
//
// Revision 1.22  2011/05/30 15:50:42  velktron
// Changed to work with new Abstract classes
//
// Revision 1.21  2011/05/26 17:52:11  velktron
// Now using ICommandLineManager
//
// Revision 1.20  2011/05/26 13:39:52  velktron
// Now using ICommandLineManager
//
// Revision 1.19  2011/05/25 17:56:52  velktron
// Introduced some fixes for mousebuttons etc.
//
// Revision 1.18  2011/05/24 17:44:37  velktron
// usemouse added for defaults
//