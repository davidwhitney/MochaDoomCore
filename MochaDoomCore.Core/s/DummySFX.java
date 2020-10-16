namespace s {  

using data.sfxinfo_t;

public class DummySFX : ISoundDriver
{

    
    public bool InitSound()
    {
        // Dummy is super-reliable ;-)
        return true;
    }

    
    public void UpdateSound()
    {
        // TODO Auto-generated method stub

    }

    
    public void SubmitSound()
    {
        // TODO Auto-generated method stub

    }

    
    public void ShutdownSound()
    {
        // TODO Auto-generated method stub

    }

    
    public int GetSfxLumpNum(sfxinfo_t sfxinfo)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public int StartSound(int id, int vol, int sep, int pitch, int priority)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    
    public void StopSound(int handle)
    {
        // TODO Auto-generated method stub

    }

    
    public bool SoundIsPlaying(int handle)
    {
        // TODO Auto-generated method stub
        return false;
    }

    
    public void UpdateSoundParams(int handle, int vol, int sep, int pitch)
    {
        // TODO Auto-generated method stub

    }

    
    public void SetChannels(int numChannels)
    {
        // TODO Auto-generated method stub

    }

}
