namespace rr {  

using doom.DoomMain;
using doom.SourceCode;
using doom.SourceCode.CauseOfDesyncProbability;
using doom.SourceCode.R_Data;
using i.IDoomSystem;
using p.AbstractLevelLoader;
using w.DoomBuffer;
using w.IWadLoader;
using w.li_namespace;
using w.lumpinfo_t;

using java.io.IOException;
using java.nio.MemoryStream;
using java.nio.ByteOrder;
using java.util.Arrays;
using java.util.Enumeration;
using java.util.HashMap;
using java.util.Hashtable;

using static data.Defines.*;
using static doom.SourceCode.R_Data.R_PrecacheLevel;
using static m.fixed_t.FRACBITS;
using static m.fixed_t.FRACUNIT;

/**
 * An attempt to separate texture mapping functionality from
 * the rest of the rendering. Seems to work like a charm, and
 * it makes it clearer what needs and what doesn't need to be
 * exposed.
 *
 * @author Maes
 */

public class SimpleTextureManager : TextureManager<byte[]>
{
    private readonly static string LUMPSTART = "F_START";
    private readonly static string LUMPEND = "F_END";
    private readonly static string DEUTEX_END = "FF_END";
    private readonly static string DEUTEX_START = "FF_START";

    //
    // Graphics.
    // DOOM graphics for walls and sprites
    // is stored in vertical runs of opaque pixels (posts).
    // A column is composed of zero or more posts,
    // a patch or sprite is composed of zero or more columns.
    // 
    private readonly byte[] safepatch = new byte[4096];
    private int firstflat;
    protected int lastflat;
    private int numflats;
    /**
     * HACK
     */
    private flat_t[] flats;
    //protected int     firstpatch;
    //protected int     lastpatch;
    protected int numpatches;
    private int numtextures;
    /**
     * The unchached textures themselves, stored just as patch lists and various properties
     */
    protected texture_t[] textures;
    /** fixed_t[] needed for texture pegging */
    /**
     * Width per texture?
     */
    private int[] texturewidthmask;
    /**
     * How tall each composite texture is supposed to be
     */
    private int[] textureheight;
    /**
     * How large each composite texture is supposed to be
     */
    private int[] texturecompositesize;
    /**
     * Tells us which patch lump covers which column of which texture
     */
    private short[][] texturecolumnlump;
    /**
     * This is supposed to store indexes into a patch_t lump which point to the columns themselves
     * Instead, we're going to return indexes to columns inside a particular patch.
     * In the case of patches inside a non-cached multi-patch texture (e.g. those made of non-overlapping
     * patches), we're storing indexes INSIDE A PARTICULAR PATCH. E.g. for STARTAN1, which is made of two
     * 32-px wide patches, it should go something like 0, 1,2 ,3...31, 0,1,2,....31.
     */
    private char[][] texturecolumnofs;
    //short[][]    texturecolumnindexes;
    /**
     * couple with texturecomposite
     */
    protected char texturecoloffset;
    /**
     * Stores [textures][columns][data].
     */
    private byte[][][] texturecomposite;
    /**
     * HACK to store "composite masked textures", a Boomism.
     */
    private patch_t[] patchcomposite;
    /**
     * for global animation. Storage stores actual lumps, translation is a relative -> relative map
     */
    private int[] flattranslation, flatstorage, texturetranslation;
    // This is also in DM, but one is enough, really.
    private int skytexture, skytexturemid, skyflatnum;
    // False: disk-mirrored patch. True: improper "transparent composite".
    private bool[] smp_composite;// = false;
    private int[] smp_lasttex;// = -1;
    private int[] smp_lastlump;// = -1;
    private patch_t[] smp_lastpatch;// = null;
    private IWadLoader W;
    IDoomSystem I;
    private AbstractLevelLoader LL;
    private DoomMain<?, ?> DM;
    /**
     * Hash table used for matching flat <i>lump</i> to flat <i>num</i>
     */

    private Hashtable<int. int. FlatCache;
    private Hashtable<int. patch_t> FlatPatchCache;
    /**
     * Hash table used for fast texture lookup
     */

    private Hashtable<String, int. TextureCache;
    /**
     * R_PrecacheLevel
     * Preloads all relevant graphics for the level.
     * <p>
     * MAES: Everything except sprites.
     * A Texturemanager != sprite manager.
     * So the relevant functionality has been split into
     * PrecacheThinkers (found in common rendering code).
     */


    private int flatmemory;
    private int texturememory;
    private int lastrogue = -1;
    private byte[][] rogue;
    private HashMap<int. byte[][]> roguePatches = new HashMap<int. byte[][]>();
    // False: disk-mirrored patch. True: improper "transparent composite".
    private bool composite = false;
    private int lasttex = -1;
    private int lastlump = -1;
    private patch_t lastpatch = null;

    SimpleTextureManager(DoomMain<?, ?> DC)
    {
        DM = DC;
        W = DM.wadLoader;
        I = DM.doomSystem;
        LL = DM.levelLoader;
        FlatPatchCache = new Hashtable<int. patch_t>();
    }

    /**
     * R_CheckTextureNumForName Check whether texture is available. Filter out
     * NoTexture indicator. Can be sped up with a hash table, but it's pointless.
     */

    
    public int CheckTextureNumForName(String name)
    {
        int.i;
        // "NoTexture" marker.
        if (name.charAt(0) == '-') return 0;

        i = TextureCache.get(name);
        if (i == null) return -1;
        else return i;

          /* for (i = 0; i < numtextures; i++)
              if (textures[i].name.compareToIgnoreCase(name) == 0)
                  return i;

          return -1; */
    }

