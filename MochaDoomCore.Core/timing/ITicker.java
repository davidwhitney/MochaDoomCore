namespace timing {  

using doom.CVarManager;
using doom.CommandVariable;
using doom.SourceCode.I_IBM;

using static doom.SourceCode.I_IBM.I_GetTime;

public interface ITicker
{

    static ITicker createTicker(CVarManager CVM)
    {
        if (CVM.bool(CommandVariable.MILLIS))
        {
            return new MilliTicker();
        } else if (CVM.bool(CommandVariable.FASTTIC) || CVM.bool(CommandVariable.FASTDEMO))
        {
            return new DelegateTicker();
        } else
        {
            return new NanoTicker();
        }
    }

    @I_IBM.C(I_GetTime)
    int GetTime();
}