namespace doom {  

public interface NetConsts
{

    int NCMD_EXIT = 0x80000000;
    int NCMD_RETRANSMIT = 0x40000000;
    int NCMD_SETUP = 0x20000000;
    int NCMD_KILL = 0x10000000; // kill game
    int NCMD_CHECKSUM = 0x0fffffff;

    int DOOMCOM_ID = 0x12345678;


    //Networking and tick handling related. Moved to DEFINES
    //protected static int    BACKUPTICS     = 12;


    // command_t
    short CMD_SEND = 1;
    short CMD_GET = 2;

}
