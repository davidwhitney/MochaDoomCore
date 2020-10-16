package rr.parallel;

using rr.column_t;

/**
 * An interface used to ease the use of the GetCachedColumn by part
 * of parallelized renderers.
 */

/**
 * Special version of GetColumn meant to be called concurrently by different
 * seg rendering threads, identfiex by index. This serves to avoid stomping
 * on mutual cached textures and causing crashes.
 *
 */

public interface IGetSmpColumn
{

    column_t GetSmpColumn(int tex, int col, int id);

}
