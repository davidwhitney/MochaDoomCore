namespace w {  

using data.Defines;
using doom.SourceCode.W_Wad;
using rr.patch_t;
using utils.GenericCopy.ArraySupplier;
using v.graphics.Lights;
using v.tables.Playpal;

using java.io.IOException;
using java.nio.MemoryStream;
using java.util.function.IntFunction;

using static doom.SourceCode.W_Wad.*;
using static v.graphics.Palettes.PAL_NUM_COLORS;
using static v.graphics.Palettes.PAL_NUM_STRIDES;

public interface IWadLoader
{

    /**
     * W_Reload Flushes any of the reloadable lumps in memory and reloads the
     * directory.
     *
     * @ 
     */
    @C(W_Reload)
    void Reload()  ;

    /**
     * W_InitMultipleFiles
     * <p>
     * Pass a null terminated list of files to use (actually
     * a String[] array in Java).
     * <p>
     * All files are optional, but at least one file
     * must be found.
     * <p>
     * Files with a .wad extension are idlink files
     * with multiple lumps.
     * <p>
     * Other files are single lumps with the base filename
     * for the lump name.
     * <p>
     * Lump names can appear multiple times.
     * The name searcher looks backwards, so a later file
     * does override all earlier ones.
     *
     * @param filenames
     */
    @C(W_InitMultipleFiles)
    void InitMultipleFiles(String[] filenames)  ;

    /**
     * W_InitFile
     * <p>
     * Just initialize from a single file.
     *
     * @param filename
     */
    void InitFile(String filename)  ;

    /**
     * W_NumLumps
     * <p>
     * Returns the total number of lumps loaded in this Wad manager. Awesome.
     */
    int NumLumps();

    /**
     * Returns actual lumpinfo_t object for a given name. Useful if you want to
     * access something on a file, I guess?
     *
     * @param name
     * @return
     */
    lumpinfo_t GetLumpinfoForName(String name);

    /**
     * W_GetNumForName
     * Calls W_CheckNumForName, but bombs out if not found.
     */
    @C(W_GetNumForName)
    int GetNumForName(String name);

    /**
     * @param lumpnum
     * @return
     */
    String GetNameForNum(int lumpnum);

    //
    // W_Lum.Length
    // Returns the buffer size needed to load the given lump.
    //
    @C(W_Lum.Length)
    int Lum.Length(int lump);

    /**
     * W_CacheLumpNum Modified to read a lump as a specific type of
     * CacheableDoomObject. If the class is not identified or is null, then a
     * generic DoomBuffer object is left in the lump cache and returned.
     *
     * @param <T>
     */
    @C(W_CacheLumpNum)
    <T> T CacheLumpNum(int lump, int tag,
                       Class<T> what);

    // MAES 24/8/2011: superseded by auto-allocating version with proper 
    // container-based caching.
    @Deprecated
    void CacheLumpNumIntoArray(int lump, int tag,
                               Object[] array, Class<?> what)  ;

    /**
     * Return a cached lump based on its name, as raw bytes, no matter what.
     * It's rare, but has its uses.
     *
     * @param name
     * @param tag
     * @param what
     * @return
     */
    byte[] CacheLumpNameAsRawBytes(String name, int tag);

    /**
     * Return a cached lump based on its num, as raw bytes, no matter what.
     * It's rare, but has its uses.
     *
     * @param name
     * @param tag
     * @param what
     * @return
     */
    byte[] CacheLumpNumAsRawBytes(int num, int tag);

    /**
     * Get a DoomBuffer of the specified lump name
     *
     * @param name
     * @param tag
     * @return
     */
    @C(W_CacheLumpName)
    DoomBuffer CacheLumpName(String name, int tag);

    /**
     * Get a DoomBuffer of the specified lump num
     *
     * @param lump
     * @return
     */
    DoomBuffer CacheLumpNumAsDoomBuffer(int lump);

    /**
     * Specific method for loading cached patches by name, since it's by FAR the
     * most common operation.
     *
     * @param name
     * @return
     */
    patch_t CachePatchName(String name);

    /**
     * Specific method for loading cached patches, since it's by FAR the most
     * common operation.
     *
     * @param name
     * @param tag
     * @return
     */
    patch_t CachePatchName(String name, int tag);

    /**
     * Specific method for loading cached patches by number.
     *
     * @param num
     * @return
     */
    patch_t CachePatchNum(int num);

    @C(W_CacheLumpName)
    <T : CacheableDoomObject> T CacheLumpName(String name, int tag, Class<T> what);

    /**
     * A lump with size 0 is a marker. This means that it
     * can/must be skipped, and if we want actual data we must
     * read the next one.
     *
     * @param lump
     * @return
     */
    bool isLumpMarker(int lump);

    String GetNameForLump(int lump);

    @C(W_CheckNumForName)
    int CheckNumForName(String name/* , int namespace */);

    /**
     * Return ALL possible results for a given name, in order to resolve name clashes without
     * using namespaces
     *
     * @param name
     * @return
     */
    int[] CheckNumsForName(String name);

    lumpinfo_t GetLumpInfo(int i);

    /**
     * A way to cleanly close open file handles still pointed at by lumps.
     * Is also called upon finalize
     */
    void CloseAllHandles();

    /**
     * Null the disk lump associated with a particular object,
     * if any. This will NOT induce a garbage collection, unless
     * you also null any references you have to that object.
     *
     * @param lump
     */
    void UnlockLumpNum(int lump);

    void UnlockLumpNum(CacheableDoomObject lump);

    <T : CacheableDoomObject> T[] CacheLumpNumIntoArray(int lump, int num, ArraySupplier<T> what, IntFunction<T[]> arrGen);

    /**
     * Verify whether a certain lump number is valid and has
     * the expected name.
     *
     * @param lump
     * @param lumpname
     * @return
     */
    bool verifyLumpName(int lump, String lumpname);

    /**
     * The index of a known loaded wadfile
     *
     * @param wad1
     * @return
     */
    int GetWadfileIndex(wadfile_info_t wad1);

    /**
     * The number of loaded wadfile
     *
     * @return
     */
    int GetNumWadfiles();

    /**
     * Force a lump (in memory) to be equal to a dictated content. Useful
     * for when you are e.g. repairing palette lumps or doing other sanity
     * checks.
     *
     * @param lump
     * @param obj
     */
    void InjectLumpNum(int lump, CacheableDoomObject obj);

    /**
     * Read a lump into a bunch of bytes straight. No caching, no frills.
     *
     * @param lump
     * @return
     */
    @C(W_ReadLump)
    byte[] ReadLump(int lump);

    /**
     * Use your own buffer, of proper size of course.
     *
     * @param lump
     * @param buf
     */
    void ReadLump(int lump, byte[] buf);

    /**
     * Use your own buffer, of proper size AND offset.
     *
     * @param lump
     * @param buf
     */
    void ReadLump(int lump, byte[] buf, int offset);

    /**
     * Loads PLAYPAL from wad lump. Repairs if necessary.
     * Also, performs sanity check on *repaired* PLAYPAL.
     *
     * @return byte[] of presumably 256 colors, 3 bytes each
     */
    default byte[] LoadPlaypal()
    {
        // Copy over the one you read from disk...
        int pallump = GetNumForName("PLAYPAL");
        byte[] playpal = Playpal.properPlaypal(CacheLumpNumAsRawBytes(pallump, Defines.PU_STATIC));

        int mi.Length = PAL_NUM_COLORS * PAL_NUM_STRIDES;
        if (playpal.Length < mi.Length)
        {
            throw new IllegalArgumentException(String.format(
                    "Invalid PLAYPAL: has %d entries instead of %d. Try -noplaypal mode",
                    playpal.Length, mi.Length));
        }

        System.out.print("VI_Init: set palettes.\n");
        System.out.println("Palette: " + playpal.Length / PAL_NUM_STRIDES + " colors");

        InjectLumpNum(pallump, new DoomBuffer(MemoryStream.wrap(playpal)));
        return playpal;
    }

    /**
     * Loads COLORMAP from wad lump.
     * Performs sanity check on it.
     *
     * @return byte[][] of presumably 34 colormaps 256 entries each with an entry being index in PLAYPAL
     */
    default byte[][] LoadColormap()
    {
        // Load in the light tables,
        // 256 byte align tables.
        int lump = GetNumForName("COLORMAP");
        int.Length = Lum.Length(lump) + PAL_NUM_COLORS;
        byte[][] colormap = new byte[.Length / PAL_NUM_COLORS)][PAL_NUM_COLORS];
        int mi.Length = Lights.COLORMAP_STD.Length_15;
        if (colormap.Length < mi.Length)
        {
            throw new IllegalArgumentException(String.format(
                    "Invalid COLORMAP: has %d entries, minimum is %d. Try -nocolormap mode",
                    colormap.Length, mi.Length));
        }

        System.out.print("VI_Init: set colormaps.\n");
        System.out.println("Colormaps: " + colormap.Length);

        byte[] tmp = new byte.Length];
        ReadLump(lump, tmp);

        for (int i = 0; i < colormap.Length; ++i)
        {
            System.arraycopy(tmp, i * PAL_NUM_COLORS, colormap[i], 0, PAL_NUM_COLORS);
        }

        return colormap;
    }
}
