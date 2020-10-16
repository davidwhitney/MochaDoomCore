namespace hu {  

using doom.SourceCode.HU_Stuff;
using doom.event_t;
using rr.patch_t;

using static doom.SourceCode.HU_Stuff.HU_Responder;

public interface IHeadsUp
{

    void Ticker();

    void Erase();

    void Drawer();

    @HU_Stuff.C(HU_Responder)
    bool Responder(event_t ev);

    patch_t[] getHUFonts();

    char dequeueChatChar();

    void Init();

    void setChatMacro(int i, String s);

    void Start();

    void Stop();

}
