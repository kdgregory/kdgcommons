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

package com.kdgregory.kdgcommons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 *  A <code>Map</code> of thread-safe counters, identified by a user-defined key.
 *  Typically used to aggregate information from a stream of data.
 *  <p>
 *  Counters are created explicitly by {@link #put putting} a value, implicitly by
 *  {@link #increment incrementing} a value that has not yet been set (the implicit
 *  starting value is zero). Counters may be set, retrieved, incremented, decremented,
 *  or removed. You can deal with counters as primitive <code>int</code> values,
 *  primitive <code>long</code> values, or as <code>Long</code> objects.
 *  <p>
 *  In addition to the set views provided by the <code>Map</code> interface, this
 *  object is also directly iterable; such iteration is equivalent to iterating on
 *  <code>entrySet()</code>.
 *  <p>
 *  Mappings are stored in a <code>ConcurrentHashMap</code>, and behavior of iterators
 *  and set retrieval methods reflect this fact. Note, however, that this class does
 *  <em>not</em> implement <code>ConcurrentMap</code>, because the internal storage is
 *  not conducive to implementing <code>replace()</code> and similar methods (at
 *  least not without adding lots of locks that aren't needed for the common case).
 *  <p>
 *  Values may not be <code>null</code>.
 *
 *  @since 1.0.10
 */
public class Counters<K>
implements Map<K,Long>, Iterable<Map.Entry<K,Long>>
{
    private ConcurrentHashMap<K,AtomicLong> map = new ConcurrentHashMap<K,AtomicLong>();

//----------------------------------------------------------------------------
//  Object overrides
//----------------------------------------------------------------------------

    /**
     *  Outputs all counters in the format "[ NAME: VALUE, ...]".
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(16384);
        sb.append("[");
        for (Map.Entry<K,AtomicLong> entry : map.entrySet())
        {
            if (sb.length() > 1)
                sb.append(", ");

            sb.append(String.valueOf(entry.getKey()))
              .append(": ")
              .append(entry.getValue().longValue());
        }
        sb.append("]");
        return sb.toString();
    }

//----------------------------------------------------------------------------
//  Implementation of Map
//----------------------------------------------------------------------------

    /**
     *  Returns the number of mappings (distinct keys) in this object.
     */
    @Override
    public int size()
    {
        return map.size();
    }


    /**
     *  Returns <code>true</code> if the underlying map is empty.
     */
    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }


    /**
     *  Returns <code>true</code> if the underlying map contains the specified key.
     */
    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }


    /**
     *  Returns <code>true</code> if the underlying map contains the specified value.
     *  The passed value must be a <code>Number</code>; its <code>longValue</code> is
     *  tested against the values in the map.
     *  <p>
     *  As this operation iterates the map, it represents a point in time, and may not
     *  reflect concurrent updates (of course, why anyone would test for a value at
     *  the same time they're updating the map is a mystery to me).
     */
    @Override
    public boolean containsValue(Object value)
    {
        long test = ((Number)value).longValue();
        for (AtomicLong entry : map.values())
        {
            if (test == entry.get())
                return true;
        }
        return false;
    }


    /**
     *  Returns the value associated with the specified key, <code>null</code> if
     *  there is not a mapping for the key. Note that this behavior differs from
     *  {@link #getLong}, which returns 0 if there is no mapping for the key.
     */
    @Override
    public Long get(Object key)
    {
        AtomicLong value = map.get(key);
        return translate(value);
    }


    /**
     *  Stores a new mapping, returning the previous mapping.
     *  <p>
     *  This method does not attempt to prevent multiple threads from updating the
     *  map at the same time; concurrent calls could return the same old value. If
     *  this is a problem for you, call {@link #putIfAbsent}.
     */
    @Override
    public Long put(K key, Long newValue)
    {
        AtomicLong value = map.get(key);
        Long oldValue = translate(value);
        if (value == null)
            map.put(key, new AtomicLong(newValue.longValue()));
        else
            value.set(newValue.longValue());

        return oldValue;
    }


    /**
     *  Removes a mapping and returns it. If called concurrently, only one call will
     *  actually remove the value.
     */
    @Override
    public Long remove(Object key)
    {
        AtomicLong oldValue = map.remove(key);
        return translate(oldValue);
    }


    /**
     *  Initializes this object from the passed map. This is not an atomic operation;
     *  each value is added individually, and other threads are allowed to update the
     *  map concurrently.
     */
    @Override
    public void putAll(Map<? extends K,? extends Long> src)
    {
        for (Map.Entry<? extends K,? extends Long> entry : src.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }


    @Override
    public void clear()
    {
        map.clear();
    }


    /**
     *  Returns the current keys in the map. Note that this represents a point-in-time
     *  view of the map, so if you are concurrently adding or removing counters it may
     *  be missing keys or contain keys that are no longer in the map.
     */
    @Override
    public Set<K> keySet()
    {
        // in Java 8, ConcurrentHashMap broke binary compatibility for keySet(), returning
        // a different concrete class; this cast forces the compiler to use an invokeinterface
        // rather than invokevirtual, so the bytecode remains compatible

        return ((Map<K,AtomicLong>)map).keySet();
    }


    /**
     *  Returns the current values in the map. This operation involves a translation from
     *  internal representation to <code>Long</code>, so is relatively inefficient. As it
     *  uses the underlying map iterator to work, the returned collection may not reflect
     *  concurrent updates.
     */
    @Override
    public Collection<Long> values()
    {
        List<Long> result = new ArrayList<Long>(map.size());
        for (AtomicLong value : map.values())
        {
            result.add(Long.valueOf(value.get()));
        }
        return result;
    }


    /**
     *  Returns the current mappings. This involves a translation from internal
     *  representation, so is relatively inefficient; if you want to iterate the
     *  map, call {@link #iterator}.
     */
    @Override
    public Set<Map.Entry<K,Long>> entrySet()
    {
        Set<Map.Entry<K,Long>> entries = new HashSet<Map.Entry<K,Long>>();
        for (Map.Entry<K,Long> entry : this)
        {
            entries.add(entry);
        }
        return entries;
    }


    /**
     *  Adds a mapping for the value, if one does not already exist. See the
     *  JavaDoc for <code>ConcurrentMap.putIfAbsent()</code> for behavior.
     */
    @Override
    public Long putIfAbsent(K key, Long value)
    {
        AtomicLong oldValue = map.get(key);
        if (oldValue == null)
        {
            oldValue = map.putIfAbsent(key, new AtomicLong(value.longValue()));
        }

        return translate(oldValue);
    }


    /**
     *  Returns an iterator for the entries in the map. The returned iterator
     *  represents a point in time; it is not subject to concurrent modification
     *  exceptions, but does not reflect any changes after it is retrieved.
     */
    @Override
    public Iterator<Map.Entry<K,Long>> iterator()
    {
        return new MyIterator(map.entrySet().iterator());
    }

//----------------------------------------------------------------------------
//  Additional Public Methods
//----------------------------------------------------------------------------

    /**
     *  Retrieves the value of the specified key as a primitive long. If there is no
     *  mapping for the key, returns 0.
     */
    public long getLong(K key)
    {
        AtomicLong mapping = map.get(key);
        return (mapping == null) ? 0 : mapping.get();
    }


    /**
     *  Retrieves the value of the specified key as a primitive int. If there is no
     *  mapping for the key, returns 0. This is only valid if you know that your
     *  counters will remain in integer range (but often works better with other
     *  variables in your code).
     */
    public int getInt(K key)
    {
        AtomicLong mapping = map.get(key);
        return (mapping == null) ? 0 : (int)mapping.get();
    }


    /**
     *  Sets the mapping to the specified value. This is equivalent to calling
     *  {@link #put}, with the same caveats regarding concurent access.
     */
    public void putLong(K key, long value)
    {
        put(key, Long.valueOf(value));
    }


    /**
     *  Increments the specified counter, creating it if necessary. This method
     *  may be called concurrently without fear of a race condition (even when
     *  creating a new counter).
     *
     *  @return The current (post-increment) value of the counter.
     */
    public long increment(K key)
    {
        AtomicLong counter = getOrCreate(key);
        return counter.incrementAndGet();
    }


    /**
     *  Decrements the specified counter, creating it if necessary. This method
     *  may be called concurrently without fear of a race condition (even when
     *  creating a new counter).
     *
     *  @return The current (post-increment) value of the counter.
     */
    public long decrement(K key)
    {
        AtomicLong counter = getOrCreate(key);
        return counter.decrementAndGet();
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private Long translate(AtomicLong value)
    {
        return (value == null) ? null : Long.valueOf(value.get());
    }


    private AtomicLong getOrCreate(K key)
    {
        AtomicLong value = map.get(key);
        if (value != null)
            return value;

        map.putIfAbsent(key, new AtomicLong());
        return map.get(key);
    }


    private class MyIterator
    implements Iterator<Map.Entry<K,Long>>
    {
        private Iterator<Map.Entry<K,AtomicLong>> baseItx;

        public MyIterator(Iterator<Map.Entry<K,AtomicLong>> baseItx)
        {
            this.baseItx = baseItx;
        }

        @Override
        public boolean hasNext()
        {
            return baseItx.hasNext();
        }

        @Override
        public Map.Entry<K,Long> next()
        {
            Map.Entry<K,AtomicLong> next = baseItx.next();
            return new MyMapEntry(next.getKey(), next.getValue());
        }

        @Override
        public void remove()
        {
            baseItx.remove();
        }
    }


    private class MyMapEntry
    implements Map.Entry<K,Long>
    {
        private K key;
        private AtomicLong value;

        public MyMapEntry(K key, AtomicLong value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public Long getValue()
        {
            return Long.valueOf(value.longValue());
        }

        @Override
        public Long setValue(Long value)
        {
            long oldValue = this.value.getAndSet(value.longValue());
            return Long.valueOf(oldValue);
        }
    }
}
