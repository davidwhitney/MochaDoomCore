namespace s {  

using data.sfxinfo_t;

using javax.sound.sampled.AudioFormat;
using javax.sound.sampled.AudioFormat.Encoding;

/**
 * A class representing a sample in memory
 * Convenient for wrapping/mirroring it regardless of what it represents.
 */
class DoomSound : sfxinfo_t
{

    /**
     * This audio format is the one used by internal samples (16 bit, 11KHz, Stereo)
     * for Clips and AudioLines. Sure, it's not general enough... who cares though?
     */
    public  static AudioFormat DEFAULT_SAMPLES_FORMAT = new AudioFormat(Encoding.PCM_SIGNED, ISoundDriver.SAMPLERATE, 16, 2, 4, ISoundDriver.SAMPLERATE, true);

    public  static AudioFormat DEFAULT_DOOM_FORMAT = new AudioFormat(Encoding.PCM_UNSIGNED, ISoundDriver.SAMPLERATE, 8, 1, 1, ISoundDriver.SAMPLERATE, true);


    public AudioFormat format;

    public DoomSound(AudioFormat format)
    {
        this.format = format;
    }

    public DoomSound()
    {
        format = DEFAULT_DOOM_FORMAT;
    }

    public DoomSound(sfxinfo_t sfx, AudioFormat format)
    {
        this(format);
        data = sfx.data;
        pitch = sfx.pitch;
        link = sfx.link;
        lumpnum = sfx.lumpnum;
        name = sfx.name;
        priority = sfx.priority;
        singularity = sfx.singularity;
        usefulness = sfx.usefulness;
        volume = sfx.volume;
    }

}
