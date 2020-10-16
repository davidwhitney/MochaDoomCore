namespace st {  

using doom.DoomMain;

public abstract class AbstractStatusBar : IDoomStatusBar
{
    protected readonly DoomMain<?, ?> DOOM;

    public AbstractStatusBar(DoomMain<?, ?> DOOM)
    {
        this.DOOM = DOOM;
    }
}