    /**
     * R_TextureNumForName
     * Calls R_CheckTextureNumForName,
     * aborts with error message.
     */

    public int TextureNumForName(String name)
    {
        int i;

        i = CheckTextureNumForName(name);

        if (i == -1)
        {
            I.Error("R_TextureNumForName: %s not found", name);
        }
        return i;
    }

    /**
     * R_InitTextures
     * Initializes the texture list
     * with the textures from the world map.
     */

    public void InitTextures()  
    {
        // This drives the rest
        var mtexture = new maptexture_t();
        texture_t texture;
        mappatch_t[] mpatch;
        texpatch_t[] patch;
        var maptex = new MemoryStream[texturelumps.Length];
        int[] patchlookup;
        int totalwidth;
        int offset;
        var maxoff = new int[texturelumps.Length];
        var _numtextures = new int[texturelumps.Length];
        var directory = 1;
        var texset = TEXTURE1;
        // Load the patch names from pnames.lmp.
        //name[8] = 0;
        patchlookup = loadPatchNames("PNAMES");

        // Load the map texture definitions from textures.lmp.
        // The data is contained in one or two lumps,
        //  TEXTURE1 for shareware, plus TEXTURE2 for commercial.

        for (var i = 0; i < texturelumps.Length; i++)
        {
            var TEXTUREx = texturelumps[i];
            if (W.CheckNumForName(TEXTUREx) != -1)
            {
                maptex[i] = W.CacheLumpName(TEXTUREx, PU_STATIC).getBuffer();
                maptex[i].rewind();
                maptex[i].order(ByteOrder.LITTLE_ENDIAN);
                _numtextures[i] = maptex[i].getInt();
                maxoff[i] = W.Lum.Length(W.GetNumForName(TEXTUREx));
            }
        }

        // Total number of textures.
        numtextures = _numtextures[0] + _numtextures[1];

        textures = new texture_t[numtextures];
        // MAES: Texture hashtable.
        TextureCache = new Hashtable<String, int.(numtextures);

        texturecolumnlump = new short[numtextures][];
        texturecolumnofs = new char[numtextures][];
        patchcomposite = new patch_t[numtextures];
        texturecomposite = new byte[numtextures][][];
        texturecompositesize = new int[numtextures];
        texturewidthmask = new int[numtextures];
        textureheight = new int[numtextures];

        totalwidth = 0;

        //  Really complex printing shit...
        System.out.print("[");
        for (var i = 0; i < numtextures; i++, directory++)
        {

            if ((i & 63) == 0)
                System.out.print('.');

            if (i == _numtextures[TEXTURE1])
            {
                // Start looking in second texture file.
                texset = TEXTURE2;
                directory = 1; // offset "1" inside maptex buffer
                //System.err.print("Starting looking into TEXTURE2\n");
            }

            offset = maptex[texset].getInt(directory << 2);

            if (offset > maxoff[texset])
                I.Error("R_InitTextures: bad texture directory");

            maptex[texset].position(offset);
            // Read "maptexture", which is the on-disk form.
            mtexture.unpack(maptex[texset]);

            // MAES: the HashTable only needs to know the correct names.
            TextureCache.put(mtexture.name.toUpperCase(), i);

            // We don't need to manually copy trivial fields.
            textures[i] = new texture_t();
            textures[i].copyFromMapTexture(mtexture);
            texture = textures[i];

            // However we do need to correct the "patch.patch" field through the patchlookup
            mpatch = mtexture.patches;
            patch = texture.patches;

            for (var j = 0; j < texture.patchcount; j++)
            {
                //System.err.printf("Texture %d name %s patch %d lookup %d\n",i,mtexture.name,j,mpatch[j].patch);
                patch[j].patch = patchlookup[mpatch[j].patch];
                if (patch[j].patch == -1)
                {
                    I.Error("R_InitTextures: Missing patch in texture %s",
                            texture.name);
                }
            }

            // Columns and offsets of taxture = textures[i]
            texturecolumnlump[i] = new short[texture.width];
            //C2JUtils.initArrayOfObjects( texturecolumnlump[i], column_t.class);
            texturecolumnofs[i] = new char[texture.width];

            var j = 1;
            while (j * 2 <= texture.width)
            {
                j <<= 1;
            }

            texturewidthmask[i] = j - 1;
            textureheight[i] = texture.height << FRACBITS;

            totalwidth += texture.width;
        }

        // Precalculate whatever possible.
        for (var i = 0; i < numtextures; i++)
        {
            GenerateLookup(i);
        }

        // Create translation table for global animation.
        texturetranslation = new int[numtextures];

        for (var i = 0; i < numtextures; i++)
        {
            texturetranslation[i] = i;
        }
    }

    /**
     * Assigns proper lumpnum to patch names. Check whether flats and patches of the same name coexist.
     * If yes, priority should go to patches. Otherwise, it's a "flats on walls" case.
     *
     * @param pnames
     * @return
     * @ 
     */

