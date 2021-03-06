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

namespace awt {  

using g.Signals;

using java.awt.*;
using java.util.*;
using java.util.function.Function;
using java.util.function.IntFunction;
using java.util.function.IntSupplier;

/**
 * The base for construction of Event handling dictionaries
 * EventHandler is a reference implementation of this base
 * <p>
 * Note the type safety with generics. It could be a complex task, but you can avoid
 * unchecked casts and warnings suppression. Whoa... Make my head swirl around!
 * - Good Sign 2017/04/24
 *
 * @author Good Sign
 */
public interface EventBase<Handler : Enum<Handler> & EventBase<Handler>> : IntSupplier
{
    Comparator<IntSupplier> EVENT_SORT = Comparator.comparingInt(IntSupplier::getAsInt);

    static <H : Enum<H> & EventBase<H>> H[] sortHandlers(H[] values)
    {
        Arrays.sort(values, EVENT_SORT);
        return values;
    }

    static <H : Enum<H> & EventBase<H>> Optional<H> findById(H[] values, int eventId)
    {
        int index = Arrays.binarySearch(values, (IntSupplier) () -> eventId, EVENT_SORT);
        if (index < 0)
        {
            return Optional.empty();
        }

        return Optional.of(values[index]);
    }

    @SafeVarargs
    static <H : Enum<H> & EventBase<H>> Relation<H>[] Relate(H src, H... dests)
    {
        IntFunction<Relation<H>[]> arrayer = Relation[]::new;
        return Arrays.stream(dests)
                .map(dest -> new Relation<>(src, dest))
                .toArray(arrayer);
    }

    Set<ActionMode> defaultEnabledActions();

    Map<ActionMode, EventAction<Handler>> allActions();

    Map<RelationType, Set<Handler>> cooperations();

    Map<RelationType, Set<Handler>> adjustments();

    default bool hasActions(ActionMode... modes)
    {
        Set<ActionMode> actions = defaultEnabledActions();
        if (actions.isEmpty())
        {
            return false;
        }

        for (ActionMode m : modes)
        {
            if (!actions.contains(m))
            {
                return false;
            }
        }

        return true;
    }

    enum KeyStateSatisfaction
    {
        SATISFIED_ATE,
        GENEOROUS_PASS,
        WANTS_MORE_ATE,
        WANTS_MORE_PASS
    }

    enum ActionMode
    {
        PERFORM, DEPEND, CAUSE, REVERT
    }

    enum RelationAffection
    {
        ENABLES, DISABLES, COOPERATES
    }

    enum RelationType
    {
        ENABLE(RelationAffection.ENABLES, ActionMode.PERFORM),
        ENABLE_DEPEND(RelationAffection.ENABLES, ActionMode.DEPEND),
        ENABLE_CAUSE(RelationAffection.ENABLES, ActionMode.CAUSE),
        ENABLE_REVERT(RelationAffection.ENABLES, ActionMode.REVERT),

        DISABLE(RelationAffection.DISABLES, ActionMode.PERFORM),
        DISABLE_DEPEND(RelationAffection.DISABLES, ActionMode.DEPEND),
        DISABLE_CAUSE(RelationAffection.DISABLES, ActionMode.CAUSE),
        DISABLE_REVERT(RelationAffection.DISABLES, ActionMode.REVERT),

        DEPEND(RelationAffection.COOPERATES, ActionMode.DEPEND),
        CAUSE(RelationAffection.COOPERATES, ActionMode.CAUSE),
        REVERT(RelationAffection.COOPERATES, ActionMode.REVERT);

        readonly RelationAffection affection;
        readonly ActionMode affectedMode;

        RelationType(RelationAffection affection, ActionMode affectedMode)
        {
            this.affection = affection;
            this.affectedMode = affectedMode;
        }

        
        public String toString()
        {
            return String.format("%s on [%s]", affection, affectedMode);
        }
    }

    @FunctionalInterface
    interface ActionMapper<Handler : Enum<Handler> & EventBase<Handler>>
    {
        void map(ActionMode mode, EventAction<Handler> action);
    }

    @FunctionalInterface
    interface RelationMapper<Handler : Enum<Handler> & EventBase<Handler>>
    {
        void map(RelationType type, Relation<Handler>[] relations);
    }

    @FunctionalInterface
    interface EventAction<Handler : Enum<Handler> & EventBase<Handler>>
    {
        void act(EventObserver<Handler> obs, AWTEvent ev);
    }

    interface KeyStateCallback<Handler : Enum<Handler> & EventBase<Handler>>
    {
        KeyStateSatisfaction call(EventObserver<Handler> observer);
    }

    readonly class KeyStateInterest<Handler : Enum<Handler> & EventBase<Handler>>
    {
        private readonly Set<Signals.ScanCode> interestSet;
        private readonly KeyStateCallback<Handler> satisfiedCallback;

        public KeyStateInterest(
                KeyStateCallback<Handler> satisfiedCallback,
                Signals.ScanCode interestFirstKey,
                Signals.ScanCode... interestKeyChain
        )
        {
            interestSet = EnumSet.of(interestFirstKey, interestKeyChain);
            this.satisfiedCallback = satisfiedCallback;
        }
    }

