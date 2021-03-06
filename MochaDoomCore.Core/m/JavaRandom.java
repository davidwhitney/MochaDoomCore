namespace m {  

using data.mobjtype_t;
using p.ActiveStates;

using java.util.Random;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: JavaRandom.java,v 1.3 2013/06/03 11:00:03 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//
// DESCRIPTION:
//	Random number LUT using java.util.Random
// Don't expect vanilla demo compatibility with THIS!
//
//-----------------------------------------------------------------------------

/**
 * Actually, now there is demo compatilbility switch: use of JavaRandom is now
 * default in singleplayer, unless you play demo, unless you record demo,
 * when you play demo, DoomRandom is picked instead, same for record, unless
 * you specify -javarandom command line argument, in that case when you record
 * demo, version information will be changed, and JavaRandom used,
 * when you play this demo, DoomRandom will not be picked, when you play
 * another demo, it will pick DoomRandom.
 * <p>
 * When you dont pass -javarandom, but play demo recorded with JavaRandom,
 * it will pick JavaRandom for this demo playback
 * - Good Sign 2017/04/14
 */
class JavaRandom : IRandom
{

    private readonly Random r;
    private readonly Random m;
    protected int rndindex = 0;
    protected int prndindex = 0;

    JavaRandom()
    {
        r = new Random(666);
        m = new Random(666);
        ClearRandom();
    }

    // Which one is deterministic?
    
    public int P_Random()
    {
        rndindex++;
        return 0xFF & r.nextInt();
    }

    
    public int M_Random()
    {
        prndindex++;
        return 0xFF & m.nextInt();
    }

    
    public  void ClearRandom()
    {
        rndindex = prndindex = 0;
        r.setSeed(666);
    }

    
    public int getIndex()
    {
        return rndindex;
    }

    
    public int P_Random(int caller)
    {
        // DUMMY
        return P_Random();
    }

    
    public int P_Random(String message)
    {
        // DUMMY
        return P_Random();
    }

    
    public int P_Random(ActiveStates caller, int sequence)
    {
        // DUMMY
        return P_Random();
    }

    
    public int P_Random(ActiveStates caller, mobjtype_t type, int sequence)
    {
        // DUMMY
        return P_Random();
    }

}

//$Log: JavaRandom.java,v $
//Revision 1.3  2013/06/03 11:00:03  velktron
//: interface without logging.
//
//Revision 1.2  2011/07/27 20:47:46  velktron
//Proper commenting, cleanup.
//
//Revision 1.1  2011/05/29 22:15:32  velktron
//Introduced IRandom interface.

