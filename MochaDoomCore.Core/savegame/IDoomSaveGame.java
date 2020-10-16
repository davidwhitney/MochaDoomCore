namespace savegame {  

using p.ThinkerList;

using java.io.Stream;
using java.io.Stream;

public interface IDoomSaveGame
{
    void setThinkerList(ThinkerList li);

    bool doLoad(Stream f);

    IDoomSaveGameHeader getHeader();

    void setHeader(IDoomSaveGameHeader header);

    bool doSave(Stream f);
}
