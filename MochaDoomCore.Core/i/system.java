// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: system.java,v 1.5 2011/02/11 00:11:13 velktron Exp $
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
// $Log: system.java,v $
// Revision 1.5  2011/02/11 00:11:13  velktron
// A MUCH needed update to v1.3.
//
// Revision 1.1  2010/06/30 08:58:50  velktron
// Let's see if this stuff will finally commit....
//
//
// Most stuff is still  being worked on. For a good place to start and get an idea of what is being done, I suggest checking out the "testers" package.
//
// Revision 1.1  2010/06/29 11:07:34  velktron
// Release often, release early they say...
//
// Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete, and there's still mixed C code in there. I suggest you load everything up in Eclpise and see what gives from there.
//
// A good place to start is the testers/ directory, where you  can get an idea of how a few of the implemented stuff works.
//
//
// DESCRIPTION:
//
//-----------------------------------------------------------------------------

namespace i {  

public class system
{
    private static int mb_used = 6;
    public static void Error(String error, Object... args)
    {
        //va_list	argptr;

        // Message first.
        //va_start (argptr,error);
        System.err.print("Error: ");
        System.err.printf(error, args);
        System.err.print("\n");
        //va_end (argptr);

        //fflush( stderr );

        // Shutdown. Here might be other errors.
        //if (demorecording)
        //G_CheckDemoStatus();

        //D_QuitNetGame ();
        //I_ShutdownGraphics();

        System.exit(-1);
    }
}
