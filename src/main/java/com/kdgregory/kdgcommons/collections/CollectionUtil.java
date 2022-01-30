// Copyright Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.kdgcommons.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;


/**
 *  Static utility methods for working with collections -- particularly
 *  parameterized collections.
 */
public class CollectionUtil
{
    private CollectionUtil()
    {
        // this is here to prevent instantiation
    }


    /**
     *  Returns a set (<code>HashSet</code>) containing the passed elements.
     */
    public static <T> HashSet<T> asSet(T... elems)
    {
        HashSet<T> result = new HashSet<T>();
        for (T elem : elems)
            result.add(elem);
        return result;
    }


    /**
     *  Returns a map (<code>HashMap</code>) built from the passed elements. Elements
     *  alternate between keys and values, and if given an odd number of elements the
     *  last will be used as the key for a null value. Elements will be added in the
     *  order they appear as parameters, so repeating a key will mean that only the
     *  last value is stored.
     *  <p>
     *  Note that the result is parameterized as <code>Object,Object</code>; as there
     *  is no way to differentiate between keys and values with varargs, we have to
     *  use the lowest common denominator.
     *
     *  @since 1.0.15
     */
    public static HashMap<Object,Object> asMap(Object... elems)
    {
        HashMap<Object,Object> result = new HashMap<Object,Object>();
        for (int ii = 0 ; ii < elems.length ; ii += 2)
        {
            Object key = elems[ii];
            Object value = (ii < elems.length - 1)
                         ? elems[ii + 1]
                         : null;
            result.put(key, value);
        }
        return result;
    }


    /**
     *  Appends an arbitrary number of explicit elements to an existing collection.
     *  Primarily useful when writing testcases.
     */
    public static <T> void addAll(Collection<T> coll, T... elems)
    {
        for (T elem : elems)
            coll.add(elem);
    }


    /**
     *  Appends the values returned by an iterator to the passed collection.
     */
    public static <T> void addAll(Collection<T> coll, Iterator<T> src)
    {
        while (src.hasNext())
            coll.add(src.next());
    }


    /**
     *  Appends the contents of an iterable object to the passed collection.
     */
    public static <T> void addAll(Collection<T> coll, Iterable<T> src)
    {
        addAll(coll, src.iterator());
    }


    /**
     *  Adds a value to the collection if the boolean expression is true.
     *  Returns the collection as a convenience for chained invocations.
     *
     *  @since 1.0.8
     */
    public static <T> Collection<T> addIf(Collection<T> coll, T value, boolean expr)
    {
        if (expr)  coll.add(value);
        return coll;
    }


    /**
     *  Adds a value to the collection if it's not null. Returns the collection
     *  as a convenience for chained invocations.
     *
     *  @since 1.0.11
     */
    public static <T> Collection<T> addIfNotNull(Collection<T> coll, T value)
    {
        return addIf(coll, value, value != null);
    }


    /**
     *  Stores an entry in a map if the boolean expression is true. Returns the
     *  map as a convenience for chained invocations.
     *
     *  @since 1.0.15
     */
    public static <K,V> Map<K,V> putIf(Map<K,V> map, K key, V value, boolean expr)
    {
        if (expr) map.put(key, value);
        return map;
    }


    /**
     *  Stores an entry in a map if the value is not <code>null</code>. Returns the
     *  map as a convenience for chained invocations.
     *
     *  @since 1.0.15
     */
    public static <K,V> Map<K,V> putIfNotNull(Map<K,V> map, K key, V value)
    {
        return putIf(map, key, value, value != null);
    }


    /**
     *  Adds the specified item to a map if it does not already exist. Returns
     *  either the added item or the existing mapping.
     *  <p>
     *  <em>Note:</em>    The return behavior differs from <code>Map.put()</code>,
     *                    in that it returns the new value if there was no prior
     *                    mapping. I find this more useful, as I typically want
     *                    to do something with the mapping.
     *  <p>
     *  <em>Warning:</em> This operation is not synchronized. In most cases, a
     *                    better approach is to use {@link DefaultMap}, with a
     *                    functor to generate new entries.
     *
     *  @since 1.0.12
     */
    public static <K,V> V putIfAbsent(Map<K,V> map, K key, V value)
    {
        if (! map.containsKey(key))
        {
            map.put(key,value);
            return value;
        }

        return map.get(key);
    }


    /**
     *  Adds entries from <code>add</code> to <code>base</code> where there is not
     *  already a mapping with the same key.
     *
     *  @since 1.0.14
     */
    public static <K,V> void putIfAbsent(Map<K,V> base, Map<K,V> add)
    {
        for (Map.Entry<K,V> entry : add.entrySet())
            putIfAbsent(base, entry.getKey(), entry.getValue());
    }


