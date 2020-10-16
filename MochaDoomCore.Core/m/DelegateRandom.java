/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
namespace m {  

using data.Defines;
using data.mobjtype_t;
using doom.SourceCode.M_Random;
using p.ActiveStates;
using utils.C2JUtils;

using static doom.SourceCode.M_Random.*;

/**
 * A "IRandom" that delegates its function to one of the two available IRandom implementations
 * By default, MochaDoom now uses JavaRandom, however it switches
 * to DoomRandom (supposedly Vanilla DOOM v1.9 compatible, tested only in Chocolate DOOM)
 * whenever you start recording or playing demo. When you start then new game, MochaDoom restores new JavaRandom.
 * <p>
 * However, if you start MochaDoom with -javarandom command line argument and -record demo,
 * then MochaDoom will record the demo using JavaRandom. Such demo will be neither compatible
 * with Vanilla DOOM v1.9, nor with another source port.
 * <p>
 * Only MochaDoom can play JavaRandom demos.
 * - Good Sign 2017/04/10
 *
 * @author Good Sign
 */
public class DelegateRandom : IRandom
{

    private IRandom random;
    private IRandom altRandom;

    public DelegateRandom()
    {
        random = new JavaRandom();
    }

    public void requireRandom(int version)
    {
        if (C2JUtils.flags(version, Defines.JAVARANDOM_MASK) && random instanceof DoomRandom)
        {
            switchRandom(true);
        } else if (!C2JUtils.flags(version, Defines.JAVARANDOM_MASK) && !(random instanceof DoomRandom))
        {
            switchRandom(false);
        }
    }

    private void switchRandom(bool which)
    {
        IRandom arandom = altRandom;
        if (arandom != null && (!which && arandom instanceof DoomRandom || which && arandom instanceof JavaRandom))
        {
            altRandom = random;
            random = arandom;
            System.out.print(String.format("M_Random: Switching to %s\n", random.getClass().getSimpleName()));
        } else
        {
            altRandom = random;
            random = which ? new JavaRandom() : new DoomRandom();
            System.out.print(String.format("M_Random: Switching to %s (new instance)\n", random.getClass().getSimpleName()));
        }
        //random.ClearRandom();
    }

    
    @C(P_Random)
    public int P_Random()
    {
        return random.P_Random();
    }

    
    @C(M_Random)
    public int M_Random()
    {
        return random.M_Random();
    }

    
    @C(M_ClearRandom)
    public void ClearRandom()
    {
        random.ClearRandom();
    }

    
    public int getIndex()
    {
        return random.getIndex();
    }

    
    public int P_Random(int caller)
    {
        return random.P_Random(caller);
    }

    
    public int P_Random(String message)
    {
        return random.P_Random(message);
    }

    
    public int P_Random(ActiveStates caller, int sequence)
    {
        return random.P_Random(caller, sequence);
    }

    
    public int P_Random(ActiveStates caller, mobjtype_t type, int sequence)
    {
        return random.P_Random(caller, type, sequence);
    }

}
