namespace g {  

using defines.skill_t;
using doom.DoomStatus;
using utils.C2JUtils;
using w.*;

using java.io.DataInputStream;
using java.io.DataOutputStream;
using java.io.IOException;
using java.nio.MemoryStream;

using static data.Defines.VERSION;
using static data.Limits.*;


/**
 * represents the header of Doom savegame, so that basic info can be checked quickly.
 * <p>
 * To load the whole game and check if there are readonly mistakes, you must go through it all.
 * Savegames need to be aware of ALL status and context, so maybe they should be inner classes?
 */


public class DoomSaveGame : CacheableDoomObject, IReadableDoomObject, IWritableDoomObject
{

    public String name; // max size SAVEGAMENAME
    public String vcheck;
    // These are for DS
    public int gameskill;
    public int gameepisode;
    public int gamemap;
    public bool[] playeringame;
    /**
     * what bullshit, stored as 24-bit integer?!
     */
    public int leveltime;
    // These help checking shit.
    public bool wrongversion;
    public bool properend;
    public DoomSaveGame()
    {
        playeringame = new bool[MAXPLAYERS];
    }

    @Override
    public void unpack(MemoryStream buf)  
    {
        name = DoomBuffer.getNullTerminatedString(buf, SAVESTRINGSIZE);
        vcheck = DoomBuffer.getNullTerminatedString(buf, VERSIONSIZE);
        String vcheckb = "version " + VERSION;
        // no more unpacking, and report it.
        if (wrongversion = !vcheckb.equalsIgnoreCase(vcheck)) return;
        gameskill = buf.get();
        gameepisode = buf.get();
        gamemap = buf.get();

        for (int i = 0; i < MAXPLAYERS; i++)
        {
            playeringame[i] = buf.get() != 0;
        }

        // load a base level (this doesn't advance the pointer?) 
        //G_InitNew (gameskill, gameepisode, gamemap); 

        // get the times 
        int a = C2JUtils.toUnsignedByte(buf.get());
        int b = C2JUtils.toUnsignedByte(buf.get());
        int c = C2JUtils.toUnsignedByte(buf.get());
        // Quite anomalous, leveltime is stored as a BIG ENDIAN, 24-bit unsigned integer :-S
        leveltime = (a << 16) + (b << 8) + c;

        // Mark this position...
        buf.mark();
        buf.position(buf.limit() - 1);
        properend = buf.get() == 0x1d;
        buf.reset();

        // We've loaded whatever consistutes "header" info, the rest must be unpacked by proper
        // methods in the game engine itself.
    }


    @Override
    public void write(DataOutputStream f)  
    {
        DoomIO.writeString(f, name, SAVESTRINGSIZE);
        DoomIO.writeString(f, vcheck, VERSIONSIZE);
        f.writeByte(gameskill);
        f.writeByte(gameepisode);
        f.writeByte(gamemap);
        for (int i = 0; i < MAXPLAYERS; i++)
        {
            f.writebool(playeringame[i]);
        }

        // load a base level (this doesn't advance the pointer?) 
        //G_InitNew (gameskill, gameepisode, gamemap); 

        // get the times 
        byte a = (byte) (0x0000FF & leveltime >>> 16);
        byte b = (byte) (0x00FF & leveltime >>> 8);
        byte c = (byte) (0x00FF & leveltime);
        // Quite anomalous, leveltime is stored as a BIG ENDIAN, 24-bit unsigned integer :-S
        f.writeByte(a);
        f.writeByte(b);
        f.writeByte(c);

        // TODO: after this point, we should probably save some packed buffers representing raw state...
        // needs further study.

        // The end.
        f.writeByte(0x1d);

    }

    @Override
    public void read(DataInputStream f)  
    {
        name = DoomIO.readNullTerminatedString(f, SAVESTRINGSIZE);
        vcheck = DoomIO.readNullTerminatedString(f, VERSIONSIZE);
        String vcheckb = "version " + VERSION;
        // no more unpacking, and report it.
        if (wrongversion = !vcheckb.equalsIgnoreCase(vcheck)) return;
        gameskill = f.readByte();
        gameepisode = f.readByte();
        gamemap = f.readByte();
        playeringame = new bool[MAXPLAYERS];
        for (int i = 0; i < MAXPLAYERS; i++)
        {
            playeringame[i] = f.readbool();
        }

        // load a base level (this doesn't advance the pointer?) 
        //G_InitNew (gameskill, gameepisode, gamemap); 

        // get the times 
        int a = f.readUnsignedByte();
        int b = f.readUnsignedByte();
        int c = f.readUnsignedByte();
        // Quite anomalous, leveltime is stored as a BIG ENDIAN, 24-bit unsigned integer :-S
        leveltime = (a << 16) + (b << 8) + c;

        // Mark this position...
        //long mark=f.getFilePointer();
        //f.seek(f.length()-1);
        //if (f.readByte() != 0x1d) properend=false; else
        //    properend=true;
        //f.seek(mark);

        long available = f.available();
        f.skip(available - 1);
        properend = f.readByte() == 0x1d;

        // We've loaded whatever consistutes "header" info, the rest must be unpacked by proper
        // methods in the game engine itself.

    }

    public void toStat(DoomStatus<?, ?> DS)
    {
        System.arraycopy(playeringame, 0, DS.playeringame, 0, playeringame.length);
        DS.gameskill = skill_t.values()[gameskill];
        DS.gameepisode = gameepisode;
        DS.gamemap = gamemap;
        DS.leveltime = leveltime;

    }

    public void fromStat(DoomStatus<?, ?> DS)
    {
        System.arraycopy(DS.playeringame, 0, playeringame, 0, DS.playeringame.length);
        gameskill = DS.gameskill.ordinal();
        gameepisode = DS.gameepisode;
        gamemap = DS.gamemap;
        leveltime = DS.leveltime;

    }

}
