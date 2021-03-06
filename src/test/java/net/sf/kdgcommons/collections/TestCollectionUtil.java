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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;


public class TestCollectionUtil extends TestCase
{
    public void testAsSet() throws Exception
    {
        Set<String> values = CollectionUtil.asSet("foo", "bar", "baz", "foo");
        assertEquals(3, values.size());
        assertTrue(values.contains("foo"));
        assertTrue(values.contains("bar"));
        assertTrue(values.contains("baz"));
    }


    public void testAsMap() throws Exception
    {
        Map<Object,Object> expected = new HashMap<Object,Object>();
        expected.put("foo", "bar");
        expected.put("argle", "bargle");

        assertEquals("basic operation, all keys have values",
                     expected,
                     CollectionUtil.asMap("foo", "bar", "argle", "bargle"));

        assertEquals("repeated keys",
                     expected,
                     CollectionUtil.asMap("foo", "biff", "argle", "bargle", "foo", "bar"));

        expected.put("biff", null);
        assertEquals("odd number of parameters",
                     expected,
                     CollectionUtil.asMap("foo", "bar", "argle", "bargle",  "biff"));

        assertEquals("no parameters",
                     new HashMap<Object,Object>(),
                     CollectionUtil.asMap());
    }


    public void testAddAllFromVarargs() throws Exception
    {
        List<String> list = new ArrayList<String>();
        CollectionUtil.addAll(list, "foo", "bar", "baz");
        assertEquals(3, list.size());


        Set<String> set = new HashSet<String>();
        CollectionUtil.addAll(set, "argle", "bargle");
        assertEquals(2, set.size());
    }


    public void testAddAllFromIterable() throws Exception
    {
        // whitebox test: this also tests adding all from an iterator

        List<String> list = new ArrayList<String>();
        CollectionUtil.addAll(list, Arrays.asList("foo", "bar", "baz"));
        assertEquals(3, list.size());


        Set<String> set = new HashSet<String>();
        CollectionUtil.addAll(set, Arrays.asList("foo", "bar", "baz", "bar"));
        assertEquals(3, set.size());
    }


    public void testAddIf() throws Exception
    {
        ArrayList<String> list = new ArrayList<String>();

        assertSame("returned collection",       list, CollectionUtil.addIf(list, "foo", true));
        assertEquals("added element",           Arrays.asList("foo"), list);

        assertSame("returned collection",       list, CollectionUtil.addIf(list, "bar", false));
        assertEquals("did not add element",     Arrays.asList("foo"), list);
    }


    public void testAddIfNotNull() throws Exception
    {
        ArrayList<String> list = new ArrayList<String>();
        CollectionUtil.addIfNotNull(list, null);
        CollectionUtil.addIfNotNull(list, "foo");
        CollectionUtil.addIfNotNull(list, "foo");
        CollectionUtil.addIfNotNull(list, null);
        CollectionUtil.addIfNotNull(list, "bar");

        assertEquals(Arrays.asList("foo", "foo", "bar"), list);
    }


    public void testPutIf() throws Exception
    {
        Map<Object,Object> map = CollectionUtil.asMap("foo", "bar");

        assertEquals("expression is true",
                     CollectionUtil.asMap("foo", "bar", "argle", "bargle"),
                     CollectionUtil.putIf(map, "argle", "bargle", true));

        assertEquals("expression is true",
                     CollectionUtil.asMap("foo", "bar", "argle", "bargle"),
                     CollectionUtil.putIf(map, "bazzle", "biff", false));
    }


    public void testPutIfNotNull() throws Exception
    {
        Map<Object,Object> map = CollectionUtil.asMap("foo", "bar");

        assertEquals("value is null",
                     CollectionUtil.asMap("foo", "bar"),
                     CollectionUtil.putIfNotNull(map, "argle", null));

        assertEquals("value is not null",
                     CollectionUtil.asMap("foo", "bar", "argle", "bargle"),
                     CollectionUtil.putIfNotNull(map, "argle", "bargle"));
    }


    public void testPutIfAbsent() throws Exception
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("argle", "bargle");

        assertEquals("put of existing entry; return",   "bargle", CollectionUtil.putIfAbsent(map, "argle", "wargle"));
        assertEquals("put of existing entry; post get", "bargle", map.get("argle"));

