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

package com.kdgregory.kdgcommons.test;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestSelfMock
{
    // note that we don't implement all methods of the interface
    public static class TestMock
    extends SelfMock<CharSequence>
    {
        public TestMock()
        {
            super(CharSequence.class);
        }

        // tests normal invocation -- can be called with different arguments
        public char charAt(int index)
        {
            return (char)('0' + (index % 10));
        }

        // tests invocation exceptions
        public int length()
        {
            throw new IllegalStateException("testing");
        }

    }


    @Test
    public void testNormalOperation() throws Exception
    {
        TestMock mock = new TestMock();
        CharSequence instance = mock.getInstance();

        assertEquals("count before invocations", 0, mock.getInvocationCount("charAt"));

        // two invocations so that we can verify history

        assertEquals('3', instance.charAt(3));
        assertEquals('5', instance.charAt(15));

        assertEquals("count after invocations",     2,                      mock.getInvocationCount("charAt"));
        assertEquals("argument count, call 0",      1,                      mock.getInvocationArgs("charAt", 0).length);
        assertEquals("argument value, call 0",      Integer.valueOf(3),     mock.getInvocationArgs("charAt", 0)[0]);
        assertEquals("as-type value, call 0",       3,                      mock.getInvocationArg("charAt", 0, 0, Integer.class).intValue());
        assertEquals("argument count, call 1",      1,                      mock.getInvocationArgs("charAt", 1).length);
        assertEquals("argument value, call 1",      Integer.valueOf(15),    mock.getInvocationArgs("charAt", 1)[0]);
        assertEquals("as-type value, call 1",       15,                     mock.getInvocationArg("charAt", 1, 0, Integer.class).intValue());

        assertEquals("argument to most recent call", Integer.valueOf(15),   mock.getMostRecentInvocationArg("charAt", 0, Integer.class));
    }


    @Test
    public void testExceptionInMock() throws Exception
    {
        TestMock mock = new TestMock();
        CharSequence instance = mock.getInstance();

        try
        {
            instance.length();
            fail("successful invocation of method that was supposed to throw");
        }
        catch (IllegalStateException ex)
        {
            assertEquals("count incremented even though method throws",     1,      mock.getInvocationCount("length"));
            assertArrayEquals("history recorded even though method throws", null,   mock.getInvocationArgs("length", 0));
        }
    }


    @Test
    public void testMissingMethod() throws Exception
    {
        TestMock mock = new TestMock();
        CharSequence instance = mock.getInstance();

        try
        {
            instance.subSequence(1, 10);
            fail("successful invocation of method that doesn't exist");
        }
        catch (UnsupportedOperationException ex)
        {
            assertEquals("count incremented even though method doesn't exist", 1, mock.getInvocationCount("subSequence"));
        }
    }


    @Test
    public void testInvokeInheritedFunctions() throws Exception
    {
        TestMock mock = new TestMock() { /* nothing new here */ };
        CharSequence instance = mock.getInstance();

        assertEquals('0', instance.charAt(10));
    }

}
