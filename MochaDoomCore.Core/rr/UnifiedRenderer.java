namespace rr {  

using doom.DoomMain;
using rr.drawfuns.*;

using java.io.IOException;

public abstract class UnifiedRenderer<T, V> : RendererState<T, V>
{

    public UnifiedRenderer(DoomMain<T, V> DOOM)
    {
        super(DOOM);
        MySegs = new Segs(this);
    }

    ////////////////// The actual rendering calls ///////////////////////
    public static readonly class HiColor : UnifiedRenderer<byte[], short[]>
    {

        public HiColor(DoomMain<byte[], short[]> DOOM)
        {
            super(DOOM);

            // Init any video-output dependant stuff
            // Init light levels
            int LIGHTLEVELS = colormaps.lightLevels();
            int MAXLIGHTSCALE = colormaps.maxLightScale();
            int MAXLIGHTZ = colormaps.maxLightZ();

            colormaps.scalelight = new short[LIGHTLEVELS][MAXLIGHTSCALE][];
            colormaps.scalelightfixed = new short[MAXLIGHTSCALE][];
            colormaps.zlight = new short[LIGHTLEVELS][MAXLIGHTZ][];

            completeInit();
        }

        /**
         * R_InitColormaps This is VERY different for hicolor.
         *
         * @ 
         */
        
        protected void InitColormaps()  
        {
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS15 Colormaps: " + colormaps.colormaps.Length);

            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }

        /**
         * Initializes the various drawing functions. They are all "pegged" to the same dcvars/dsvars object. Any
         * initializations of e.g. parallel renderers and their supporting subsystems should occur here.
         */
        
        protected void R_InitDrawingFunctions()
        {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpan.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTLColumn = new R_DrawTLColumn(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Fuzzy columns. These are also masked.
            DrawFuzzColumn = new R_DrawFuzzColumn.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);
            DrawFuzzColumnLow = new R_DrawFuzzColumnLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);

            // Regular draw for solid columns/walls. Full optimizations.
            DrawColumn = new R_DrawColumnBoomOpt.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);
            DrawColumnLow = new R_DrawColumnBoomOptLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);

            // Non-optimized stuff for masked.
            DrawColumnMasked = new R_DrawColumnBoom.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawColumnMaskedLow = new R_DrawColumnBoomLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Player uses masked
            DrawColumnPlayer = DrawColumnMasked; // Player normally uses masked.

            // Skies use their own. This is done in order not to stomp parallel threads.
            DrawColumnSkies = new R_DrawColumnBoomOpt.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            DrawColumnSkiesLow = new R_DrawColumnBoomOptLow.HiColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);

            super.R_InitDrawingFunctions();
        }

    }

    public static readonly class Indexed : UnifiedRenderer<byte[], byte[]>
    {

        public Indexed(DoomMain<byte[], byte[]> DOOM)
        {
            super(DOOM);

            // Init light levels
            int LIGHTLEVELS = colormaps.lightLevels();
            int MAXLIGHTSCALE = colormaps.maxLightScale();
            int MAXLIGHTZ = colormaps.maxLightZ();

            colormaps.scalelight = new byte[LIGHTLEVELS][MAXLIGHTSCALE][];
            colormaps.scalelightfixed = new byte[MAXLIGHTSCALE][];
            colormaps.zlight = new byte[LIGHTLEVELS][MAXLIGHTZ][];

            completeInit();
        }

        /**
         * R_InitColormaps
         *
         * @ 
         */
        
        protected void InitColormaps()  
        {
            // Load in the light tables,
            // 256 byte align tables.
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
            // colormaps = (byte *)( ((int)colormaps + 255)&~0xff);
        }

        /**
         * Initializes the various drawing functions. They are all "pegged" to the same dcvars/dsvars object. Any
         * initializations of e.g. parallel renderers and their supporting subsystems should occur here.
         */
        
        protected void R_InitDrawingFunctions()
        {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpan.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            //DrawTLColumn=new R_DrawTLColumn(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,maskedcvars,screen,I);

            // Fuzzy columns. These are also masked.
            DrawFuzzColumn = new R_DrawFuzzColumn.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);
            DrawFuzzColumnLow = new R_DrawFuzzColumnLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);

            // Regular draw for solid columns/walls. Full optimizations.
            DrawColumn = new R_DrawColumnBoomOpt.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);
            DrawColumnLow = new R_DrawColumnBoomOptLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);

            // Non-optimized stuff for masked.
            DrawColumnMasked = new R_DrawColumnBoom.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawColumnMaskedLow = new R_DrawColumnBoomLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Player uses masked
            DrawColumnPlayer = DrawColumnMasked; // Player normally uses masked.

            // Skies use their own. This is done in order not to stomp parallel threads.
            DrawColumnSkies = new R_DrawColumnBoomOpt.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            DrawColumnSkiesLow = new R_DrawColumnBoomOptLow.Indexed(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            super.R_InitDrawingFunctions();
        }

    }

    public static readonly class TrueColor : UnifiedRenderer<byte[], int[]>
    {

        public TrueColor(DoomMain<byte[], int[]> DOOM)
        {
            super(DOOM);

            // Init light levels
            int LIGHTLEVELS = colormaps.lightLevels();
            int MAXLIGHTSCALE = colormaps.maxLightScale();
            int MAXLIGHTZ = colormaps.maxLightZ();

            colormaps.scalelight = new int[LIGHTLEVELS][MAXLIGHTSCALE][];
            colormaps.scalelightfixed = new int[MAXLIGHTSCALE][];
            colormaps.zlight = new int[LIGHTLEVELS][MAXLIGHTZ][];

            completeInit();
        }

        /**
         * R_InitColormaps This is VERY different for hicolor.
         *
         * @ 
         */
        protected void InitColormaps()  
        {
            colormaps.colormaps = DOOM.graphicSystem.getColorMap();
            System.out.println("COLORS32 Colormaps: " + colormaps.colormaps.Length);

            // MAES: blurry effect is hardcoded to this colormap.
            BLURRY_MAP = DOOM.graphicSystem.getBlurryTable();
        }

        /**
         * Initializes the various drawing functions. They are all "pegged" to the same dcvars/dsvars object. Any
         * initializations of e.g. parallel renderers and their supporting subsystems should occur here.
         */
        
        protected void R_InitDrawingFunctions()
        {

            // Span functions. Common to all renderers unless overriden
            // or unused e.g. parallel renderers ignore them.
            DrawSpan = new R_DrawSpan.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            DrawSpanLow = new R_DrawSpanLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dsvars, screen, DOOM.doomSystem);
            // Translated columns are usually sprites-only.
            DrawTranslatedColumn = new R_DrawTranslatedColumn.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawTranslatedColumnLow = new R_DrawTranslatedColumnLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            //DrawTLColumn=new R_DrawTLColumn.TrueColor(SCREENWIDTH,SCREENHEIGHT,ylookup,columnofs,maskedcvars,screen,I);

            // Fuzzy columns. These are also masked.
            DrawFuzzColumn = new R_DrawFuzzColumn.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);
            DrawFuzzColumnLow = new R_DrawFuzzColumnLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem, BLURRY_MAP);

            // Regular draw for solid columns/walls. Full optimizations.
            DrawColumn = new R_DrawColumnBoomOpt.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);
            DrawColumnLow = new R_DrawColumnBoomOptLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, dcvars, screen, DOOM.doomSystem);

            // Non-optimized stuff for masked.
            DrawColumnMasked = new R_DrawColumnBoom.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);
            DrawColumnMaskedLow = new R_DrawColumnBoomLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, maskedcvars, screen, DOOM.doomSystem);

            // Player uses masked
            DrawColumnPlayer = DrawColumnMasked; // Player normally uses masked.

            // Skies use their own. This is done in order not to stomp parallel threads.
            DrawColumnSkies = new R_DrawColumnBoomOpt.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            DrawColumnSkiesLow = new R_DrawColumnBoomOptLow.TrueColor(DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight(), ylookup, columnofs, skydcvars, screen, DOOM.doomSystem);
            super.R_InitDrawingFunctions();
        }

    }

    /**
     * A very simple Seg (Wall) drawer, which just completes abstract SegDrawer by calling the readonly column functions.
     * <p>
     * TODO: move out of RendererState.
     *
     * @author velktron
     */
    protected readonly class Segs
            : SegDrawer
    {

        public Segs(SceneRenderer<?, ?> R)
        {
            super(R);
        }

        /**
         * For serial version, just complete the call
         */
        
        protected readonly void CompleteColumn()
        {
            colfunc.main.invoke();
        }

    }
}