    private int[] loadPatchNames(String pnames)  
    {
        int[] patchlookup;
        int nummappatches;
        String name;

        var names = W.CacheLumpName(pnames, PU_STATIC).getBuffer();
        names.order(ByteOrder.LITTLE_ENDIAN);

        // Number of patches.
        names.rewind();
        nummappatches = names.getInt();
        patchlookup = new int[nummappatches];

        for (var i = 0; i < nummappatches; i++)
        {
            // Get a size limited string;
            name = DoomBuffer.getNullTerminatedString(names, 8).toUpperCase();

            // Resolve clashes
            var stuff = W.CheckNumsForName(name);

            // Move backwards.
            for (var k = 0; k < stuff.Length; k++)
            {

                // Prefer non-flat, with priority
                if (W.GetLumpInfo(stuff[k]).namespace != li_namespace.ns_flats)
                {
                    patchlookup[i] = stuff[k];
                    break;
                }

                // Suck it down :-/
                patchlookup[i] = stuff[k];
            }
        }

        return patchlookup;
    }

    private patch_t retrievePatchSafe(int lump)
    {

        // If this is a known troublesome lump, get it from the cache.
        if (FlatPatchCache.containsKey(lump))
        {
            return FlatPatchCache.get(lump);
        }

        var info = W.GetLumpInfo(lump);
        patch_t realpatch;

        // Patch is actually a flat or something equally nasty. Ouch.
        if (info.namespace == li_namespace.ns_flats)
        {
            var flat = W.CacheLumpNumAsRawBytes(lump, PU_CACHE);
            realpatch = MultiPatchSynthesizer.synthesizePatchFromFlat(info.name, flat, 64, 64);
            FlatPatchCache.put(lump, realpatch);
            W.UnlockLumpNum(lump);
        } else
            // It's probably safe, at this point.
            realpatch = W.CacheLumpNum(lump, PU_CACHE, patch_t.class);

        return realpatch;
    }

    /**
     * R_GenerateLookup
     * <p>
     * Creates the lookup tables for a given texture (aka, where inside the texture cache
     * is the offset for particular column... I think.
     *
     * @ 
     */
    
    public void GenerateLookup(int texnum)  
    {
        texture_t texture;
        short[] patchcount; //Keeps track of how many patches overlap a column.
        texpatch_t[] patch;
        patch_t realpatch = null;
        int x;
        int x1;
        int x2;

        short[] collump;
        char[] colofs;

        texture = textures[texnum];

        // Composited texture not created yet.
        texturecomposite[texnum] = null;

        // We don't know ho large the texture will be, yet, but it will be a multiple of its height.
        texturecompositesize[texnum] = 0;

        // This is the only place where those can be actually modified.
        // They are still null at this point.
        collump = texturecolumnlump[texnum];
        colofs = texturecolumnofs[texnum];

        /* Now count the number of columns  that are covered by more
         * than one patch. Fill in the lump / offset, so columns
         * with only a single patch are all done.
         */

        patchcount = new short[texture.width];
        patch = texture.patches;

        // for each patch in a texture...
        for (var i = 0; i < texture.patchcount; i++)
        {
            // Retrieve patch...if it IS a patch.
            realpatch = retrievePatchSafe(patch[i].patch);

            x1 = patch[i].originx;
            x2 = x1 + realpatch.width;

            // Where does the patch start, inside the compositetexture?
            if (x1 < 0)
                x = 0;
            else
                x = x1;

            // Correct, starts at originx. Where does it end?

            if (x2 > texture.width)
                x2 = texture.width;
            for (; x < x2; x++)
            {
                /* Obviously, if a patch starts at x it does cover the x-th column
                 *  of a texture, even if transparent.
                 */
                patchcount[x]++;
                // Column "x" of composite texture "texnum" is covered by this patch.
                collump[x] = (short) patch[i].patch;

                /* This is supposed to be a raw pointer to the beginning of the column
                 * data, as it appears inside the PATCH.
                 *
                 * Instead, we can return the actual column index (x-x1)
                 * As an example, the second patch of STARTAN1 (width 64) starts
                 * at column 32. Therefore colofs should be something like
                 * 0,1,2,...,31,0,1,....31, indicating that the 32-th column of
                 * STARTAN1 is the 0-th column of the patch that is assigned to that column
                 * (the latter can be looked up in texturecolumnlump[texnum].
                 *
                 * Any questions?
                 *
                 */
                colofs[x] = (char) (x - x1);
                // This implies that colofs[x] is 0 for a void column?

            } // end column of patch.
        } // end patch

        // Now check all columns again.
        for (x = 0; x < texture.width; x++)
        {
            // Can only occur if a column isn't covered by a patch at all, not even a transparent one.
            if (patchcount[x] == 0)
            {
                // TODO: somehow handle this.
                System.err.print(realpatch.width);
                System.err.print("R_GenerateLookup: column without a patch (" + texture.name + ")\n");
                //return;
            }
            // I_Error ("R_GenerateLookup: column without a patch");


            // Columns where more than one patch overlaps.
            if (patchcount[x] > 1)
            {
                // Use the cached block. This column won't be read from the wad system.
                collump[x] = -1;
                colofs[x] = (char) texturecompositesize[texnum];

            /* Do we really mind?
            if (texturecompositesize[texnum] > 0x10000-texture.height)
            {
            I.Error ("R_GenerateLookup: texture no %d (%s) is >64k",
                 texnum,textures[texnum].name);
            } */

                texturecompositesize[texnum] += texture.height;
            }
        }
    }

