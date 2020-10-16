// Emacs style mode select -*- C++ -*-
// -----------------------------------------------------------------------------
//
// $Id: WadLoader.java,v 1.64 2014/03/28 00:55:32 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// DESCRIPTION:
// Handles WAD file header, directory, lump I/O.
//
// -----------------------------------------------------------------------------

namespace w {  

using doom.SourceCode;
using doom.SourceCode.W_Wad;
using i.DummySystem;
using i.IDoomSystem;
using mochadoom.Loggers;
using rr.patch_t;
using utils.C2JUtils;
using utils.GenericCopy.ArraySupplier;

using java.io.*;
using java.nio.MemoryStream;
using java.util.ArrayList;
using java.util.HashMap;
using java.util.List;
using java.util.function.IntFunction;
using java.util.logging.Level;
using java.util.zip.ZipEntry;
using java.util.zip.ZipInputStream;

using static data.Defines.PU_CACHE;
using static doom.SourceCode.W_Wad.W_CacheLumpName;
using static doom.SourceCode.W_Wad.W_CheckNumForName;
using static utils.GenericCopy.malloc;

public class WadLoader : IWadLoader
{
    //// FIELDS
    private readonly ArrayList<Integer> list = new ArrayList<>();
    /**
     * Location of each lump on disk.
     */
    private lumpinfo_t[] lumpinfo;
    private int numlumps;
    protected IDoomSystem I;
    private int reloadlump;
    // MAES: was char*
    private String reloadname;
    /**
     * Maes 12/12/2010: Some credit must go to Killough for first
     * Introducing the hashtable system into Boom. On early releases I had
     * copied his implementation, but it proved troublesome later on and slower
     * than just using the language's built-in hash table. Lesson learned, kids:
     * don't reinvent the wheel.
     * <p>
     * TO get an idea of how superior using a hashtable is, on 1000000 random
     * lump searches the original takes 48 seconds, searching for precomputed
     * hashes takes 2.84, and using a HashMap takes 0.2 sec.
     * <p>
     * And the best part is that Java provides a perfectly reasonable implementation.
     */

    private HashMap<String, Integer> doomhash;

    //
    // LUMP BASED ROUTINES.
    //

    //
    // W_AddFile
    // All files are optional, but at least one file must be
    // found (PWAD, if all required lumps are present).
    // Files with a .wad extension are wadlink files
    // with multiple lumps.
    // Other files are single lumps with the base filename
    // for the lump name.
    //
    // If filename starts with a tilde, the file is handled
    // specially to allow map reloads.
    // But: the reload feature is a fragile hack...
    private HashMap<CacheableDoomObject, Integer> zone;
    /**
     * MAES: probably array of byte[]??? void** lumpcache;
     * <p>
     * Actually, loaded objects will be deserialized here as the general type
     * "CacheableDoomObject" (in the worst case they will be byte[] or
     * MemoryStream).
     * <p>
     * Not to brag, but this system is FAR superior to the inline unmarshaling
     * used in other projects ;-)
     */

    private CacheableDoomObject[] lumpcache;
    private bool[] preloaded;
    /**
     * Added for Boom compliance
     */
    private List<wadfile_info_t> wadfiles;

