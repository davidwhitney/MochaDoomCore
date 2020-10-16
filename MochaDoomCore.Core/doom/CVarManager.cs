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
using System.Diagnostics;
using System.Security;

namespace doom
{



    /**
 * New, object-oriented Console Variable Manager
 * Usage:
 * 1. Define CVars in CommandVariable Enum
 * 2. In program entry main function, create any ICommandLineManager and pass an instance to create CVarManager
 * 3. Use methods bool, present, get and with to check or get CVars
 *
 * @author Good Sign
 */
    public class CVarManager
    {

        private readonly Dictionary<CommandVariable, Object[]> cVarMap = new Dictionary<CommandVariable, Object[]>();

        public CVarManager(List<String> commandList)
        {
            System.Diagnostics.Debug.WriteLine(processAllArgs(commandList) + " command-line variables");
        }

        /**
     * Checks that CVar of switch-type is passed as Command Line Argument
     *
     * @param cv
     * @return boolean
     */
        public bool Bool(CommandVariable cv)
        {
            return cv.getType() == CommandVariable.Type.SWITCH && cVarMap.ContainsKey(cv);
        }

        /**
     * Checks that CVar of any type is passed as Command Line Argument with proper value(s)
     *
     * @param cv
     * @return boolean
     */
        public bool present(CommandVariable cv)
        {
            return cVarMap[cv] != null;
        }

        /**
     * Checks that CVar of any type is passed as Command Line Argument
     *
     * @param cv
     * @return boolean
     */
        public bool specified(CommandVariable cv)
        {
            return cVarMap.ContainsKey(cv);
        }

        /**
     * Gets an Optional with or without a value of CVar argument at position
     *
     * @param cv
     * @return Optional
     */
        public T get<T>(CommandVariable cv, Class<T> itemType, int position)
        {
            if (cv.arguments[position] != itemType)
            {
                throw new ArgumentException(String.Format("CVar argument at position %d is not of class %s",
                    position, itemType.getName()));
            }

            if (!cVarMap.ContainsKey(cv))
            {
                return Optional.empty();
            }

            var ret = (T) cVarMap.Get(cv)[position];
            return Optional.ofNullable(ret);
        }

        /**
     * Tries to apply a CVar argument at position to the consuming function
     * The magic is that you declare a lambda function or reference some method
     * and the type of object will be automatically picked from what you hinted
     * <p>
     * i.e. (String s) -> System.out.println(s) will try to get string,
     * (Object o) -> map.put(key, o) or o -> list.add(o.hashCode()) will try to get objects
     * and you dont have to specify class
     * <p>
     * The drawback is the ClassCastException will be thrown if the value is neither
     * what you expected, nor a subclass of it
     *
     * @param cv
     * @param position
     * @param action
     * @return false if CVar is not passed as Command Line Argument or the consuming action is incompatible
     */
        public bool with<T>(CommandVariable cv, int position, Consumer<T> action)
        {
            try
            {
                var mapped = cVarMap.Get(cv);
                if (mapped == null)
                {
                    return false;
                }

                var item = (T) mapped[position];
                action.accept(item);
                return true;
            }
            catch (Exception ex)
            {
                return false;
            }
        }

        /**
     * Tries to replace the CVar argument if already present or add it along with CVar
     *
     * @param cv
     * @param value
     * @param position
     * @return false if invalid position or value class
     */
        public bool unknown<T>(CommandVariable cv, T value, int position)
        {
            if (position < 0 || position >= cv.arguments.length)
            {
                return false;
            }

            if (!cv.arguments[position].isInstance(value))
            {
                return false;
            }

            cVarMap.compute(cv, (key, array)-> {
                if (array == null)
                {
                    array = new Object[cv.arguments.length];
                }

                array[position] = value;
                return array;
            });

            return true;
        }

        private void readResponseFile(String filename)
        {
            var r = new ResponseReader();
            if (new ResourceIO(filename).readLines(r))
            {
                System.Diagnostics.Debug.WriteLine(String.Format("Found response file %s, read %d command line variables", filename,
                    r.cVarCount));
            }
            else
            {
                System.Diagnostics.Debug.WriteLine(String.Format("No such response file %s!", filename));
                Environment.Exit(-1);
            }
        }

