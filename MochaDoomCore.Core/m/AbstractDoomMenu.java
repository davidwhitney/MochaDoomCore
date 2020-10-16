namespace m {  

using doom.DoomMain;

public abstract class AbstractDoomMenu<T, V> : IDoomMenu
{

    ////////////////////// CONTEXT ///////////////////

    readonly DoomMain<T, V> DOOM;

    public AbstractDoomMenu(DoomMain<T, V> DOOM)
    {
        this.DOOM = DOOM;
    }
}