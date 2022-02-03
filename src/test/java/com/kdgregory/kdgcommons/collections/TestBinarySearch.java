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

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.kdgcommons.collections.BinarySearch.IndexedComparator;


public class TestBinarySearch
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    // we'll test with character arrays, in keeping with the examples in the
    // method doc ... it's also really easy to implement
    private static class CharacterArrayAccessor
    implements BinarySearch.Accessor<Character>
    {
        private char[] array;
        private int start;
        private int end;

        public CharacterArrayAccessor(char[] array, int start, int end)
        {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        public CharacterArrayAccessor(char[] array)
        {
            this(array, 0, array.length);
        }

        @Override
        public int start()
        {
            return start;
        }

        @Override
        public int end()
        {
            return end;
        }

        @Override
        public int compare(Character value, int index)
        {
            return value.charValue() - array[index];
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    @Test
    public void testAccessorSearchZeroLength() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(new char[0]);

        assertEquals(-1, BinarySearch.search(accessor, 'A'));
    }


    @Test
    public void testAccessorSearchOneElement() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(
                                            new char[] { 'B' });

        assertEquals(-1, BinarySearch.search(accessor, 'A'));
        assertEquals(0,  BinarySearch.search(accessor, 'B'));
        assertEquals(-2, BinarySearch.search(accessor, 'C'));
    }


    @Test
    public void testAccessorSearchTwoElements() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(
                                            new char[] { 'B', 'D' });

        assertEquals(-1, BinarySearch.search(accessor, 'A'));
        assertEquals(0,  BinarySearch.search(accessor, 'B'));
        assertEquals(-2, BinarySearch.search(accessor, 'C'));
        assertEquals(1,  BinarySearch.search(accessor, 'D'));
        assertEquals(-3, BinarySearch.search(accessor, 'E'));
    }


    @Test
    public void testAccessorSearchThreeElements() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(
                                            new char[] { 'B', 'D', 'F' });

        assertEquals(-1, BinarySearch.search(accessor, 'A'));
        assertEquals(0,  BinarySearch.search(accessor, 'B'));
        assertEquals(-2, BinarySearch.search(accessor, 'C'));
        assertEquals(1,  BinarySearch.search(accessor, 'D'));
        assertEquals(-3, BinarySearch.search(accessor, 'E'));
        assertEquals(2,  BinarySearch.search(accessor, 'F'));
        assertEquals(-4, BinarySearch.search(accessor, 'G'));
    }


    // this search will require at least two passes through the loop
    @Test
    public void testAccessorSearchFourElements() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(
                                            new char[] { 'B', 'D', 'F', 'H' });

        assertEquals(-1, BinarySearch.search(accessor, 'A'));
        assertEquals(0,  BinarySearch.search(accessor, 'B'));
        assertEquals(-2, BinarySearch.search(accessor, 'C'));
        assertEquals(1,  BinarySearch.search(accessor, 'D'));
        assertEquals(-3, BinarySearch.search(accessor, 'E'));
        assertEquals(2,  BinarySearch.search(accessor, 'F'));
        assertEquals(-4, BinarySearch.search(accessor, 'G'));
        assertEquals(3,  BinarySearch.search(accessor, 'H'));
        assertEquals(-5, BinarySearch.search(accessor, 'I'));
    }


    // this search will require multiple passes, with even and odd portions
    @Test
    public void testAccessorSearchMultipleElements() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(
                    new char[] { 'B', 'D', 'F', 'H', 'J', 'L', 'N', 'P', 'R'});

        assertEquals(-1, BinarySearch.search(accessor, 'A'));
        assertEquals(0,  BinarySearch.search(accessor, 'B'));
        assertEquals(-2, BinarySearch.search(accessor, 'C'));
        assertEquals(1,  BinarySearch.search(accessor, 'D'));
        assertEquals(-3, BinarySearch.search(accessor, 'E'));
        assertEquals(2,  BinarySearch.search(accessor, 'F'));
        assertEquals(-4, BinarySearch.search(accessor, 'G'));
        assertEquals(3,  BinarySearch.search(accessor, 'H'));
        assertEquals(-5, BinarySearch.search(accessor, 'I'));
        assertEquals(4,  BinarySearch.search(accessor, 'J'));
        assertEquals(-6, BinarySearch.search(accessor, 'K'));
    }


    // this will demonstrate the examples in the method doc
    @Test
    public void testAccessorSearchSubArray() throws Exception
    {
        CharacterArrayAccessor accessor = new CharacterArrayAccessor(
                    new char[] { 'B', 'D', 'F', 'H', 'J', 'L', 'N', 'P', 'R'},
                    2, 4);

        assertEquals(-3, BinarySearch.search(accessor, 'A'));
        assertEquals(-3, BinarySearch.search(accessor, 'B'));
        assertEquals(-3, BinarySearch.search(accessor, 'C'));
        assertEquals(-3, BinarySearch.search(accessor, 'D'));
        assertEquals(-3, BinarySearch.search(accessor, 'E'));
        assertEquals(2,  BinarySearch.search(accessor, 'F'));
        assertEquals(-4, BinarySearch.search(accessor, 'G'));
        assertEquals(3,  BinarySearch.search(accessor, 'H'));
        assertEquals(-5, BinarySearch.search(accessor, 'I'));
        assertEquals(-5, BinarySearch.search(accessor, 'J'));
        assertEquals(-5, BinarySearch.search(accessor, 'K'));
    }


    @Test
    public void testIndexSearch() throws Exception
    {
        final char[] chars = new char[] { 'D', 'H', 'F', 'B', 'J' };
        final int[] index = new int[] { 3, 0, 2, 1, 4 };

        IndexedComparator<Character> cmp = new IndexedComparator<Character>()
        {
            @Override
            public int compare(Character value, int ii)
            {
                return value.charValue() - chars[ii];
            }
        };

        assertEquals(-1, BinarySearch.search(index, 'A', cmp));
        assertEquals(0,  BinarySearch.search(index, 'B', cmp));
        assertEquals(-2, BinarySearch.search(index, 'C', cmp));
        assertEquals(1,  BinarySearch.search(index, 'D', cmp));
        assertEquals(-3, BinarySearch.search(index, 'E', cmp));
        assertEquals(2,  BinarySearch.search(index, 'F', cmp));
        assertEquals(-4, BinarySearch.search(index, 'G', cmp));
        assertEquals(3,  BinarySearch.search(index, 'H', cmp));
        assertEquals(-5, BinarySearch.search(index, 'I', cmp));
        assertEquals(4,  BinarySearch.search(index, 'J', cmp));
        assertEquals(-6, BinarySearch.search(index, 'K', cmp));
    }
}
