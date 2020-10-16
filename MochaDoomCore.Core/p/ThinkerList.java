package p;

import doom.SourceCode.P_Tick;
import doom.thinker_t;

import static doom.SourceCode.P_Tick.*;

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
