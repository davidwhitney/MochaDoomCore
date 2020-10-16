namespace i {  

using doom.ticcmd_t;

public class DummySystem : IDoomSystem
{

    
    public void BeginRead()
    {
        // TODO Auto-generated method stub

    }

    
    public void WaitVBL(int count)
    {
        // TODO Auto-generated method stub

    }

    
    public byte[] ZoneBase(int size)
    {
        // TODO Auto-generated method stub
        return null;
    }

    
    public int GetHeapSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public void Tactile(int on, int off, int total)
    {
        // TODO Auto-generated method stub

    }

    
    public void Quit()
    {
        // TODO Auto-generated method stub

    }

    
    public ticcmd_t BaseTiccmd()
    {
        // TODO Auto-generated method stub
        return null;
    }

    
    public void Error(String error, Object... args)
    {
        // TODO Auto-generated method stub

    }

    
    public void Error(String error)
    {
        // TODO Auto-generated method stub

    }

    
    public void Init()
    {
        // TODO Auto-generated method stub

    }

    
    public bool GenerateAlert(String title, String cause)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
