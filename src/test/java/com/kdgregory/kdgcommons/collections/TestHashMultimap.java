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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.kdgcommons.collections.HashMultimap.Behavior;


public class TestHashMultimap
{
    @Test
    public void testInitialCapacityComputation() throws Exception
    {
        HashMultimap<String,String> map1 = new HashMultimap<String,String>();
        assertEquals(8, map1.getTableSize());

        HashMultimap<String,String> map2 = new HashMultimap<String,String>(Behavior.SET, 4, .75);
        assertEquals(8, map2.getTableSize());

        HashMultimap<String,String> map3 = new HashMultimap<String,String>(Behavior.SET, 18, .75);
        assertEquals(32, map3.getTableSize());
    }


    @Test
    public void testPutAndGetSingleValue() throws Exception
    {
        final String key1 = "foo";
        final String val1 = "argle";
        final String key2 = "bar";
        final String val2 = "bargle";
        final String key3 = "baz";

        HashMultimap<String,String> map = new HashMultimap<String,String>();
        assertEquals(0, map.size());
        assertEquals(8, map.getTableSize());

        map.put(key1, val1);
        assertEquals(1, map.size());
        assertEquals(val1, map.get(key1));

        map.put(key2, val2);
        assertEquals(2, map.size());
        assertEquals(val1, map.get(key1));
        assertEquals(val2, map.get(key2));

        assertNull(map.get(key3));
    }


    @Test
    public void testPutAndGetMultipleValuesSetBehavior() throws Exception
    {
        final String key1  = "foo";
        final String val1a = "argle";
        final String val1b = "wargle";

        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.SET);
        assertEquals(0, map.size());

        map.put(key1, val1a);
        assertEquals(1, map.size());
        assertEquals(CollectionUtil.asSet(val1a), map.getAll(key1));

        map.put(key1, val1b);
        assertEquals(2, map.size());
        assertEquals(CollectionUtil.asSet(val1a, val1b), map.getAll(key1));