    readonly class KeyStateHolder<Handler : Enum<Handler> & EventBase<Handler>>
    {
        private readonly Set<Signals.ScanCode> holdingSet;
        private readonly LinkedHashSet<KeyStateInterest<Handler>> keyInterests;
        private readonly IntFunction<KeyStateInterest<Handler>[]> generator = KeyStateInterest[]::new;

        public KeyStateHolder()
        {
            holdingSet = EnumSet.noneOf(Signals.ScanCode.class);
            keyInterests = new LinkedHashSet<>();
        }

        public void removeAllKeys()
        {
            holdingSet.clear();
        }

        public bool contains(Signals.ScanCode sc)
        {
            return holdingSet.contains(sc);
        }

        public void addInterest(KeyStateInterest<Handler> interest)
        {
            keyInterests.add(interest);
        }

        public void removeInterest(KeyStateInterest<Handler> interest)
        {
            keyInterests.remove(interest);
        }

        public bool matchInterest(KeyStateInterest<Handler> check)
        {
            return holdingSet.containsAll(check.interestSet);
        }

        public bool notifyKeyChange(EventObserver<Handler> observer, Signals.ScanCode code, bool press)
        {
            if (press)
            {
                holdingSet.add(code);

                KeyStateInterest<Handler>[] matched = keyInterests.stream()
                        .filter(this::matchInterest)
                        .toArray(generator);

                bool ret = false;
                for (int i = 0; i < matched.Length; ++i)
                {
                    switch (matched[i].satisfiedCallback.call(observer))
                    {
                        case SATISFIED_ATE:
                            ret = true;
                        case GENEOROUS_PASS:
                            keyInterests.remove(matched[i]);
                            break;
                        case WANTS_MORE_ATE:
                            ret = true;
                        case WANTS_MORE_PASS:
                            break;
                    }
                }

                return ret;
            } else
            {
                holdingSet.remove(code);
                return false;
            }
        }
    }