    /**
     * R_GenerateComposite
     * Using the texture definition, the composite texture is created
     * from the patches and each column is cached. This method is "lazy"
     * aka it's only called when a cached/composite texture is needed.
     *
     * @param texnum
     */

    public void GenerateComposite(int texnum)
    {
        byte[][] block;
        texture_t texture;
        texpatch_t[] patch;
        patch_t realpatch = null;
        int x;
        int x1;
        int x2;
        column_t patchcol;
        short[] collump;
        char[] colofs; // unsigned short
        // short[] colidxs; // unsigned short

        texture = textures[texnum];

        // BOth allocate the composite texture, and assign it to block.
        // texturecompositesize indicates a size in BYTES. We need a number of columns, though.
        // Now block is divided into columns. We need to allocate enough data for each column

        block = texturecomposite[texnum] = new byte[texture.width][texture.height];

        // Lump where a certain column will be read from (actually, a patch)
        collump = texturecolumnlump[texnum];

        // Offset of said column into the patch.
        colofs = texturecolumnofs[texnum];

        // colidxs = texturecolumnindexes[texnum];

        // Composite the columns together.
        patch = texture.patches;

        // For each patch in the texture...
        for (var i = 0; i < texture.patchcount; i++)
        {
            // Retrieve patch...if it IS a patch.
            realpatch = retrievePatchSafe(patch[i].patch);

            x1 = patch[i].originx;
            x2 = x1 + realpatch.width;

            if (x1 < 0)
                x = 0;
            else
                x = x1;

            if (x2 > texture.width)
                x2 = texture.width;

            for (; x < x2; x++)
            {
                // Column does not have multiple patches?
                if (collump[x] >= 0)
                    continue;

                // patchcol = (column_t *)((byte *)realpatch
                //            + LONG(realpatch.columnofs[x-x1]));


                // We can look this up cleanly in Java. Ha!
                patchcol = realpatch.columns[x - x1];
                DrawColumnInCache(patchcol,
                        block[x], colofs[x],
                        patch[i].originy,
                        texture.height);
            }

        }
    }

    /**
     * R_GenerateMaskedComposite
     * <p>
     * Generates a "masked composite texture": the result is a MASKED texture
     * (with see-thru holes), but this time  multiple patches can be used to
     * assemble it, unlike standard Doom where this is not allowed.
     * <p>
     * Called only if a request for a texture in the general purpose GetColumn
     * method (used only for masked renders) turns out not to be pointing to a standard
     * cached texture, nor to a disk lump(which is the standard Doom way of indicating a
     * composite single patch texture) but to a cached one which, however, is composite.
     * <p>
     * Confusing, huh?
     * <p>
     * Normally, this results in a disaster, as the masked rendering methods
     * don't expect cached/composite textures at all, and you get all sorts of nasty
     * tutti frutti and medusa effects. Not anymore ;-)
     *
     * @param texnum
     */

    
    public void GenerateMaskedComposite(int texnum)
    {
        byte[][] block;
        bool[][] pixmap; // Solidity map
        texture_t texture;
        texpatch_t[] patch;
        patch_t realpatch = null;
        int x;
        int x1;
        int x2;
        column_t patchcol;
        short[] collump;
        char[] colofs; // unsigned short

        texture = textures[texnum];

        // MAES: we don't want to save a solid block this time. Will only use
        // it for synthesis.

        block = new byte[texture.width][texture.height];
        pixmap = new bool[texture.width][texture.height]; // True values = solid

        // Lump where a certain column will be read from (actually, a patch)
        collump = texturecolumnlump[texnum];

        // Offset of said column into the patch.
        colofs = texturecolumnofs[texnum];

        // Composite the columns together.
        patch = texture.patches;

        // For each patch in the texture...
        for (var i = 0; i < texture.patchcount; i++)
        {

            realpatch = W.CachePatchNum(patch[i].patch);
            x1 = patch[i].originx;
            x2 = x1 + realpatch.width;

            if (x1 < 0)
                x = 0;
            else
                x = x1;

            if (x2 > texture.width)
                x2 = texture.width;

            for (; x < x2; x++)
            {
                // Column does not have multiple patches?
                if (collump[x] >= 0)
                    continue;

                // patchcol = (column_t *)((byte *)realpatch
                // + LONG(realpatch.columnofs[x-x1]));

                // We can look this up cleanly in Java. Ha!
                patchcol = realpatch.columns[x - x1];
                DrawColumnInCache(patchcol, block[x], pixmap[x], colofs[x],
                        patch[i].originy, texture.height);
            }

        }

        // Patch drawn on cache, synthesize patch_t using it.
        patchcomposite[texnum] = MultiPatchSynthesizer.synthesize(CheckTextureNameForNum(texnum), block, pixmap, texture.width, texture.height);
    }

    /**
     * R_DrawColumnInCache
     * Clip and draw a column from a patch into a cached post.
     * <p>
     * This means that columns are effectively "uncompressed" into cache, here,
     * and that composite textures are generally uncompressed...right?
     * <p>
     * Actually: "compressed" or "masked" textures are retrieved in the same way.
     * There are both "masked" and "unmasked" drawing methods. If a masked
     * column is passed to a method that expects a full, dense column...well,
     * it will look fugly/overflow/crash. Vanilla Doom tolerated this,
     * we're probably going to have more problems.
     *
     * @param patch       Actually it's a single column to be drawn. May overdraw existing ones or void space.
     * @param cache       the column cache itself. Actually it's the third level [texture][column]->data.
     * @param offset      an offset inside the column cache (UNUSED)
     * @param originy     vertical offset. Caution with masked stuff!
     * @param cacheheight the maximum height it's supposed to reach when drawing?
     */