        assertEquals("put of absent entry; pre get",    null, map.get("foo"));
        assertEquals("put of abseent entry; return",    "bar", CollectionUtil.putIfAbsent(map, "foo", "bar"));
        assertEquals("put of absent entry; post get",   "bar", map.get("foo"));
    }


    public void testPutAbsent() throws Exception
    {
        Map<String,String> base = new HashMap<String,String>();
        base.put("foo", "bar");
        base.put("baz", "bar");

        Map<String,String> add = new HashMap<String,String>();
        add.put("foo", "biff");
        add.put("argle", "bargle");

        CollectionUtil.putIfAbsent(base, add);
        assertEquals("resulting map size", 3, base.size());
        assertEquals("get(foo)",   "bar",    base.get("foo"));
        assertEquals("get(baz)",   "bar",    base.get("baz"));
        assertEquals("get(argle)", "bargle", base.get("argle"));
    }


    public void testFirstAndLast() throws Exception
    {
        List<String> l1 = Arrays.asList("foo", "bar", "baz");
        assertEquals("first(), list size == 3", "foo", CollectionUtil.first(l1));
        assertEquals("last(),  list size == 3", "baz", CollectionUtil.last(l1));

        List<String> l2 = Arrays.asList("foo");
        assertEquals("first(), list size == 1", "foo", CollectionUtil.first(l2));
        assertEquals("last(),  list size == 1", "foo", CollectionUtil.last(l2));

        List<String> l3 = Arrays.asList();
        assertEquals("first(), list size == 0", null, CollectionUtil.first(l3));
        assertEquals("last(),  list size == 0", null, CollectionUtil.last(l3));

        List<String> l4 = null;
        assertEquals("first(), list is null",   null, CollectionUtil.first(l4));
        assertEquals("last(),  list is null",   null, CollectionUtil.last(l4));

        // this test is just here for coverage; we don't verify behavior
        List<String> l5 = new LinkedList<String>(Arrays.asList("foo", "bar", "baz"));
        assertEquals("first(), LinkedList",     "foo", CollectionUtil.first(l5));
        assertEquals("last(),  LinkedList",     "baz", CollectionUtil.last(l5));
    }


    public void testIsEmpty() throws Exception
    {
        List<String> list1 = null;
        assertTrue(CollectionUtil.isEmpty(list1));
        assertFalse(CollectionUtil.isNotEmpty(list1));

        List<String> list2 = new ArrayList<String>();
        assertTrue(CollectionUtil.isEmpty(list2));
        assertFalse(CollectionUtil.isNotEmpty(list2));

        List<String> list3 = Arrays.asList("foo");
        assertFalse(CollectionUtil.isEmpty(list3));
        assertTrue(CollectionUtil.isNotEmpty(list3));

        Map<String,String> map1 = null;
        assertTrue(CollectionUtil.isEmpty(map1));
        assertFalse(CollectionUtil.isNotEmpty(map1));

        Map<String,String> map2 = new HashMap<String,String>();
        assertTrue(CollectionUtil.isEmpty(map2));
        assertFalse(CollectionUtil.isNotEmpty(map2));

        Map<String,String> map3 = new HashMap<String,String>();
        map3.put("foo", "bar");
        assertFalse(CollectionUtil.isEmpty(list3));
        assertTrue(CollectionUtil.isNotEmpty(map3));
    }


    public void testCompareEqualCollections() throws Exception
    {
        List<String> l1 = Arrays.asList("foo", "bar", "baz");
        List<String> l2 = Arrays.asList("foo", "bar", "baz");

        assertTrue(CollectionUtil.compare(l1, l2) == 0);
    }


    public void testCompareUnequalCollections() throws Exception
    {
        List<String> l1 = Arrays.asList("foo", "bar", "baz");
        List<String> l2 = Arrays.asList("foo", "baz", "bar");

        assertTrue(CollectionUtil.compare(l1, l2) < 0);
        assertTrue(CollectionUtil.compare(l2, l1) > 0);
    }


    public void testCompareCollectionsOfDifferentLength() throws Exception
    {
        List<String> l1 = Arrays.asList("foo", "bar");
        List<String> l2 = Arrays.asList("foo", "bar", "baz");

        assertTrue(CollectionUtil.compare(l1, l2) < 0);
        assertTrue(CollectionUtil.compare(l2, l1) > 0);
    }


    public void testDefaultIfNull() throws Exception
    {
        Iterable<String> it1 = CollectionUtil.defaultIfNull(Arrays.asList("foo"), Arrays.asList("bar"));
        assertEquals("foo", it1.iterator().next());

        Iterable<String> it2 = CollectionUtil.defaultIfNull(null, Arrays.asList("bar"));
        assertEquals("bar", it2.iterator().next());
    }


    public void testDefaultIfEmpty() throws Exception
    {
        Collection<String> c1 = CollectionUtil.defaultIfEmpty(Arrays.asList("foo"), Arrays.asList("bar"));
        assertEquals("foo", c1.iterator().next());

        Collection<String> c2 = CollectionUtil.defaultIfEmpty(Arrays.<String>asList(), Arrays.asList("bar"));
        assertEquals("bar", c2.iterator().next());

        Collection<String> c3 = CollectionUtil.defaultIfEmpty(null, Arrays.asList("bar"));
        assertEquals("bar", c3.iterator().next());
    }


    public void testCastList() throws Exception
    {
        ArrayList<Object> x = new ArrayList<Object>();
        x.add("foo");
        x.add("bar");
        x.add("baz");

        List<String> y = CollectionUtil.cast(x, String.class);
        assertSame(x, y);
    }


    public void testCastListFailure() throws Exception
    {
        ArrayList<Object> x = new ArrayList<Object>();
        x.add("foo");
        x.add(Integer.valueOf(1));
        x.add("baz");

        try
        {
            CollectionUtil.cast(x, String.class);
            fail("should have thrown");
        }
        catch (ClassCastException ignored)
        {
            // success
        }
    }


    public void testCastListWithNull() throws Exception
    {
        ArrayList<Object> x = new ArrayList<Object>();
        x.add("foo");
        x.add(null);
        x.add("baz");

        List<String> y = CollectionUtil.cast(x, String.class);
        assertSame(x, y);
    }


    public void testCastSet() throws Exception
    {
        HashSet<Object> x = new HashSet<Object>();
        x.add("foo");
        x.add("bar");
        x.add("baz");

        Set<String> y = CollectionUtil.cast(x, String.class);
        assertSame(x, y);
    }


    public void testCastSetFailure() throws Exception
    {
        HashSet<Object> x = new HashSet<Object>();
        x.add("foo");
        x.add(Integer.valueOf(1));
        x.add("baz");

        try
        {
            CollectionUtil.cast(x, String.class);
            fail("should have thrown");
        }
        catch (ClassCastException ignored)
        {
            // success
        }
    }


    public void testCastSetWithNull() throws Exception
    {
        HashSet<Object> x = new HashSet<Object>();
        x.add("foo");
        x.add(null);
        x.add("baz");

        Set<String> y = CollectionUtil.cast(x, String.class);
        assertSame(x, y);
    }


    public void testCastMap() throws Exception
    {
        Map<Object,Object> x = new HashMap<Object,Object>();
        x.put("foo", "bar");
        x.put("argle", "bargle");

        Map<String,String> y = CollectionUtil.cast(x, String.class, String.class);
        assertSame(x, y);
    }


    public void testCastMapKeyFailure() throws Exception
    {
        Map<Object,Object> x = new HashMap<Object,Object>();
        x.put("foo", "bar");
        x.put(Integer.valueOf(123), "bargle");

        try
        {
            CollectionUtil.cast(x, String.class, String.class);
            fail("should have thrown");
        }
        catch (ClassCastException ignored)
        {
            // success
        }
    }

    public void testCastMapValueFailure() throws Exception
    {
        Map<Object,Object> x = new HashMap<Object,Object>();
        x.put("foo", "bar");
        x.put("argle", Integer.valueOf(123));

        try
        {
            CollectionUtil.cast(x, String.class, String.class);
            fail("should have thrown");
        }
        catch (ClassCastException ignored)
        {
            // success
        }
    }


    public void testCastMapWithNullValue() throws Exception
    {
        Map<Object,Object> x = new HashMap<Object,Object>();
        x.put("foo", "bar");
        x.put("argle", null);

        Map<String,String> y = CollectionUtil.cast(x, String.class, String.class);
        assertSame(x, y);
    }


    public void testResize() throws Exception
    {
        ArrayList<String> list1 = new ArrayList<String>();

        assertSame(list1, CollectionUtil.resize(list1, 2));
        assertEquals(2, list1.size());
        assertNull(list1.get(0));
        assertNull(list1.get(1));

        // verify list unchanged if passed same size
        list1.set(0, "foo");
        list1.set(1, "bar");
        assertSame(list1, CollectionUtil.resize(list1, 2));
        assertEquals(2, list1.size());
        assertEquals("foo", list1.get(0));
        assertEquals("bar", list1.get(1));

        // add another element (this test is fluff)
        assertSame(list1, CollectionUtil.resize(list1, 3));
        assertEquals(3, list1.size());
        assertEquals("foo", list1.get(0));
        assertEquals("bar", list1.get(1));
        assertNull(list1.get(2));

        // reduce size
        assertSame(list1, CollectionUtil.resize(list1, 2));
        assertEquals(2, list1.size());
        assertEquals("foo", list1.get(0));
        assertEquals("bar", list1.get(1));

        // verify alternate paths for non-RandomAccess

        LinkedList<String> list2 = new LinkedList<String>();
        list2.add("foo");
        list2.add("bar");

        assertSame(list2, CollectionUtil.resize(list2, 3));
        assertEquals(3, list2.size());
        assertEquals("foo", list2.get(0));
        assertEquals("bar", list2.get(1));
        assertNull(list2.get(2));

        assertSame(list2, CollectionUtil.resize(list2, 2));
        assertEquals(2, list2.size());
        assertEquals("foo", list2.get(0));
        assertEquals("bar", list2.get(1));
    }


    public void testResizeWithValue() throws Exception
    {
        List<String> list = new ArrayList<String>();
        assertSame(list, CollectionUtil.resize(list, 2, "foo"));
        assertEquals(2, list.size());
        assertEquals("foo", list.get(0));
        assertEquals("foo", list.get(1));
    }


    public void testJoin() throws Exception
    {
        // part 1: join various numbers of elements
        assertEquals("",            CollectionUtil.join(Arrays.asList(), ","));
        assertEquals("foo",         CollectionUtil.join(Arrays.asList("foo"), ","));
        assertEquals("foo,bar",     CollectionUtil.join(Arrays.asList("foo", "bar"), ","));
        assertEquals("foo,bar,baz", CollectionUtil.join(Arrays.asList("foo", "bar", "baz"), ","));

        // part 2: arbitrary objects
        assertEquals("1,foo,2",     CollectionUtil.join(Arrays.asList(new Integer(1), "foo", new Long(2)), ","));

        // part 3: nulls in various places
        assertEquals("",            CollectionUtil.join(null, ","));
        assertEquals("foo,,baz",    CollectionUtil.join(Arrays.asList("foo", null, "baz"), ","));
        assertEquals(",,baz",       CollectionUtil.join(Arrays.asList(null, null, "baz"), ","));
        assertEquals("foo,,",       CollectionUtil.join(Arrays.asList("foo", null, null), ","));
        assertEquals(",bar,",       CollectionUtil.join(Arrays.asList(null, "bar", null), ","));
    }


    public void testCombineList() throws Exception
    {
        List<String> dest = CollectionUtil.combine(new ArrayList<String>(),
                                                  Arrays.asList("foo", "bar", "baz"),
                                                  Arrays.asList("baz", "bargle", "bazzle"));

        assertEquals("number of elements",      6, dest.size());
        assertEquals("first item from src1",    "foo", dest.get(0));
        assertEquals("last item from src1",     "baz", dest.get(2));
        assertEquals("first item from src2",    "baz", dest.get(3));
        assertEquals("last item from src2",     "bazzle", dest.get(5));
    }


    public void testCombineSet() throws Exception
    {
        Set<String> dest = CollectionUtil.combine(new HashSet<String>(),
                                                  Arrays.asList("foo", "bar", "baz"),
                                                  Arrays.asList("baz", "bargle", "bazzle"));

        assertEquals("number of elements",  5, dest.size());
        assertTrue("item from src1",        dest.contains("foo"));
        assertTrue("item from src2",        dest.contains("bazzle"));
    }


    public void testCombineMap() throws Exception
    {
        Map<String,String> src1 = new MapBuilder<String,String>(new HashMap<String,String>())
                                  .put("foo", "bar")
                                  .put("argle", "bargle")
                                  .toMap();
        Map<String,String> src2 = new MapBuilder<String,String>(new HashMap<String,String>())
                                  .put("foo", "baz")
                                  .put("bazzle", "bizzle")
                                  .toMap();
        Map<String,String> dest = CollectionUtil.combine(new HashMap<String,String>(), src1, src2);

        assertEquals("number of elements",  3, dest.size());
        assertEquals("item from src1",      "bargle", dest.get("argle"));
        assertEquals("item from src2",      "bizzle", dest.get("bazzle"));
        assertEquals("item in both",        "baz", dest.get("foo"));
    }


    public void testFilterStringList() throws Exception
    {
        List<String> src = Arrays.asList("foo", "bar", "baz", "");

        assertEquals(Arrays.asList("foo", "bar", "baz"),
                     CollectionUtil.filter(src, ".+", true));
        assertEquals(Arrays.asList(""),
                     CollectionUtil.filter(src, ".+", false));

        assertEquals(Arrays.asList("bar", "baz"),
                     CollectionUtil.filter(src, ".a.", true));
        assertEquals(Arrays.asList("foo", ""),
                     CollectionUtil.filter(src, ".a.", false));
    }


    public void testFilterNonStringList() throws Exception
    {
        Integer i1 = Integer.valueOf(1);
        Integer i2 = Integer.valueOf(2);
        Integer i3 = Integer.valueOf(12);

        List<Integer> src = Arrays.asList(i1, i2, i3);

        assertEquals(Arrays.asList(i1, i2, i3),
                     CollectionUtil.filter(src, ".+", true));
        assertEquals(Collections.emptyList(),
                     CollectionUtil.filter(src, ".+", false));

        assertEquals(Arrays.asList(i2, i3),
                     CollectionUtil.filter(src, ".*2.*", true));
        assertEquals(Arrays.asList(i1),
                     CollectionUtil.filter(src, ".*2.*", false));
    }


    public void testFilterListWithNull() throws Exception
    {
        List<String> src = Arrays.asList("foo", null, "bar");

        assertEquals(Arrays.asList("foo", "bar"),
                     CollectionUtil.filter(src, ".+", true));
        assertEquals(Arrays.asList((String)null),
                     CollectionUtil.filter(src, ".+", false));
    }


    public void testMap() throws Exception
    {
        List<Integer> src = Arrays.asList(1, 2, 3, 4);
        List<Integer> dst = CollectionUtil.map(src, new CollectionUtil.IndexValueMapFunctor<Integer,Integer>()
        {
            public Integer invoke(int index, Integer value)
            {
                return Integer.valueOf(value.intValue() * index);
            }
        });

        assertEquals(Arrays.asList(0, 2, 6, 12), dst);
    }


    public void testMapWithException() throws Exception
    {
        try
        {
            List<Integer> src = Arrays.asList(1, 2, null, 4);
            CollectionUtil.map(src, new CollectionUtil.IndexValueMapFunctor<Integer,Integer>()
            {
                public Integer invoke(int index, Integer value)
                {
                    return Integer.valueOf(value.intValue() * index);
                }
            });
            fail("map returned normally");
        }
        catch (CollectionUtil.MapException ex)
        {
            assertEquals("wrapped exception",   NullPointerException.class, ex.getCause().getClass());
            assertEquals("failure index",       2,                          ex.getIndex());
            assertEquals("failure value",       null,                       ex.getValue());
            assertEquals("partial results",     Arrays.asList(0, 2),        ex.getPartialResults());
        }
    }


    public void testParallelMap() throws Exception
    {
        int poolSize = 4;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Integer> src = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        List<String> result = CollectionUtil.map(pool, src, new CollectionUtil.IndexValueMapFunctor<Integer,String>()
        {
            public String invoke(int index, Integer value)
            {
                return Thread.currentThread().getName();
            }
        });
        pool.shutdown();

        assertEquals("number of items returned", src.size(), result.size());

        Set<String> threadNames = new HashSet<String>(result);
        // this is a brittle assertions: there's no guarantee that each thread in the pool
        // will be used, although with a large-enough input, it should be valid
        assertEquals("number of threads", poolSize, threadNames.size());
    }


    public void testParallelMapWithException() throws Exception
    {
        int poolSize = 4;
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Integer> src = Arrays.asList(1, 2, 3, 4, 5);
        try
        {
            CollectionUtil.map(pool, src, new CollectionUtil.IndexValueMapFunctor<Integer,Integer>()
            {
                public Integer invoke(int index, Integer value)
                {
                    if (index != 2)
                        return value;
                    else
                        throw new IllegalArgumentException("foo");
                }
            });
            fail("map returned normally");
        }
        catch (CollectionUtil.MapException ex)
        {
            assertEquals("wrapped exception",   IllegalArgumentException.class, ex.getCause().getClass());
            assertEquals("wrapped exception",   "foo",                          ex.getCause().getMessage());
            assertEquals("failure index",       2,                              ex.getIndex());
            assertEquals("failure value",       Integer.valueOf(3),             ex.getValue());
            assertEquals("partial results",     Arrays.asList(1, 2, null, 4, 5), ex.getPartialResults());

        }
        finally
        {
            pool.shutdown();
        }
    }


    public void testReduce() throws Exception
    {
        List<Integer> src = Arrays.asList(1, 2, 3, 4);
        Integer rslt = CollectionUtil.reduce(src, new CollectionUtil.IndexValueReduceFunctor<Integer,Integer>()
        {
            public Integer invoke(int index, Integer value, Integer pendingResult)
            {
                if (pendingResult == null)
                    return value;
                else
                    return Integer.valueOf(value.intValue() + pendingResult.intValue());
            }
        });

        assertEquals(rslt, Integer.valueOf(10));
    }


    public void testReduceWithException() throws Exception
    {
        try
        {
            List<Integer> src = Arrays.asList(1, 2, null, 4);
            CollectionUtil.reduce(src, new CollectionUtil.IndexValueReduceFunctor<Integer,Integer>()
            {
                public Integer invoke(int index, Integer value, Integer pendingResult)
                {
                    if (pendingResult == null)
                        return value;
                    else
                        return Integer.valueOf(value.intValue() + pendingResult.intValue());
                }
            });
        }
        catch (CollectionUtil.ReduceException ex)
        {
            assertEquals("wrapped exception",   NullPointerException.class, ex.getCause().getClass());
            assertEquals("failure index",       2,                          ex.getIndex());
            assertEquals("failure value",       null,                       ex.getValue());
            assertEquals("partial results",     Integer.valueOf(3),         ex.getPartialResults());
        }
    }


    public void testFilter() throws Exception
    {
        List<Integer> src = Arrays.asList(1, 2, 3, 4);
        List<Integer> dst = CollectionUtil.filter(src, new CollectionUtil.Predicate<Integer>()
        {
            public boolean invoke(int index, Integer value)
            {
                return (value.intValue() & 1) == 0;
            }
        });

        assertEquals(Arrays.asList(2, 4), dst);
    }


    public void testFilterWithException() throws Exception
    {
        List<Integer> src = Arrays.asList(1, 2, null, 4);
        try
        {
            CollectionUtil.filter(src, new CollectionUtil.Predicate<Integer>()
            {
                public boolean invoke(int index, Integer value)
                {
                    return (value.intValue() & 1) == 0;
                }
            });
        }
        catch (CollectionUtil.FilterException ex)
        {
            assertEquals("wrapped exception",   NullPointerException.class, ex.getCause().getClass());
            assertEquals("failure index",       2,                          ex.getIndex());
            assertEquals("failure value",       null,                       ex.getValue());
            assertEquals("partial results",     Arrays.asList(2),           ex.getPartialResults());
        }
    }


    @SuppressWarnings("serial")
    public void testGetVia() throws Exception
    {
        // I *really* hate this technique to make it look like I've got an inline map initializer
        // (when it's really creating a new class) but in this case I think that the concise code
        // justifies the means

        Map<Object,Object> root = new HashMap<Object,Object>()
        {{
            put(0, "fribble");
            put(1, new HashMap<Object,Object>()
                    {{
                        put("gorf", "blerp");
                    }});
            put("foo", new LinkedList<String>(Arrays.asList("argle", "bargle", "wargle", "zerg")));
            put("bar", new String[] { "ix", "ax", "ox", "fx"});
            put("baz", Arrays.asList(
                    new HashMap<Object,Object>()
                        {{
                            put("foo", Arrays.asList("something", "wicked", "this", "way", "comes"));
                        }},
                    Arrays.asList("123", "bcd", "efg")));
        }};

        // happy paths
        assertEquals("keypath: 0",
                     "fribble",
                     CollectionUtil.getVia(root, 0));
        assertEquals("keypath: nothing",
                     null,
                     CollectionUtil.getVia(root, "nothing"));
        assertEquals("keypath: 1 gorf",
                     "blerp",
                     CollectionUtil.getVia(root, 1, "gorf"));
        assertEquals("keypath: foo 1",
                     "bargle",
                     CollectionUtil.getVia(root, "foo", 1));
        assertEquals("keypath: foo 3",
                     "zerg",
                     CollectionUtil.getVia(root, "foo", 3));
        assertEquals("keypath: bar 2",
                     "ox",
                     CollectionUtil.getVia(root, "bar", 2));
        assertEquals("keypath: bar 3",
                     "fx",
                     CollectionUtil.getVia(root, "bar", 3));
        assertEquals("keypath: baz 0 foo 3",
                     "way",
                     CollectionUtil.getVia(root, "baz", 0, "foo", 3));
        assertEquals("keypath: baz 1 2",
                     "efg",
                     CollectionUtil.getVia(root, "baz", 1, 2));

        // these should all find something missing

        assertEquals("empty keypath",
                     root,
                     CollectionUtil.getVia(root));
        assertEquals("null root",
                     null,
                     CollectionUtil.getVia(null, "fribble", "bibble", "biff"));
        assertEquals("keypath: foo 4",
                     null,
                     CollectionUtil.getVia(root, "foo", 4));
        assertEquals("keypath: foo 17",
                     null,
                     CollectionUtil.getVia(root, "foo", 17));
        assertEquals("keypath: bar 17",
                     null,
                     CollectionUtil.getVia(root, "bar", 17));
        assertEquals("keypath: bar 4",
                     null,
                     CollectionUtil.getVia(root, "bar", 4));

        // and now the error tests -- note we're checking messages

        try
        {
            CollectionUtil.getVia(root, "foo", "biff", "nope");
            fail("did not throw when attempting to get from array via string index");
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("exception indicates class", ex.getMessage().contains("java.util.LinkedList"));
            assertTrue("exception indicates path", ex.getMessage().contains(Arrays.asList("foo", "biff").toString()));
        }

        try
        {
            CollectionUtil.getVia(root, "bar", 2, "nuh-huh", "nope");
            fail("did not throw when attempting to get from non-collection");
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue("exception indicates class", ex.getMessage().contains("java.lang.String"));
            assertTrue("exception indicates path", ex.getMessage().contains(Arrays.asList("bar", 2, "nuh-huh").toString()));
        }
    }


    public void testPartition() throws Exception
    {
        assertEquals(
                "null source",
                Collections.<Integer>emptyList(),
                CollectionUtil.partition(null, 2));

        assertEquals(
                "empty source list",
                Collections.<Integer>emptyList(),
                CollectionUtil.partition(Collections.<Integer>emptyList(), 2));

        assertEquals(
                "non-empty source array",
                Arrays.asList(Arrays.asList(1,2), Arrays.asList(3,4), Arrays.asList(5)),
                CollectionUtil.partition(Arrays.asList(1,2,3,4,5), 2));
    }


    public void testSubmap() throws Exception
    {
        Map<Integer,String> source = new HashMap<Integer,String>();
        source.put(1, "foo");
        source.put(2, "bar");
        source.put(3, "baz");

        assertEquals("null source",
                     Collections.emptyMap(),
                     CollectionUtil.submap(null, Arrays.asList(1, 2, 3)));

        assertEquals("null keylist",
                     Collections.emptyMap(),
                     CollectionUtil.submap(source, null));

        assertEquals("null destination",
                     null,
                     CollectionUtil.submap(source, Arrays.asList(1,2), null));

        Map<Integer,String> expected = new HashMap<Integer,String>();
        expected.put(2, "bar");

        assertEquals("normal operation",
                     expected,
                     CollectionUtil.submap(source, Arrays.asList(2)));

        Map<Integer,String> dest = new TreeMap<Integer,String>();

        assertSame("explicit destination",
                     dest,
                     CollectionUtil.submap(source, Arrays.asList(2), dest));
    }
}
