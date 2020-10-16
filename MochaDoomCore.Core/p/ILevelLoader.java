package p;

import defines.skill_t;
import doom.SourceCode.P_Setup;
import rr.subsector_t;

import java.io.IOException;

import static doom.SourceCode.P_Setup.P_SetupLevel;

public interface ILevelLoader
{

    // Lump order in a map WAD: each map needs a couple of lumps
    // to provide a complete scene geometry description.

    int ML_LABEL = 0;

    /**
     * A separator, name, ExMx or MAPxx
     */
    int ML_THINGS = 1;

    /**
     * Monsters, items..
     */
    int ML_LINEDEFS = 2;

    /**
     * LineDefs, from editing
     */
    int ML_SIDEDEFS = 3;

    /**
     * SideDefs, from editing
     */
    int ML_VERTEXES = 4;

    /**
     * Vertices, edited and BSP splits generated
     */
    int ML_SEGS = 5;

    /**
     * LineSegs, from LineDefs split by BSP
     */
    int ML_SSECTORS = 6;

    /**
     * SubSectors, list of LineSegs
     */
    int ML_NODES = 7;

    /**
     * BSP nodes
     */
    int ML_SECTORS = 8;

    /**
     * Sectors, from editing
     */
    int ML_REJECT = 9;

    /**
     * LUT, sector-sector visibility
     */
    int ML_BLOCKMAP = 10;

    // Maintain single and multi player starting spots.
    int MAX_DEATHMATCH_STARTS = 10;

    /**
     * Expected lump names for verification
     */
    String[] LABELS = {"MAPNAME", "THINGS", "LINEDEFS", "SIDEDEFS",
            "VERTEXES", "SEGS", "SSECTORS", "NODES",
            "SECTORS", "REJECT", "BLOCKMAP"};

    /**
     * P_SetupLevel
     *
     * @param episode
     * @param map
     * @param playermask
     * @param skill
     * @throws IOException
     */
    @P_Setup.C(P_SetupLevel)
    void SetupLevel(int episode, int map, int playermask, skill_t skill) throws IOException;

    /**
     * P_SetThingPosition Links a thing into both a block and a subsector based
     * on it's x y. Sets thing.subsector properly
     *
     * @param thing
     */
    void SetThingPosition(mobj_t thing);

    /**
     * R_PointInSubsector
     * <p>
     * MAES: it makes more sense to have this here.
     *
     * @param x fixed
     * @param y fixed
     */

    subsector_t PointInSubsector(int x, int y);


}