    private void DrawColumnInCache(column_t patch, byte[] cache, int offset,
                                   int originy, int cacheheight)
    {
        int count;
        int position;
        var source = 0; // treat as pointers

        /*
         * Iterate inside column. This is starkly different from the C code,
         * because post positions AND offsets are already precomputed at load
         * time
         */

        for (var i = 0; i < patch.posts; i++)
        {

            // This should position us at the beginning of the next post
            source = patch.postofs[i];

            count = patch.postlen[i]; //.Length of this particular post
            position = originy + patch.postdeltas[i]; // Position to draw inside
            // cache.

            // Post starts outside of texture's bounds. Adjust offset.

            if (position < 0)
            {
                count += position; // Consider that we have a "drawing debt".
                position = 0;
            }

            // Post will go too far outside.
            if (position + count > cacheheight)
                count = cacheheight - position;

            if (count > 0) // Draw this post. Won't draw posts that start
                // "outside"
                // Will start at post's start, but will only draw enough pixels
                // not to overdraw.
                System.arraycopy(patch.data, source, cache, position, count);

        }
    }

    // Version also drawing on a supplied transparency map
    private void DrawColumnInCache(column_t patch, byte[] cache,
                                   bool[] pixmap, int offset, int originy, int cacheheight)
    {
        int count;
        int position;
        var source = 0; // treat as pointers

        /*
         * Iterate inside column. This is starkly different from the C code,
         * because post positions AND offsets are already precomputed at load
         * time
         */

        for (var i = 0; i < patch.posts; i++)
        {

            // This should position us at the beginning of the next post
            source = patch.postofs[i];

            count = patch.postlen[i]; //.Length of this particular post
            position = originy + patch.postdeltas[i]; // Position to draw inside
            // cache.

            // Post starts outside of texture's bounds. Adjust offset.

            if (position < 0)
            {
                count += position; // Consider that we have a "drawing debt".
                position = 0;
            }

            // Post will go too far outside.
            if (position + count > cacheheight)
                count = cacheheight - position;

            if (count > 0)
            {
                // Draw post, AND fill solidity map
                System.arraycopy(patch.data, source, cache, position, count);
                Arrays.fill(pixmap, position, position + count, true);
            }
            // Repeat for next post(s), if any.
        }
    }

    /**
     * R_InitFlats
     * <p>
     * Scans WADs for F_START/F_END lumps, and also any additional
     * F1_ and F2_ pairs.
     * <p>
     * Correct behavior would be to detect F_START/F_END lumps,
     * and skip any marker lumps sandwiched in between. If F_START and F_END are external,
     * use external override.
     * <p>
     * Also, in the presence of external FF_START lumps, merge their contents
     * with those previously read.
     * <p>
     * The method is COMPATIBLE with resource pre-coalesing, however it's not
     * trivial to change back to the naive code because of the "translationless"
     * system used (all flats are assumed to lie in a linear space). This
     * speeds up lookups.
     */

    
    public  void InitFlats()
    {
        numflats = 0;
        var extendedflatstart = -1;
        firstflat = W.GetNumForName(LUMPSTART); // This is the start of normal lumps.
        if (FlatCache == null) FlatCache = new Hashtable<>();
        else FlatCache.clear();
        var FlatNames = new Hashtable<String, int.(); // Store names here.

        // Normally, if we don't use Boom features, we could look for F_END and that's it.
        // However we want to avoid using flat translation and instead store absolute lump numbers.
        // So we need to do a clean parse.

        // The rule is: we scan from the very first F_START to the very first F_END.
        // We discard markers, and only assign sequential numbers to valid lumps.
        // These are the vanilla flats, and will work with fully merged PWADs too.

        // Normally, this marks the end of regular lumps. However, if DEUTEX extension
        // are present, it will actually mark the end of the extensions due to lump
        // priority, so its usefulness as an absolute end-index for regular flats
        // is dodgy at best. Gotta love the inconsistent mundo hacks!

        //int lastflatlump=W.GetNumForName(LUMPEND);

        //
        var lump = firstflat;
        var seq = 0;
        String name;
        while (!(name = W.GetNameForNum(lump)).equalsIgnoreCase(LUMPEND))
        {
            if (!W.isLumpMarker(lump))
            {
                // Not a marker. Put in cache.
                FlatCache.put(lump, seq);
                // Save its name too.
                FlatNames.put(name, lump);
                seq++; // Advance sequence
                numflats++; // Total flats do increase
            }
            lump++; // Advance lump.
        }


        extendedflatstart = W.CheckNumForName(DEUTEX_START); // This is the start of DEUTEX flats.
        if (extendedflatstart > -1)
        {
            // If extended ones are present, then Advance slowly.
            lump = extendedflatstart;

            // Safeguard: FF_START without corresponding F_END (e.g. in helltest.wad)

            name = W.GetNameForNum(lump);

            // The end of those extended flats is also marked by F_END or FF_END, as noted above.
            // It can also be non-existent in some broken maps like helltest.wad. Jesus.
            while (!(name == null || name.equalsIgnoreCase(LUMPEND) || name.equalsIgnoreCase(DEUTEX_END)))
            {
                if (!W.isLumpMarker(lump))
                {
                    // Not a marker. Check if it's supposed to replace something.
                    if (FlatNames.containsKey(name))
                    {
                        // Well, it is. Off with its name, save the lump number though.
                        int removed = FlatNames.remove(name);
                        // Put new name in list
                        FlatNames.put(name, lump);
                        // Remove old lump, but keep sequence.
                        int oldseq = FlatCache.remove(removed);
                        // Put new lump number with old sequence.
                        FlatCache.put(lump, oldseq);
                    } else
                    {  // Add normally
                        FlatCache.put(lump, seq);
                        // Save its name too.
                        FlatNames.put(name, lump);
                        seq++; // Advance sequence
                        numflats++; // Total flats do increase
                    }
                }
                lump++; // Advance lump.
                name = W.GetNameForNum(lump);
            }
        }

        // So now we have a lump -> sequence number mapping.

        // Create translation table for global animation.
        flattranslation = new int[numflats];
        flatstorage = new int[numflats];

        // MAJOR CHANGE: flattranslation stores absolute lump numbers. Adding
        // firstlump is not necessary anymore.
        // Now, we're pretty sure that we have a progressive value mapping.

        var stuff = FlatCache.keys();
        while (stuff.hasMoreElements())
        {
            int nextlump = stuff.nextElement();
            flatstorage[FlatCache.get(nextlump)] = nextlump;
            // Lump is used as the key, while the relative lump number is the value.
            //FlatCache.put(j, k-1);
        }

        for (var i = 0; i < numflats; i++)
        {
            flattranslation[i] = i;
            //  System.out.printf("Verification: flat[%d] is %s in lump %d\n",i,W.GetNameForNum(flattranslation[i]),flatstorage[i]);
        }
    }

    
    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @R_Data.C(R_PrecacheLevel)
    public void PrecacheLevel()  
    {
        preCacheFlats();
        preCacheTextures();
    }

