namespace savegame {  

using p.ThinkerList;

using java.io.DataInputStream;
using java.io.DataOutputStream;

public interface IDoomSaveGame
{
    void setThinkerList(ThinkerList li);

    bool doLoad(DataInputStream f);

    IDoomSaveGameHeader getHeader();

    void setHeader(IDoomSaveGameHeader header);

    bool doSave(DataOutputStream f);
}
