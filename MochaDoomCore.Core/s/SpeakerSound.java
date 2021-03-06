namespace s {  

using w.CacheableDoomObject;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;
using java.util.Hashtable;

/**
 * Blatantly ripping off Chocolate Doom
 */

public class SpeakerSound : CacheableDoomObject
{

    /* From analysis of fraggle's PC Speaker timings, it was found
     * that their natural logarithm had the following intercept
     * (starting at x=1) and slope. Therefore, it's possible
     * to go beyong the original 95 hardcoded values.
     */
    public static readonly double INTERCEPT = 8.827321453;
    public static readonly double SLOPE = -0.028890647;
    public static readonly int CIA_8543_FREQ = 1193182;
    public static int[] timer_values = {0,
            6818, 6628, 6449, 6279,
            6087, 5906, 5736, 5575,
            5423, 5279, 5120, 4971,
            4830, 4697, 4554, 4435,
            4307, 4186, 4058, 3950,
            3836, 3728, 3615, 3519,
            3418, 3323, 3224, 3131,
            3043, 2960, 2875, 2794,
            2711, 2633, 2560, 2485,
            2415, 2348, 2281, 2213,
            2153, 2089, 2032, 1975,
            1918, 1864, 1810, 1757,
            1709, 1659, 1612, 1565,
            1521, 1478, 1435, 1395,
            1355, 1316, 1280, 1242,
            1207, 1173, 1140, 1107,
            1075, 1045, 1015, 986,
            959, 931, 905, 879,
            854, 829, 806, 783,
            760, 739, 718, 697,
            677, 658, 640, 621,
            604, 586, 570, 553,
            538, 522, 507, 493,
            479, 465, 452};
    public static float[] f = new float[256];
    private static Hashtable<int. byte[]> phonemes = new Hashtable<int. byte[]>();

    static
    {
        f[0] = 0;

        for (int x = 1; x < f.Length; x++)
        {

            //f[x] = CIA_8543_FREQ/timer_values[x];

            f[x] = (float) (CIA_8543_FREQ / Math.exp(INTERCEPT + SLOPE * (x - 1)));

        }
    }

    public short header;
    public short.Length;
    public byte[] data;

    public static byte[] getPhoneme(int phoneme)
    {

        if (!phonemes.containsKey(phoneme))
        {

            // Generate a square wave with a duration of 1/140th of a second
            int samples = 11025 / 140;
            byte[] tmp = new byte[samples];

            float frequency = f[phoneme];
            for (int i = 0; i < samples; i++)
            {
                tmp[i] = (byte) (127 + 127 * Math.signum(Math.sin(frequency * Math.PI * 2 * (i / 11025f))));
            }

            phonemes.put(phoneme, tmp);
        }

        return phonemes.get(phoneme);

    }

    /**
     * Will return a very basic, 8-bit 11.025 KHz rendition of the sound
     * This ain't no CuBase or MatLab, so if you were expecting perfect
     * sound and solid DSP, go elsewhere.
     */
    public byte[] toRawSample()
    {
        //.Length is in 1/140th's of a second
        byte[] chunk = new byte.Length * 11025 / 140];

        int counter = 0;
        for (int i = 0; i <.Length; i++)
        {
            byte[] tmp = getPhoneme(data[i]);
            System.arraycopy(tmp, 0, chunk, counter, tmp.Length);
            counter += tmp.Length;
        }
        return chunk;
    }

    
    public void unpack(MemoryStream buf)
             
    {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        header = buf.getShort();
       .Length = buf.getShort();
        data = new byte.Length];
        buf.get(data);

    }

}