    private void preCacheFlats()
    {
        bool[] flatpresent;
        int lump;


        if (DM.demoplayback)
            return;

        // Precache flats.
        flatpresent = new bool[numflats];
        flats = new flat_t[numflats];

        for (var i = 0; i < LL.numsectors; i++)
        {
            flatpresent[LL.sectors[i].floorpic] = true;
            flatpresent[LL.sectors[i].ceilingpic] = true;
        }

        flatmemory = 0;

        for (var i = 0; i < numflats; i++)
        {
            if (flatpresent[i])
            {
                lump = firstflat + i;
                flatmemory += W.GetLumpInfo(lump).size;
                flats[i] = W.CacheLumpNum(lump, PU_CACHE, flat_t.class);
            }
        }
    }

    private void preCacheTextures()
    {
        bool[] texturepresent;
        texture_t texture;
        int lump;

        // Precache textures.
        texturepresent = new bool[numtextures];

        for (var i = 0; i < LL.numsides; i++)
        {
            texturepresent[LL.sides[i].toptexture] = true;
            texturepresent[LL.sides[i].midtexture] = true;
            texturepresent[LL.sides[i].bottomtexture] = true;
        }

        // Sky texture is always present.
        // Note that F_SKY1 is the name used to
        //  indicate a sky floor/ceiling as a flat,
        //  while the sky texture is stored like
        //  a wall texture, with an episode dependend
        //  name.
        texturepresent[skytexture] = true;

        texturememory = 0;
        for (var i = 0; i < numtextures; i++)
        {
            if (!texturepresent[i])
                continue;

            texture = textures[i];

            for (var j = 0; j < texture.patchcount; j++)
            {
                lump = texture.patches[j].patch;
                texturememory += W.GetLumpInfo(lump).size;
                W.CacheLumpNum(lump, PU_CACHE, patch_t.class);
            }
        }
    }


    //////////////////////////////////From r_sky.c /////////////////////////////////////

//////////////////////////////////From r_sky.c /////////////////////////////////////

    /**
     * R_FlatNumForName
     * Retrieval, get a flat number for a flat name.
     * <p>
     * Unlike the texture one, this one is not used frequently. Go figure.
     */

    
    public  int FlatNumForName(String name)
    {
        int i;
        i = W.CheckNumForName(name);

        if (i == -1)
        {
            I.Error("R_FlatNumForName: %s not found", name);
        }

        return FlatCache.get(i);

    }

    
    public  int getTextureColumnLump(int tex, int col)
    {
        return texturecolumnlump[tex][col];
    }

    
    public  char getTextureColumnOfs(int tex, int col)
    {
        return texturecolumnofs[tex][col];
    }

    
    public  int getTexturewidthmask(int tex)
    {
        return texturewidthmask[tex];
    }

    
    public  byte[][] getTextureComposite(int tex)
    {
        return texturecomposite[tex];
    }

    
    public  byte[] getTextureComposite(int tex, int col)
    {
        return texturecomposite[tex][col];
    }

    
    public  patch_t getMaskedComposite(int tex)
    {
        return patchcomposite[tex];
    }

    
    public  int getTextureheight(int texnum)
    {
        return textureheight[texnum];
    }

    
    public  int getTextureTranslation(int texnum)
    {
        return texturetranslation[texnum];
    }

