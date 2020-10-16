package savegame;

import p.ThinkerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface IDoomSaveGame
{
    void setThinkerList(ThinkerList li);

    boolean doLoad(DataInputStream f);

    IDoomSaveGameHeader getHeader();

    void setHeader(IDoomSaveGameHeader header);

    boolean doSave(DataOutputStream f);
}
