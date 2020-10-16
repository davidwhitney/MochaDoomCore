namespace i {  

public interface IDiskDrawer : IDrawer
{

    /**
     * Disk displayer is currently active
     *
     * @return
     */
    bool isReading();

    /**
     * Set a timeout (in tics) for displaying the disk icon
     *
     * @param timeout
     */
    void setReading(int reading);

    /**
     * Only call after the Wadloader is instantiated and initialized itself.
     */
    void Init();

    /**
     * Status only valid after the last tic has been drawn. Use to know when to redraw status bar.
     *
     * @return
     */
    bool justDoneReading();

}
