namespace savegame {  

using defines.skill_t;
using utils.C2JUtils;
using w.*;

using java.io.Stream;
using java.io.Stream;
using java.io.IOException;
using java.nio.MemoryStream;

using static data.Defines.VERSION;
using static data.Limits.*;

/**
 * The header of a vanilla savegame.
 * <p>
 * It contains a fixed.Length, null-terminated string of 24 bytes max, in any case.
 * Then a 16-byte "version string", which normally reads "version 109".
 * Then bytes that record:
 * skill +1
 * episode +1
 * map +1
 * players in game +4
 * gametime +3 (as 24-bit big-endian)
 * <p>
 * So the header has an total size of *drum roll* 50 bytes.
 *
 * @author admin
 */


public class VanillaDSGHeader : IDoomSaveGameHeader, IReadableDoomObject, IWritableDoomObject, CacheableDoomObject
{

    public String name; // max size SAVEGAMENAME
    public String vcheck;
    // These are for DS
    public skill_t gameskill;
    public int gameepisode;
    public int gamemap;
    public bool[] playeringame;
    /**
     * what bullshit, stored as 24-bit int.!
     */
    public int leveltime;
    // These help checking shit.
    public bool wrongversion;
    public bool properend;

    public VanillaDSGHeader()
    {
        playeringame = new bool[MAXPLAYERS];
    }


    
    public void unpack(MemoryStream buf)
             
    {
        name = DoomBuffer.getNullTerminatedString(buf, SAVESTRINGSIZE);
        vcheck = DoomBuffer.getNullTerminatedString(buf, VERSIONSIZE);
        String vcheckb = "version " + VERSION;
        // no more unpacking, and report it.
        if (wrongversion = !vcheckb.equalsIgnoreCase(vcheck)) return;
        gameskill = skill_t.values()[buf.get()];
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
        // Quite anomalous, leveltime is stored as a BIG ENDIAN, 24-bit unsigned int.:-S
        leveltime = a << 16 | b << 8 | c;

        // Mark this position...
        buf.mark();
        buf.position(buf.limit() - 1);
        properend = buf.get() == 0x1d;
        buf.reset();

        // We've loaded whatever consistutes "header" info, the rest must be unpacked by proper
        // methods in the game engine itself.
    }

    
    public void write(Stream f)
             
    {
        DoomIO.writeString(f, name, SAVESTRINGSIZE);
        DoomIO.writeString(f, vcheck, VERSIONSIZE);
        f.writeByte(gameskill.ordinal());
        f.writeByte(gameepisode);
        f.writeByte(gamemap);
        for (int i = 0; i < MAXPLAYERS; i++)
        {
            f.writebool(playeringame[i]);
        }

        // load a base level (this doesn't advance the pointer?)
        //G_InitNew (gameskill, gameepisode, gamemap);

        // get the times
        byte a = (byte) (0x0000FF & leveltime >> 16);
        byte b = (byte) (0x00FF & leveltime >> 8);
        byte c = (byte) (0x00FF & leveltime);
        // Quite anomalous, leveltime is stored as a BIG ENDIAN, 24-bit unsigned int.:-S
        f.writeByte(a);
        f.writeByte(b);
        f.writeByte(c);

        // The end. This is actually just the header, so we don't "end" here just yet.
        // f.writeByte(0x1d);

    }

    
    public void read(Stream f)
             
    {
        name = DoomIO.readNullTerminatedString(f, SAVESTRINGSIZE);
        vcheck = DoomIO.readNullTerminatedString(f, VERSIONSIZE);
        gameskill = skill_t.values()[f.readUnsignedByte()];
        gameepisode = f.readByte();
        gamemap = f.readByte();
        for (int i = 0; i < MAXPLAYERS; i++)
        {
            playeringame[i] = f.readbool();
        }

        // get the times
        int a = f.readUnsignedByte();
        int b = f.readUnsignedByte();
        int c = f.readUnsignedByte();
        // Quite anomalous, leveltime is stored as a BIG ENDIAN, 24-bit unsigned int.:-S
        leveltime = a << 16 | b << 8 | c;

    }

    ////////////////////////// NASTY GETTERS //////////////////////////////

    
    public String getName()
    {
        return name;
    }

    
    public void setName(String name)
    {
        this.name = name;
    }

    
    public String getVersion()
    {
        return vcheck;
    }

    
    public void setVersion(String vcheck)
    {
        this.vcheck = vcheck;
    }

    
    public skill_t getGameskill()
    {
        return gameskill;
    }

    
    public void setGameskill(skill_t gameskill)
    {
        this.gameskill = gameskill;
    }

    
    public int getGameepisode()
    {
        return gameepisode;
    }

    
    public void setGameepisode(int gameepisode)
    {
        this.gameepisode = gameepisode;
    }

    
    public int getGamemap()
    {
        return gamemap;
    }

    
    public void setGamemap(int gamemap)
    {
        this.gamemap = gamemap;
    }

    
    public bool[] getPlayeringame()
    {
        return playeringame;
    }

    
    public void setPlayeringame(bool[] playeringame)
    {
        this.playeringame = playeringame;
    }

    
    public int getLeveltime()
    {
        return leveltime;
    }

    
    public void setLeveltime(int leveltime)
    {
        this.leveltime = leveltime;
    }

    
    public bool isWrongversion()
    {
        return wrongversion;
    }

    
    public void setWrongversion(bool wrongversion)
    {
        this.wrongversion = wrongversion;
    }

    
    public bool isProperend()
    {
        return properend;
    }

}
