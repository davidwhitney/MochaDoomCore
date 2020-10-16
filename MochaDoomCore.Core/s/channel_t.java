namespace s {  

using data.sfxinfo_t;
using p.mobj_t;

using javax.sound.sampled.AudioFormat;
using javax.sound.sampled.SourceDataLine;

public class channel_t
{

    public int sfxVolume;
    /**
     * Currently playing sound. If null, then it's free
     */
    DoomSound currentSound = null;

    sfxinfo_t sfxinfo;

    // origin of sound (usually a mobj_t).
    mobj_t origin;

    // handle of the sound being played
    int handle;

    AudioFormat format;
    SourceDataLine auline = null;

    public channel_t()
    {
        sfxinfo = new sfxinfo_t();
    }
}