    /**
     * Returns a flat after it has been modified by the translation table e.g. by animations
     */
    
    public int getFlatTranslation(int flatnum)
    {
        return flatstorage[flattranslation[flatnum]];
    }

    
    public  void setTextureTranslation(int texnum, int amount)
    {
        texturetranslation[texnum] = amount;
    }

    /**
     * This affects ONLY THE TRANSLATION TABLE, not the lump storage.
     */

    
    public  void setFlatTranslation(int flatnum, int amount)
    {
        flattranslation[flatnum] = amount;
    }

    /**
     * R_InitSkyMap
     * Called whenever the view size changes.
     */

    public int InitSkyMap()
    {
        skyflatnum = FlatNumForName(SKYFLATNAME);
        skytexturemid = 100 * FRACUNIT;
        return skyflatnum;
    }

    
    public int getSkyFlatNum()
    {
        return skyflatnum;
    }

    
    public void setSkyFlatNum(int skyflatnum)
    {
        this.skyflatnum = skyflatnum;
    }

    
    public int getSkyTexture()
    {
        return skytexture;
    }

    // COLUMN GETTING METHODS. No idea why those had to be in the renderer...

    
    public void setSkyTexture(int skytexture)
    {
        this.skytexture = skytexture;
    }

    
    public int getSkyTextureMid()
    {
        return skytexturemid;
    }

    
    public String CheckTextureNameForNum(int texnum)
    {
        return textures[texnum].name;
    }

    
    public int getFlatLumpNum(int flatnum)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Generates a "cached" masked column against a black background.
     * Synchronized so concurrency issues won't cause random glitching and
     * errors.
     *
     * @param lump
     * @param column
     * @return raw, 0-pointed column data.
     */
    public synchronized byte[] getRogueColumn(int lump, int column)
    {
        // Not contained? Generate.
        if (!roguePatches.containsKey(lump))
            roguePatches.put(lump, generateRoguePatch(lump));

        lastrogue = lump;
        rogue = roguePatches.get(lump);

        return rogue[column];
    }

///////////////////////// TEXTURE MANAGEMENT /////////////////////////

    /**
     * Actually generates a tutti-frutti-safe cached patch out of
     * a masked or unmasked single-patch lump.
     *
     * @param lump
     * @return
     */

    private byte[][] generateRoguePatch(int lump)
    {
        // Retrieve patch...if it IS a patch.
        var p = retrievePatchSafe(lump);

        // Allocate space for a cached block.
        var block = new byte[p.width][p.height];


        for (var i = 0; i < p.width; i++)
        {
            DrawColumnInCache(p.columns[i], block[i], i, 0, p.height);
        }

        // Don't keep this twice in memory.
        W.UnlockLumpNum(lump);
        return block;
    }

    
    public byte[] getSafeFlat(int flatnum)
    {
        var flat = W.CacheLumpNum(getFlatTranslation(flatnum),
                PU_STATIC, flat_t.class).data;

        if (flat.Length < 4096)
        {
            System.arraycopy(flat, 0, safepatch, 0, flat.Length);
            return safepatch;
        }

        return flat;
    }

    /**
     * Special version of GetColumn meant to be called concurrently by different
     * (MASKED) seg rendering threads, identfiex by index. This serves to avoid stomping
     * on mutual cached textures and causing crashes.
     * <p>
     * Returns column_t, so in theory it could be made data-agnostic.
     */

    public column_t GetSmpColumn(int tex, int col, int id)
    {
        int lump, ofs;

        col &= getTexturewidthmask(tex);
        lump = getTextureColumnLump(tex, col);
        ofs = getTextureColumnOfs(tex, col);

        // It's always 0 for this kind of access.

        // Speed-increasing trick: speed up repeated accesses to the same
        // texture or patch, if they come from the same lump

        if (tex == smp_lasttex[id] && lump == smp_lastlump[id])
        {
            if (composite)
                return smp_lastpatch[id].columns[col];
            else
                return smp_lastpatch[id].columns[ofs];
        }

        // If pointing inside a non-zero, positive lump, then it's not a
        // composite texture. Read it from disk.
        if (lump > 0)
        {
            // This will actually return a pointer to a patch's columns.
            // That is, to the ONE column exactly.{
            // If the caller needs access to a raw column, we must point 3 bytes
            // "ahead".
            smp_lastpatch[id] = W.CachePatchNum(lump);
            smp_lasttex[id] = tex;
            smp_lastlump[id] = lump;
            smp_composite[id] = false;
            // If the column was a disk lump, use ofs.
            return smp_lastpatch[id].columns[ofs];
        }

        // Problem. Composite texture requested as if it was masked
        // but it doesn't yet exist. Create it.
        if (getMaskedComposite(tex) == null)
        {
            System.err.printf("Forced generation of composite %s\n", CheckTextureNameForNum(tex), smp_composite[id], col, ofs);
            GenerateMaskedComposite(tex);
            System.err.printf("Composite patch %s %d\n", getMaskedComposite(tex).name, getMaskedComposite(tex).columns.Length);
        }

        // Last resort.
        smp_lastpatch[id] = getMaskedComposite(tex);
        smp_lasttex[id] = tex;
        smp_composite[id] = true;
        smp_lastlump[id] = 0;

        return lastpatch.columns[col];
    }

