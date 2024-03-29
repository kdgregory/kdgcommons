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
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestCompoundKey
{
    @Test
    public void testSingleItem() throws Exception
    {
        CompoundKey key1a = new CompoundKey("foo");
        CompoundKey key1b = new CompoundKey("foo");
        CompoundKey key2 = new CompoundKey("bar");

        assertTrue(key1a.equals(key1b));
        assertFalse(key1a.equals(key2));

        assertTrue(key1a.hashCode() == key1b.hashCode());
        assertFalse(key1a.hashCode() == key2.hashCode());   // known values
    }


    @Test
    public void testMultiItem() throws Exception
    {
        CompoundKey key1a = new CompoundKey("foo", "bar");
        CompoundKey key1b = new CompoundKey("foo", "bar");
        CompoundKey key2 = new CompoundKey("bar", "baz");

        assertTrue(key1a.equals(key1b));
        assertFalse(key1a.equals(key2));

        assertTrue(key1a.hashCode() == key1b.hashCode());
        assertFalse(key1a.hashCode() == key2.hashCode());   // known values
    }


    @Test
    public void testMultiItemVersusSingleItem() throws Exception
    {
        CompoundKey key1 = new CompoundKey("foo");
        CompoundKey key2 = new CompoundKey("foo", "bar");

        assertFalse(key1.equals(key2));
        assertFalse(key1.hashCode() == key2.hashCode());   // known values
    }


    @Test
    public void testNulls() throws Exception
    {
        CompoundKey key1a = new CompoundKey("foo", null, "bar");
        CompoundKey key1b = new CompoundKey("foo", null, "bar");
        CompoundKey key2 = new CompoundKey(null, "bar", "baz");

        assertTrue(key1a.equals(key1b));
        assertFalse(key1a.equals(key2));

        assertTrue(key1a.hashCode() == key1b.hashCode());
        assertFalse(key1a.hashCode() == key2.hashCode());   // known values
    }



    @Test
    public void testToString() throws Exception
    {
        assertEquals("[]", new CompoundKey().toString());
        assertEquals("[foo]", new CompoundKey("foo").toString());
        assertEquals("[foo,bar]", new CompoundKey("foo", "bar").toString());
        assertEquals("[foo,bar,baz]", new CompoundKey("foo", "bar", "baz").toString());
    }


    @Test
    public void testIterable() throws Exception
    {
        Iterator<Object> itx = new CompoundKey("foo", "bar", "baz").iterator();
        assertEquals("foo", itx.next());
        assertEquals("bar", itx.next());
        assertEquals("baz", itx.next());
        assertFalse(itx.hasNext());
    }


    @Test
    @SuppressWarnings("resource")
    public void testSerialization() throws Exception
    {
        CompoundKey key = new CompoundKey("foo", "bar", "baz");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(key);
        oos.close();

        ByteArrayInputStream bis= new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        CompoundKey ret = (CompoundKey)ois.readObject();
        assertEquals(key, ret);
    }


    @Test
    public void testToStringCache() throws Exception
    {
        // this test is to increase coverage; caching is not part of the contract
        CompoundKey key = new CompoundKey();
        String s1 = key.toString();
        String s2 = key.toString();
        assertSame(s1, s2);
    }


    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void testEqualsEdgeCases() throws Exception
    {
        // this test is to increase coverage
        CompoundKey key = new CompoundKey("foo", "bar");

        assertTrue("key.equals(key)",    key.equals(key));
        assertFalse("key.equals(bogus)", key.equals("bogus"));
        assertFalse("key.equals(null)",  key.equals(null));
    }
}
