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

using System;
using System.Collections.Generic;
// ReSharper disable IdentifierTypo
// ReSharper disable UnusedMember.Global

namespace doom
{

    /**
 * A new way to define Command Line Arguments for the Engine
 *
 * @author Good Sign
 */

    public class CommandVariable
    {
        public readonly int MIN_CVAR_LENGTH = 4;
        public readonly char prefix;
        public readonly List<System.Type> arguments;

        public CommandVariable(char prefix, params System.Type[] arguments)
        {
            this.prefix = prefix;
            this.arguments = new List<System.Type>(arguments);
        }

        public CommandVariable(params System.Type[] arguments)
            : this('-', arguments)
        {
        }

        public CommandVariable.Type getType()
        {
            return arguments.Count > 0
                ? arguments[^1].GetType().IsArray
                    ? CommandVariable.Type.VARARG
                    : CommandVariable.Type.PARAMETER
                : CommandVariable.Type.SWITCH;
        }

        public enum Type
        {
            PARAMETER,
            VARARG,
            SWITCH
        }

        public interface WarpMetric
        {
            int getEpisode();

            int getMap();
        }

        public class ForbidFormat
        {
            public static ForbidFormat FORBID = new ForbidFormat("disable");
            public static ForbidFormat ALLOW = new ForbidFormat(null);
            private readonly bool isForbidden;

            public ForbidFormat(String forbidString)
            {
                isForbidden = "disable".Equals(forbidString);
            }

            public int hashCode()
            {
                int hash = 3;
                hash = 67 * hash + (isForbidden ? 1 : 0);
                return hash;
            }

            public bool equals(Object obj)
            {
                if (this == obj)
                {
                    return true;
                }

                if (obj == null)
                {
                    return false;
                }

                if (!(obj is ForbidFormat)) // getClass() != obj.getClass())
                {
                    return false;
                }

                ForbidFormat other = (ForbidFormat)obj;
                return isForbidden == other.isForbidden;
            }
        }

        public class WarpFormat
        {
            public readonly int warpInt;

            public WarpFormat(int warpInt)
            {
                this.warpInt = warpInt;
            }

            public WarpFormat(String warpString)
            {
                int tryParse;
                try
                {
                    tryParse = int.Parse(warpString);
                }
                catch (Exception e)
                {
                    // swallow exception. No warp.
                    tryParse = 0;
                }

                warpInt = tryParse;
            }

            public WarpMetric getMetric(bool commercial)
            {
                return new Metric(commercial, warpInt);
            }

            public class Metric : WarpMetric
            {
                int episode;
                int map;

                public Metric(bool commercial, int warpInt)
                {
                    if (commercial)
                    {
                        episode = 1;
                        map = warpInt;
                    }
                    else
                    {
                        int evalInt = warpInt > 99
                            ? warpInt % 100
                            : warpInt;

                        episode = evalInt / 10;
                        map = evalInt % 10;
                    }
                }

                public int getEpisode()
                {
                    return episode;
                }

                public int getMap()
                {
                    return map;
                }
            }
        }

        public class MapFormat
        {
            public String mapString;

            public MapFormat(String mapString)
            {
                this.mapString = mapString.ToLower();
            }

            protected int parseAsMapXX()
            {
                if (mapString.Length != 5 || mapString.LastIndexOf("map") != 0)
                {
                    return -1; // Meh.
                }

                int map;
                try
                {
                    map = int.Parse(mapString.Substring(3));
                }
                catch (Exception e)
                {
                    return -1; // eww
                }

                return map;
            }

            protected int parseAsExMx()
            {
                if (mapString.Length != 4 || mapString[0] != 'e' || mapString[2] != 'm')
                {
                    return -1; // Nah.
                }

                char episode = mapString[1];
                char mission = mapString[3];

                if (episode < '0' || episode > '9' || mission < '0' || mission > '9')
                    return -1;

                return (episode - '0') * 10 + mission - '0';
            }

            public WarpMetric getMetric(bool commercial)
            {
                int parse = commercial
                    ? parseAsMapXX()
                    : parseAsExMx();

                return new WarpFormat(Math.Max(parse, 0)).getMetric(commercial);
            }
        }
    }

