namespace doom {  

using defines.skill_t;

/**
 * Groups functions formerly in d_game,
 * in case you want to provide a different implementation
 */

public interface IDoomGame
{

    void ExitLevel();

    void WorldDone();

    bool CheckDemoStatus();

    /**
     * Can be called by the startup code or M_Responder.
     * A normal game starts at map 1,
     * but a warp test can start elsewhere
     */

    void DeferedInitNew(skill_t skill, int episode, int map);

    /**
     * Can be called by the startup code or M_Responder,
     * calls P_SetupLevel or W_EnterWorld.
     */
    void LoadGame(String name);

    /**
     * Called by M_Responder.
     */
    void SaveGame(int slot, String description);

    /**
     * Takes a screenshot *NOW*
     */
    void ScreenShot();

    void StartTitle();

    gameaction_t getGameAction();

    void setGameAction(gameaction_t ga);

    // public void PlayerReborn(int player);

    void DeathMatchSpawnPlayer(int playernum);

}