        map.put(key1, val1a);
        assertEquals(2, map.size());
        assertEquals(CollectionUtil.asSet(val1a, val1b), map.getAll(key1));
    }


    @Test
    public void testPutAndGetMultipleValuesListBehavior() throws Exception
    {
        final String key1  = "foo";
        final String val1a = "argle";
        final String val1b = "wargle";

        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        assertEquals(0, map.size());

        map.put(key1, val1a);
        assertEquals(1, map.size());
        assertEquals(Arrays.asList(val1a), map.getAll(key1));
        assertEquals(val1a, map.get(key1));

        map.put(key1, val1b);
        assertEquals(2, map.size());
        assertEquals(Arrays.asList(val1a, val1b), map.getAll(key1));
        assertEquals(val1a, map.get(key1));

        map.put(key1, val1a);
        assertEquals(3, map.size());
        assertEquals(Arrays.asList(val1a, val1b, val1a), map.getAll(key1));
        assertEquals(val1a, map.get(key1));
    }


    @Test
    public void testPutAndGetWhereKeysHaveSameHashcode() throws Exception
    {
        final String key1  = "AAABB";
        final String val1a = "argle";
        final String val1b = "wargle";
        final String key2  = "AAAAa";
        final String val2a = "argle";
        final String val2b = "wargle";

        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        map.put(key1, val1a);
        map.put(key2, val2a);
        map.put(key1, val1b);
        map.put(key2, val2b);

        assertEquals(4, map.size());
        assertEquals(Arrays.asList(val1a, val1b), map.getAll(key1));
        assertEquals(Arrays.asList(val2a, val2b), map.getAll(key2));
    }


    @Test
    public void testRemoveByKey() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        assertEquals(0, map.size());

        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("foo", "bar");
        assertEquals(3, map.size());

        assertEquals("bar", map.remove("foo"));
        assertEquals(2, map.size());
        assertEquals(Arrays.asList("baz", "bar"), map.getAll("foo"));

        assertEquals("baz", map.remove("foo"));
        assertEquals(1, map.size());
        assertEquals(Arrays.asList("bar"), map.getAll("foo"));

        assertEquals("bar", map.remove("foo"));
        assertEquals(0, map.size());
        assertEquals(Arrays.asList(), map.getAll("foo"));

        assertEquals(null, map.remove("foo"));
        assertEquals(0, map.size());
        assertEquals(Arrays.asList(), map.getAll("foo"));
    }


    @Test
    public void testRemoveByKeyAndValue() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        assertEquals(0, map.size());

        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("foo", "bar");
        assertEquals(3, map.size());

        assertTrue(map.remove("foo", "baz"));
        assertEquals(2, map.size());
        assertEquals(Arrays.asList("bar", "bar"), map.getAll("foo"));

        assertFalse(map.remove("foo", "baz"));
        assertTrue(map.remove("foo", "bar"));
        assertEquals(1, map.size());
        assertEquals(Arrays.asList("bar"), map.getAll("foo"));

        assertTrue(map.remove("foo", "bar"));
        assertEquals(0, map.size());
        assertEquals(Arrays.asList(), map.getAll("foo"));

        assertFalse(map.remove("foo", "bar"));
        assertEquals(0, map.size());
        assertEquals(Arrays.asList(), map.getAll("foo"));
    }


    @Test
    public void testRemoveAll() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        assertEquals(0, map.size());

        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("argle", "bargle");
        assertEquals(3, map.size());

        assertEquals(Arrays.asList("bar", "baz"), map.getAll("foo"));
        assertEquals(Arrays.asList("bar", "baz"), map.removeAll("foo"));
        assertEquals(1, map.size());
        assertEquals(Arrays.asList(), map.getAll("foo"));
        assertEquals(Arrays.asList(), map.removeAll("foo"));

        assertEquals(Arrays.asList("bargle"), map.getAll("argle"));
        assertEquals(Arrays.asList("bargle"), map.removeAll("argle"));
        assertEquals(0, map.size());
        assertEquals(Arrays.asList(), map.getAll("argle"));
        assertEquals(Arrays.asList(), map.removeAll("argle"));
    }


    @Test
    public void testGetIterator() throws Exception
    {
        final String key1  = "foo";
        final String val1a = "argle";
        final String val1b = "wargle";

        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        map.put(key1, val1a);
        map.put(key1, val1b);
        assertEquals(2, map.size());
        assertEquals(Arrays.asList(val1a, val1b), map.getAll(key1));

        Iterator<String> itx1 = map.getIterator(key1);
        assertEquals(val1a, itx1.next());
        assertEquals(val1b, itx1.next());
        assertFalse(itx1.hasNext());

        Iterator<String> itx2 = map.getIterable(key1).iterator();
        assertEquals(val1a, itx2.next());
        assertEquals(val1b, itx2.next());
        assertFalse(itx2.hasNext());
    }


    @Test
    public void testIteratorRemove() throws Exception
    {
        final String key1  = "foo";
        final String val1a = "argle";
        final String val1b = "wargle";

        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        map.put(key1, val1a);
        map.put(key1, val1b);
        assertEquals(2, map.size());

        Iterator<String> itx = map.getIterator(key1);

        assertEquals(val1a, itx.next());
        itx.remove();
        assertEquals(1, map.size());

        assertEquals(val1b, itx.next());
        itx.remove();
        assertEquals(0, map.size());

        assertFalse(itx.hasNext());

        itx = map.getIterator(key1);
        assertFalse(itx.hasNext());
    }


    @Test
    public void testIteratorThrowsWhenNextCalledAtEnd() throws Exception
    {
        // an empty map is a good way to get a failing iterator
        HashMultimap<String,String> map = new HashMultimap<String,String>();
        Iterator<String> itx = map.getIterator("foo");
        assertFalse(itx.hasNext());

        try
        {
            itx.next();
            fail("next() succeeded when no next element");
        }
        catch (NoSuchElementException ex)
        {
            // success
        }
    }


    @Test
    public void testIteratorIsFailFast() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>(Behavior.LIST);
        map.put("foo", "bar");
        map.put("foo", "baz");

        Iterator<String> itx = map.getIterator("foo");
        assertEquals("bar", itx.next());

        map.put("argle", "bargle");

        try
        {
            assertEquals("baz", itx.next());
            fail("iterator continued to work after modification");
        }
        catch (ConcurrentModificationException ex)
        {
            // success
        }
    }


    @Test
    public void testNullValues() throws Exception
    {
        final String key1  = "foo";
        final String val1a = "argle";
        final String val1b = "wargle";

        // whitebox test: we know that Set behavior has to check values, while List doesn't
        HashMultimap<String,String> map = new HashMultimap<String,String>(HashMultimap.Behavior.SET);
        assertEquals(0, map.size());

        map.put(key1, val1a);
        assertEquals(1, map.size());
        assertEquals(CollectionUtil.asSet(val1a), map.getAll(key1));

        map.put(key1, null);
        assertEquals(2, map.size());
        assertEquals(CollectionUtil.asSet(val1a, null), map.getAll(key1));

        map.put(key1, val1b);
        assertEquals(3, map.size());
        assertEquals(CollectionUtil.asSet(val1a, null, val1b), map.getAll(key1));
    }


    @Test
    public void testClearAndIsEmpty() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>();
        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("argle", "bargle");
        assertEquals(3, map.size());
        assertFalse(map.isEmpty());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("foo"));
    }


    @Test
    public void testContains() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>();
        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("argle", "bargle");

        assertTrue(map.containsKey("argle"));
        assertFalse(map.containsKey("bargle"));

        assertTrue(map.containsMapping("argle", "bargle"));
        assertFalse(map.containsMapping("argle", "biff"));

        // make sure we don't stop looking
        assertTrue(map.containsMapping("foo", "baz"));
    }


    @Test
    public void testKeySet() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>();
        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("argle", "bargle");

        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("argle"));

        // changes to the map do not affect the set
        map.put("bargle", "wargle");
        assertEquals(2, keys.size());
        assertFalse(keys.contains("bargle"));

        // and changes to the set don't affect the map
        keys.remove("foo");
        assertEquals(4, map.size());
        assertTrue(map.containsKey("foo"));
    }


    @Test
    public void testIterateEntries() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>();
        map.put("foo", "bar");
        map.put("foo", "baz");
        map.put("argle", "bargle");

        Collection<Map.Entry<String,String>> entries = map.entries();
        assertEquals(3, entries.size());
    }


    // TODO - remove from entry iterator


    @Test
    public void testToMapListBehavior() throws Exception
    {
        HashMultimap<String,String> multimap = new HashMultimap<String,String>(Behavior.LIST);
        multimap.put("argle", "bargle");
        multimap.put("argle", "wargle");
        multimap.put("foo", "bar");

        Map<String,Collection<String>> map = multimap.toMap();
        assertEquals("size", 2, map.size());

        Collection<String> v1 = map.get("argle");
        assertTrue("collection is a list", v1 instanceof List);
        assertEquals("collection values", Arrays.asList("bargle", "wargle"), v1);

        Collection<String> v2 = map.get("foo");
        assertTrue("collection is a list", v2 instanceof List);
        assertEquals("collection values", Arrays.asList("bar"), v2);
    }


    @Test
    public void testToMapSetBehavior() throws Exception
    {
        HashMultimap<String,String> multimap = new HashMultimap<String,String>(Behavior.SET);
        multimap.put("argle", "bargle");
        multimap.put("argle", "wargle");
        multimap.put("foo", "bar");

        Map<String,Collection<String>> map = multimap.toMap();
        assertEquals("size", 2, map.size());

        Collection<String> v1 = map.get("argle");
        assertTrue("collection is a set", v1 instanceof Set);
        assertEquals("collection values", CollectionUtil.asSet("bargle", "wargle"), v1);

        Collection<String> v2 = map.get("foo");
        assertTrue("collection is a set", v2 instanceof Set);
        assertEquals("collection values", CollectionUtil.asSet("bar"), v2);
    }


    @Test
    public void testResize() throws Exception
    {
        // we'll use Integer keys because the hashcode is the intValue()
        HashMultimap<Integer,String> map = new HashMultimap<Integer,String>(Behavior.LIST, 8, .5);
        assertEquals(8, map.getTableSize());

        // we should be able to add lots of values without a resize
        map.put(1, "A1");
        map.put(1, "A2");
        map.put(1, "A3");
        map.put(1, "A4");
        map.put(1, "A5");
        assertEquals(8, map.getTableSize());

        map.put(2, "B");
        map.put(3, "C");
        map.put(4, "D");
        assertEquals(8, map.getTableSize());

        // but this one should push us over the limit
        map.put(5, "E");
        assertEquals(16, map.getTableSize());

        // and verify that we didn't break anything when we resized
        assertEquals(Arrays.asList("A1", "A2", "A3", "A4", "A5"), map.getAll(1));
        assertEquals("B", map.get(2));
        assertEquals("C", map.get(3));
        assertEquals("D", map.get(4));
        assertEquals("E", map.get(5));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception
    {
        HashMultimap<String,String> map = new HashMultimap<String,String>();
        map.put("foo", "bar");
        map.put("foo", "baz");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos))
        {
            oos.writeObject(map);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        HashMultimap<String,String> ret = (HashMultimap<String,String>)ois.readObject();
        assertEquals(map, ret);
    }


    @Test
    public void testEqualsAndHashcode() throws Exception
    {
        HashMultimap<String,String> map1 = new HashMultimap<String,String>();
        map1.put("foo", "bar");
        map1.put("foo", "baz");

        HashMultimap<String,String> map2 = new HashMultimap<String,String>();
        map2.put("foo", "bar");
        map2.put("foo", "baz");

        HashMultimap<String,String> map3 = new HashMultimap<String,String>();
        map3.put("foo", "bar");
        map3.put("baz", "baz");

        HashMultimap<String,String> map4 = new HashMultimap<String,String>();
        map4.put("foo", "bar");
        map4.put("foo", "bif");

        HashMultimap<String,String> map5 = new HashMultimap<String,String>();
        map4.put("foo", "bar");

        assertTrue("identical maps are equal", map1.equals(map1));
        assertTrue("maps with same mappings are equal", map1.equals(map2));
        assertFalse("maps with different keys are not equal", map1.equals(map3));
        assertFalse("maps with different values are not equal", map1.equals(map4));
        assertFalse("maps with different sizes are not equal", map1.equals(map5));

        assertEquals("equal maps have equal hashcodes",         map1.hashCode(), map2.hashCode());
        assertFalse("different hashcodes (known value test)",   map1.hashCode() == map3.hashCode());
    }
}
