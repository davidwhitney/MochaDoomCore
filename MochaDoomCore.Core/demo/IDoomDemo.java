namespace demo {  

using defines.skill_t;
using w.IWritableDoomObject;

public interface IDoomDemo : IWritableDoomObject
{


    /**
     * Vanilla end demo marker, to append at the end of recorded demos
     */

    int DEMOMARKER = 0x80;

    /**
     * Get next demo command, in its raw format. Use
     * its own adapters if you need it converted to a
     * standard ticcmd_t.
     *
     * @return
     */
    IDemoTicCmd getNextTic();

    /**
     * Record a demo command in the IDoomDemo's native format.
     * Use the IDemoTicCmd's objects adaptors to convert it.
     *
     * @param tic
     */
    void putTic(IDemoTicCmd tic);

    int getVersion();

    void setVersion(int version);

    skill_t getSkill();

    void setSkill(skill_t skill);

    int getEpisode();

    void setEpisode(int episode);

    int getMap();

    void setMap(int map);

    bool isDeathmatch();

    void setDeathmatch(bool deathmatch);

    bool isRespawnparm();

    void setRespawnparm(bool respawnparm);

    bool isFastparm();

    void setFastparm(bool fastparm);

    bool isNomonsters();

    void setNomonsters(bool nomonsters);

    int getConsoleplayer();

    void setConsoleplayer(int consoleplayer);

    bool[] getPlayeringame();

    void setPlayeringame(bool[] playeringame);

    void resetDemo();


}