    ///// CONSTRUCTOR
    public WadLoader(IDoomSystem I)
    {
        this();
        this.I = I;
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#InitMultipleFiles(java.lang.String[])
     */

    public WadLoader()
    {
        lumpinfo = new lumpinfo_t[0];
        zone = new HashMap<>();
        wadfiles = new ArrayList<>();
		I = new DummySystem();
    }

    private static bool IsMarker(String marker, String name)
    {
        // Safeguard against nameless marker lumps e.g. in Galaxia.wad
        if (name == null || name.length() == 0) return false;

        return name.equalsIgnoreCase(marker) ||
                // doubled first character test for single-character prefixes only
                // FF_* is valid alias for F_*, but HI_* should not allow HHI_*
                marker.charAt(1) == '_' && name.charAt(0) == marker.charAt(0) &&
                        name.substring(1).equalsIgnoreCase(marker);
    }

    /**
     * #define strcmpi strcasecmp MAES: this is just capitalization. However we
     * can't manipulate String object in Java directly like this, so this must
     * be a return type.
     * <p>
     * TODO: maybe move this in utils?
     */

    private String strupr(String s)
    {
        return s.toUpperCase();
    }

    /**
     * This is where lumps are actually read + loaded from a file.
     *
     * @ 
     */

    private void AddFile(String uri, ZipEntry entry, int type)  
    {
        var header = new wadinfo_t();

        InputStream handle;
        InputStream storehandle;
        long length;
        int startlump;

        var singleinfo = new filelump_t();

        // handle reload indicator.
        if (uri.charAt(0) == '~')
        {
            uri = uri.substring(1);
            reloadname = uri;
            reloadlump = numlumps;
        }

        // open the resource and add to directory
        // It can be any streamed type handled by the "sugar" utilities.

        try
        {
            handle = InputStreamSugar.createInputStreamFromURI(uri, entry, type);
        }
        catch (Exception e)
        {
            I.Error(" couldn't open resource %s \n", uri);
            return;
        }

        // Create and set wadfile info
        var wadinfo = new wadfile_info_t();
        wadinfo.handle = handle;
        wadinfo.name = uri;
        wadinfo.entry = entry;
        wadinfo.type = type;

        // System.out.println(" adding " + filename + "\n");

        // We start at the number of lumps. This allows appending stuff.
        startlump = numlumps;

        var checkname = wadinfo.entry != null ? wadinfo.entry.getName() : uri;
        // If not "WAD" then we check for single lumps.
        // MAES: was *
        var fileinfo = new filelump_t[1];
        if (!C2JUtils.checkForExtension(checkname, "wad"))
        {

            fileinfo[0] = singleinfo;
            singleinfo.filepos = 0;
            singleinfo.size = InputStreamSugar.getSizeEstimate(handle, wadinfo.entry);

            // Single lumps. Only use 8 characters
            singleinfo.actualname = singleinfo.name = C2JUtils.removeExtension(uri).toUpperCase();

            // MAES: check out certain known types of extension
            if (C2JUtils.checkForExtension(uri, "lmp"))
                wadinfo.src = wad_source_t.source_lmp;
            else if (C2JUtils.checkForExtension(uri, "deh"))
                wadinfo.src = wad_source_t.source_deh;
            else if (C2JUtils.checkForExtension(uri, null))
                wadinfo.src = wad_source_t.source_deh;

            numlumps++;

        }
        else
        {
            // MAES: 14/06/10 this is historical, for this is the first time I
            // implement reading something from RAF into Doom's structs.
            // Kudos to the JAKE2 team who solved  this problem before me.
            // MAES: 25/10/11: In retrospect, this solution, while functional, was
            // inelegant and limited.

            var dis = new DataInputStream(handle);

            // Read header in one go. Usually doesn't cause trouble?
            header.read(dis);

            if (header.identification.compareTo("IWAD") != 0)
            {
                // Homebrew levels?
                if (header.identification.compareTo("PWAD") != 0)
                {
                    I.Error("Wad file %s doesn't have IWAD or PWAD id\n", checkname);
                }
                else
                {
                    wadinfo.src = wad_source_t.source_pwad;
                }
            }
            else
            {
                wadinfo.src = wad_source_t.source_iwad;
            }

            length = header.numlumps;
            // Init everything:
            fileinfo = malloc(filelump_t::new, filelump_t[]::new, (int) length);

            dis.close();

            handle = InputStreamSugar.streamSeek(handle, header.infotableofs, wadinfo.maxsize, uri, entry, type);

            // FIX: sometimes reading from zip files doesn't work well, so we pre-cache the TOC
            var TOC = new byte[(int) (length * filelump_t.sizeof())];

            var read = 0;
            while (read < TOC.length)
            {
                // Make sure we have all of the TOC, sometimes ZipInputStream "misses" bytes.
                // when wrapped.
                read += handle.read(TOC, read, TOC.length - read);
            }

            var bais = new ByteArrayInputStream(TOC);

            // MAES: we can't read raw structs here, and even less BLOCKS of
            // structs.

            dis = new DataInputStream(bais);
            DoomIO.readObjectArray(dis, fileinfo, (int) length);

            numlumps += header.numlumps;
            wadinfo.maxsize = estimateWadSize(header, lumpinfo);

        } // end loading wad

        //  At this point, a WADFILE or LUMPFILE been successfully loaded,
        // and so is added to the list
		wadfiles.add(wadinfo);

        // Fill in lumpinfo
        // MAES: this was a realloc(lumpinfo, numlumps*sizeof(lumpinfo_t)),
        // so we have to increase size and copy over. Maybe this should be
        // an ArrayList?

        var oldsize = lumpinfo.length;
        var newlumpinfo = malloc(lumpinfo_t::new, lumpinfo_t[]::new, numlumps);

        try
        {
            System.arraycopy(lumpinfo, 0, newlumpinfo, 0, oldsize);
        }
        catch (Exception e)
        {
            // if (!lumpinfo)
            I.Error("Couldn't realloc lumpinfo");
        }

        // Bye bye, old lumpinfo!
        lumpinfo = newlumpinfo;

        // MAES: lum_p was an alias for lumpinfo[startlump]. I know it's a
        // bit crude as an approximation but heh...

        // MAES: was lumpinfo_t* , but we can use it as an array
        var lump_p = startlump;

        // MAES: if reloadname is null, handle is stored...else an invalid
        // handle?
        storehandle = reloadname != null ? null : handle;

        // This iterates through single files.
        var fileinfo_p = 0;


        for (var i = startlump; i < numlumps; i++, lump_p++, fileinfo_p++)
        {
            lumpinfo[lump_p].handle = storehandle;
            lumpinfo[lump_p].position = fileinfo[fileinfo_p].filepos;
            lumpinfo[lump_p].size = fileinfo[fileinfo_p].size;
            // Make all lump names uppercase. Searches should also be uppercase only.
            lumpinfo[lump_p].name = fileinfo[fileinfo_p].name.toUpperCase();
            lumpinfo[lump_p].hash = lumpinfo[lump_p].name.hashCode();
            // lumpinfo[lump_p].stringhash = name8.getLongHash(strupr(lumpinfo[lump_p].name));
            // LumpNameHash(lumpinfo[lump_p].name);
            lumpinfo[lump_p].intname = name8.getIntName(strupr(lumpinfo[lump_p].name));
            //System.out.println(lumpinfo[lump_p]);
            lumpinfo[lump_p].wadfile = wadinfo; // MAES: Add Boom provenience info
        }


        if (reloadname != null)
            handle.close();
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#GetNumForName(java.lang.String)
     */

    /**
     * Try to guess a realistic wad size limit based only on the number of lumps and their
     * STATED contents, in case it's not possible to get an accurate stream size otherwise.
     * Of course, they may be way off with deliberately malformed files etc.
     *
     * @param header
     * @return
     */

    private long estimateWadSize(wadinfo_t header, lumpinfo_t[] lumpinfo)
    {
        var maxsize = header.infotableofs + header.numlumps * 16;

        for (var lumpinfo_t : lumpinfo)
        {
            if (lumpinfo_t.position + lumpinfo_t.size > maxsize)
            {
                maxsize = lumpinfo_t.position + lumpinfo_t.size;
            }
        }

        return maxsize;
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#Reload()
     */
    @Override
    @SuppressWarnings("null")
    public void Reload()  
    {
        var header = new wadinfo_t();
        DataInputStream handle = null;

        if (reloadname == null)
            return;

        try
        {
            handle = new DataInputStream(new BufferedInputStream(new FileInputStream(reloadname)));
        }
        catch (Exception e)
        {
            I.Error("W_Reload: couldn't open %s", reloadname);
        }

        header.read(handle);
        // Actual number of lumps in file...
        var lumpcount = (int) header.numlumps;
        var fileinfo = new filelump_t[lumpcount];

        handle.reset();
        handle.skip(header.infotableofs);

        // MAES: we can't read raw structs here, and even less BLOCKS of
        // structs.

        DoomIO.readObjectArrayWithReflection(handle, fileinfo, lumpcount);

        // Fill in lumpinfo
        // Maes: same as in W_WADload
        var lump_p = reloadlump;
        var fileinfo_p = 0;
        for (var i = reloadlump; i < reloadlump + lumpcount; i++, lump_p++, fileinfo_p++)
        {
            if (lumpcache[i] != null)
            {
                // That's like "freeing" it, right?
                lumpcache[i] = null;
                preloaded[i] = false;
            }

            lumpinfo[lump_p].position = fileinfo[fileinfo_p].filepos;
            lumpinfo[lump_p].size = fileinfo[fileinfo_p].size;
        }
    }

    @Override
    public void InitMultipleFiles(String[] filenames)  
    {
        int size;

        // open all the files, load headers, and count lumps
        numlumps = 0;

        // will be realloced as lumps are added
        lumpinfo = new lumpinfo_t[0];

        for (var s : filenames)
        {
            if (s != null)
            {
                if (C2JUtils.testReadAccess(s))
                {
                    // Resource is readable, guess type.
                    var type = C2JUtils.guessResourceType(s);
                    if (C2JUtils.flags(type, InputStreamSugar.ZIP_FILE))
                    {
                        addZipFile(s, type);
                    } else
                    {
						AddFile(s, null, type);
                    }

                    System.out.printf("\tadded %s (zipped: %s network: %s)\n", s,
                            C2JUtils.flags(type, InputStreamSugar.ZIP_FILE),
                            C2JUtils.flags(type, InputStreamSugar.NETWORK_FILE));

                } else
                    System.err.printf("Couldn't open resource %s\n", s);
            }
        }

        if (numlumps == 0)
            I.Error("W_InitFiles: no files found");

        CoalesceMarkedResource("S_START", "S_END", li_namespace.ns_sprites);
        CoalesceMarkedResource("F_START", "F_END", li_namespace.ns_flats);
        // CoalesceMarkedResource("P_START", "P_END", li_namespace.ns_flats);

        // set up caching
        size = numlumps;
        lumpcache = new CacheableDoomObject[size];
        preloaded = new bool[size];

        if (lumpcache == null)
            I.Error("Couldn't allocate lumpcache");

		InitLumpHash();
    }

    /**
     * @param s
     * @param type
     * @ 
     * @ 
     */
    private void addZipFile(String s, int type)
             , Exception
    {
        // Get entries
        var is = new BufferedInputStream(
                InputStreamSugar.createInputStreamFromURI(s, null, type)
        );
        var zip = new ZipInputStream(is);
        var zes = InputStreamSugar.getAllEntries(zip);
        zip.close();
        for (var zz : zes)
        {
            // The name of a zip file will be used as an identifier
            if (!zz.isDirectory())
				AddFile(s, zz, type);
        }
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#InitFile(java.lang.String)
     */
    @Override
    public void InitFile(String filename)  
    {
        var names = new String[1];

        names[0] = filename;
        // names[1] = null;
        InitMultipleFiles(names);
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#NumLumps()
     */
    @Override
    public  int NumLumps()
    {
        return numlumps;
    }

    /**
     * Old, shitty method for CheckNumForName. It's an overly literal
     * translation of how the C original worked, which was none too good
     * even without the overhead of converting a string to
     * its integer representation. It's so bad, that it's two orders
     * of magnitude slower than a HashMap implemetation, and one from
     * a direct hash/longname comparison with linear search.
     *
     * @param name
     * @return public int CheckNumForName3(String name) {
     * <p>
     * int v1;
     * int v2;
     * // lumpinfo_t lump_p;
     * <p>
     * int lump_p;
     * // make the name into two integers for easy compares
     * // case insensitive
     * name8 union = new name8(strupr(name));
     * <p>
     * v1 = union.x[0];
     * v2 = union.x[1];
     * <p>
     * // scan backwards so patch lump files take precedence
     * lump_p = numlumps;
     * <p>
     * while (lump_p-- != 0) {
     * int a = name8.stringToInt(lumpinfo[lump_p].name, 0);
     * int b = name8.stringToInt(lumpinfo[lump_p].name, 4);
     * if ((a == v1) && (b == v2)) {
     * return lump_p;
     * }
     * }
     * <p>
     * // TFB. Not found.
     * return -1;
     * }
     */

    /* (non-Javadoc)
     * @see w.IWadLoader#GetLumpinfoForName(java.lang.String)
     */
    @Override
    public lumpinfo_t GetLumpinfoForName(String name)
    {

        int v1;
        int v2;
        // lumpinfo_t lump_p;

        int lump_p;
        // make the name into two integers for easy compares
        // case insensitive
        var union = new name8(strupr(name));

        v1 = union.x[0];
        v2 = union.x[1];

        // scan backwards so patch lump files take precedence
        lump_p = numlumps;

        while (lump_p-- != 0)
        {
            var a = name8.stringToInt(lumpinfo[lump_p].name, 0);
            var b = name8.stringToInt(lumpinfo[lump_p].name, 4);
            if (a == v1 && b == v2)
            {
                return lumpinfo[lump_p];
            }
        }

        // TFB. Not found.
        return null;
    }

    @Override
    public int GetNumForName(String name)
    {
        int i;

        i = CheckNumForName(name.toUpperCase());

        if (i == -1)
        {
            var e = new Exception();
            e.printStackTrace();
            System.err.println("Error: " + name + " not found");
            System.err.println("Hash: "
                    + Long.toHexString(name8.getLongHash(name)));
            I.Error("W_GetNumForName: %s not found!", name);
        }

        return i;
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#GetNameForNum(int)
     */
    @Override
    public String GetNameForNum(int lumpnum)
    {
        if (lumpnum >= 0 && lumpnum < numlumps)
        {
            return lumpinfo[lumpnum].name;
        }
        return null;
    }

    //
    // W_LumpLength
    // Returns the buffer size needed to load the given lump.
    //
    /* (non-Javadoc)
     * @see w.IWadLoader#LumpLength(int)
     */
    @Override
    public int LumpLength(int lump)
    {
        if (lump >= numlumps)
            I.Error("W_LumpLength: %i >= numlumps", lump);

        return (int) lumpinfo[lump].size;
    }

    @Override
    public  byte[] ReadLump(int lump)
    {
        var l = lumpinfo[lump];
        var buf = new byte[(int) l.size];
        ReadLump(lump, buf, 0);
        return buf;

    }

    /* (non-Javadoc)
     * @see w.IWadLoader#CacheLumpNameAsRawBytes(java.lang.String, int)
     */

    @Override
    public  void ReadLump(int lump, byte[] buf)
    {
        ReadLump(lump, buf, 0);
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#CacheLumpNumAsRawBytes(int, int)
     */

    /**
     * W_ReadLump Loads the lump into the given buffer, which must be >=
     * W_LumpLength(). SKIPS CACHING
     *
     * @ 
     */

    @Override
    public  void ReadLump(int lump, byte[] buf, int offset)
    {
        var c = 0;
        lumpinfo_t l;
        InputStream handle = null;

        if (lump >= numlumps)
        {
            I.Error("W_ReadLump: %i >= numlumps", lump);
            return;
        }

        l = lumpinfo[lump];

        if (l.handle == null)
        {
            // reloadable file, so use open / read / close
            try
            {
                // FIXME: reloadable files can only be that. Files.
                handle = InputStreamSugar.createInputStreamFromURI(reloadname, null, 0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                I.Error("W_ReadLump: couldn't open %s", reloadname);
            }
        } else
            handle = l.handle;

        try
        {

            handle = InputStreamSugar.streamSeek(handle, l.position,
                    l.wadfile.maxsize, l.wadfile.name, l.wadfile.entry, l.wadfile.type);

            // read buffered. Unfortunately that interferes badly with
            // guesstimating the actual stream position.
            var bis = new BufferedInputStream(handle, 8192);

			while (c < l.size)
			{
				c += bis.read(buf, offset + c, (int) (l.size - c));
			}

            // Well, that's a no-brainer.
            //l.wadfile.knownpos=l.position+c;

            if (c < l.size)
                System.err.printf("W_ReadLump: only read %d of %d on lump %d %d\n", c, l.size,
                        lump, l.position);

            if (l.handle == null)
                handle.close();
            else
                l.handle = handle;

            I.BeginRead();

            return;

            // ??? I_EndRead ();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            I.Error("W_ReadLump: could not read lump " + lump);
            e.printStackTrace();
            return;
        }

    }


    /* (non-Javadoc)
     * @see w.IWadLoader#CacheLumpName(java.lang.String, int)
     */

    /**
     * The most basic of the Wadloader functions. Will attempt to read a lump
     * off disk, based on the specific class type (it will call the unpack()
     * method). If not possible to call the unpack method, it will leave a
     * DoomBuffer object in its place, with the raw byte contents. It's
     */

    @Override
    @SuppressWarnings("unchecked")
    public <T> T CacheLumpNum(int lump, int tag, Class<T> what)
    {

        if (lump >= numlumps)
        {
            I.Error("W_CacheLumpNum: %i >= numlumps", lump);
        }

        // Nothing cached here...
        // SPECIAL case : if no class is specified (null), the lump is re-read anyway
        // and you get a raw doombuffer. Plus, it won't be cached.

        if (lumpcache[lump] == null || what == null)
        {

            // read the lump in

            // System.out.println("cache miss on lump "+lump);
            // Fake Zone system: mark this particular lump with the tag specified
            // ptr = Z_Malloc (W_LumpLength (lump), tag, &lumpcache[lump]);
            // Read as a byte buffer anyway.
            var thebuffer = MemoryStream.wrap(ReadLump(lump));

            // Class type specified

            if (what != null)
            {
                try
                {
                    // Can it be uncached? If so, deserialize it.

                    if (:Interface(what, CacheableDoomObject.class))
                    {
                        // MAES: this should be done whenever single lumps
                        // are read. DO NOT DELEGATE TO THE READ OBJECTS THEMSELVES.
                        // In case of sequential reads of similar objects, use
                        // CacheLumpNumIntoArray instead.
                        thebuffer.rewind();
                        lumpcache[lump] = (CacheableDoomObject) what.newInstance();
                        lumpcache[lump].unpack(thebuffer);

                        // Track it for freeing
                        Track(lumpcache[lump], lump);

                        if (what == patch_t.class)
                        {
                            ((patch_t) lumpcache[lump]).name = lumpinfo[lump].name;
                        }
                    } else
                    {
                        // replace lump with parsed object.
                        lumpcache[lump] = (CacheableDoomObject) thebuffer;

                        // Track it for freeing
                        Track((CacheableDoomObject) thebuffer, lump);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Could not auto-instantiate lump "
                            + lump + " of class " + what);
                    e.printStackTrace();
                }

            } else
            {
                // Class not specified? Then gimme a containing DoomBuffer!
                var db = new DoomBuffer(thebuffer);
                lumpcache[lump] = db;
            }
        } else
        {
            // System.out.println("cache hit on lump " + lump);
            // Z.ChangeTag (lumpcache[lump],tag);
        }

        return (T) lumpcache[lump];
    }

    /**
     * A very useful method when you need to load a lump which can consist
     * of an arbitrary number of smaller fixed-size objects (assuming that you
     * know their number/size and the size of the lump). Practically used
     * by the level loader, to handle loading of sectors, segs, things, etc.
     * since their size/lump/number relationship is well-defined.
     * <p>
     * It possible to do this in other ways, but it's extremely convenient this way.
     * <p>
     * MAES 24/8/2011: This method is deprecated, Use the much more convenient
     * and slipstreamed generic version, which also handles caching of arrays
     * and auto-allocation.
     *
     * @param lump  The lump number to load.
     * @param tag   Caching tag
     * @param array The array with objects to load. Its size implies how many to read.
     * @return
     */

    @Override
    @Deprecated
    public void CacheLumpNumIntoArray(int lump, int tag, Object[] array,
                                      Class<?> what)  
    {

        if (lump >= numlumps)
        {
            I.Error("W_CacheLumpNum: %i >= numlumps", lump);
        }

        // Nothing cached here...
        if (lumpcache[lump] == null)
        {

            // read the lump in

            //System.out.println("cache miss on lump " + lump);
            // Read as a byte buffer anyway.
            var thebuffer = MemoryStream.wrap(ReadLump(lump));
            // Store the buffer anyway (as a DoomBuffer)
            lumpcache[lump] = new DoomBuffer(thebuffer);

            // Track it (as ONE lump)
            Track(lumpcache[lump], lump);


        } else
        {
            //System.out.println("cache hit on lump " + lump);
            // Z.ChangeTag (lumpcache[lump],tag);
        }

        // Class type specified. If the previously cached stuff is a
        // "DoomBuffer" we can go on.

        if (what != null && lumpcache[lump].getClass() == DoomBuffer.class)
        {
            try
            {
                // Can it be uncached? If so, deserialize it. FOR EVERY OBJECT.
                var b = ((DoomBuffer) lumpcache[lump]).getBuffer();
                b.rewind();

                for (var i = 0; i < array.length; i++)
                {
                    if (:Interface(what, CacheableDoomObject.class))
                    {
                        ((CacheableDoomObject) array[i]).unpack(b);
                    }
                }
                // lumpcache[lump]=array;
            }
            catch (Exception e)
            {
                System.err.println("Could not auto-unpack lump " + lump
                        + " into an array of objects of class " + what);
                e.printStackTrace();
            }

        }


        return;
    }


    /* (non-Javadoc)
     * @see w.IWadLoader#CachePatchName(java.lang.String)
     */

    /**
     * A very useful method when you need to load a lump which can consist
     * of an arbitrary number of smaller fixed-size objects (assuming that you
     * know their number/size and the size of the lump). Practically used
     * by the level loader, to handle loading of sectors, segs, things, etc.
     * since their size/lump/number relationship is well-defined.
     * <p>
     * It possible to do this in other (more verbose) ways, but it's
     * extremely convenient this way, as a lot of common and repetitive code
     * is only written once, and generically, here. Trumps the older
     * method in v 1.43 of WadLoader, which is deprecated.
     *
     * @param lump The lump number to load.
     * @param num  number of objects to read	 *
     * @return a properly sized array of the correct type.
     */

    @Override
    public <T extends CacheableDoomObject> T[] CacheLumpNumIntoArray(int lump, int num, ArraySupplier<T> what, IntFunction<T[]> arrGen)
    {
        if (lump >= numlumps)
        {
            I.Error("CacheLumpNumIntoArray: %i >= numlumps", lump);
        }

        /**
         * Impossible condition unless you hack generics somehow
         *  - Good Sign 2017/05/07
         */
		/*if (!:Interface(what, CacheableDoomObject.class)){
			I.Error("CacheLumpNumIntoArray: %s does not implement CacheableDoomObject", what.getName());
		}*/

        // Nothing cached here...
        if (lumpcache[lump] == null && what != null)
        {
            //System.out.println("cache miss on lump " + lump);
            // Read as a byte buffer anyway.
            var thebuffer = MemoryStream.wrap(ReadLump(lump));
            var stuff = malloc(what, arrGen, num);

            // Store the buffer anyway (as a CacheableDoomObjectContainer)
            lumpcache[lump] = new CacheableDoomObjectContainer<>(stuff);

            // Auto-unpack it, if possible.

            try
            {
                thebuffer.rewind();
                lumpcache[lump].unpack(thebuffer);
            }
            catch (IOException e)
            {
                Loggers.getLogger(WadLoader.class.getName()).log(Level.WARNING, String.format(
                        "Could not auto-unpack lump %s into an array of objects of class %s", lump, what
                ), e);
            }

            // Track it (as ONE lump)
            Track(lumpcache[lump], lump);
        } else
        {
            //System.out.println("cache hit on lump " + lump);
            // Z.ChangeTag (lumpcache[lump],tag);
        }

        if (lumpcache[lump] == null)
        {
            return null;
        }

        @SuppressWarnings("unchecked") var cont = (CacheableDoomObjectContainer<T>) lumpcache[lump];
        return cont.getStuff();
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#CachePatchName(java.lang.String, int)
     */

    public CacheableDoomObject CacheLumpNum(int lump)
    {
        return lumpcache[lump];
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#CachePatchNum(int, int)
     */

    /**
     * Tells us if a class : a certain interface.
     * If you know of a better way, be my guest.
     *
     * @param what
     * @param which
     * @return
     */

    private bool :Interface(Class<?> what, Class<?> which)
    {
        var shit = what.getInterfaces();
        for (var i = 0; i < shit.length; i++)
        {
            if (shit[i].equals(which))
                return true;
        }

        return false;
    }

    @Override
    public byte[] CacheLumpNameAsRawBytes(String name, int tag)
    {
        return ((DoomBuffer) CacheLumpNum(GetNumForName(name), tag,
                null)).getBuffer().array();
    }

    //
    // W_Profile
    //
	/* USELESS
	 char[][] info = new char[2500][10];

	int profilecount;

	void Profile()   {
		int i;
		// memblock_t block = null;
		Object ptr;
		char ch;
		FileWriter f;
		int j;
		String name;

		for (i = 0; i < numlumps; i++) {
			ptr = lumpcache[i];
			if ((ptr == null)) {
				ch = ' ';
				continue;
			} else {
				// block = (memblock_t *) ( (byte *)ptr - sizeof(memblock_t));
				if (block.tag < PU_PURGELEVEL)
					ch = 'S';
				else
					ch = 'P';
			}
			info[i][profilecount] = ch;
		}
		profilecount++;

		f = new FileWriter(new File("waddump.txt"));
		// name[8] = 0;

		for (i = 0; i < numlumps; i++) {
			name = lumpinfo[i].name;

			f.write(name);

			for (j = 0; j < profilecount; j++)
				f.write("    " + info[i][j]);

			f.write("\n");
		}
		f.close();
	} */

    @Override
    public byte[] CacheLumpNumAsRawBytes(int num, int tag)
    {
        return ((DoomBuffer) CacheLumpNum(num, tag,
                null)).getBuffer().array();
    }

    @Override
    public DoomBuffer CacheLumpName(String name, int tag)
    {
        return CacheLumpNum(GetNumForName(name), tag,
                DoomBuffer.class);

    }

    // /////////////////// HASHTABLE SYSTEM ///////////////////

    //
    // killough 1/31/98: Initialize lump hash table
    //

    @Override
    public DoomBuffer CacheLumpNumAsDoomBuffer(int lump)
    {
        return CacheLumpNum(lump, 0,
                DoomBuffer.class);
    }

    @Override
    public patch_t CachePatchName(String name)
    {
        return CacheLumpNum(GetNumForName(name), PU_CACHE,
                patch_t.class);

    }

    @Override
    public patch_t CachePatchName(String name, int tag)
    {
        return CacheLumpNum(GetNumForName(name), tag,
                patch_t.class);
    }

    @Override
    public patch_t CachePatchNum(int num)
    {
        return CacheLumpNum(num, PU_CACHE, patch_t.class);
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#CacheLumpName(java.lang.String, int, java.lang.Class)
     */
    @Override
    @W_Wad.C(W_CacheLumpName)
    public <T extends CacheableDoomObject> T CacheLumpName(String name, int tag, Class<T> what)
    {
        return CacheLumpNum(GetNumForName(name.toUpperCase()), tag, what);
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#isLumpMarker(int)
     */
    @Override
    public bool isLumpMarker(int lump)
    {
        return lumpinfo[lump].size == 0;
    }

    /* (non-Javadoc)
     * @see w.IWadLoader#GetNameForLump(int)
     */
    @Override
    public String GetNameForLump(int lump)
    {
        return lumpinfo[lump].name;
    }

    private void InitLumpHash()
    {

        doomhash = new HashMap<String, Integer>(numlumps);

        //for (int i = 0; i < numlumps; i++)
        //	lumpinfo[i].index = -1; // mark slots empty

        // Insert nodes to the beginning of each chain, in first-to-last
        // lump order, so that the last lump of a given name appears first
        // in any chain, observing pwad ordering rules. killough

        for (var i = 0; i < numlumps; i++)
        { // hash function:
            doomhash.put(lumpinfo[i].name.toUpperCase(), new Integer(i));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see w.IWadLoader#CheckNumForName(java.lang.String)
     */
    @Override
    @SourceCode.Compatible
    @W_Wad.C(W_CheckNumForName)
    public int CheckNumForName(String name/* , int namespace */)
    {
        var r = doomhash.get(name);
        // System.out.print("Found "+r);

        if (r != null)
        {
            return r;
        }

        // System.out.print(" found "+lumpinfo[i]+"\n" );
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see w.IWadLoader#CheckNumForName(java.lang.String)
     */
    @Override
    public int[] CheckNumsForName(String name)
    {
        list.clear();

        // Dumb search, no chained hashtables I'm afraid :-/
        // Move backwards, so list is compiled with more recent ones first.
        for (var i = numlumps - 1; i >= 0; i--)
        {
            if (name.compareToIgnoreCase(lumpinfo[i].name) == 0)
            {
                list.add(i);
            }
        }

        var num = list.size();
        var result = new int[num];
        for (var i = 0; i < num; i++)
        {
            result[i] = list.get(i);
        }

        // Might be empty/null, so check that out.
        return result;
    }

    @Override
    public lumpinfo_t GetLumpInfo(int i)
    {
        return lumpinfo[i];
    }

    @Override
    public void CloseAllHandles()
    {
        var d = new ArrayList<InputStream>();

        for (var i = 0; i < lumpinfo.length; i++)
        {
            if (!d.contains(lumpinfo[i].handle)) d.add(lumpinfo[i].handle);
        }

        var count = 0;

        for (var e : d)
        {
            try
            {
                e.close();
                //System.err.printf("%s file handle closed",e.toString());
                count++;
            }
            catch (IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        //System.err.printf("%d file handles closed",count);

    }

    @Override
    public void finalize()
    {
        CloseAllHandles();
    }

    /**
     * Based on Boom's W_CoalesceMarkedResource
     * Sort of mashes similar namespaces together so that they form
     * a continuous space (single start and end, e.g. so that multiple
     * S_START and S_END as well as special DEUTEX lumps mash together
     * under a common S_START/S_END boundary). Also also sort of performs
     * a "bubbling down" of marked lumps at the end of the namespace.
     * <p>
     * It's convenient for sprites, but can be replaced by alternatives
     * for flats.
     * <p>
     * killough 4/17/98: add namespace tags
     *
     * @param start_marker
     * @param end_marker
     * @param namespace
     * @return
     */
    private int CoalesceMarkedResource(String start_marker,
                                       String end_marker, li_namespace namespace)
    {
        var result = 0;
        var marked = new lumpinfo_t[numlumps];
        // C2JUtils.initArrayOfObjects(marked, lumpinfo_t.class);
        int num_marked = 0, num_unmarked = 0;
        bool is_marked = false, mark_end = false;
        lumpinfo_t lump;

        // Scan for specified start mark
        for (var i = 0; i < numlumps; i++)
        {
            lump = lumpinfo[i];
            if (IsMarker(start_marker, lump.name)) // start marker found
            { // If this is the first start marker, add start marker to marked lumps
//	    	System.err.printf("%s identified as starter mark for %s index %d\n",lump.name,
//	    			start_marker,i);
                if (num_marked == 0)
                {
                    marked[0] = new lumpinfo_t();
                    marked[0].name = start_marker;
                    marked[0].size = 0;  // killough 3/20/98: force size to be 0
                    marked[0].namespace = li_namespace.ns_global;        // killough 4/17/98
                    marked[0].handle = lump.handle;
                    // No real use for this yet
                    marked[0].wadfile = lump.wadfile;
                    num_marked = 1;
                    //System.err.printf("%s identified as FIRST starter mark for %s index %d\n",lump.name,
                    //		start_marker,i);
                }
                is_marked = true;                            // start marking lumps
            }
            else if (IsMarker(end_marker, lump.name))       // end marker found
            {
                //	System.err.printf("%s identified as end mark for %s index %d\n",lump.name,
                //			end_marker,i);
                mark_end = true;                           // add end marker below
                is_marked = false;                          // stop marking lumps
            }
            else if (is_marked || lump.namespace == namespace)
            {
                // if we are marking lumps,
                // move lump to marked list
                // sf: check for namespace already set

                // sf 26/10/99:
                // ignore sprite lumps smaller than 8 bytes (the smallest possible)
                // in size -- this was used by some dmadds wads
                // as an 'empty' graphics resource
                if (namespace != li_namespace.ns_sprites || lump.size > 8)
                {
                    marked[num_marked] = lump.clone();
                    // System.err.printf("Marked %s as %d for %s\n",lump.name,num_marked,namespace);
                    marked[num_marked++].namespace = namespace;  // killough 4/17/98
                    result++;
                }
            }
            else
            {
                lumpinfo[num_unmarked++] = lump.clone();       // else move down THIS list
            }
        }

        // Append marked list to end of unmarked list
        System.arraycopy(marked, 0, lumpinfo, num_unmarked, num_marked);

        numlumps = num_unmarked + num_marked;           // new total number of lumps

        if (mark_end)                                   // add end marker
        {
            lumpinfo[numlumps].size = 0;  // killough 3/20/98: force size to be 0
            //lumpinfo[numlumps].wadfile = NULL;
            lumpinfo[numlumps].namespace = li_namespace.ns_global;   // killough 4/17/98
            lumpinfo[numlumps++].name = end_marker;
        }

        return result;
    }

    @Override
    public void UnlockLumpNum(int lump)
    {
        lumpcache[lump] = null;
    }

    //// Merged remnants from LumpZone here.

    @Override
    public void InjectLumpNum(int lump, CacheableDoomObject obj)
    {
        lumpcache[lump] = obj;
    }

    /**
     * Add a lump to the tracking
     */

    private void Track(CacheableDoomObject lump, int index)
    {
        zone.put(lump, index);
    }

    @Override
    public void UnlockLumpNum(CacheableDoomObject lump)
    {
        // Remove it from the reference
        var lumpno = zone.remove(lump);


        // Force nulling. This should trigger garbage collection,
        // and reclaim some memory, provided you also nulled any other
        // reference to a certain lump. Therefore, make sure you null
        // stuff right after calling this method, if you want to make sure
        // that they won't be referenced anywhere else.

        if (lumpno != null)
        {
            lumpcache[lumpno] = null;
            //System.out.printf("Lump %d %d freed\n",lump.hashCode(),lumpno);
        }
    }

    @Override
    public bool verifyLumpName(int lump, String lumpname)
    {
        // Lump number invalid
        if (lump < 0 || lump > numlumps - 1) return false;

        var name = GetNameForLump(lump);

        // Expected lump name not found
		return name != null && lumpname.compareToIgnoreCase(name) == 0;

        // Everything should be OK now...
	}

    @Override
    public int GetWadfileIndex(wadfile_info_t wad1)
    {
        return wadfiles.indexOf(wad1);
    }

    @Override
    public int GetNumWadfiles()
    {
        return wadfiles.size();
    }
}