package rr;

import doom.SourceCode.R_Draw;
import doom.player_t;
import i.IDoomSystem;
import rr.drawfuns.ColFuncs;
import rr.drawfuns.ColVars;
import rr.drawfuns.SpanVars;
import v.tables.LightsAndColors;
import w.IWadLoader;

import static data.Tables.FINEANGLES;
import static doom.SourceCode.R_Draw.R_FillBackScreen;
import static m.fixed_t.FRACUNIT;

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

    boolean isFullHeight();

    void resetLimits();

    boolean getSetSizeNeeded();

    boolean isFullScreen();

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
