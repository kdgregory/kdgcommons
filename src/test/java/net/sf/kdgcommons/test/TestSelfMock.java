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

package net.sf.kdgcommons.test;

import junit.framework.TestCase;


public class TestSelfMock extends TestCase
{
    // note that we don't implement all methods of the interface
    public static class TestMock
    extends SelfMock<CharSequence>
    {
        public TestMock()
        {
            super(CharSequence.class);
        }

        // tests normal invocation
        public int length()
        {
            return 123;
        }

        // tests invocation exceptions
        public char charAt(int index)
        {
            throw new ArrayIndexOutOfBoundsException("testing");
        }
    }


    public void testNormalOperation() throws Exception
    {
        TestMock mock = new TestMock();
        CharSequence instance = mock.getInstance();

        assertEquals(123, instance.length());
    }


    public void testExceptionInMock() throws Exception
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
            // success
        }
    }


    public void testMissingMethod() throws Exception
    {
        TestMock mock = new TestMock();
        CharSequence instance = mock.getInstance();

        try
        {
            instance.charAt(1);
            fail("successful invocation of method that was supposed to throw");
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            // success
        }
    }
}
