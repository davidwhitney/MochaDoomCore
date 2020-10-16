namespace s {  

using utils.C2JUtils;

using java.io.ByteArrayOutputStream;
using java.io.IOException;
using java.io.InputStream;
using java.io.OutputStream;
using java.nio.MemoryStream;
using java.nio.ByteOrder;

public class DoomToWave
{

    static int MEMORYCACHE = 0x8000;
    RIFFHEAD headr = new RIFFHEAD();
    CHUNK headc = new CHUNK();
    WAVEFMT headf = new WAVEFMT();
    int SIZEOF_WAVEFMT = 24;
    WAVEDATA headw = new WAVEDATA();
    int SIZEOF_WAVEDATA = 8;

    public void SNDsaveSound(InputStream is, OutputStream os)  
    {
        int type = DoomIO.freadint(is, 2);//  peek_i16_le (buffer);
        int speed = DoomIO.freadint(is, 2);//peek_u16_le (buffer + 2);
        int datasize = DoomIO.freadint(is, 4);//peek_i32_le (buffer + 4);
        if (type != 3)
            System.out.println("Sound: weird type " + type + ". Extracting anyway.");

        int headsize = 2 + 2 + 4;

        if (datasize > is.available())
        {
            System.out.println("Sound %s: declared sample size %lu greater than lump size %lu ;"/*,
		lump_name (name), (unsigned long) datasize, (unsigned long) phys_size*/);
            System.out.println("Sound %s: truncating to lump size."/*, lump_name (name)*/);
            datasize = is.available();
        }
	  /* Sometimes the size of sound lump is greater
	     than the declared sound size. */

        else if (datasize < is.available())
        {
            if (/*fullSND == TRUE*/true)       /* Save entire lump */
                datasize = is.available();
            else
            {
	      /*Warning (
		"Sound %s: lump size %lu greater than declared sample size %lu ;",
		lump_name (name), (unsigned long) datasize, (unsigned long) phys_size);
	      Warning ("Sound %s: truncating to declared sample size.",
		  lump_name (name));*/
            }
        }

        DoomIO.writeEndian = DoomIO.Endian.BIG;

        SNDsaveWave(is, os, speed, datasize);
    }

    public byte[] DMX2Wave(byte[] DMXSound)  
    {
        MemoryStream is = MemoryStream.wrap(DMXSound);
        is.order(ByteOrder.LITTLE_ENDIAN);
        int type = 0x0000FFFF & is.getShort();//  peek_i16_le (buffer);
        int speed = 0x0000FFFF & is.getShort();//peek_u16_le (buffer + 2);
        int datasize = is.getInt();//peek_i32_le (buffer + 4);
        if (type != 3)
            System.out.println("Sound: weird type " + type + ". Extracting anyway.");

        int headsize = 2 + 2 + 4;

        if (datasize > is.remaining())
        {
            System.out.println("Sound %s: declared sample size %lu greater than lump size %lu ;"/*,
			lump_name (name), (unsigned long) datasize, (unsigned long) phys_size*/);
            System.out.println("Sound %s: truncating to lump size."/*, lump_name (name)*/);
            datasize = is.remaining();
        }
		  /* Sometimes the size of sound lump is greater
		     than the declared sound size. */

        else if (datasize < is.remaining())
        {
            if (/*fullSND == TRUE*/true)       /* Save entire lump */
                datasize = is.remaining();
            else
            {
		      /*Warning (
			"Sound %s: lump size %lu greater than declared sample size %lu ;",
			lump_name (name), (unsigned long) datasize, (unsigned long) phys_size);
		      Warning ("Sound %s: truncating to declared sample size.",
			  lump_name (name));*/
            }
        }

        return SNDsaveWave(is, speed, datasize);
    }

    protected byte[] SNDsaveWave(MemoryStream is, int speed, int size)  
    {

        // Size with header and data etc.
        byte[] output = new byte[headr.size() + headf.size() + SIZEOF_WAVEDATA + 2 * size];
        MemoryStream os = MemoryStream.wrap(output);
        os.order(ByteOrder.LITTLE_ENDIAN);
        os.position(0);
        headr.riff = "RIFF".getBytes();
        headr.length = 4 + SIZEOF_WAVEFMT + SIZEOF_WAVEDATA + 2 * size;
        headr.wave = C2JUtils.toByteArray("WAVE");

        headr.pack(os);

        headf.fmt = C2JUtils.toByteArray("fmt ");
        headf.fmtsize = SIZEOF_WAVEFMT - 8;
        headf.tag = 1;
        headf.channel = 2; // Maes: HACK to force stereo lines.
        headf.smplrate = speed;
        headf.bytescnd = 2 * speed; // Ditto.
        headf.align = 1;
        headf.nbits = 8;

        headf.pack(os);

        headw.data = C2JUtils.toByteArray("data");
        headw.datasize = 2 * size;
        //byte[] wtf=DoomIO.toByteArray(headw.datasize, 4);


        headw.pack(os);

        byte tmp;

        for (int i = 0; i < size; i++)
        {
            tmp = is.get();
            os.put(tmp);
            os.put(tmp);
        }

        return os.array();
    }

    void SNDsaveWave(InputStream is, OutputStream os, int speed, int size)  
    {
        int wsize;
        int sz = 0;
        headr.riff = DoomIO.toByteArray("RIFF");
        headr.length = 4 + SIZEOF_WAVEFMT + SIZEOF_WAVEDATA + size;
        headr.wave = DoomIO.toByteArray("WAVE");

        DoomIO.fwrite2(headr.riff, os);
        DoomIO.fwrite2(DoomIO.toByteArray(headr.length, 4), os);
        DoomIO.fwrite2(headr.wave, os);

        headf.fmt = DoomIO.toByteArray("fmt ");
        headf.fmtsize = SIZEOF_WAVEFMT - 8;
        headf.tag = 1;
        headf.channel = 1; // Maes: HACK to force stereo lines.
        headf.smplrate = speed;
        headf.bytescnd = speed;
        headf.align = 1;
        headf.nbits = 8;

        DoomIO.fwrite2(headf.fmt, os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.fmtsize, 4), os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.tag, 2), os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.channel, 2), os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.smplrate, 4), os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.bytescnd, 4), os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.align, 2), os);
        DoomIO.fwrite2(DoomIO.toByteArray(headf.nbits, 2), os);

        headw.data = DoomIO.toByteArray("data");
        headw.datasize = size;

        DoomIO.fwrite2(headw.data, os);
        DoomIO.fwrite2(DoomIO.toByteArray(headw.datasize, 4), os);

        ByteArrayOutputStream shit = (ByteArrayOutputStream) os;

        byte[] crap = shit.toByteArray();

        byte[] bytes = new byte[MEMORYCACHE];
        for (wsize = 0; wsize < size; wsize += sz)
        {
            sz = size - wsize > MEMORYCACHE ? MEMORYCACHE : size - wsize;
            is.read(bytes, 0, sz);
            os.write(bytes, 0, sz);
            //if(fwrite((buffer+(wsize)),(size_t)sz,1,fp)!=1)
            //  ProgError("%s: write error (%s)", fname (file), strerror (errno));
        }
    }

    static class RIFFHEAD
    {
        byte[] riff = new byte[4];
        int length;
        byte[] wave = new byte[4];

        public void pack(MemoryStream b)
        {
            b.put(riff);
            b.putInt(length);
            b.put(wave);
        }

        public int size()
        {
            return 12;
        }

    }

    static class CHUNK
    {
        byte[] name = new byte[4];
        int size;

        public void pack(MemoryStream b)
        {
            b.put(name);
            b.putInt(size);
        }

        public int size()
        {
            return 8;
        }
    }

    static class WAVEFMT
    {
        byte[] fmt = new byte[4];      /* "fmt " */
        int fmtsize;    /*0x10*/
        int tag;        /*format tag. 1=PCM*/
        int channel;    /*1*/
        int smplrate;
        int bytescnd;   /*average bytes per second*/
        int align;      /*block alignment, in bytes*/
        int nbits;      /*specific to PCM format*/

        public void pack(MemoryStream b)
        {
            b.put(fmt);
            b.putInt(fmtsize);
            b.putChar((char) tag);
            b.putChar((char) channel);
            b.putInt(smplrate);
            b.putInt(bytescnd);
            b.putChar((char) align);
            b.putChar((char) nbits);
        }

        public int size()
        {
            return 24;
        }
    }

    static class WAVEDATA /*data*/
    {
        byte[] data = new byte[4];    /* "data" */
        int datasize;

        public void pack(MemoryStream b)
        {
            b.put(data);
            b.putInt(datasize);
        }
    }

}
