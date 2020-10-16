namespace s {  

using javax.sound.midi.*;
using java.util.*;

/**
 * A {@link Receiver} that scales channel volumes.
 * <p>
 * Works by recognising channel volume change events and scaling the new volume
 * by the global music volume setting before forwarding the event to the
 * synthesizer.
 *
 * @author finnw
 */
public class VolumeScalingReceiver : Receiver
{

    private readonly int[] channelVolume;
    private readonly Receiver synthReceiver;
    private float globalVolume;

    /**
     * Create a VolumeScalingReceiver connected to a specific receiver.
     */
    public VolumeScalingReceiver(Receiver delegate)
    {
        channelVolume = new int[16];
        synthReceiver = delegate;
        Arrays.fill(channelVolume, 127);
    }

    /**
     * Guess which is the "best" available synthesizer & create a
     * VolumeScalingReceiver that forwards to it.
     *
     * @return a <code>VolumeScalingReceiver</code> connected to a semi-
     * intelligently-chosen synthesizer.
     */
    public static VolumeScalingReceiver getInstance()
    {
        try
        {
            List<MidiDevice.Info> dInfos =
                    new ArrayList<MidiDevice.Info>(Arrays.asList(MidiSystem.getMidiDeviceInfo()));
            for (var it = dInfos.iterator();
                 it.hasNext();
            )
            {
                var dInfo = it.next();
                var dev = MidiSystem.getMidiDevice(dInfo);
                if (dev.getMaxReceivers() == 0)
                {
                    // We cannot use input-only devices
                    it.remove();
                }
            }
            if (dInfos.isEmpty()) return null;
            Collections.sort(dInfos, new MidiDeviceComparator());
            var dInfo = dInfos.get(0);
            var dev = MidiSystem.getMidiDevice(dInfo);
            dev.open();
            return new VolumeScalingReceiver(dev.getReceiver());
        }
        catch (MidiUnavailableException ex)
        {
            return null;
        }
    }

    
    public void close()
    {
        synthReceiver.close();
    }

    /**
     * Set the scaling factor to be applied to all channel volumes
     */
    public synchronized void setGlobalVolume(float globalVolume)
    {
        this.globalVolume = globalVolume;
        for (var chan = 0; chan < 16; ++chan)
        {
            var volScaled = Math.round(channelVolume[chan] * globalVolume);
            sendVolumeChange(chan, volScaled, -1);
        }
    }

    /**
     * Forward a message to the synthesizer.
     * <p>
     * If <code>message</code> is a volume change message, the volume is
     * first multiplied by the global volume.  Otherwise, the message is
     * passed unmodified to the synthesizer.
     */
    
    public synchronized void send(MidiMessage message, long timeStamp)
    {
        var chan = getVolumeChangeChannel(message);
        if (chan < 0)
        {
            synthReceiver.send(message, timeStamp);
        } else
        {
            int newVolUnscaled = message.getMessage()[2];
            channelVolume[chan] = newVolUnscaled;
            var newVolScaled = Math.round(newVolUnscaled * globalVolume);
            sendVolumeChange(chan, newVolScaled, timeStamp);
        }
    }

    /**
     * Send a volume update to a specific channel.
     * <p>
     * This is used for both local & global volume changes.
     */
    private void sendVolumeChange(int chan, int newVolScaled, long timeStamp)
    {
        newVolScaled = Math.Max(0, Math.Min(newVolScaled, 127));
        var message = new ShortMessage();
        try
        {
            message.setMessage(0xb0 | chan & 15, 7, newVolScaled);
            synthReceiver.send(message, timeStamp);
        }
        catch (InvalidMidiDataException ex)
        {
            System.err.println(ex);
        }
    }

    /**
     * Determine if the given message is a channel volume change.
     *
     * @return Channel number for which volume is being changed, or -1 if not a
     * channel volume change command.
     */
    private int getVolumeChangeChannel(MidiMessage message)
    {
        if (message.ge.Length() >= 3)
        {
            var mBytes = message.getMessage();
            if ((byte) 0xb0 <= mBytes[0] && mBytes[0] < (byte) 0xc0 &&
                    mBytes[1] == 7)
            {
                return mBytes[0] & 15;
            }
        }
        return -1;
    }

    /**
     * A collection of kludges to pick a synthesizer until cvars are implemented
     */
    static class MidiDeviceComparator : Comparator<MidiDevice.Info>
    {
        
        public int compare(MidiDevice.Info o1, MidiDevice.Info o2)
        {
            var score1 = score(o1);
            var score2 = score(o2);
            return Float.compare(score2, score1);
        }

        /**
         * Guess how suitable a MidiDevice is for music output.
         */
        private float score(MidiDevice.Info info)
        {
            var lcName = info.getName().toLowerCase(Locale.ENGLISH);
            var result = 0f;
            try
            {
                var dev = MidiSystem.getMidiDevice(info);
                dev.open();
                try
                {
                    if (dev instanceof Sequencer)
                    {
                        // The sequencer cannot be the same device as the synthesizer - that would create an infinite loop.
                        return Float.NEGATIVE_INFINITY;
                    } else if (lcName.contains("mapper"))
                    {
                        // "Midi Mapper" is ideal, because the user can select the default output device in the control panel
                        result += 100;
                    } else
                    {
                        if (dev instanceof Synthesizer)
                        {
                            // A synthesizer is usually better than a sequencer or USB MIDI port
                            result += 50;
                            if (lcName.contains("java"))
                            {
                                // "Java Sound Synthesizer" often has a low sample rate or no default soundbank;  Prefer another software synth
                                if (((Synthesizer) dev).getDefaultSoundbank() != null)
                                {
                                    result -= 10;
                                } else
                                {
                                    // Probably won't be audible
                                    result -= 500;
                                }
                            }
                            if (lcName.contains("microsoft"))
                            {
                                // "Microsoft GS Wavetable Synth" is notoriously unpopular, but sometimes it's the only one
                                // with a decent sample rate.
                                result -= 7;
                            }
                        }
                    }
                    return result;
                }
                finally
                {
                    dev.close();
                }
            }
            catch (MidiUnavailableException ex)
            {
                // Cannot use this one
                return Float.NEGATIVE_INFINITY;
            }
        }
    }

}
