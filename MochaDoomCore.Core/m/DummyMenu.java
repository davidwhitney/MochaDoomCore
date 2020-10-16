namespace m {  

using doom.DoomMain;
using doom.event_t;

/**
 * A dummy menu, useful for testers that do need a defined
 * menu object.
 *
 * @author Maes
 */

public class DummyMenu<T, V> : AbstractDoomMenu<T, V>
{
    public DummyMenu(DoomMain<T, V> DOOM)
    {
        super(DOOM);
    }

    
    public bool Responder(event_t ev)
    {
        // TODO Auto-generated method stub
        return false;
    }

    
    public void Ticker()
    {
        // TODO Auto-generated method stub

    }

    
    public void Drawer()
    {
        // TODO Auto-generated method stub

    }

    
    public void Init()
    {
        // TODO Auto-generated method stub

    }

    
    public void StartControlPanel()
    {
        // TODO Auto-generated method stub

    }

    
    public bool getShowMessages()
    {
        // TODO Auto-generated method stub
        return false;
    }

    
    public void setShowMessages(bool val)
    {
        // TODO Auto-generated method stub

    }

    
    public int getScreenBlocks()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public void setScreenBlocks(int val)
    {
        // TODO Auto-generated method stub

    }

    
    public int getDetailLevel()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public void ClearMenus()
    {
        // TODO Auto-generated method stub

    }

}
