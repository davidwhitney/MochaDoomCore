namespace n {  

using doom.CommandVariable;
using doom.DoomMain;
using doom.NetConsts;
using doom.doomcom_t;
using mochadoom.Engine;

/**
 * Does nothing.
 * Allows running single-player games without an actual network.
 * Hopefully, it will be replaced by a real UDP-based driver one day.
 *
 * @author Velktron
 */

public class DummyNetworkDriver<T, V> : NetConsts, DoomSystemNetworking
{

    ////////////// STATUS ///////////

    private readonly DoomMain<T, V> DOOM;

    public DummyNetworkDriver(DoomMain<T, V> DOOM)
    {
        this.DOOM = DOOM;
    }

    
    public void InitNetwork()
    {
        doomcom_t doomcom = new doomcom_t();
        doomcom.id = DOOMCOM_ID;
        doomcom.ticdup = 1;

        // single player game
        DOOM.netgame = Engine.getCVM().present(CommandVariable.NET);
        doomcom.id = DOOMCOM_ID;
        doomcom.numplayers = doomcom.numnodes = 1;
        doomcom.deathmatch = 0;
        doomcom.consoleplayer = 0;
        DOOM.gameNetworking.setDoomCom(doomcom);
    }

    
    public void NetCmd()
    {
        // TODO Auto-generated method stub

    }
}