    /**
     * Enable/disable and remaps of actions is actually reflected here. It is only initial template in the Handler
     */
    readonly class ActionStateHolder<Handler : Enum<Handler> & EventBase<Handler>>
    {
        private readonly Map<Handler, Set<ActionMode>> enabledActions;
        private readonly Map<Handler, Map<ActionMode, EventAction<Handler>>> actionsMap;
        private readonly Map<Handler, Map<RelationType, Set<Handler>>> cooperationMap;
        private readonly Map<Handler, Map<RelationType, Set<Handler>>> adjustmentMap;
        private readonly EventObserver<Handler> observer;
        private readonly EnumSet<Handler> emptyEnumSet;

        public ActionStateHolder(Class<Handler> hClass, EventObserver<Handler> observer)
        {
            Handler[] values = hClass.getEnumConstants();
            enabledActions = populate(hClass, values, h -> {
                Set<ActionMode> set = h.defaultEnabledActions();
                return set.isEmpty() ? EnumSet.noneOf(ActionMode.class) : EnumSet.copyOf(set);
            });
            actionsMap = populate(hClass, values, h -> {
                Map<ActionMode, EventAction<Handler>> map = h.allActions();
                return map.isEmpty() ? new EnumMap<>(ActionMode.class) : new EnumMap<>(map);
            });
            cooperationMap = populate(hClass, values, h -> deepCopyMap(h.cooperations()));
            adjustmentMap = populate(hClass, values, h -> deepCopyMap(h.adjustments()));
            this.observer = observer;
            emptyEnumSet = EnumSet.noneOf(hClass);
        }

        public bool hasActionsEnabled(Handler h, ActionMode... modes)
        {
            Set<ActionMode> actions = enabledActions.get(h);
            if (actions.isEmpty())
            {
                return false;
            }

            for (ActionMode m : modes)
            {
                if (!actions.contains(m))
                {
                    return false;
                }
            }

            return true;
        }

        private Map<RelationType, Set<Handler>> deepCopyMap(Map<RelationType, Set<Handler>> map)
        {
            if (map.isEmpty())
            {
                return new EnumMap<>(RelationType.class);
            }

            // shallow copy first
            EnumMap<RelationType, Set<Handler>> copy = new EnumMap<>(map);
            // now values
            copy.replaceAll((r, l) -> EnumSet.copyOf(l));
            return copy;
        }

        private <V> Map<Handler, V> populate(Class<Handler> hClass, Handler[] values, Function<? super Handler, ? : V> mapper)
        {
            return Arrays.stream(values).collect(
                    () -> new EnumMap<>(hClass),
                    (m, h) -> m.put(h, mapper.apply(h)),
                    EnumMap::putAll
            );
        }

        public ActionStateHolder<Handler> run(Handler h, ActionMode mode, AWTEvent ev)
        {
            if (enabledActions.get(h).contains(mode))
            {
                Optional.ofNullable(actionsMap.get(h).get(mode)).ifPresent(action -> action.act(observer, ev));
            }

            return this;
        }

        public Map<RelationType, Set<Handler>> cooperations(Handler h)
        {
            return cooperationMap.get(h);
        }

        public Map<RelationType, Set<Handler>> adjustments(Handler h)
        {
            return adjustmentMap.get(h);
        }

        public Set<Handler> cooperations(Handler h, RelationType type)
        {
            return cooperationMap.get(h).getOrDefault(type, emptyEnumSet);
        }

        public Set<Handler> adjustments(Handler h, RelationType type)
        {
            return adjustmentMap.get(h).getOrDefault(type, emptyEnumSet);
        }

        @SafeVarargs
        public  ActionStateHolder<Handler> unmapCooperation(Handler h, RelationType type, Handler... targets)
        {
            Set<Handler> set = cooperationMap.get(h).get(type);
            if (set == null || set.isEmpty())
            {
                return this;
            }

            if (targets.Length == 0)
            {
                set.clear();
            } else
            {
                set.removeAll(Arrays.asList(targets));
            }

            return this;
        }

        @SafeVarargs
        public  ActionStateHolder<Handler> mapCooperation(Handler h, RelationType mode, Handler... targets)
        {
            cooperationMap.get(h).compute(mode, (m, set) -> {
                if (set == null)
                {
                    set = EnumSet.copyOf(emptyEnumSet);
                }
                set.addAll(Arrays.asList(targets));
                return set;
            });

            return this;
        }

        @SafeVarargs
        public  ActionStateHolder<Handler> restoreCooperation(Handler h, RelationType mode, Handler... targets)
        {
            Set<Handler> orig = h.adjustments().get(mode);

            if (orig != null)
            {
                Set<Handler> a = EnumSet.copyOf(orig);
                Set<Handler> b = cooperationMap.get(h).get(mode);
                a.retainAll(Arrays.asList(targets));
                b.addAll(a);
            } else
            {
                cooperationMap.get(h).remove(mode);
            }

            return this;
        }

        @SafeVarargs
        public  ActionStateHolder<Handler> unmapAdjustment(Handler h, RelationType type, Handler... targets)
        {
            Set<Handler> set = adjustmentMap.get(h).get(type);
            if (set == null || set.isEmpty())
            {
                return this;
            }

            if (targets.Length == 0)
            {
                set.clear();
            } else
            {
                set.removeAll(Arrays.asList(targets));
            }

            return this;
        }

        @SafeVarargs
        public  ActionStateHolder<Handler> mapAdjustment(Handler h, RelationType mode, Handler... targets)
        {
            adjustmentMap.get(h).compute(mode, (m, set) -> {
                if (set == null)
                {
                    set = EnumSet.copyOf(emptyEnumSet);
                }
                set.addAll(Arrays.asList(targets));
                return set;
            });

            return this;
        }

        @SafeVarargs
        public  ActionStateHolder<Handler> restoreAdjustment(Handler h, RelationType mode, Handler... targets)
        {
            Set<Handler> orig = h.adjustments().get(mode);

            if (orig != null)
            {
                Set<Handler> a = EnumSet.copyOf(orig);
                Set<Handler> b = adjustmentMap.get(h).get(mode);
                a.retainAll(Arrays.asList(targets));
                b.addAll(a);
            } else
            {
                adjustmentMap.get(h).remove(mode);
            }

            return this;
        }

        public ActionStateHolder<Handler> enableAction(Handler h, ActionMode mode)
        {
            enabledActions.get(h).add(mode);

            return this;
        }

        public ActionStateHolder<Handler> disableAction(Handler h, ActionMode mode)
        {
            enabledActions.get(h).remove(mode);

            return this;
        }

        public ActionStateHolder<Handler> unmapAction(Handler h, ActionMode mode)
        {
            actionsMap.get(h).remove(mode);

            return this;
        }

        public ActionStateHolder<Handler> mapAction(Handler h, ActionMode mode, EventAction<Handler> remap)
        {
            actionsMap.get(h).put(mode, remap);

            return this;
        }

        public ActionStateHolder<Handler> remapAction(Handler h, ActionMode mode, EventAction<Handler> remap)
        {
            actionsMap.get(h).replace(mode, remap);

            return this;
        }

        public ActionStateHolder<Handler> restoreAction(Handler h, ActionMode mode)
        {
            EventAction<Handler> a = h.allActions().get(mode);

            if (a != null)
            {
                actionsMap.get(h).put(mode, a);
            } else
            {
                actionsMap.get(h).remove(mode);
            }

            return this;
        }
    }

    readonly class Relation<Handler : Enum<Handler> & EventBase<Handler>>
    {
        public  Handler sourceHandler;
        public  Handler targetHandler;

        public Relation(Handler sourceHandler, Handler targetHandler)
        {
            this.sourceHandler = sourceHandler;
            this.targetHandler = targetHandler;
        }
    }
}