    public static class CommandVariables
    {
        public static CommandVariable DISP => new CommandVariable(typeof(string));
        public static CommandVariable GEOM => new CommandVariable(typeof(string[]));
        public static CommandVariable CONFIG => new CommandVariable(typeof(string[]));
        public static CommandVariable TRANMAP => new CommandVariable(typeof(string));
        public static CommandVariable PLAYDEMO => new CommandVariable(typeof(string));
        public static CommandVariable FASTDEMO => new CommandVariable(typeof(string));
        public static CommandVariable TIMEDEMO => new CommandVariable(typeof(string));
        public static CommandVariable RECORD => new CommandVariable(typeof(string));
        public static CommandVariable STATCOPY => new CommandVariable(typeof(string));
        public static CommandVariable TURBO => new CommandVariable(typeof(int));
        public static CommandVariable SKILL => new CommandVariable(typeof(int));
        public static CommandVariable EPISODE => new CommandVariable(typeof(int));
        public static CommandVariable TIMER => new CommandVariable(typeof(int));
        public static CommandVariable PORT => new CommandVariable(typeof(int));
        public static CommandVariable MULTIPLY => new CommandVariable(typeof(int));
        public static CommandVariable WIDTH => new CommandVariable(typeof(int));
        public static CommandVariable HEIGHT => new CommandVariable(typeof(int));
                
        public static CommandVariable PARALLELRENDERER => new CommandVariable(typeof(int), typeof(int), typeof(int));
                
        public static CommandVariable PARALLELRENDERER2 => new CommandVariable(typeof(int), typeof(int), typeof(int));
                
        public static CommandVariable LOADGAME => new CommandVariable(typeof(char));
        public static CommandVariable DUP => new CommandVariable(typeof(char));
        public static CommandVariable NET => new CommandVariable(typeof(char), typeof(string[]));
                
        public static CommandVariable WART => new CommandVariable(typeof(int), typeof(int));
        public static CommandVariable WARP => new CommandVariable(typeof(CommandVariable.WarpFormat));
        public static CommandVariable MAP => new CommandVariable('+', typeof(CommandVariable.MapFormat));
        public static CommandVariable FILE => new CommandVariable(typeof(string[]));
        public static CommandVariable IWAD => new CommandVariable(typeof(string));
        public static CommandVariable NOVERT => new CommandVariable(typeof(CommandVariable.ForbidFormat));
        public static CommandVariable NOVOLATILEIMAGE => new CommandVariable(typeof(CommandVariable.ForbidFormat));
                
        public static CommandVariable AWTFRAME => new CommandVariable();
        public static CommandVariable DEBUGFILE => new CommandVariable();
        public static CommandVariable SHDEV => new CommandVariable();
        public static CommandVariable REGDEV => new CommandVariable();
        public static CommandVariable FRDMDEV => new CommandVariable();
        public static CommandVariable FR1DEV => new CommandVariable();
        public static CommandVariable FR2DEV => new CommandVariable();
        public static CommandVariable COMDEV => new CommandVariable();
        public static CommandVariable NOMONSTERS => new CommandVariable();
        public static CommandVariable RESPAWN => new CommandVariable();
        public static CommandVariable FAST => new CommandVariable();
        public static CommandVariable DEVPARM => new CommandVariable();
        public static CommandVariable ALTDEATH => new CommandVariable();
        public static CommandVariable DEATHMATCH => new CommandVariable();
        public static CommandVariable MILLIS => new CommandVariable();
        public static CommandVariable FASTTIC => new CommandVariable();
        public static CommandVariable CDROM => new CommandVariable();
        public static CommandVariable AVG => new CommandVariable();
        public static CommandVariable NODRAW => new CommandVariable();
        public static CommandVariable NOBLIT => new CommandVariable();
        public static CommandVariable NOPLAYPAL => new CommandVariable();
        public static CommandVariable NOCOLORMAP => new CommandVariable();
        public static CommandVariable SERIALRENDERER => new CommandVariable();
        public static CommandVariable EXTRATIC => new CommandVariable();
        public static CommandVariable NOMUSIC => new CommandVariable();
        public static CommandVariable NOSOUND => new CommandVariable();
        public static CommandVariable NOSFX => new CommandVariable();
        public static CommandVariable AUDIOLINES => new CommandVariable();
        public static CommandVariable SPEAKERSOUND => new CommandVariable();
        public static CommandVariable CLIPSOUND => new CommandVariable();
        public static CommandVariable CLASSICSOUND => new CommandVariable();
        public static CommandVariable INDEXED => new CommandVariable();
        public static CommandVariable HICOLOR => new CommandVariable();
        public static CommandVariable TRUECOLOR => new CommandVariable();
        public static CommandVariable ALPHATRUECOLOR => new CommandVariable();
        public static CommandVariable BLOCKMAP => new CommandVariable();
        public static CommandVariable SHOWFPS => new CommandVariable();
        public static CommandVariable JAVARANDOM => new CommandVariable();
        public static CommandVariable GREYPAL => new CommandVariable();


    }
}