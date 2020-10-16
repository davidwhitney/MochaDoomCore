namespace rr {  

using doom.thinker_t;

/**
 * Used for special sector-based function for doors, ceilings
 * etc. that are treated as a thinker by the engine. The sector
 * is part of the spec, so extending classes don't need to override
 * it. Also, it : thinker so futher extensions are thinkers too.
 */

public abstract class SectorAction : thinker_t
{

    public sector_t sector;

    /**
     * Special, only used when (un)archiving in order to re-link stuff
     * to their proper sector.
     */
    public int sectorid;
}