    /**
     *  Returns the first element of the passed list, <code>null</code> if
     *  the list is empty or null.
     */
    public static <T> T first(List<T> list) {
        return isNotEmpty(list) ? list.get(0) : null;
    }


    /**
     *  Returns the last element of the passed list, <code>null</code> if
     *  the list is empty or null. Uses an indexed get unless the list is
     *  a subclass of <code>java.util.LinkedList</code>
     */
    public static <T> T last(List<T> list) {
        if (isEmpty(list))              return null;

        if (list instanceof LinkedList<?>) return ((LinkedList<T>)list).getLast();
        return list.get(list.size() - 1);
    }


    /**
     *  Verifies that the passed list contains only elements of the given type,
     *  and returns it as a parameterized list (does not create a new list).
     *  <p>
     *  This function exists to avoid suppressing warnings in application code.
     *
     *  @throws ClassCastException if any element is a different type.
     */
    public static <T> List<T> cast(List<?> list, Class<T> klass)
    {
        for (Object obj : list)
        {
            klass.cast(obj);
        }
        return (List<T>)list;
    }


    /**
     *  Verifies that the passed set contains only elements of the given type,
     *  and returns it as a parameterized set (does not create a new set).
     *  <p>
     *  This function exists to avoid suppressing warnings in application code.
     *
     *  @throws ClassCastException if any element is a different type.
     */
    public static <T> Set<T> cast(Set<?> set, Class<T> klass)
    {
        for (Object obj : set)
        {
            klass.cast(obj);
        }
        return (Set<T>)set;
    }


    /**
     *  Verifies that the passed map contains only keys and values of the given
     *  types, and returns it as a parameterized map (does not create a new map).
     *  <p>
     *  This function exists to avoid suppressing warnings in application code.
     *
     *  @throws ClassCastException if any key/value is a different type.
     *
     *  @since 1.0.15
     */
    public static <K,V> Map<K,V> cast(Map<?,?> map, Class<K> keyClass, Class<V> valueClass)
    {
        for (Map.Entry<?,?> entry : map.entrySet())
        {
            keyClass.cast(entry.getKey());
            valueClass.cast(entry.getValue());
        }
        return (Map<K,V>)map;
    }


    /**
     *  Resizes the passed list to N entries. Will add the specified object if
     *  the list is smaller than the desired size, discard trailing entries if
     *  larger. This is primarily used to presize a list that will be accessed
     *  by index, but it may also be used to truncate a list for display.
     *
     *  @return The list, as a convenience for callers
     *
     *  @throws UnsupportedOperationException if the list does not implement
     *          <code>RandomAccess</code> and its list iterator does not
     *          support the <code>remove()</code> operation
     */
    public static <T> List<T> resize(List<T> list, int newSize, T obj)
    {
        if (list instanceof ArrayList)
            ((ArrayList<T>)list).ensureCapacity(newSize);

        if (list.size() < newSize)
        {
            for (int ii = list.size() ; ii < newSize ; ii++)
                list.add(obj);
        }
        else if (list.size() > newSize)
        {
            if (list instanceof RandomAccess)
            {
                for (int ii = list.size() - 1 ; ii >= newSize ; ii--)
                    list.remove(ii);
            }
            else
            {
                ListIterator<T> itx = list.listIterator(newSize);
                while (itx.hasNext())
                {
                    itx.next();
                    itx.remove();
                }
            }
        }

        return list;
    }


    /**
     *  Resizes the passed list to N entries. Will add nulls if the list is
     *  smaller than the desired size, discard trailing entries if larger.
     *  This is primarily used to presize a list that will be accessed by
     *  index, but it may also be used to truncate a list for display.
     *
     *  @return The list, as a convenience for callers
     *  @throws UnsupportedOperationException if the list does not implement
     *          <code>RandomAccess</code> and its list iterator does not
     *          support the <code>remove()</code> operation
     */
    public static <T> List<T> resize(List<T> list, int newSize)
    {
        return resize(list, newSize, null);
    }


    /**
     *  Iterates the passed collection, converts its elements to strings, then
     *  concatenates those strings with the specified delimiter between them.
     *  Nulls are converted to empty strings.
     *
     *  @since 1.0.2
     */
    public static <T> String join(Iterable<T> coll, String delim)
    {
        if (coll == null)
            return "";

        boolean isFirst = true;
        StringBuilder buf = new StringBuilder(1024);
        for (T item : coll)
        {
            if (isFirst)
                isFirst = false;
            else
                buf.append(delim);

            if (item != null)
                buf.append(String.valueOf(item));
        }
        return buf.toString();
    }


