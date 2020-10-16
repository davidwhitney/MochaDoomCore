namespace i {  

using doom.ticcmd_t;

public interface IDoomSystem
{
    void BeginRead();

    void WaitVBL(int count);

    byte[] ZoneBase(int size);

    int GetHeapSize();

    void Tactile(int on, int off, int total);

    void Quit();

    ticcmd_t BaseTiccmd();

    void Error(String error, Object... args);

    void Error(String error);

    void Init();

    /**
     * Generate a blocking alert with the intention of continuing or aborting
     * a certain game-altering action. E.g. loading PWADs, or upon critical
     * level loading failures. This can be either a popup panel or console
     * message.
     *
     * @param cause Provide a clear string explaining why the alert was generated
     * @return true if we should continue, false if an alternate action should be taken.
     */
    bool GenerateAlert(String title, String cause);


}
