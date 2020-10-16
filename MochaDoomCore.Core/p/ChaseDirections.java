namespace p {  

using static data.Defines.TIC_MUL;
using static m.fixed_t.MAPFRACUNIT;

public  class ChaseDirections
{

    public static readonly int DI_EAST = 0;

    public static readonly int DI_NORTHEAST = 1;

    public static readonly int DI_NORTH = 2;

    public static readonly int DI_NORTHWEST = 3;

    public static readonly int DI_WEST = 4;

    public static readonly int DI_SOUTHWEST = 5;

    public static readonly int DI_SOUTH = 6;

    public static readonly int DI_SOUTHEAST = 7;

    public static readonly int DI_NODIR = 8;

    public static readonly int NUMDIR = 9;

    //
    // P_NewChaseDir related LUT.
    //
    public  static int[] opposite =
            {DI_WEST, DI_SOUTHWEST, DI_SOUTH, DI_SOUTHEAST, DI_EAST, DI_NORTHEAST,
                    DI_NORTH, DI_NORTHWEST, DI_NODIR};

    public  static int[] diags =
            {DI_NORTHWEST, DI_NORTHEAST, DI_SOUTHWEST, DI_SOUTHEAST};

    public  static int[] xspeed =
            {MAPFRACUNIT, 47000 / TIC_MUL, 0, -47000 / TIC_MUL, -MAPFRACUNIT, -47000 / TIC_MUL, 0, 47000 / TIC_MUL}; // all
    // fixed

    public  static int[] yspeed =
            {0, 47000 / TIC_MUL, MAPFRACUNIT, 47000 / TIC_MUL, 0, -47000 / TIC_MUL, -MAPFRACUNIT, -47000 / TIC_MUL}; // all
    // fixed

}