    /**
     *  Adds all elements of the <code>src</code> collections to <code>dest</code>,
     *  returning <code>dest</code>. This is typically used when you need to combine
     *  collections temporarily for a method argument.
     *
     *  <p><strong>Warning:</strong> this mutates the provided list. In version 2.0
     *  it will create a new list, with a variant of <code>addAll()</code> that mutates
     *  the list.
     *
     *  @since 1.0.7
     */
    public static <T> List<T> combine(List<T> dest, Collection<T>... src)
    {
        for (Collection<T> cc : src)
        {
            dest.addAll(cc);
        }
        return dest;
    }


    /**
     *  Adds all elements of the <code>src</code> collections to <code>dest</code>,
     *  returning <code>dest</code>. This is typically used when you need to combine
     *  collections temporarily for a method argument.
     *
     *  <p><strong>Warning:</strong> this mutates the provided set. In version 2.0
     *  it will create a new set, with a variant of <code>addAll()</code> that mutates
     *  the set.
     *
     *  @since 1.0.7
     */
    public static <T> Set<T> combine(Set<T> dest, Collection<T>... src)
    {
        for (Collection<T> cc : src)
        {
            dest.addAll(cc);
        }
        return dest;
    }


    /**
     *  Adds all elements of the <code>src</code> collections to <code>dest</code>,
     *  returning <code>dest</code>. This is typically used when you need to combine
     *  collections temporarily for a method argument.
     *  <p>
     *  Note: source maps are added in order; if the same keys are present in multiple
     *  sources, the last one wins.
     *
     *  <p><strong>Warning:</strong> this mutates the provided map. In version 2.0
     *  it will create a new map, with a variant of <code>putAll()</code> that mutates
     *  the map.
     *
     *  @since 1.0.7
     */
    public static <K,V> Map<K,V> combine(Map<K,V> dest, Map<K,V>... src)
    {
        for (Map<K,V> cc : src)
        {
            dest.putAll(cc);
        }
        return dest;
    }


    /**
     *  Returns <code>true</code> if the passed collection is either <code>null</code>
     *  or has size 0.
     */
    public static boolean isEmpty(Collection<?> c)
    {
        return (c == null)
             ? true
             : (c.size() == 0);
    }


    /**
     *  Returns <code>true</code> if the passed collection is not <code>null</code>
     *  and has size &gt; 0.
     */
    public static boolean isNotEmpty(Collection<?> c)
    {
        return (c != null) && (c.size() > 0);
    }


    /**
     *  Returns <code>true</code> if the passed map is either <code>null</code>
     *  or has size 0.
     */
    public static boolean isEmpty(Map<?,?> m)
    {
        return (m == null)
             ? true
             : (m.size() == 0);
    }


    /**
     *  Returns <code>true</code> if the passed map is not <code>null</code>
     *  and has size &gt; 0.
     */
    public static boolean isNotEmpty(Map<?,?> m)
    {
        return (m != null) && (m.size() > 0);
    }


    /**
     *  Compares two collections of <code>Comparable</code> elements. The two collections are
     *  iterated, and the first not-equal <code>compareTo()</code> result is returned. If the
     *  collections are of equal length and contain the same elements in iteration order, they
     *  are considered equal. If they are of unequal length but contain the same elements in
     *  iteration order, the shorter is considered less than the longer.
     *  <p>
     *  Note that two collections that are equal based on their intrinsic <code>equals()</code>
     *  method, but iterate in a different order (ie, hash-based collections) are not considered
     *  equal by this method.
     *
     *   @since 1.0.14
     */
    @SuppressWarnings("rawtypes")
    public static int compare(Collection<? extends Comparable> c1, Collection<? extends Comparable> c2)
    {
        Iterator<? extends Comparable> itx1 = c1.iterator();
        Iterator<? extends Comparable> itx2 = c2.iterator();

        while (itx1.hasNext())
        {
            if (! itx2.hasNext())
                return 1;

            Comparable v1 = itx1.next();
            Comparable v2 = itx2.next();
            int cmp = v1.compareTo(v2);
            if (cmp != 0)
                return cmp;
        }

        if (itx2.hasNext())
            return -1;
        else
            return 0;
    }


    /**
     *  Returns the second iterable if the first is null. This is used for a null-safe
     *  for loop.
     */
    public static <T> Iterable<T> defaultIfNull(Iterable<T> reg, Iterable<T> def)
    {
        return (reg == null) ? def : reg;
    }


    /**
     *  Returns the default collection if the regular object is null or empty.
     */
    public static <T> Collection<T> defaultIfEmpty(Collection<T> reg, Collection<T> def)
    {
        return ((reg == null) || (reg.size() == 0)) ? def : reg;
    }


