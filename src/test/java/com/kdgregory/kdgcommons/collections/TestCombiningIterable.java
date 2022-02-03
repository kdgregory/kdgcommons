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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestCombiningIterable
{
    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyConstructor() throws Exception
    {
        CombiningIterable<String> iterable = new CombiningIterable<String>();
        Iterator<String> itx = iterable.iterator();
        assertFalse(itx.hasNext());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testSingleEmptyList() throws Exception
    {
        List<String> list = Collections.emptyList();

        CombiningIterable<String> iterable = new CombiningIterable<String>(list);
        Iterator<String> itx = iterable.iterator();
        assertFalse(itx.hasNext());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testSingleList() throws Exception
    {
        List<String> list = Arrays.asList("foo", "bar");

        CombiningIterable<String> iterable = new CombiningIterable<String>(list);
        Iterator<String> itx = iterable.iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());
        assertFalse(itx.hasNext());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleLists() throws Exception
    {
        List<String> list1 = Arrays.asList("foo", "bar");
        List<String> list2 = Arrays.asList("baz");

        CombiningIterable<String> iterable = new CombiningIterable<String>(list1, list2);
        Iterator<String> itx = iterable.iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertFalse(itx.hasNext());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testItrableOfIterables() throws Exception
    {
        List<String> list1 = Arrays.asList("foo", "bar");
        List<String> list2 = Arrays.asList("baz");

        List<List<String>> iterables = new ArrayList<List<String>>();
        iterables.add(list1);
        iterables.add(list2);

        CombiningIterable<String> iterable = new CombiningIterable<String>(list1, list2);

        // clearing the source list ensures that we make a defensive copy
        iterables.clear();

        Iterator<String> itx = iterable.iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertFalse(itx.hasNext());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testCanProduceMultipleIndependentIterables() throws Exception
    {
        List<String> list1 = Arrays.asList("foo", "bar");
        List<String> list2 = Arrays.asList("baz");

        CombiningIterable<String> iterable = new CombiningIterable<String>(list1, list2);

        // create them both at the same time in case one affects the other
        Iterator<String> itx1 = iterable.iterator();
        Iterator<String> itx2 = iterable.iterator();

        // verify they're both usable
        assertEquals("foo", itx1.next());
        assertEquals("bar", itx1.next());
        assertEquals("baz", itx1.next());
        assertFalse(itx1.hasNext());
        assertEquals("foo", itx2.next());
        assertEquals("bar", itx2.next());
        assertEquals("baz", itx2.next());
        assertFalse(itx2.hasNext());

        // now create a third to verify that iteration doesn't affect source
        Iterator<String> itx3 = iterable.iterator();
        assertEquals("foo", itx3.next());
        assertEquals("bar", itx3.next());
        assertEquals("baz", itx3.next());
        assertFalse(itx3.hasNext());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveFromModifiableList() throws Exception
    {
        List<String> list1 = new ArrayList<String>(Arrays.asList("foo", "bar"));
        List<String> list2 = new ArrayList<String>(Arrays.asList("baz"));

        CombiningIterable<String> iterable = new CombiningIterable<String>(list1, list2);

        Iterator<String> itx = iterable.iterator();
        assertEquals("foo", itx.next());
        itx.remove();
        assertEquals(1, list1.size());
        assertEquals("bar", list1.get(0));
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        itx.remove();
        assertEquals(0, list2.size());
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveFromUnodifiableListThrows() throws Exception
    {
        List<String> list1 = Arrays.asList("foo", "bar");
        List<String> list2 = Arrays.asList("baz");

        CombiningIterable<String> iterable = new CombiningIterable<String>(list1, list2);

        Iterator<String> itx = iterable.iterator();
        assertEquals("foo", itx.next());
        try
        {
            itx.remove();
            fail("able to remove from unmodifiable list");
        }
        catch (UnsupportedOperationException ex)
        {
            // success
        }
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testIteratingOffEndWillThrow() throws Exception
    {
        List<String> list = Arrays.asList("foo", "bar");

        CombiningIterable<String> iterable = new CombiningIterable<String>(list);
        Iterator<String> itx = iterable.iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());

        try
        {
            itx.next();
            fail("able to iterate off end of list");
        }
        catch (NoSuchElementException ex)
        {
            // success
        }
    }
}
