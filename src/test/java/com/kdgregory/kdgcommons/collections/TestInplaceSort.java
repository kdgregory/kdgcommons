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
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.kdgcommons.collections.InplaceSort.Accessor;


public class TestInplaceSort
{

//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    @Test
    public void testIntSortEmptyArray() throws Exception
    {
        int[] src = new int[0];
        int[] exp = new int[0];

        InplaceSort.sort(src, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    @Test
    public void testIntSortOneElement() throws Exception
    {
        int[] src = new int[] { 3 };
        int[] exp = new int[] { 3 };

        InplaceSort.sort(src, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    @Test
    public void testIntSortTwoElements() throws Exception
    {
        int[] src = new int[] { 3, 5 };
        int[] exp = new int[] { 5, 3 };

        InplaceSort.sort(src, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    @Test
    public void testIntSortThreeElements() throws Exception
    {
        int[] src = new int[] { 5, 3, 4 };
        int[] exp = new int[] { 5, 4, 3 };

        InplaceSort.sort(src, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    @Test
    public void testIntSortFourElements() throws Exception
    {
        int[] src = new int[] { 5, 3, 4, 12 };
        int[] exp = new int[] { 12, 5, 4, 3 };

        InplaceSort.sort(src, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    // this test added for internal comparator coverage; it won't affect
    // affect coverage of Heapsort itself, but I want to cover all cases
    @Test
    public void testIntSortWithEqualElements() throws Exception
    {
        int[] src = new int[] { 5, 3, 3, 4, 12 };
        int[] exp = new int[] { 12, 5, 4, 3, 3 };

        InplaceSort.sort(src, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    // along with verifying that we didn't get lucky, this test will verify
    // that we're O(logN)
    @Test
    public void testIntSortManyElements() throws Exception
    {
        final int size = 10000;

        int[] src = createRandomArray(size);
        int[] exp = createSortedCopy(src);

        CountingIntComparator cmp = new CountingIntComparator(size);
        InplaceSort.sort(src, cmp);
        assertArraysEqual(exp, src);

        cmp.assertCompareCount();
    }


    @Test
    public void testIntSortPortionOfArray() throws Exception
    {
        int[] src = new int[] { 5, 3, 2, 4, 12 };
        int[] exp = new int[] { 5, 4, 3, 2, 12 };

        InplaceSort.sort(src, 1, 4, new ReversingIntComparator());
        assertArraysEqual(exp, src);
    }


    @Test
    public void testObjectSort() throws Exception
    {
        int[] base = createRandomArray(100);
        Integer[] src = toObjectArray(base);
        Integer[] exp = toObjectArray(base);
        Arrays.sort(exp);

        InplaceSort.sort(src);
        assertEquals(Arrays.asList(exp), Arrays.asList(src));
    }


    @Test
    public void testObjectSortPortionOfArray() throws Exception
    {
        int[] base = createRandomArray(100);
        Integer[] src = toObjectArray(base);
        Integer[] exp = toObjectArray(base);
        Arrays.sort(exp, 4, 8);

        InplaceSort.sort(src, 4, 8);
        assertEquals(Arrays.asList(exp), Arrays.asList(src));
    }


    @Test
    public void testListSort() throws Exception
    {
        List<Integer> base = Arrays.asList(toObjectArray(createRandomArray(100)));
        List<Integer> src = new ArrayList<Integer>(base);
        List<Integer> exp = new ArrayList<Integer>(base);
        Collections.sort(exp);

        InplaceSort.sort(src);
        assertEquals(exp, src);
    }


    @Test
    public void testListSortPortionOfArray() throws Exception
    {
        Integer[] base = toObjectArray(createRandomArray(100));
        List<Integer> src = new ArrayList<Integer>(Arrays.asList(base));
        Arrays.sort(base, 4, 8);
        List<Integer> exp = new ArrayList<Integer>(Arrays.asList(base));

        InplaceSort.sort(src, 4, 8);
        assertEquals(exp, src);
    }


    // accessors are used internally, and I never realized they weren't public
    // ... so here's a test that makes sure Accessor is accessible
    @Test
    public void testAccessorSort() throws Exception
    {
        final char[] data = new char[] { 'A', '2', 'R', 'r', 'R', 'x' };
        char[] exp = new char[] { '2', 'A', 'R', 'R', 'r', 'x' };

        InplaceSort.sort(new Accessor()
        {
            @Override
            public int start()
            {
                return 0;
            }

            @Override
            public int end()
            {
                return data.length;
            }

            @Override
            public int compare(int index1, int index2)
            {
                return data[index1] - data[index2];
            }

            @Override
            public void swap(int index1, int index2)
            {
                char tmp = data[index1];
                data[index1] = data[index2];
                data[index2] = tmp;
            }
        });

        assertTrue(Arrays.equals(exp, data));
    }

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    // the "big" tests will all start with a random int[]
    private static int[] createRandomArray(int size)
    {
        int[] arr = new int[size];
        for (int ii = 0 ; ii < arr.length ; ii++)
            arr[ii] = (int)(Math.random() * Integer.MAX_VALUE);
        return arr;
    }


    // and will need to compare to an already-sorted array
    private static int[] createSortedCopy(int[] src)
    {
        int[] ret = new int[src.length];
        System.arraycopy(src, 0, ret, 0, src.length);
        Arrays.sort(ret);
        return ret;
    }


    // and we'll convert these to objects if needed
    private static Integer[] toObjectArray(int[] src)
    {
        Integer[] ret = new Integer[src.length];
        for (int ii = 0 ; ii < src.length ; ii++)
            ret[ii] = Integer.valueOf(src[ii]);
        return ret;
    }


    // this is used for tests where we rely on Comparable objects
    public static class ForwardIntComparator
    implements InplaceSort.IntComparator
    {
        @Override
        public int compare(int i1, int i2)
        {
            return (i1 > i2) ? 1
                 : (i1 < i2) ? -1
                 : 0;
        }
    }


    // this throws a wrench into int[] tests
    public static class ReversingIntComparator
    implements InplaceSort.IntComparator
    {
        @Override
        public int compare(int i1, int i2)
        {
            return (i1 > i2) ? -1
                 : (i1 < i2) ? 1
                 : 0;
        }
    }


    // this will be used to verify the O(NlogN) property; it orders by
    // increasing values so we can compare result to Arrays.sort()
    public static class CountingIntComparator
    implements InplaceSort.IntComparator
    {
        public int count;
        public int expectedCount;

        public CountingIntComparator(int size)
        {
            // our implementation of heapsort should perform at most 3 compares per element
            expectedCount = 3 * size * (int)Math.ceil(Math.log(size) / Math.log(2));
        }

        @Override
        public int compare(int i1, int i2)
        {
            count++;
            return (i1 < i2) ? -1
                 : (i1 > i2) ? 1
                 : 0;
        }

        public void assertCompareCount()
        {
            assertTrue("expected > 0", count > 0);
            assertTrue("expected < " + expectedCount + ", was " + count,
                       count < expectedCount);
        }
    }


    // but use a straightforward comparison for integers
    public static class ForwardComparator<T extends Comparable<T>>
    implements Comparator<T>
    {
        @Override
        public int compare(T o1, T o2)
        {
            return o1.compareTo(o2);
        }
    }


    private void assertArraysEqual(int[] expected, int[] actual)
    {
        // we'll convert to List<Integer> because the reporting is nicer

        ArrayList<Integer> expected2 = new ArrayList<Integer>(expected.length);
        for (int ii = 0 ; ii < expected.length ; ii++)
            expected2.add(Integer.valueOf(expected[ii]));

        ArrayList<Integer> actual2 = new ArrayList<Integer>(expected.length);
        for (int ii = 0 ; ii < expected.length ; ii++)
            actual2.add(Integer.valueOf(actual[ii]));

        assertEquals(expected2, actual2);
    }

}