    /**
     *  Retrieves a value from a nested collection hierarchy by following a sequence
     *  of keys. Supports arbitrary nesting of maps, arrays, and lists.
     *  <p>
     *  Example; given:
     *  <pre>
     *      Map&lt;String,Object&gt; bottom = new HashMap&lt;&gt;();
     *      bottom.put("argle", "bargle");
     *
     *      List&lt;Object&gt; middle = new ArrayList&lt;&gt;();
     *      middle.add(bottom);
     *
     *      Map&lt;String,Object&gt; top = new HashMap&lt;&gt;();
     *      top.put("foo", middle);
     *  </pre>
     *  Then:
     *  <p>
     *  <ul>
     *  <li> <code>CollectionUtil.getVia(top, "foo", 0, "argle");</code> returns <code>"bargle"</code>.
     *  <li> <code>CollectionUtil.getVia(top, "bar", 0, "argle");</code> returns <code>null</code>.
     *  <li> <code>CollectionUtil.getVia(top, "foo", "junk", "argle");</code> throws <code>IllegalArgumentException</code>.
     *  </ul>
     *
     *  @param  root    The root object.
     *  @param  keys    One or more keys. The first key is applied to the root object,
     *                  the second key is applied to the result of that, and so on.
     *                  Arrays and lists may only be accessed via numeric keys; maps
     *                  may be accessed via any type of key.
     *
     *  @return The object located via the sequence of keys, <code>null</code> if
     *          <em>any</em> key along the path does not resolve to an object.
     *
     *  @throws IllegalArgumentException if any object found during the traversal is
     *          not a valid collection type, or if the key is not appropriate for the
     *          collection.
     *
     *  @since 1.0.15
     */
    public static Object getVia(Object root, Object... keys)
    {
        Object current = root;

        // I iterate by index rather than for-in so that I can construct exceptions
        for (int ii = 0 ; ii < keys.length ; ii++)
        {
            try
            {
                Object key = keys[ii];
                if (current == null)
                {
                    return null;
                }
                if (current instanceof Map)
                {
                    current = ((Map<Object,Object>)current).get(key);
                }
                else if (current instanceof List)
                {
                    int index = ((Number)key).intValue();
                    List<Object> list = (List<Object>)current;
                    current = (index < list.size())
                            ? list.get(index)
                            : null;
                }
                else if (current.getClass().isArray())
                {
                    int index = ((Number)key).intValue();
                    current = (index < Array.getLength(current))
                            ? Array.get(current, index)
                            : null;
                }
                else
                {
                    List<Object> currentKeys = Arrays.asList(keys).subList(0, ii+1);
                    throw new IllegalArgumentException(
                            "attempted to get from " + current.getClass().getName()
                            + " (path: " + currentKeys + ")");
                }
            }
            catch (ClassCastException ex)
            {
                // I believe this is the only way that we can get here ...
                List<Object> currentKeys = Arrays.asList(keys).subList(0, ii+1);
                throw new IllegalArgumentException(
                        "attempted to get from " + current.getClass().getName() + " with non-numeric index"
                        + " (path: " + currentKeys + ")");
            }
        }

        return current;
    }


    /**
     *  Partitions the passed iterable into N sublists, each of which has
     *  at most <code>maxSize</code> elements.
     */
    public static <T> List<List<T>> partition(Iterable<T> source, int maxSize)
    {
        if (source == null) return Collections.emptyList();

        List<List<T>> result = new ArrayList<List<T>>();
        List<T> sublist = new ArrayList<T>(maxSize);
        int count = 0;
        for (T item : source)
        {
            sublist.add(item);
            count++;
            if (count >= maxSize)
            {
                result.add(sublist);
                sublist = new ArrayList<T>(maxSize);
                count = 0;
            }
        }
        if (sublist.size() > 0)
        {
            result.add(sublist);
        }
        return result;
    }


    /**
     *  Returns a map that contains all keys in the specified collection.
     *  <p>
     *  The returned map is a HashMap; see variant for choosing map type.
     */
    public static <K,V> Map<K,V> submap(Map<K,V> src, Collection<K> keys)
    {
        return submap(src, keys, new HashMap<K,V>());
    }


    /**
     *  Extracts all mappings from the source map that correspond to the passed
     *  keys, and stores them in the destination map. Returns the destination
     *  map as a convenience.
     */
    public static <K,V> Map<K,V> submap(Map<K,V> src, Collection<K> keys, Map<K,V> dest)
    {
        if ((src == null) || (keys == null) || (dest == null))
        {
            return dest;
        }

        for (K key : keys)
        {
            if (src.containsKey(key))
            {
                dest.put(key, src.get(key));
            }
        }
        return dest;
    }
}
