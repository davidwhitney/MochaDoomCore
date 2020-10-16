namespace p {  

using doom.SourceCode.P_Tick;
using doom.thinker_t;

using static doom.SourceCode.P_Tick.*;

public interface ThinkerList
{

    @C(P_AddThinker)
    void AddThinker(thinker_t thinker);

    @C(P_RemoveThinker)
    void RemoveThinker(thinker_t thinker);

    @C(P_InitThinkers)
    void InitThinkers();

    thinker_t getRandomThinker();

    thinker_t getThinkerCap();
}
