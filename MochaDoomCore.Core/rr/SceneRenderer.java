namespace rr {  

using doom.SourceCode.R_Draw;
using doom.player_t;
using i.IDoomSystem;
using rr.drawfuns.ColFuncs;
using rr.drawfuns.ColVars;
using rr.drawfuns.SpanVars;
using v.tables.LightsAndColors;
using w.IWadLoader;

using static data.Tables.FINEANGLES;
using static doom.SourceCode.R_Draw.R_FillBackScreen;
using static m.fixed_t.FRACUNIT;

public interface SceneRenderer<T, V>
{

    /**
     * Fineangles in the SCREENWIDTH wide window.
     */
    int FIELDOFVIEW = FINEANGLES / 4;
    int MINZ = FRACUNIT * 4;
    int FUZZTABLE = 50;

    /**
     * killough: viewangleoffset is a legacy from the pre-v1.2 days, when Doom
     * had Left/Mid/Right viewing. +/-ANG90 offsets were placed here on each
     * node, by d_net.c, to set up a L/M/R session.
     */
    long viewangleoffset = 0;

    void Init();

    void RenderPlayerView(player_t player);

    void ExecuteSetViewSize();

    @R_Draw.C(R_FillBackScreen)
    void FillBackScreen();

    void DrawViewBorder();

    void SetViewSize(int size, int detaillevel);

    long PointToAngle2(int x1, int y1, int x2, int y2);

    void PreCacheThinkers();

    int getValidCount();

    void increaseValidCount(int amount);

    bool isFullHeight();

    void resetLimits();

    bool getSetSizeNeeded();

    bool isFullScreen();

    // Isolation methods
    TextureManager<T> getTextureManager();

    PlaneDrawer<T, V> getPlaneDrawer();

    ViewVars getView();

    SpanVars<T, V> getDSVars();

    LightsAndColors<V> getColorMap();

    IDoomSystem getDoomSystem();

    IWadLoader getWadLoader();

    /**
     * Use this to "peg" visplane drawers (even parallel ones) to
     * the same set of visplane variables.
     *
     * @return
     */
    Visplanes getVPVars();

    SegVars getSegVars();

    ISpriteManager getSpriteManager();

    BSPVars getBSPVars();

    IVisSpriteManagement<V> getVisSpriteManager();

    ColFuncs<T, V> getColFuncsHi();

    ColFuncs<T, V> getColFuncsLow();

    ColVars<T, V> getMaskedDCVars();

    //public subsector_t PointInSubsector(int x, int y);
}
