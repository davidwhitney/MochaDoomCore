namespace p {  

using data.*;
using data.sounds.sfxenum_t;
using defines.statenum_t;
using doom.DoomMain;
using doom.SourceCode.fixed_t;
using doom.player_t;
using doom.thinker_t;
using p.ActiveStates.MobjConsumer;
using rr.subsector_t;
using s.ISoundOrigin;
using w.IPackableDoomObject;
using w.IReadableDoomObject;
using w.IWritableDoomObject;

using java.io.DataInputStream;
using java.io.DataOutputStream;
using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

using static data.Defines.*;
using static data.info.states;
using static p.MapUtils.AproxDistance;
using static utils.C2JUtils.eval;
using static utils.C2JUtils.pointer;

/**
 * NOTES: mobj_t
 * <p>
 * mobj_ts are used to tell the refresh where to draw an image, tell the world
 * simulation when objects are contacted, and tell the sound driver how to
 * position a sound.
 * <p>
 * The refresh uses the next and prev links to follow lists of things in sectors
 * as they are being drawn. The sprite, frame, and angle elements determine
 * which patch_t is used to draw the sprite if it is visible. The sprite and
 * frame values are allmost allways set from state_t structures. The
 * statescr.exe utility generates the states.h and states.c files that contain
 * the sprite/frame numbers from the statescr.txt source file. The xyz origin
 * point represents a point at the bottom middle of the sprite (between the feet
 * of a biped). This is the default origin position for patch_ts grabbed with
 * lumpy.exe. A walking creature will have its z equal to the floor it is
 * standing on.
 * <p>
 * The sound code uses the x,y, and subsector fields to do stereo positioning of
 * any sound effited by the mobj_t.
 * <p>
 * The play simulation uses the blocklinks, x,y,z, radius, height to determine
 * when mobj_ts are touching each other, touching lines in the map, or hit by
 * trace lines (gunshots, lines of sight, etc). The mobj_t->flags element has
 * various bit flags used by the simulation.
 * <p>
 * Every mobj_t is linked into a single sector based on its origin coordinates.
 * The subsector_t is found with R_PointInSubsector(x,y), and the sector_t can
 * be found with subsector->sector. The sector links are only used by the
 * rendering code, the play simulation does not care about them at all.
 * <p>
 * Any mobj_t that needs to be acted upon by something else in the play world
 * (block movement, be shot, etc) will also need to be linked into the blockmap.
 * If the thing has the MF_NOBLOCK flag set, it will not use the block links. It
 * can still interact with other things, but only as the instigator (missiles
 * will run into other things, but nothing can run into a missile). Each block
 * in the grid is 128*128 units, and knows about every line_t that it contains a
 * piece of, and every interactable mobj_t that has its origin contained.
 * <p>
 * A valid mobj_t is a mobj_t that has the proper subsector_t filled in for its
 * xy coordinates and is linked into the sector from which the subsector was
 * made, or has the MF_NOSECTOR flag set (the subsector_t needs to be valid even
 * if MF_NOSECTOR is set), and is linked into a blockmap block or has the
 * MF_NOBLOCKMAP flag set. Links should only be modified by the
 * P_[Un]SetThingPosition() functions. Do not change the MF_NO? flags while a
 * thing is valid.
 * <p>
 * Any questions?
 *
 * @author admin
 */

public class mobj_t extends thinker_t : ISoundOrigin, Interceptable,
        IWritableDoomObject, IPackableDoomObject, IReadableDoomObject
{

    // Call P_SpecialThing when touched.
    public static readonly int MF_SPECIAL = 1;
    // Blocks.
    public static readonly int MF_SOLID = 2;
    // Can be hit.
    public static readonly int MF_SHOOTABLE = 4;
    // Don't use the sector links (invisible but touchable).
    public static readonly int MF_NOSECTOR = 8;

    /* List: thinker links. */
    // public thinker_t thinker;
    // Don't use the blocklinks (inert but displayable)
    public static readonly int MF_NOBLOCKMAP = 16;
    // Not to be activated by sound, deaf monster.
    public static readonly int MF_AMBUSH = 32;

    // More drawing info: to determine current sprite.
    // Will try to attack right back.
    public static readonly int MF_JUSTHIT = 64;
    // Will take at least one step before attacking.
    public static readonly int MF_JUSTATTACKED = 128;
    // On level spawning (initial position),
    // hang from ceiling instead of stand on floor.
    public static readonly int MF_SPAWNCEILING = 256;
    // Don't apply gravity (every tic),
    // that is, object will float, keeping current height
    // or changing it actively.
    public static readonly int MF_NOGRAVITY = 512;
    // Movement flags.
    // This allows jumps from high places.
    public static readonly int MF_DROPOFF = 0x400;
    // For players, will pick up items.
    public static readonly int MF_PICKUP = 0x800;
    // Player cheat. ???
    public static readonly int MF_NOCLIP = 0x1000;
    // Player: keep info about sliding along walls.
    public static readonly int MF_SLIDE = 0x2000;
    // Allow moves to any height, no gravity.
    // For active floaters, e.g. cacodemons, pain elementals.
    public static readonly int MF_FLOAT = 0x4000;
    // Don't cross lines
    // ??? or look at heights on teleport.
    public static readonly int MF_TELEPORT = 0x8000;
    // Don't hit same species, explode on block.
    // Player missiles as well as fireballs of various kinds.
    public static readonly int MF_MISSILE = 0x10000;
    // Dropped by a demon, not level spawned.
    // E.g. ammo clips dropped by dying former humans.
    public static readonly int MF_DROPPED = 0x20000;
    // Use fuzzy draw (shadow demons or spectres),
    // temporary player invisibility powerup.
    public static readonly int MF_SHADOW = 0x40000;
    // Flag: don't bleed when shot (use puff),
    // barrels and shootable furniture shall not bleed.
    public static readonly int MF_NOBLOOD = 0x80000;
    // Don't stop moving halfway off a step,
    // that is, have dead bodies slide down all the way.
    public static readonly int MF_CORPSE = 0x100000;
    // Floating to a height for a move, ???
    // don't auto float to target's height.
    public static readonly int MF_INFLOAT = 0x200000;
    // On kill, count this enemy object
    // towards intermission kill total.
    // Happy gathering.
    public static readonly int MF_COUNTKILL = 0x400000;
    // On picking up, count this item object
    // towards intermission item total.
    public static readonly int MF_COUNTITEM = 0x800000;
    // Special handling: skull in flight.
    // Neither a cacodemon nor a missile.
    public static readonly int MF_SKULLFLY = 0x1000000;
    // Don't spawn this object
    // in death match mode (e.g. key cards).
    public static readonly int MF_NOTDMATCH = 0x2000000;
    // Player sprites in multiplayer modes are modified
    // using an internal color lookup table for re-indexing.
    // If 0x4 0x8 or 0xc,
    // use a translation table for player colormaps
    public static readonly int MF_TRANSLATION = 0xc000000;
    // Hmm ???.
    public static readonly int MF_TRANSSHIFT = 26;
    /*
     * @Override protected void finalize(){ count++; if (count%100==0)
     * System.err
     * .printf("Total %d Mobj %s@%d finalized free memory: %d\n",count,
     * this.type.name(),this.hashCode(),Runtime.getRuntime().freeMemory()); }
     */
    protected static int count = 0;
    private static MemoryStream buffer = MemoryStream.allocate(154);
    private static MemoryStream fastclear = MemoryStream.allocate(154);

    // // MF_ flags for mobjs.
    public  ActionFunctions A;
    /**
     * Info for drawing: position.
     */
    @fixed_t
    public int x;
    @fixed_t
    public int y;
    @fixed_t
    public int z;
    /**
     * More list: links in sector (if needed)
     */
    public thinker_t snext;
    public thinker_t sprev;
    /**
     * orientation. This needs to be long or else certain checks will fail...but
     * I need to see it working in order to confirm
     */
    public long angle;
    /**
     * used to find patch_t and flip value
     */
    public spritenum_t mobj_sprite;
    /**
     * might be ORed with FF_FULLBRIGHT
     */
    public int mobj_frame;
    /**
     * Interaction info, by BLOCKMAP. Links in blocks (if needed).
     */
    public thinker_t bnext;
    public thinker_t bprev;
    /**
     * MAES: was actually a pointer to a struct subsector_s
     */
    public subsector_t subsector;
    /**
     * The closest interval over all contacted Sectors.
     */
    @fixed_t
    public int floorz;
    @fixed_t
    public int ceilingz;
    /**
     * For movement checking.
     */
    @fixed_t
    public int radius;
    @fixed_t
    public int height;
    /**
     * Momentums, used to update position.
     */
    @fixed_t
    public int momx;
    @fixed_t
    public int momy;
    @fixed_t
    public int momz;
    /**
     * If == validcount, already checked.
     */
    public int validcount;
    public mobjtype_t type;
    // MAES: was a pointer
    public mobjinfo_t info; // &mobjinfo[mobj.type]
    public long mobj_tics; // state tic counter
    // MAES: was a pointer
    public state_t mobj_state;
    public long flags;
    public int health;
    /**
     * Movement direction, movement generation (zig-zagging).
     */
    public int movedir; // 0-7
    public int movecount; // when 0, select a new dir
    /**
     * Thing being chased/attacked (or NULL), also the originator for missiles.
     * MAES: was a pointer
     */
    public mobj_t target;
    public int p_target; // for savegames
    /**
     * Reaction time: if non 0, don't attack yet. Used by player to freeze a bit
     * after teleporting.
     */
    public int reactiontime;
    /**
     * If >0, the target will be chased no matter what (even if shot)
     */
    public int threshold;
    /**
     * Additional info record for player avatars only. Only valid if type ==
     * MT_PLAYER struct player_s* player;
     */

    public player_t player;
    /**
     * Player number last looked for.
     */
    public int lastlook;
    /**
     * For nightmare respawn.
     */
    public mapthing_t spawnpoint; // struct
    /**
     * Thing being chased/attacked for tracers.
     */

    public mobj_t tracer; // MAES: was a pointer

    /*
     * The following methods were for the most part "contextless" and
     * instance-specific, so they were implemented here rather that being
     * scattered all over the package.
     */
    public int eflags; // DOOM LEGACY
    // Fields used only during DSG unmarshalling
    public int stateid;
    public int playerid;
    public int p_tracer;
    /**
     * Unique thing id, used during sync debugging
     */
    public int thingnum;
    private mobj_t()
    {
        spawnpoint = new mapthing_t();
        A = null;
    }

    private mobj_t(ActionFunctions A)
    {
        spawnpoint = new mapthing_t();
        this.A = A;
        // A mobj_t is ALSO a thinker, as it always contains the struct.
        // Don't fall for C's trickery ;-)
        // this.thinker=new thinker_t();
    }

    public static mobj_t createOn(DoomMain<?, ?> context)
    {
        if (eval(context.actions))
        {
            return new mobj_t(context.actions);
        }

        return new mobj_t();
    }

    /**
     * P_SetMobjState Returns true if the mobj is still present.
     */

    public bool SetMobjState(statenum_t state)
    {
        state_t st;

        do
        {
            if (state == statenum_t.S_NULL)
            {
                mobj_state = null;
                // MAES/_D_: uncommented this as it should work by now (?).
                A.RemoveMobj(this);
                return false;
            }

            st = states[state.ordinal()];
            mobj_state = st;
            mobj_tics = st.tics;
            mobj_sprite = st.sprite;
            mobj_frame = st.frame;

            // Modified handling.
            // Call action functions when the state is set
            // TODO: try find a bug
            if (st.action.isParamType(MobjConsumer.class))
            {
                st.action.fun(MobjConsumer.class).accept(A, this);
            }

            state = st.nextstate;
        }
        while (!eval(mobj_tics));

        return true;
    }

    /**
     * P_ZMovement
     */

    public void ZMovement()
    {
        @fixed_t int dist;
        @fixed_t int delta;

        // check for smooth step up
        if (player != null && z < floorz)
        {
            player.viewheight -= floorz - z;

            player.deltaviewheight = VIEWHEIGHT - player.viewheight >> 3;
        }

        // adjust height
        z += momz;

        if ((flags & MF_FLOAT) != 0 && target != null)
        {
            // float down towards target if too close
            if ((flags & MF_SKULLFLY) == 0 && (flags & MF_INFLOAT) == 0)
            {
                dist = AproxDistance(x - target.x, y - target.y);

                delta = target.z + (height >> 1) - z;

                if (delta < 0 && dist < -(delta * 3))
                    z -= FLOATSPEED;
                else if (delta > 0 && dist < delta * 3)
                    z += FLOATSPEED;
            }

        }

        // clip movement
        if (z <= floorz)
        {
            // hit the floor

            // Note (id):
            // somebody left this after the setting momz to 0,
            // kinda useless there.
            if ((flags & MF_SKULLFLY) != 0)
            {
                // the skull slammed into something
                momz = -momz;
            }

            if (momz < 0)
            {
                if (player != null && momz < -GRAVITY * 8)
                {
                    // Squat down.
                    // Decrease viewheight for a moment
                    // after hitting the ground (hard),
                    // and utter appropriate sound.
                    player.deltaviewheight = momz >> 3;
                    A.DOOM.doomSound.StartSound(this, sfxenum_t.sfx_oof);
                }
                momz = 0;
            }
            z = floorz;

            if ((flags & MF_MISSILE) != 0 && (flags & MF_NOCLIP) == 0)
            {
                A.ExplodeMissile(this);
                return;
            }
        } else if ((flags & MF_NOGRAVITY) == 0)
        {
            if (momz == 0)
                momz = -GRAVITY * 2;
            else
                momz -= GRAVITY;
        }

        if (z + height > ceilingz)
        {
            // hit the ceiling
            if (momz > 0)
                momz = 0;
            z = ceilingz - height;

            if ((flags & MF_SKULLFLY) != 0)
            { // the skull slammed into
                // something
                momz = -momz;
            }

            if ((flags & MF_MISSILE) != 0 && (flags & MF_NOCLIP) == 0)
            {
                A.ExplodeMissile(this);
            }
        }
    }

    public void clear()
    {
        fastclear.rewind();
        try
        {
            unpack(fastclear);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // _D_: to permit this object to save/load
    @Override
    public void read(DataInputStream f)  
    {
        // More efficient, avoids duplicating code and
        // handles little endian better.
        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        f.read(buffer.array());
        unpack(buffer);
    }

    @Override
    public void write(DataOutputStream f)  
    {

        // More efficient, avoids duplicating code and
        // handles little endian better.
        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        pack(buffer);
        f.write(buffer.array());

    }

    @Override
    public void pack(MemoryStream b)  
    {
        b.order(ByteOrder.LITTLE_ENDIAN);
        super.pack(b); // Pack the head thinker.
        b.putInt(x);
        b.putInt(y);
        b.putInt(z);
        b.putInt(pointer(snext));
        b.putInt(pointer(sprev));
        b.putInt((int) (angle & Tables.BITS32));
        b.putInt(mobj_sprite.ordinal());
        b.putInt(mobj_frame);
        b.putInt(pointer(bnext));
        b.putInt(pointer(bprev));
        b.putInt(pointer(subsector));
        b.putInt(floorz);
        b.putInt(ceilingz);
        b.putInt(radius);
        b.putInt(height);
        b.putInt(momx);
        b.putInt(momy);
        b.putInt(momz);
        b.putInt(validcount);
        b.putInt(type.ordinal());
        b.putInt(pointer(info)); // TODO: mobjinfo
        b.putInt((int) (mobj_tics & Tables.BITS32));
        b.putInt(mobj_state.id); // TODO: state OK?
        b.putInt((int) flags); // truncate
        b.putInt(health);
        b.putInt(movedir);
        b.putInt(movecount);
        b.putInt(pointer(target)); // TODO: p_target?
        b.putInt(reactiontime);
        b.putInt(threshold);
        // Check for player.
        if (player != null)
        {
            b.putInt(1 + player.identify());

            // System.out.printf("Mobj with hashcode %d is player %d",pointer(this),1+this.player.identify());
        } else
            b.putInt(0);
        b.putInt(lastlook);
        spawnpoint.pack(b);
        b.putInt(pointer(tracer)); // tracer pointer stored.

    }

    @Override
    public void unpack(MemoryStream b)  
    {
        b.order(ByteOrder.LITTLE_ENDIAN);
        super.unpack(b); // 12 Read the head thinker.
        x = b.getInt(); // 16
        y = b.getInt(); // 20
        z = b.getInt(); // 24
        b.getLong(); // TODO: snext, sprev. When are those set? 32
        angle = Tables.BITS32 & b.getInt(); // 36
        mobj_sprite = spritenum_t.values()[b.getInt()]; // 40
        mobj_frame = b.getInt(); // 44
        b.getLong(); // TODO: bnext, bprev. When are those set? 52
        b.getInt(); // TODO: subsector 56
        floorz = b.getInt(); // 60
        ceilingz = b.getInt(); // 64
        radius = b.getInt(); // 68
        height = b.getInt(); // 72
        momx = b.getInt(); // 76
        momy = b.getInt(); // 80
        momz = b.getInt(); // 84
        validcount = b.getInt(); // 88
        type = mobjtype_t.values()[b.getInt()]; // 92
        b.getInt(); // TODO: mobjinfo (deduced from type) //96
        mobj_tics = Tables.BITS32 & b.getInt(); // 100
        // System.out.println("State"+f.readLEInt());
        stateid = b.getInt(); // TODO: state OK?
        flags = b.getInt() & Tables.BITS32; // Only 32-bit flags can be restored
        health = b.getInt();
        movedir = b.getInt();
        movecount = b.getInt();
        p_target = b.getInt();
        reactiontime = b.getInt();
        threshold = b.getInt();
        playerid = b.getInt(); // TODO: player. Non null should mean that
        // it IS a player.
        lastlook = b.getInt();
        spawnpoint.unpack(b);
        p_tracer = b.getInt(); // TODO: tracer
    }

    // TODO: a linked list of sectors where this object appears
    // public msecnode_t touching_sectorlist;

    // Sound origin stuff

    @Override
    public  int getX()
    {
        return x;
    }

    @Override
    public  int getY()
    {
        return y;
    }

    @Override
    public  int getZ()
    {
        return z;
    }

    @Override
    public String toString()
    {
        return String.format("%s %d", type, thingnum);
    }

}
