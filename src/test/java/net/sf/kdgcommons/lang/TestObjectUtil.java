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

package net.sf.kdgcommons.lang;

import junit.framework.TestCase;

public class TestObjectUtil extends TestCase
{
    public void testEquals() throws Exception
    {
        // identity
        Object o1 = new Object();
        assertTrue(ObjectUtil.equals(null, null));
        assertTrue(ObjectUtil.equals(o1, o1));
        assertFalse(ObjectUtil.equals(o1, null));
        assertFalse(ObjectUtil.equals(null, o1));
        assertFalse(ObjectUtil.equals(o1, new Object()));

        // content
        assertTrue(ObjectUtil.equals(new Integer(12345), new Integer(12345)));

        // arrays
        int[] a1 = new int[] { 1, 2, 3 };
        int[] a2 = new int[] { 1, 2, 3 };
        int[] a3 = new int[] { 1, 2 };
        short[] a4 = new short[] { 1, 2, 3 };
        assertTrue(ObjectUtil.equals(a1, a2));
        assertFalse(ObjectUtil.equals(a1, a3));
        assertFalse(ObjectUtil.equals(a1, a4));
        assertTrue(ObjectUtil.equals(new Object[0], new Object[0]));
    }

    public void testHashCode() throws Exception
    {
        assertEquals(0, ObjectUtil.hashCode(null));

        Object obj = new Object();
        assertEquals(obj.hashCode(), ObjectUtil.hashCode(obj));
    }


    public void testIdentityToString() throws Exception
    {
        assertEquals("null", ObjectUtil.identityToString(null));

        String foo = "foo";
        String fooStr = ObjectUtil.identityToString(foo);
        assertTrue(fooStr.contains("java.lang.String"));
        assertTrue(fooStr.contains("@"));
        assertTrue(fooStr.contains(String.valueOf(System.identityHashCode(foo))));
    }


    public void testDefaultValue() throws Exception
    {
        assertEquals("non-default", "foo", ObjectUtil.defaultValue("foo", "bar"));
        assertEquals("default",     "bar", ObjectUtil.defaultValue(null, "bar"));
    }


    public void testDefaultValueFromFactory() throws Exception
    {
        ObjectFactory<String> fact = new ObjectFactory<String>()
        {
            public String newInstance()
            {
                return "bar";
            }
        };

        assertEquals("non-default", "foo", ObjectUtil.defaultValue("foo", fact));
        assertEquals("default",     "bar", ObjectUtil.defaultValue(null, fact));
    }

}