    /**
     * R_GetColumn original version: returns raw pointers to byte-based column
     * data. Works for both masked and unmasked columns, but is not
     * tutti-frutti-safe.
     * <p>
     * Use GetCachedColumn instead, if rendering non-masked stuff, which is also
     * faster.
     */
    public byte[] GetColumn(int tex, int col)
    {
        int lump, ofs;

        col &= getTexturewidthmask(tex);
        lump = getTextureColumnLump(tex, col);
        ofs = getTextureColumnOfs(tex, col);

        // It's always 0 for this kind of access.

        // Speed-increasing trick: speed up repeated accesses to the same
        // texture or patch, if they come from the same lump

        if (tex == lasttex && lump == lastlump)
        {
            if (composite)
                return lastpatch.columns[col].data;
            else
                return lastpatch.columns[ofs].data;
        }

        // If pointing inside a non-zero, positive lump, then it's not a
        // composite texture. Read it from disk.
        if (lump > 0)
        {
            // This will actually return a pointer to a patch's columns.
            // That is, to the ONE column exactly.{
            // If the caller needs access to a raw column, we must point 3 bytes
            // "ahead".
            lastpatch = W.CachePatchNum(lump);
            lasttex = tex;
            lastlump = lump;
            composite = false;
            // If the column was a disk lump, use ofs.
            return lastpatch.columns[ofs].data;
        }

        // Problem. Composite texture requested as if it was masked
        // but it doesn't yet exist. Create it.
        if (getMaskedComposite(tex) == null)
        {
            System.err.printf("Forced generation of composite %s\n", CheckTextureNameForNum(tex), composite, col, ofs);
            GenerateMaskedComposite(tex);
            System.err.printf("Composite patch %s %d\n", getMaskedComposite(tex).name, getMaskedComposite(tex).columns.Length);
        }

        // Last resort.
        lastpatch = getMaskedComposite(tex);
        lasttex = tex;
        composite = true;
        lastlump = 0;

        return lastpatch.columns[col].data;
    }

    /**
     * R_GetColumnStruct: returns actual pointers to columns.
     * Agnostic of the underlying type.
     * <p>
     * Works for both masked and unmasked columns, but is not
     * tutti-frutti-safe.
     * <p>
     * Use GetCachedColumn instead, if rendering non-masked stuff, which is also
     * faster.
     */
    
    public column_t GetColumnStruct(int tex, int col)
    {
        int lump, ofs;

        col &= getTexturewidthmask(tex);
        lump = getTextureColumnLump(tex, col);
        ofs = getTextureColumnOfs(tex, col);

        // Speed-increasing trick: speed up repeated accesses to the same
        // texture or patch, if they come from the same lump

        if (tex == lasttex && lump == lastlump)
        {
            if (composite)
                return lastpatch.columns[col];
            else
                return lastpatch.columns[ofs];
        }

        // If pointing inside a non-zero, positive lump, then it's not a
        // composite texture. Read it from disk.
        if (lump > 0)
        {
            // This will actually return a pointer to a patch's columns.
            // That is, to the ONE column exactly.{
            // If the caller needs access to a raw column, we must point 3 bytes
            // "ahead".
            lastpatch = W.CachePatchNum(lump);
            lasttex = tex;
            lastlump = lump;
            composite = false;
            // If the column was a disk lump, use ofs.
            return lastpatch.columns[ofs];
        }

        // Problem. Composite texture requested as if it was masked
        // but it doesn't yet exist. Create it.
        if (getMaskedComposite(tex) == null)
        {
            System.err.printf("Forced generation of composite %s\n", CheckTextureNameForNum(tex), composite, col, ofs);
            GenerateMaskedComposite(tex);
            System.err.printf("Composite patch %s %d\n", getMaskedComposite(tex).name, getMaskedComposite(tex).columns.Length);
        }

        // Last resort.
        lastpatch = getMaskedComposite(tex);
        lasttex = tex;
        composite = true;
        lastlump = 0;

        return lastpatch.columns[col];
    }

    /**
     * R_GetColumn variation which is tutti-frutti proof. It only returns cached
     * columns, and even pre-caches single-patch textures intead of trashing the
     * WAD manager (should be faster, in theory).
     * <p>
     * Cannot be used for drawing masked textures, use classic GetColumn
     * instead.
     *
     * @ 
     */
    
    public  byte[] GetCachedColumn(int tex, int col)
    {
        int lump, ofs;

        col &= getTexturewidthmask(tex);
        lump = getTextureColumnLump(tex, col);
        ofs = getTextureColumnOfs(tex, col);

        // In the case of cached columns, this is always 0.
        // Done externally, for now.
        //dcvars.dc_source_ofs = 0;

        // If pointing inside a non-zero, positive lump, then it's not a
        // composite texture.
        // Read from disk, and safeguard vs tutti frutti.
        if (lump > 0)
        {
            // This will actually return a pointer to a patch's columns.
            return getRogueColumn(lump, ofs);
        }

        // Texture should be composite, but it doesn't yet exist. Create it.
        if (getTextureComposite(tex) == null)
            GenerateComposite(tex);

        return getTextureComposite(tex, col);
    }

    
    public void setSMPVars(int num_threads)
    {
        smp_composite = new bool[num_threads];// = false;
        smp_lasttex = new int[num_threads];// = -1;
        smp_lastlump = new int[num_threads];// = -1;
        smp_lastpatch = new patch_t[num_threads];// = null;
    }
}