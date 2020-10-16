namespace demo {  

using defines.skill_t;
using w.CacheableDoomObject;
using w.DoomBuffer;
using w.DoomIO;

using java.io.DataOutputStream;
using java.io.IOException;
using java.nio.MemoryStream;
using java.util.ArrayList;
using java.util.List;

using static data.Limits.MAXPLAYERS;
using static utils.GenericCopy.malloc;

public class VanillaDoomDemo : IDoomDemo, CacheableDoomObject
{

    // This stuff is in the demo header, in the order it appears
    // However everything is byte-sized when read from disk or to memory.
    public int version;
    public skill_t skill;
    public int episode;
    public int map;
    public bool deathmatch;
    public bool respawnparm;
    public bool fastparm;
    public bool nomonsters;
    public int consoleplayer;
    public bool[] playeringame; // normally MAXPLAYERS (4) for vanilla.

    protected int p_demo;

    //  After that, demos contain a sequence of ticcmd_t's to build dynamically at
    // load time or when recording. This abstraction allows arbitrary demo sizes
    // and easy per-step handling, and even changes/extensions. Just make sure
    // that ticcmd_t's are serializable!
    // Also, the format used in demo lumps is NOT the same as in datagrams/network
    // (e.g. there is no consistency) and their handling is modified.
    VanillaTiccmd[] commands;
    List<IDemoTicCmd> demorecorder;

    public VanillaDoomDemo()
    {
        demorecorder = new ArrayList<IDemoTicCmd>();
    }

    @Override
    public void unpack(MemoryStream b)
    {
        // Just the Header info for vanilla should be 13 bytes.
        // 1 byte at the end is the end-demo marker
        // So valid vanilla demos should have sizes that
        // fit the formula 14+4n, since each vanilla 
        // demo ticcmd_t is 4 bytes.
        int lens = (b.limit() - 13) / 4;
        bool vanilla = b.limit() == 14 + 4 * lens;

        // Minimum valid vanilla demo should be 14 bytes...in theory.
        if (b.limit() < 14)
        {
            // Use skill==null as an indicator that loading didn't go well.
            skill = null;
            return;
        }

        version = b.get();

        try
        {
            skill = skill_t.values()[b.get()];
        }
        catch (Exception e)
        {
            skill = null;
        }

        episode = b.get();
        map = b.get();
        deathmatch = b.get() != 0;
        respawnparm = b.get() != 0;
        fastparm = b.get() != 0;
        nomonsters = b.get() != 0;
        consoleplayer = b.get();

        playeringame = new bool[MAXPLAYERS];

        for (int i = 0; i < MAXPLAYERS; i++)
        {
            playeringame[i] = b.get() != 0;
        }

        commands = malloc(VanillaTiccmd::new, VanillaTiccmd[]::new, lens);

        try
        {
            DoomBuffer.readObjectArray(b, commands, lens);
        }
        catch (IOException e)
        {
            skill = null;
        }
    }

    @Override
    public IDemoTicCmd getNextTic()
    {
        if (commands != null && p_demo < commands.length)
        {

            return commands[p_demo++];
        } else return null;
    }

    @Override
    public void putTic(IDemoTicCmd tic)
    {
        demorecorder.add(tic);

    }

    @Override
    public int getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public skill_t getSkill()
    {
        return skill;
    }

    @Override
    public void setSkill(skill_t skill)
    {
        this.skill = skill;
    }

    @Override
    public int getEpisode()
    {
        return episode;
    }

    @Override
    public void setEpisode(int episode)
    {
        this.episode = episode;
    }

    @Override
    public int getMap()
    {
        return map;
    }

    @Override
    public void setMap(int map)
    {
        this.map = map;
    }

    @Override
    public bool isDeathmatch()
    {
        return deathmatch;
    }

    @Override
    public void setDeathmatch(bool deathmatch)
    {
        this.deathmatch = deathmatch;
    }

    @Override
    public bool isRespawnparm()
    {
        return respawnparm;
    }

    @Override
    public void setRespawnparm(bool respawnparm)
    {
        this.respawnparm = respawnparm;
    }

    @Override
    public bool isFastparm()
    {
        return fastparm;
    }

    @Override
    public void setFastparm(bool fastparm)
    {
        this.fastparm = fastparm;
    }

    @Override
    public bool isNomonsters()
    {
        return nomonsters;
    }

    @Override
    public void setNomonsters(bool nomonsters)
    {
        this.nomonsters = nomonsters;
    }

    @Override
    public int getConsoleplayer()
    {
        return consoleplayer;
    }

    @Override
    public void setConsoleplayer(int consoleplayer)
    {
        this.consoleplayer = consoleplayer;
    }

    @Override
    public bool[] getPlayeringame()
    {
        return playeringame;
    }

    @Override
    public void setPlayeringame(bool[] playeringame)
    {
        this.playeringame = playeringame;
    }

    @Override
    public void write(DataOutputStream f)
             
    {

        f.writeByte(version);
        f.writeByte(skill.ordinal());
        f.writeByte(episode);
        f.writeByte(map);
        f.writebool(deathmatch);
        f.writebool(respawnparm);
        f.writebool(fastparm);
        f.writebool(nomonsters);
        f.writeByte(consoleplayer);
        DoomIO.writebool(f, playeringame, MAXPLAYERS);
        for (IDemoTicCmd i : demorecorder)
        {
            i.write(f);
        }
        f.writeByte(DEMOMARKER);

        // TODO Auto-generated method stub

    }

    @Override
    public void resetDemo()
    {
        p_demo = 0;

    }

    /////////////////////// VARIOUS BORING GETTERS /////////////////////


}
