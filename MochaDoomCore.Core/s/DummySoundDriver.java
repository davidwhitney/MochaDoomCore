namespace s {  

using data.sounds.musicenum_t;
using data.sounds.sfxenum_t;
using p.mobj_t;

/**
 * Does nothing. Just allows me to code without
 * commenting out ALL sound-related code. Hopefully
 * it will be superseded by a real sound driver one day.
 *
 * @author Velktron
 */

public class DummySoundDriver : IDoomSound
{

    
    public void Init(int sfxVolume, int musicVolume)
    {
        // TODO Auto-generated method stub

    }

    
    public void Start()
    {
        // TODO Auto-generated method stub

    }

    
    public void StartSound(ISoundOrigin origin, int sound_id)
    {
        // TODO Auto-generated method stub

    }

    
    public void StartSound(ISoundOrigin origin, sfxenum_t sound_id)
    {
        // TODO Auto-generated method stub

    }

    
    public void StartSoundAtVolume(ISoundOrigin origin, int sound_id, int volume)
    {
        // TODO Auto-generated method stub

    }

    
    public void StopSound(ISoundOrigin origin)
    {
        // TODO Auto-generated method stub

    }

    
    public void ChangeMusic(int musicnum, bool looping)
    {
        // TODO Auto-generated method stub

    }

    
    public void StopMusic()
    {
        // TODO Auto-generated method stub

    }

    
    public void PauseSound()
    {
        // TODO Auto-generated method stub

    }

    
    public void ResumeSound()
    {
        // TODO Auto-generated method stub

    }

    
    public void UpdateSounds(mobj_t listener)
    {
        // TODO Auto-generated method stub

    }

    
    public void SetMusicVolume(int volume)
    {
        // TODO Auto-generated method stub

    }

    
    public void SetSfxVolume(int volume)
    {
        // TODO Auto-generated method stub

    }

    
    public void StartMusic(int music_id)
    {
        // TODO Auto-generated method stub

    }

    
    public void StartMusic(musicenum_t music_id)
    {
        // TODO Auto-generated method stub

    }

    
    public void ChangeMusic(musicenum_t musicnum, bool looping)
    {
        // TODO Auto-generated method stub

    }

}
