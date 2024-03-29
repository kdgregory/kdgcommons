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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 *  A map that can have multiple values, and which uses hashed lookups for its keys.
 *  <p>
 *  Unlike other implementations (eg, Google's), this is not based around a JDK
 *  <code>HashMap</code> that uses a JDK <code>Collection</code> object as its
 *  value. Instead, it maintains the values as part of the hash bucket chain. While
 *  this will increase the time to retrieve or add a value, it is significantly
 *  more memory efficient (particularly when there are a small number of values per
 *  key and you enable set semantics).
 *  <p>
 *  Partly as a result, this class does <em>not</em> implement <code>Map</code>. I
 *  decided that the parameterization that would be required did not offer anything
 *  valuable.
 *  <p>
 *  You can configure this multimap to provide either Set or List behavior for its
 *  values, by passing one of the {@link HashMultimap.Behavior} options to the
 *  constructor. With Set semantics, each key-value pair is stored only once;
 *  subsequent puts of the same pair are ignored. With List semantics, each key-value
 *  pair may be stored multiple times.
 *  <p>
 *  Null keys are not allowed. Null values are.
 *  <p>
 *  This class is not threadsafe.
 */
public class HashMultimap<K,V>
implements Serializable
{
    private static final long serialVersionUID = 2L;

    /**
     *  Controls the handling of equal key-value pairs.
     */
    public enum Behavior { LIST, SET }

//----------------------------------------------------------------------------
//  Instance variables and Constructors
//----------------------------------------------------------------------------

    private Behavior behavior;

    private int size = 0;
    private int modCount = 0;

    private HashEntry<K,V>[] table;
    private int mask;

    private int resizeThreshold;
    private int filledSlots;

    // this is updated by calls to findEntry()
    private HashEntry<K,V> prev;


    /**
     *  Base constructor, lets you control everything.
     *
     *  @param  behavior        The desired multi-value behavior.
     *  @param  initialCapacity The initial capacity of the table. This will be rounded
     *                          up to the nearest power of 2 that is >= 8.
     *  @param  loadFactor      A factor used to control the expansion of the table: if
     *                          the number of occupied buckets is >= this fraction of
     *                          current capacity, the table will be doubled.
     */
    @SuppressWarnings("unchecked")
    public HashMultimap(Behavior behavior, int initialCapacity, double loadFactor)
    {
        this.behavior = behavior;

        int realCapacity = 8;
        while (initialCapacity > 8)
        {
            realCapacity <<= 1;
            initialCapacity >>= 1;
        }

        this.mask = realCapacity - 1;
        this.table = (HashEntry<K,V>[])new HashEntry[realCapacity];

        this.resizeThreshold = (int)(table.length * loadFactor);
    }


    /**
     *  Convenience constructor: creates a stable with small initial capacity and
     *  the specified multi-value behavior.
     */
    public HashMultimap(Behavior behavior)
    {
        this(behavior, 8, .75);
    }


    /**
     *  Default constructor: instance will have Set behavior and a small
     *  initial capacity.
     */
    public HashMultimap()
    {
        this(Behavior.SET, 8, .75);
    }

//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns the current number of key-value pairs in the map.
     */
    public int size()
    {
        return size;
    }


    /**
     *  Convenience method to determine whether the map is empty (size is 0).
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }


    /**
     *  Removes all entries from the map. May or may not change the size of the
     *  internal table.
     */
    public void clear()
    {
        modCount++;
        size = 0;
        for (int ii = 0 ; ii < table.length ; ii++)
            table[ii] = null;
    }


    /**
     *  Adds a key-value pair to the map.
     */
    public void put(K key, V value)
    {
        if (filledSlots >= resizeThreshold)
            resize();

        int index = index(key);
        HashEntry<K,V> current = findEntry(table[index], key, value);
        if ((current != null) && (behavior == Behavior.SET))
            return;

        if (current == null)
            current = table[index];
        current = skipToEnd(current);

        size++;
        modCount++;
        if (current == null)
        {
            table[index] = new HashEntry<K,V>(key, value, null);
            filledSlots++; // won't actually resize until next put()
        }
        else
            current.next = new HashEntry<K,V>(key, value, null);
    }


    /**
     *  Retrieves a single value from the map. If the given key has multiple
     *  values associated, will retrieve the first (which only has meaning if
     *  the map is configured with List behavior).
     */
    public V get(K key)
    {
        HashEntry<K,V> current = findFirstEntry(key);
        return (current == null) ? null : current.value;
    }


    /**
     *  Retrieves all values associated with the specified key. The actual
     *  collection class will depend on the map's behavior setting.
     */
    public Collection<V> getAll(K key)
    {
        Collection<V> result = (behavior == Behavior.LIST)
                             ? new ArrayList<V>()
                             : new HashSet<V>();

        Iterator<V> itx = getIterator(key);
        while (itx.hasNext())
            result.add(itx.next());

        return result;
    }


    /**
     *  Retrieves an iterator over the values for a given key. This iterator is
     *  backed by the map itself, so is more efficient than calling {@link #getAll}
     *  and iterating the results.
     */
    public Iterator<V> getIterator(K key)
    {
        return new KeyIterator(key);
    }


    /**
     *  Returns an <code>Iterable</code> that simply calls {@link #getIterator}. This
     *  is typically more useful than the latter method, because it can be passed to
     *  a for-each loop.
     */
    public Iterable<V> getIterable(K key)
    {
        return new KeyIterable(key);
    }


    /**
     *  Removes the first entry with the given key. "First" has meaning only for
     *  maps with list behavior; for maps with set behavior, will remove an
     *  arbitrary entry.
     *
     *  @return The removed value, <code>null</code> if there was no value
     *          associated with the key (or the value was null).
     */
    public V remove(K key)
    {
        Iterator<V> itx = new KeyIterator(key);
        if (itx.hasNext())
        {
            V result = itx.next();
            itx.remove();
            return result;
        }
        return null;
    }


    /**
     *  Removes all key-value pairs with the given key.
     *
     *  @return The values that were removed. If no values were removed,
     *          will return an empty collection.
     */
    public Collection<V> removeAll(K key)
    {
        ArrayList<V> result = new ArrayList<V>();
        Iterator<V> itx = getIterator(key);
        while (itx.hasNext())
        {
            result.add(itx.next());
            itx.remove();
        }
        return result;
    }


    /**
     *  Removes the first entry with the given key and value. "First" has meaning
     *  only for maps with list behavior; for maps with set behavior, will remove
     *  an arbitrary entry.
     *
     *  @return <code>true</code> if the specified key-value pair was removed,
     *          <code>false</code> if there was no such pair in the map.
     */
    public boolean remove(K key, V value)
    {
        Iterator<V> itx = new KeyValueIterator(key, value);
        if (itx.hasNext())
        {
            itx.next();
            itx.remove();
            return true;
        }
        return false;
    }


    /**
     *  Removes all entries with the given key and value
     *
     *  @return <code>true</code> if the specified key-value pair was removed,
     *          <code>false</code> if there was no such pair in the map.
     */
    public Collection<V> removeAll(K key, V value)
    {
        ArrayList<V> result = new ArrayList<V>();
        Iterator<V> itx = getIterator(key);
        while (itx.hasNext())
        {
            result.add(itx.next());
            itx.remove();
        }
        return result;
    }


    /**
     *  Determines whether this object contains a mapping for the given key,
     *  regardless of value.
     */
    public boolean containsKey(K key)
    {
        return (findFirstEntry(key) != null);
    }


    /**
     *  Determines whether this object contains a the given key-value pair.
     */
    public boolean containsMapping(K key, V value)
    {
        return (findFirstEntry(key, value) != null);
    }


    /**
     *  Returns a set containing the keys from this map. This set is <em>not</em>
     *  backed by the map.
     */
    public Set<K> keySet()
    {
        Set<K> ret = new HashSet<K>();
        InternalEntryIterator itx = new InternalEntryIterator();
        while (itx.hasNext())
            ret.add(itx.next().key);
        return ret;
    }


    /**
     *  Returns the current entries in this map. This collection is <em>not</em>
     *  backed by the map; changes to one will not affect the other. It is built
     *  using {@link #entryIterator}; see that method for behavior.
     */
    public Collection<Map.Entry<K,V>> entries()
    {
        ArrayList<Map.Entry<K,V>> result = new ArrayList<Map.Entry<K,V>>(size());
        Iterator<Map.Entry<K,V>> entryItx = entryIterator();
        while (entryItx.hasNext())
            result.add(entryItx.next());
        return result;
    }


    /**
     *  Iterates all entries in the map. If the map has List behavior, then the
     *  order of values for a given key is retained. In all other cases, order
     *  is undefined. In particular, there is no guarantee that mappings with the
     *  same key will be adjacent; they may be interspersed with other mappings.
     */
    public Iterator<Map.Entry<K,V>> entryIterator()
    {
        return new PublicEntryIterator();
    }


    /**
     *  Returns a <code>java.util.HashMap</code> containing the entries from this
     *  multimap. Values in the returned map will be either <code>ArrayList</code>s
     *  or <code>HashSet</code>s depending on the behavior of this map.
     */
    public Map<K,Collection<V>> toMap()
    {
        Map<K,Collection<V>> result = new HashMap<K,Collection<V>>();
        Iterator<Map.Entry<K,V>> entryItx = entryIterator();
        while (entryItx.hasNext())
        {
            Map.Entry<K,V> entry = entryItx.next();
            Collection<V> coll = result.get(entry.getKey());
            if (coll == null)
            {
                coll = behavior == Behavior.LIST
                     ? new ArrayList<V>()
                     : new HashSet<V>();
                result.put(entry.getKey(), coll);
            }
            coll.add(entry.getValue());
        }
        return result;
    }

//----------------------------------------------------------------------------
//  Object overrides
//----------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    /**
     *  Two instances are equal if they are the same size and have the same
     *  mappings.
     *
     *  @since 1.0.13
     */
    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        else if (obj instanceof HashMultimap)
        {
            HashMultimap<Object,Object> that = (HashMultimap<Object,Object>)obj;
            if (this.size() != that.size())
                return false;

            for (Map.Entry<?,?> entry : this.entries())
            {
                if (! that.containsMapping(entry.getKey(), entry.getValue()))
                    return false;
            }

            return true;
        }
        return false;
    }


    @Override
    /**
     *  Returns a hashcode for the map. This value is dependent on the current
     *  mappings, so objects of this class should not be stored in a hashed
     *  collection.
     *
     *  @since 1.0.13
     */
    public final int hashCode()
    {
        int hashCode = 0;
        for (InternalEntryIterator itx = new InternalEntryIterator() ; itx.hasNext() ; )
        {
            hashCode = hashCode * 37 + itx.next().getKey().hashCode();
        }
        return hashCode;
    }

//----------------------------------------------------------------------------
//  Hashtable internals
//----------------------------------------------------------------------------

    private static class HashEntry<KK,VV>
    implements Map.Entry<KK,VV>, Serializable
    {
        private static final long serialVersionUID = 1L;

        public KK key;
        public VV value;
        public HashEntry<KK,VV> next;

        public HashEntry(KK key, VV value, HashEntry<KK,VV> next)
        {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public boolean isEqualTo(KK k)
        {
            return this.key.equals(k);
        }

        public boolean isEqualTo(KK k, VV v)
        {
            if (!isEqualTo(k))
                return false;

            if (this.value == null)
                return v == null;

            return this.value.equals(v);
        }

        @Override
        public String toString()
        {
            // meant for debugging
            return super.toString() + "[" + key + "," + value + "," +
            ((next == null) ? "null" : Integer.toHexString(System.identityHashCode(next)))
            + "]";
        }

        @Override
        public KK getKey()
        {
            return key;
        }

        @Override
        public VV getValue()
        {
            return value;
        }

        @Override
        public VV setValue(VV value)
        {
            throw new UnsupportedOperationException();
        }
    }


    private int index(K key)
    {
        return key.hashCode() & mask;
    }


    private HashEntry<K,V> findFirstEntry(K key)
    {
        return findEntry(table[index(key)], key);
    }


    private HashEntry<K,V> findFirstEntry(K key, V value)
    {
        return findEntry(table[index(key)], key, value);
    }


    private HashEntry<K,V> findEntry(HashEntry<K,V> current, K key)
    {
        prev = null;
        while (current != null)
        {
            if (current.isEqualTo(key))
                return current;
            else
            {
                prev = current;
                current = current.next;
            }
        }
        return null;
    }


    private HashEntry<K,V> findEntry(HashEntry<K,V> current, K key, V value)
    {
        prev = null;
        while (current != null)
        {
            if (current.isEqualTo(key, value))
                return current;
            else
            {
                prev = current;
                current = current.next;
            }
        }
        return null;
    }


    private HashEntry<K,V> skipToEnd(HashEntry<K,V> current)
    {
        while ((current != null) && (current.next != null))
            current = current.next;
        return current;
    }


    @SuppressWarnings("unchecked")
    private void resize()
    {
        // there might come a time where we can't resize
        if ((mask & 0x40000000) != 0)
        {
            // and if we ever it this, well, time to write a new class
            resizeThreshold = Integer.MAX_VALUE;
            return;
        }

        HashEntry<K,V>[] oldTable = table;

        modCount++;
        table = new HashEntry[oldTable.length * 2];
        mask = (mask << 1) | 1;
        resizeThreshold *= 2;

        // the entries are already in the correct order, we just need to assign
        // them to new buckets; rather than pay the cost of walking destination
        // lists, we'll create yet another table, this one holding list tails
        HashEntry<K,V>[] tmpTable = new HashEntry[oldTable.length * 2];
        for (int ii = 0 ; ii < oldTable.length ; ii++)
        {
            // we'll lazily update the "next" pointers, which makes this loop easy
            for (HashEntry<K,V> current = oldTable[ii] ; current != null ; current = current.next)
            {
                int newSlot = current.key.hashCode() & mask;
                if (table[newSlot] == null)
                    table[newSlot] = current;

                if (tmpTable[newSlot] != null)
                {
                    tmpTable[newSlot].next = current;
                }
                tmpTable[newSlot] = current;
            }
        }

        // now we have to cap the new chains, and update the slot count as a bonus
        filledSlots = 0;
        for (int ii = 0 ; ii < tmpTable.length ; ii++)
        {
            if (tmpTable[ii] != null)
            {
                tmpTable[ii].next = null;
                filledSlots++;
            }
        }
    }


//----------------------------------------------------------------------------
//  The various and sundry iterator classes
//----------------------------------------------------------------------------

    private class KeyIterable
    implements Iterable<V>
    {
        private K myKey;

        public KeyIterable(K key)
        {
            myKey = key;
        }

        @Override
        public Iterator<V> iterator()
        {
            return new KeyIterator(myKey);
        }
    }


    private class KeyIterator
    implements Iterator<V>
    {
        protected int myModCount;
        protected K myKey;
        protected HashEntry<K,V> pred;           // predecessor of current
        protected HashEntry<K,V> current;        // to be returned by next()
        protected HashEntry<K,V> last;           // previously returned by next()

        public KeyIterator(K key)
        {
            this.myModCount = modCount;
            this.myKey = key;
            this.current = findFirstEntry(key);
            this.pred = prev;
        }

        @Override
        public boolean hasNext()
        {
            if (myModCount != modCount)
                throw new ConcurrentModificationException();

            return current != null;
        }

        @Override
        public V next()
        {
            if (!hasNext())
                throw new NoSuchElementException("no more values for: " + myKey);

            if (current != null)
            {
                last = current;
                pred = prev;
                current = findEntry(current.next, myKey);
            }
            return last.value;
        }

        @Override
        public void remove()
        {
            if (last == null)
                throw new IllegalStateException("must call next()");
            if (pred == null)
                table[index(myKey)] = last.next;
            else
                pred.next = last.next;
            size--;
            myModCount = ++modCount;
        }
    }


    private class KeyValueIterator
    extends KeyIterator
    implements Iterator<V>
    {
        private V myValue;

        public KeyValueIterator(K key, V value)
        {
            super(key);
            myValue = value;
            current = findFirstEntry(key, value);
            pred = prev;
        }

        @Override
        public V next()
        {
            if (!hasNext())
                throw new NoSuchElementException("no more values for: " + myKey);

            if (current != null)
            {
                last = current;
                pred = prev;
                current = findEntry(current.next, myKey, myValue);
            }
            return last.value;
        }
    }


    private class InternalEntryIterator
    implements Iterator<HashEntry<K,V>>
    {
        protected int myModCount;
        protected int tableIndex;
        protected HashEntry<K,V> current;

        public InternalEntryIterator()
        {
            myModCount = modCount;
            tableIndex = 0;
            findNext();
        }

        @Override
        public boolean hasNext()
        {
            if (myModCount != modCount)
                throw new ConcurrentModificationException();

            return current != null;
        }

        @Override
        public HashEntry<K,V> next()
        {
            if (!hasNext())
                throw new NoSuchElementException("end of entry iterator");

            HashEntry<K,V> ret = current;
            findNext();
            return ret;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        private void findNext()
        {
            if (current != null)
                current = current.next;

            while ((current == null) && (tableIndex < table.length))
                current = table[tableIndex++];
        }
    }


    // can't reuse InternalEntryIterator, because generics don't imply class hierarchy
    private class PublicEntryIterator
    implements Iterator<Map.Entry<K,V>>
    {
        private InternalEntryIterator realItx = new InternalEntryIterator();

        @Override
        public boolean hasNext()
        {
            return realItx.hasNext();
        }

        @Override
        public Entry<K,V> next()
        {
            return realItx.next();
        }

        @Override
        public void remove()
        {
            realItx.remove();
        }
    }


//----------------------------------------------------------------------------
//  Test Hooks -- protected methods used to report internal activity
//----------------------------------------------------------------------------

    protected int getTableSize()
    {
        return table.length;
    }
}
