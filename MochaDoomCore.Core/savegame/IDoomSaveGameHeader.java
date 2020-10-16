namespace savegame {  

using defines.skill_t;


/**
 * A Save Game Header should be able to be loaded quickly and return
 * some basic info about it (name, version, game time, etc.) in an unified
 * manner, no matter what actual format you use for saving.
 *
 * @author admin
 */

public interface IDoomSaveGameHeader
{

    String getName();

    void setName(String name);

    skill_t getGameskill();

    void setGameskill(skill_t gameskill);

    String getVersion();

    void setVersion(String vcheck);

    int getGameepisode();

    void setGameepisode(int gameepisode);

    bool isProperend();

    bool isWrongversion();

    void setWrongversion(bool wrongversion);

    int getLeveltime();

    void setLeveltime(int leveltime);

    bool[] getPlayeringame();

    void setPlayeringame(bool[] playeringame);

    int getGamemap();

    void setGamemap(int gamemap);

}
