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

namespace moachadoom { }

using awt.DoomWindow;
using awt.EventBase;
using awt.EventBase.ActionMode;
using awt.EventBase.ActionStateHolder;
using awt.EventBase.RelationType;
using p.ActiveStates;
using v.graphics.Patches;

using java.awt.*;
using java.io.OutputStream;
using java.util.Arrays;
using java.util.HashMap;
using java.util.Map;
using java.util.Set;
using java.util.function.IntFunction;
using java.util.logging.ConsoleHandler;
using java.util.logging.Level;
using java.util.logging.Logger;
using java.util.stream.Collectors;
using java.util.stream.Stream;

/**
 * Facility to manage Logger Levels for different classes
 * All of that should be used instead of System.err.println for debug
 *
 * @author Good Sign
 */
public class Loggers
{
    private static readonly Level DEFAULT_LEVEL = Level.WARNING;

    private static readonly Map<Level, Logger> PARENT_LOGGERS_MAP = Stream.of(
            Level.FINE, Level.FINER, Level.FINEST, Level.INFO, Level.SEVERE, Level.WARNING
    ).collect(Collectors.toMap(l -> l, Loggers::newLoggerHandlingLevel));

    private static readonly Logger DEFAULT_LOGGER = PARENT_LOGGERS_MAP.get(DEFAULT_LEVEL);
    private static readonly HashMap<String, Logger> INDIVIDUAL_CLASS_LOGGERS = new HashMap<>();
    private static EventBase<?> lastHandler = null;

    static
    {
        //INDIVIDUAL_CLASS_LOGGERS.put(EventObserver.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINE));
        //INDIVIDUAL_CLASS_LOGGERS.put(TraitFactory.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINER));
        INDIVIDUAL_CLASS_LOGGERS.put(ActiveStates.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINER));
        INDIVIDUAL_CLASS_LOGGERS.put(DoomWindow.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINE));
        INDIVIDUAL_CLASS_LOGGERS.put(Patches.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
    }

    private Loggers()
    {
    }

    public static Logger getLogger(String className)
    {
        var ret = Logger.getLogger(className);
        ret.setParent(INDIVIDUAL_CLASS_LOGGERS.getOrDefault(className, DEFAULT_LOGGER));

        return ret;
    }

    public static <EventHandler : Enum<EventHandler> & EventBase<EventHandler>> void LogEvent(
            Logger logger,
            ActionStateHolder<EventHandler> actionStateHolder,
            EventHandler handler,
            AWTEvent event
    )
    {
        if (!logger.isLoggable(Level.ALL) && lastHandler == handler)
        {
            return;
        }

        lastHandler = handler;

        IntFunction<EventBase<EventHandler>[]> arrayGenerator = EventBase[]::new;
        var depends = actionStateHolder
                .cooperations(handler, RelationType.DEPEND)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        var adjusts = actionStateHolder
                .adjustments(handler);

        var causes = actionStateHolder
                .cooperations(handler, RelationType.CAUSE)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        var reverts = actionStateHolder
                .cooperations(handler, RelationType.REVERT)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        if (logger.isLoggable(Level.FINEST))
            logger.log(Level.FINEST, () -> String.format(
                    "\n\nENCOUNTERED EVENT: %s [%s] \n%s: %s \n%s \n%s: %s \n%s: %s \nOn event: %s",
                    handler, ActionMode.PERFORM,
                    RelationType.DEPEND, Arrays.toString(depends),
                    adjusts.entrySet().stream().collect(StringBuilder::new, (sb, e) -> sb.append(e.getKey()).append(' ').append(e.getValue()).append('\n'), StringBuilder::append),
                    RelationType.CAUSE, Arrays.toString(causes),
                    RelationType.REVERT, Arrays.toString(reverts),
                    event
            ));
        else if (logger.isLoggable(Level.FINER))
        {
            logger.log(Level.FINER, () -> String.format(
                    "\n\nENCOUNTERED EVENT: %s [%s] \n%s: %s \n%s \n%s: %s \n%s: %s \n",
                    handler, ActionMode.PERFORM,
                    RelationType.DEPEND, Arrays.toString(depends),
                    adjusts.entrySet().stream().collect(StringBuilder::new, (sb, e) -> sb.append(e.getKey()).append(' ').append(e.getValue()).append('\n'), StringBuilder::append),
                    RelationType.CAUSE, Arrays.toString(causes),
                    RelationType.REVERT, Arrays.toString(reverts)
            ));
        } else
        {
            logger.log(Level.FINE, () -> String.format(
                    "\nENCOUNTERED EVENT: %s [%s]",
                    handler, ActionMode.PERFORM
            ));
        }
    }

    private static Logger newLoggerHandlingLevel(Level l)
    {
        var h = new OutHandler();
        h.setLevel(l);
        var ret = Logger.getAnonymousLogger();
        ret.setUseParentHandlers(false);
        ret.setLevel(l);
        ret.addHandler(h);
        return ret;
    }

    private static readonly class OutHandler : ConsoleHandler
    {
        
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        protected synchronized void setOutputStream(OutputStream out)  
        {
            super.setOutputStream(System.out);
        }
    }
}