        private int processAllArgs(List<String> commandList)
        {
            var cVarCount = 0;
            var position = 0;

            for (
                var limit = commandList.Count;
                limit > position;
                position = processCVar(commandList, position),
                ++position,
                ++cVarCount
            )
            {
            }

            return cVarCount;
        }

        private int processCVar(List<String> commandList, int position)
        {
            var arg = commandList[position];
            if (!isCommandArgument(arg))
            {
                return position;
            }

            var cVarPrefix = arg[0];
            var cVarName = arg.Substring(1);

            if (cVarPrefix == '@')
            {
                readResponseFile(cVarName);
                return position;
            }

            try
            {
                var cVar = CommandVariables.valueOf(cVarName.toUpperCase());
                if (cVar.prefix == cVarPrefix)
                {
                    switch (cVar.getType())
                    {
                        case PARAMETER:
                            cVarMap.put(cVar, null);
                        case VARARG:
                            return processCVarSubArgs(commandList, position, cVar);
                        case SWITCH:
                        default:
                            cVarMap.put(cVar, null);
                            return position;
                    }
                }
            }
            catch (Exception ignored)
            {
            }

            return position;
        }

        private int processCVarSubArgs(List<String> commandList, int position, CommandVariable cVar)
        {
            var cVarMappings = new Object[cVar.arguments.length];
            for (var j = 0; j < cVar.arguments.length; ++j)
            {
                if (cVar.arguments[j].isArray())
                {
                    var elementClass = cVar.arguments[j].getComponentType();
                    var mapping = processVarArg(elementClass, commandList, position + 1);
                    cVarMappings[j] = mapping;
                    position += mapping.length;
                    if (mapping.length == 0)
                    {
                        break;
                    }
                }
                else if ((cVarMappings[j] = processValue(cVar.arguments[j], commandList, position + 1)) == null)
                {
                    break;
                }
                else
                {
                    ++position;
                }
            }

            cVarMap.put(cVar, cVarMappings);
            return position;
        }

        private Object processValue(Class<?> elementClass, List<String> commandList, int position)
        {
            if (position >= commandList.size())
            {
                return null;
            }

            var arg = commandList.get(position);
            if (isCommandArgument(arg))
            {
                return null;
            }

            return formatArgValue(elementClass, arg);
        }

        private Object[] processVarArg(Class<?> elementClass, List<String> commandList, int position)
        {
            var list = new List<object>();
            for (Object value; (value = processValue(elementClass, commandList, position)) != null; ++position)
            {
                list.Add(value);
            }

            // as String[] instanceof Object[], upcast
            return list.toArray((Object[]) Array.newInstance(elementClass, list.size()));
        }

        private Object formatArgValue(Class<?> format, String arg)
        {
            if (format == Integer.class)
            {
                try
                {
                    return int.Parse(arg);
                }
                catch (NumberFormatException ex)
                {
                    Loggers.getLogger(CommandVariable.class.getName()).log(Level.WARNING, null, ex);
                    return null;
                }
            }
            else if (format == String.class)
            {
                return arg;
            }

            try
            {
                return format.getDeclaredConstructor(String.class).newInstance(arg);
            }
            catch (
                NoSuchMethodException
                |

            SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException ex
                )
            {
                Loggers.getLogger(CommandVariable.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        private boolean isCommandArgument(String arg)
        {
            if (arg.length() < CommandVariable.MIN_CVAR_LENGTH)
                return false;

            switch (arg.charAt(0))
            {
                case '-':
                case '+':
                case '@':
                    return true;
            }

            return false;
        }

        private class ResponseReader

        implements Consumer<String>
        {
            int cVarCount = 0;

            @Override

            public void accept(String line)
            {
                cVarCount += processAllArgs(Arrays.asList(line.split(" ")));
            }
        }
    }

}