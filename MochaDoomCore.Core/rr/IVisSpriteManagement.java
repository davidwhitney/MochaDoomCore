namespace rr {  

/**
 * A sprite manager does everything but drawing the sprites. It creates lists
 * of sprites-per-sector, sorts them, and stuff like that.
 * that gory visibiliy
 *
 * @param <V>
 * @author velkton
 */


public interface IVisSpriteManagement<V> : ILimitResettable
{

    void AddSprites(sector_t sec);

    /**
     * Cache the sprite manager, if possible
     */

    void cacheSpriteManager(ISpriteManager SM);

    void SortVisSprites();

    int getNumVisSprites();

    vissprite_t<V>[] getVisSprites();

    void ClearSprites();

}
