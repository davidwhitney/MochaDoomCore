package m;

import data.mobjtype_t;
import p.ActiveStates;

public interface IRandom
{
    int P_Random();

    int M_Random();

    void ClearRandom();

    int getIndex();

    int P_Random(int caller);

    int P_Random(String message);

    int P_Random(ActiveStates caller, int sequence);

    int P_Random(ActiveStates caller, mobjtype_t type, int sequence);
}
