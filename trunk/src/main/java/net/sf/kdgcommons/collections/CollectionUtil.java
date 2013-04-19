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

package net.sf.kdgcommons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.regex.Pattern;


/**
 *  Static utility methods for working with collections -- particularly
 *  parameterized collections.
 */
public class CollectionUtil
{
    /**
     *  Returns a set containing the passed elements.
     */
    public static <T> Set<T> asSet(T... elems)
    {
        Set<T> result = new HashSet<T>();
        for (T elem : elems)
            result.add(elem);
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
        if (expr)
            coll.add(value);

        return coll;
    }


    /**
     *  Verifies that the passed list contains only elements of the given
     *  type, and returns it as a parameterized type. Throws if any element
     *  is a different type.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> cast(List<?> list, Class<T> klass)
    {
        for (Object obj : list)
        {
            klass.cast(obj);
        }
        return (List<T>)list;
    }


    /**
     *  Verifies that the passed set contains only elements of the given
     *  type, and returns it as a parameterized type. Throws if any element
     *  is a different type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> cast(Set<?> set, Class<T> klass)
    {
        for (Object obj : set)
        {
            klass.cast(obj);
        }
        return (Set<T>)set;
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
     *  @ince 1.0.2
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
     *  Applies the given regex to the string value of every item in the passed
     *  list, building a new list from those value that either match or do not
     *  match. Null entries are treated as an empty string for matching, but
     *  will be returned as null.
     *
     *  @since 1.0.3
     *
     *  @param  list    The source list; this is unmodified.
     *  @param  regex   Regex applied to every string in the list.
     *  @param  include If <code>true</code>, strings that match are copied
     *                  to the output list; if <code>false</code>, strings
     *                  that don't match are copied.
     */
    public static <T> List<T> filter(List<T> list, String regex, boolean include)
    {
        Pattern pattern = Pattern.compile(regex);
        List<T> result = new ArrayList<T>(list.size());
        for (T obj : list)
        {
            String str = (obj == null) ? "" : obj.toString();
            if (pattern.matcher(str).matches() == include)
                result.add(obj);
        }
        return result;
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
     *  Returns the default <code>Iterable</code> if the regular object is null.
     *  This is used for a null-safe for loop.
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
     *  Applies the specified functor to every element of the given collection, in
     *  its natural iteration order, and returns a list of the results.
     *  <p>
     *  If the functor throws, it will be rethrown in a {@link #MapException}, which
     *  provides detailed information and partial work.
     */
    public static <V,R> List<R> map(Collection<V> coll, MapFunctor<V,R> functor)
    {
        List<R> result = new ArrayList<R>(coll.size());
        int index = 0;
        for (V value : coll)
        {
            try
            {
                result.add(functor.invoke(index, value));
                index++;
            }
            catch (Throwable ex)
            {
                throw new MapException(ex, index, value, result);
            }
        }
        return result;
    }


    /**
     *  Applies the specified functor to every element in the given collection, with
     *  the expectation that it will return a single value based on the item and any
     *  previous value.
     */
    public static <V,R> R reduce(Collection<V> coll, ReduceFunctor<V,R> functor)
    {
        R pendingResult = null;
        int index = 0;
        for (V value : coll)
        {
            try
            {
                pendingResult = functor.invoke(index, value, pendingResult);
                index++;
            }
            catch (Throwable ex)
            {
                throw new ReduceException(ex, index, value, pendingResult);
            }
        }
        return pendingResult;
    }


//----------------------------------------------------------------------------
//  Supporting Objects
//----------------------------------------------------------------------------

    /**
     *  A functor interface for {@link #map}. The {@link #invoke} function is
     *  called for every element in the collection, and is passed the element
     *  value and its position (0-based) in the iteration order.
     *  <p>
     *  The implementation is permitted to throw anything, checked or not.
     */
    public interface MapFunctor<V,R>
    {
        public R invoke(int index, V value)
        throws Throwable;
    }


    /**
     *  An exception wrapper for {@link #map}. Contains the wrapped exception,
     *  the value and index that caused the exception, and the results-to-date.
     *  <p>
     *  Note: because Java does not allow parameterization of <code>Throwable</code>
     *  subclasses (JLS XX), the value and results are held as <code>Object</code>s.
     */
    public static class MapException
    extends RuntimeException
    {
        private static final long serialVersionUID = 1;

        private int _index;
        private Object _value;
        private List<?> _partialResults;

        public MapException(Throwable cause, int index, Object value, List<?> partialResults)
        {
            super(cause);
            _index = index;
            _value = value;
            _partialResults = partialResults;
        }

        /**
         *  Returns the position (0-based) in the original collection's iteration where the
         *  wrapped exception was thrown.
         */
        public int getIndex()
        {
            return _index;
        }

        /**
         *  Returns the value that caused the exception.
         */
        public Object getValue()
        {
            return _value;
        }

        /**
         *  Returns any partial results from the map operation.
         *  <p>
         *  Warning: the contents of this list are undefined in the case of a parallel map
         *  operation.
         */
        public List<?> getPartialResults()
        {
            return _partialResults;
        }
    }


    /**
     *  A functor used for the {@link #reduce} operation. The {@link #invoke}
     *  function is called for every element of a collection, and is responsible
     *  for aggregating the results. On the first invocation, the "pending"
     *  result is <code>null</code>; on subsequent invocations, it is the value
     *  returned from the previous invocation.
     */
    public interface ReduceFunctor<V, R>
    {
        public R invoke(int index, V value, R pendingResult)
        throws Throwable;
    }


    /**
     *  An exception wrapper for {@link #reduce}. Contains the wrapped exception,
     *  the value and index that caused the exception, and the results-to-date.
     *  <p>
     *  Note: because Java does not allow parameterization of <code>Throwable</code>
     *  subclasses (JLS XX), the value and results are held as <code>Object</code>s.
     */
    public static class ReduceException
    extends RuntimeException
    {
        private static final long serialVersionUID = 1;

        private int _index;
        private Object _value;
        private Object _partialResults;

        public ReduceException(Throwable cause, int index, Object value, Object partialResults)
        {
            super(cause);
            _index = index;
            _value = value;
            _partialResults = partialResults;
        }

        /**
         *  Returns the position (0-based) in the original collection's iteration where the
         *  wrapped exception was thrown.
         */
        public int getIndex()
        {
            return _index;
        }

        /**
         *  Returns the value that caused the exception.
         */
        public Object getValue()
        {
            return _value;
        }

        /**
         *  Returns any partial results. This is the <code>pendingResult</code>
         *  value passed to the functor at the time the exception was thrown.
         */
        public Object getPartialResults()
        {
            return _partialResults;
        }
    }
}
