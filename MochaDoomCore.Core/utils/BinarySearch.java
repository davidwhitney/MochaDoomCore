package utils;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.function.*;

public enum BinarySearch
{
    ;

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to another
     * @param key       a value of another object type
     * @return
     */
    public static <T, E extends Comparable<? super E>> int find(List<? extends T> list,
                                                                Function<? super T, ? extends E> converter,
                                                                E key)
    {
        return find(list, converter, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to another
     * @param key       a value of another object type
     * @return
     */
    public static <T, E extends Comparable<? super E>> int find(T[] array,
                                                                Function<? super T, ? extends E> converter,
                                                                E key)
    {
        return find(array, converter, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param list       of one type of objects
     * @param comparator - a comparator for objects of type E
     * @param converter  from one type of objects to another
     * @param key        a value of another object type
     * @return
     */
    public static <T, E> int find(List<? extends T> list,
                                  Function<? super T, ? extends E> converter,
                                  Comparator<? super E> comparator,
                                  E key)
    {
        return find(list, converter, comparator, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param array      of one type of objects
     * @param comparator - a comparator for objects of type E
     * @param converter  from one type of objects to another
     * @param key        a value of another object type
     * @return
     */
    public static <T, E> int find(T[] array,
                                  Function<? super T, ? extends E> converter,
                                  Comparator<? super E> comparator,
                                  E key)
    {
        return find(array, converter, comparator, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to a primitive integer
     * @param a         primitive integer key value
     * @return
     */
    public static <T> int findByInt(List<? extends T> list,
                                    ToIntFunction<? super T> converter,
                                    int key)
    {
        return findByInt(list, converter, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to a primitive integer
     * @param a         primitive integer key value
     * @return
     */
    public static <T> int findByInt(T[] array,
                                    ToIntFunction<? super T> converter,
                                    int key)
    {
        return findByInt(array, converter, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to a primitive integer
     * @param comparator - a comparator for primitive integer values
     * @param a          primitive integer key value
     * @return
     */
    public static <T> int findByInt(List<? extends T> list,
                                    ToIntFunction<? super T> converter,
                                    IntBinaryOperator comparator,
                                    int key)
    {
        return findByInt(list, converter, comparator, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to a primitive integer
     * @param comparator - a comparator for primitive integer values
     * @param a          primitive integer key value
     * @return
     */
    public static <T> int findByInt(T[] array,
                                    ToIntFunction<? super T> converter,
                                    IntBinaryOperator comparator,
                                    int key)
    {
        return findByInt(array, converter, comparator, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to a primitive long
     * @param a         primitive long key value
     * @return
     */
    public static <T> int findByLong(List<? extends T> list,
                                     ToLongFunction<? super T> converter,
                                     long key)
    {
        return findByLong(list, converter, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to a primitive long
     * @param a         primitive long key value
     * @return
     */
    public static <T> int findByLong(T[] array,
                                     ToLongFunction<? super T> converter,
                                     long key)
    {
        return findByLong(array, converter, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to a primitive long
     * @param comparator - a comparator for primitive long values
     * @param a          primitive long key value
     * @return
     */
    public static <T> int findByLong(List<? extends T> list,
                                     ToLongFunction<? super T> converter,
                                     LongComparator comparator,
                                     long key)
    {
        return findByLong(list, converter, comparator, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to a primitive long
     * @param comparator - a comparator for primitive long values
     * @param a          primitive long key value
     * @return
     */
    public static <T> int findByLong(T[] array,
                                     ToLongFunction<? super T> converter,
                                     LongComparator comparator,
                                     long key)
    {
        return findByLong(array, converter, comparator, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to a primitive double
     * @param a         primitive double key value
     * @return
     */
    public static <T> int findByDouble(List<? extends T> list,
                                       ToDoubleFunction<? super T> converter,
                                       double key)
    {
        return findByDouble(list, converter, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to a primitive double
     * @param a         primitive double key value
     * @return
     */
    public static <T> int findByDouble(T[] array,
                                       ToDoubleFunction<? super T> converter,
                                       double key)
    {
        return findByDouble(array, converter, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to a primitive double
     * @param comparator - a comparator for primitive double values
     * @param a          primitive double key value
     * @return
     */
    public static <T> int findByDouble(List<? extends T> list,
                                       ToDoubleFunction<? super T> converter,
                                       DoubleComparator comparator,
                                       double key)
    {
        return findByDouble(list, converter, comparator, 0, list.size(), key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to a primitive double
     * @param comparator - a comparator for primitive double values
     * @param a          primitive double key value
     * @return
     */
    public static <T> int findByDouble(T[] array,
                                       ToDoubleFunction<? super T> converter,
                                       DoubleComparator comparator,
                                       double key)
    {
        return findByDouble(array, converter, comparator, 0, array.length, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to another
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param key       a value of another object type
     * @return
     */
    public static <T, E extends Comparable<? super E>> int find(List<? extends T> list,
                                                                Function<? super T, ? extends E> converter,
                                                                int from, int to, E key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByIndex(i -> converter.apply(getter.apply(i)).compareTo(key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to another
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param key       a value of another object type
     * @return
     */
    public static <T, E extends Comparable<? super E>> int find(T[] array,
                                                                Function<? super T, ? extends E> converter,
                                                                int from, int to, E key)
    {
        rangeCheck(array.length, from, to);
        return findByIndex(i -> converter.apply(array[i]).compareTo(key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to another
     * @param comparator - a comparator for objects of type E
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param key        a value of another object type
     * @return
     */
    public static <T, E> int find(List<? extends T> list,
                                  Function<? super T, ? extends E> converter,
                                  Comparator<? super E> comparator,
                                  int from, int to, E key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByIndex(i -> comparator.compare(converter.apply(getter.apply(i)), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using object of another type, given from any object of one type
     * a function can get an object of another type
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to another
     * @param comparator - a comparator for objects of type E
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param key        a value of another object type
     * @return
     */
    public static <T, E> int find(T[] array,
                                  Function<? super T, ? extends E> converter,
                                  Comparator<? super E> comparator,
                                  int from, int to, E key)
    {
        rangeCheck(array.length, from, to);
        return findByIndex(i -> comparator.compare(converter.apply(array[i]), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to a primitive integer
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param a         primitive integer key value
     * @return
     */
    public static <T> int findByInt(List<? extends T> list,
                                    ToIntFunction<? super T> converter,
                                    int from, int to, int key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByInt(i -> converter.applyAsInt(getter.apply(i)), from, to, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to a primitive integer
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param a         primitive integer key value
     * @return
     */
    public static <T> int findByInt(T[] array,
                                    ToIntFunction<? super T> converter,
                                    int from, int to, int key)
    {
        rangeCheck(array.length, from, to);
        return findByInt(i -> converter.applyAsInt(array[i]), from, to, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to a primitive integer
     * @param comparator - a comparator for primitive integer values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param a          primitive integer key value
     * @return
     */
    public static <T> int findByInt(List<? extends T> list,
                                    ToIntFunction<? super T> converter,
                                    IntBinaryOperator comparator,
                                    int from, int to, int key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByIndex(i -> comparator.applyAsInt(converter.applyAsInt(getter.apply(i)), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive integer, given from any object
     * of one type a function can get a primitive integer
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to a primitive integer
     * @param comparator - a comparator for primitive integer values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param a          primitive integer key value
     * @return
     */
    public static <T> int findByInt(T[] array,
                                    ToIntFunction<? super T> converter,
                                    IntBinaryOperator comparator,
                                    int from, int to, int key)
    {
        rangeCheck(array.length, from, to);
        return findByIndex(i -> comparator.applyAsInt(converter.applyAsInt(array[i]), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to a primitive long
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param a         primitive long key value
     * @return
     */
    public static <T> int findByLong(List<? extends T> list,
                                     ToLongFunction<? super T> converter,
                                     int from, int to, long key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByLong(i -> converter.applyAsLong(getter.apply(i)), from, to, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to a primitive long
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param a         primitive long key value
     * @return
     */
    public static <T> int findByLong(T[] array,
                                     ToLongFunction<? super T> converter,
                                     int from, int to, long key)
    {
        rangeCheck(array.length, from, to);
        return findByLong(i -> converter.applyAsLong(array[i]), from, to, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to a primitive long
     * @param comparator - a comparator for primitive long values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param a          primitive long key value
     * @return
     */
    public static <T> int findByLong(List<? extends T> list,
                                     ToLongFunction<? super T> converter,
                                     LongComparator comparator,
                                     int from, int to, long key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByIndex(i -> comparator.compareAsLong(converter.applyAsLong(getter.apply(i)), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive long, given from any object
     * of one type a function can get a primitive long
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to a primitive long
     * @param comparator - a comparator for primitive long values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param a          primitive long key value
     * @return
     */
    public static <T> int findByLong(T[] array,
                                     ToLongFunction<? super T> converter,
                                     LongComparator comparator,
                                     int from, int to, long key)
    {
        rangeCheck(array.length, from, to);
        return findByIndex(i -> comparator.compareAsLong(converter.applyAsLong(array[i]), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param list      of one type of objects
     * @param converter from one type of objects to a primitive double
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param a         primitive double key value
     * @return
     */
    public static <T> int findByDouble(List<? extends T> list,
                                       ToDoubleFunction<? super T> converter,
                                       int from, int to, double key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByDouble(i -> converter.applyAsDouble(getter.apply(i)), from, to, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param array     of one type of objects
     * @param converter from one type of objects to a primitive double
     * @param from      - an index (inclusive) from which to start search
     * @param to        - an index (exclusive) from which to start search
     * @param a         primitive double key value
     * @return
     */
    public static <T> int findByDouble(T[] array,
                                       ToDoubleFunction<? super T> converter,
                                       int from, int to, double key)
    {
        rangeCheck(array.length, from, to);
        return findByDouble(i -> converter.applyAsDouble(array[i]), from, to, key);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param list       of one type of objects
     * @param converter  from one type of objects to a primitive double
     * @param comparator - a comparator for primitive double values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param a          primitive double key value
     * @return
     */
    public static <T> int findByDouble(List<? extends T> list,
                                       ToDoubleFunction<? super T> converter,
                                       DoubleComparator comparator,
                                       int from, int to, double key)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByIndex(i -> comparator.compareAsDouble(converter.applyAsDouble(getter.apply(i)), key), from, to);
    }

    /**
     * Binary search supporting search for one type of objects
     * using primitive double, given from any object
     * of one type a function can get a primitive double
     *
     * @param array      of one type of objects
     * @param converter  from one type of objects to a primitive double
     * @param comparator - a comparator for primitive double values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param a          primitive double key value
     * @return
     */
    public static <T> int findByDouble(T[] array,
                                       ToDoubleFunction<? super T> converter,
                                       DoubleComparator comparator,
                                       int from, int to, double key)
    {
        rangeCheck(array.length, from, to);
        return findByIndex(i -> comparator.compareAsDouble(converter.applyAsDouble(array[i]), key), from, to);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by some key object, using the getter
     * who, given an index in the invisible structure, can produce a key
     * object someway used to sort it.
     *
     * @param getter - a function accepting indexes, producing a key object used for sort
     * @param from   - an index (inclusive) from which to start search
     * @param to     - an index (exclusive) from which to start search
     * @param key    - a key object
     */
    public static <E extends Comparable<? super E>> int find(IntFunction<? extends E> getter,
                                                             int from, int to, E key)
    {
        return findByIndex(i -> getter.apply(i).compareTo(key), from, to);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by some key object, using the getter
     * who, given an index in the invisible structure, can produce a key
     * object someway used to sort it.
     *
     * @param getter     - a function accepting indexes, producing a key object used for sort
     * @param comparator - a comparator for objects of type E
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param key        - a key object
     */
    public static <E> int find(IntFunction<? extends E> getter,
                               Comparator<? super E> comparator,
                               int from, int to, E key)
    {
        return findByIndex(i -> comparator.compare(getter.apply(i), key), from, to);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by primitive integer key,
     * using the getter who, given an index in the invisible structure, can produce
     * the primitive integer key someway used to sort it.
     *
     * @param getter - a function accepting indexes, producing a primitive integer used for sort
     * @param from   - an index (inclusive) from which to start search
     * @param to     - an index (exclusive) from which to start search
     * @param key    - a primitive integer key
     */
    public static int findByInt(IntUnaryOperator getter,
                                int from, int to, int key)
    {
        return findByInt(getter, Integer::compare, from, to, key);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by primitive integer key,
     * using the getter who, given an index in the invisible structure, can produce
     * the primitive integer key someway used to sort it.
     *
     * @param getter     - a function accepting indexes, producing a primitive integer used for sort
     * @param comparator - a comparator for primitive integers
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param key        - a primitive integer key
     */
    public static int findByInt(IntUnaryOperator getter,
                                IntBinaryOperator comparator,
                                int from, int to, int key)
    {
        return findByIndex(i -> comparator.applyAsInt(getter.applyAsInt(i), key), from, to);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by primitive long key,
     * using the getter who, given an index in the invisible structure, can produce
     * the primitive long key someway used to sort it.
     *
     * @param getter - a function accepting indexes, producing a primitive long used for sort
     * @param from   - an index (inclusive) from which to start search
     * @param to     - an index (exclusive) from which to start search
     * @param key    - a primitive long key
     */
    public static int findByLong(LongGetter getter,
                                 int from, int to, long key)
    {
        return findByLong(getter, Long::compare, from, to, key);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by primitive long key,
     * using the getter who, given an index in the invisible structure, can produce
     * the primitive long key someway used to sort it.
     *
     * @param getter     - a function accepting indexes, producing a primitive long used for sort
     * @param comparator - a comparator for primitive long values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param key        - a primitive long key
     */
    public static int findByLong(LongGetter getter,
                                 LongComparator comparator,
                                 int from, int to, long key)
    {
        return findByIndex(i -> comparator.compareAsLong(getter.getAsLong(i), key), from, to);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by primitive double key,
     * using the getter who, given an index in the invisible structure, can produce
     * the primitive double key someway used to sort it.
     *
     * @param getter - a function accepting indexes, producing a primitive double used for sort
     * @param from   - an index (inclusive) from which to start search
     * @param to     - an index (exclusive) from which to start search
     * @param key    - a primitive double key
     */
    public static int findByDouble(DoubleGetter getter,
                                   int from, int to, double key)
    {
        return findByDouble(getter, Double::compare, from, to, key);
    }

    /**
     * Blind binary search, presuming there is some sorted structure,
     * whose sorting is someway ensured by primitive double key,
     * using the getter who, given an index in the invisible structure, can produce
     * the primitive double key someway used to sort it.
     *
     * @param getter     - a function accepting indexes, producing a primitive double used for sort
     * @param comparator - a comparator for primitive double values
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     * @param key        - a primitive double key
     */
    public static int findByDouble(DoubleGetter getter,
                                   DoubleComparator comparator,
                                   int from, int to, double key)
    {
        return findByIndex(i -> comparator.compareAsDouble(getter.getAsDouble(i), key), from, to);
    }

    /**
     * Blind binary search applying array elements to matching function until it returns 0
     *
     * @param list    of one type of objects
     * @param matcher - a matcher returning comparison result based on single list element
     **/
    public static <T> int findByMatch(T[] array,
                                      ToIntFunction<? super T> matcher)
    {
        return findByMatch(array, matcher, 0, array.length);
    }

    /**
     * Blind binary search applying List elements to matching function until it returns 0
     *
     * @param list    of one type of objects
     * @param matcher - a matcher returning comparison result based on single list element
     **/
    public static <T> int findByMatch(List<? extends T> list,
                                      ToIntFunction<? super T> matcher)
    {
        return findByMatch(list, matcher, 0, list.size());
    }

    /**
     * Blind binary search applying array elements to matching function until it returns 0
     *
     * @param list    of one type of objects
     * @param matcher - a matcher returning comparison result based on single list element
     * @param from    - an index (inclusive) from which to start search
     * @param to      - an index (exclusive) from which to start search
     **/
    public static <T> int findByMatch(T[] array,
                                      ToIntFunction<? super T> matcher,
                                      int from,
                                      int to)
    {
        rangeCheck(array.length, from, to);
        return findByIndex(i -> matcher.applyAsInt(array[i]), from, to);
    }

    /**
     * Blind binary search applying List elements to matching function until it returns 0
     *
     * @param list    of one type of objects
     * @param matcher - a matcher returning comparison result based on single list element
     * @param from    - an index (inclusive) from which to start search
     * @param to      - an index (exclusive) from which to start search
     **/
    public static <T> int findByMatch(List<? extends T> list,
                                      ToIntFunction<? super T> matcher,
                                      int from,
                                      int to)
    {
        IntFunction<? extends T> getter = listGetter(list);
        return findByIndex(i -> matcher.applyAsInt(getter.apply(i)), from, to);
    }

    /**
     * Blind binary search applying index to comparison function until it returns 0
     *
     * @param comparator - index-comparing function
     * @param from       - an index (inclusive) from which to start search
     * @param to         - an index (exclusive) from which to start search
     **/
    public static int findByIndex(IntUnaryOperator comparator, int from, int to)
    {
        int low = from;
        int high = to - 1;

        while (low <= high)
        {
            int mid = low + high >>> 1;
            int cmp = comparator.applyAsInt(mid);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    /**
     * A copy of Arrays.rangeCheck private method from JDK
     */
    private static void rangeCheck(int arrayLength, int fromIndex, int toIndex)
    {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        if (fromIndex < 0)
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLength)
            throw new ArrayIndexOutOfBoundsException(toIndex);
    }

    /**
     * A copy of Collections.get private method from JDK
     */
    private static <T> T get(ListIterator<? extends T> i, int index)
    {
        T obj = null;
        int pos = i.nextIndex();
        if (pos <= index)
            do
            {
                obj = i.next();
            }
            while (pos++ < index);
        else
            do
            {
                obj = i.previous();
            }
            while (--pos > index);
        return obj;
    }

    private static <T, L extends List<? extends T>> IntFunction<? extends T> listGetter(L list)
    {
        if (list instanceof RandomAccess)
            return list::get;

        ListIterator<? extends T> it = list.listIterator();
        return i -> get(it, i);
    }

    @FunctionalInterface
    public interface LongComparator
    {
        int compareAsLong(long f1, long f2);
    }

    @FunctionalInterface
    public interface DoubleComparator
    {
        int compareAsDouble(double f1, double f2);
    }

    @FunctionalInterface
    public interface LongGetter
    {
        long getAsLong(int i);
    }

    @FunctionalInterface
    public interface DoubleGetter
    {
        double getAsDouble(int i);
    }
}