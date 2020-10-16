namespace p {  

/**
 * YEAH, I'M USING THE CONSTANTS INTERFACE PATTERN. DEAL WITH IT
 */

public interface MobjFlags
{
    // // MF_ flags for mobjs.

    // Call P_SpecialThing when touched.
    long MF_SPECIAL = 1;
    // Blocks.
    long MF_SOLID = 2;
    // Can be hit.
    long MF_SHOOTABLE = 4;
    // Don't use the sector links (invisible but touchable).
    long MF_NOSECTOR = 8;
    // Don't use the blocklinks (inert but displayable)
    long MF_NOBLOCKMAP = 16;

    // Not to be activated by sound, deaf monster.
    long MF_AMBUSH = 32;
    // Will try to attack right back.
    long MF_JUSTHIT = 64;
    // Will take at least one step before attacking.
    long MF_JUSTATTACKED = 128;
    // On level spawning (initial position),
    // hang from ceiling instead of stand on floor.
    long MF_SPAWNCEILING = 256;
    // Don't apply gravity (every tic),
    // that is, object will float, keeping current height
    // or changing it actively.
    long MF_NOGRAVITY = 512;

    // Movement flags.
    // This allows jumps from high places.
    long MF_DROPOFF = 0x400;
    // For players, will pick up items.
    long MF_PICKUP = 0x800;
    // Player cheat. ???
    int MF_NOCLIP = 0x1000;
    // Player: keep info about sliding along walls.
    int MF_SLIDE = 0x2000;
    // Allow moves to any height, no gravity.
    // For active floaters, e.g. cacodemons, pain elementals.
    int MF_FLOAT = 0x4000;
    // Don't cross lines
    // ??? or look at heights on teleport.
    int MF_TELEPORT = 0x8000;
    // Don't hit same species, explode on block.
    // Player missiles as well as fireballs of various kinds.
    int MF_MISSILE = 0x10000;
    // Dropped by a demon, not level spawned.
    // E.g. ammo clips dropped by dying former humans.
    int MF_DROPPED = 0x20000;
    // Use fuzzy draw (shadow demons or spectres),
    // temporary player invisibility powerup.
    int MF_SHADOW = 0x40000;
    // Flag: don't bleed when shot (use puff),
    // barrels and shootable furniture shall not bleed.
    long MF_NOBLOOD = 0x80000;
    // Don't stop moving halfway off a step,
    // that is, have dead bodies slide down all the way.
    long MF_CORPSE = 0x100000;
    // Floating to a height for a move, ???
    // don't auto float to target's height.
    long MF_INFLOAT = 0x200000;

    // On kill, count this enemy object
    // towards intermission kill total.
    // Happy gathering.
    long MF_COUNTKILL = 0x400000;

    // On picking up, count this item object
    // towards intermission item total.
    long MF_COUNTITEM = 0x800000;

    // Special handling: skull in flight.
    // Neither a cacodemon nor a missile.
    long MF_SKULLFLY = 0x1000000;

    // Don't spawn this object
    // in death match mode (e.g. key cards).
    long MF_NOTDMATCH = 0x2000000;

    // Player sprites in multiplayer modes are modified
    // using an internal color lookup table for re-indexing.
    // If 0x4 0x8 or 0xc,
    // use a translation table for player colormaps
    long MF_TRANSLATION = 0xc000000;
    // Hmm ???.
    long MF_TRANSSHIFT = 26;

    long MF_UNUSED2 = 0x0000000010000000;
    long MF_UNUSED3 = 0x0000000020000000;

    // Translucent sprite?                                          // phares
    long MF_TRANSLUCENT = 0x0000000040000000;

    // this is free            LONGLONG(0x0000000100000000)

    // these are greater than an int. That's why the flags below are now uint_64_t

    long MF_TOUCHY = 0x0000000100000000L;
    long MF_BOUNCES = 0x0000000200000000L;
    long MF_FRIEND = 0x0000000400000000L;

    long MF_RESSURECTED = 0x0000001000000000L;
    long MF_NO_DEPTH_TEST = 0x0000002000000000L;
    long MF_FOREGROUND = 0x0000004000000000L;

}
