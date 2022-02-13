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


public class TestStringAsserts
{
    @Test
    public void testAsserNotEmpty() throws Exception
    {
        StringAsserts.assertNotEmpty("this succeeds");

        AssertionError fail1 = null;
        try
        {
            StringAsserts.assertNotEmpty(null);
        }
        catch (AssertionError ex)
        {
            fail1 = ex;
            assertEquals("expected exception to indicate value was null", "expected not-empty, was null", ex.getMessage());
        }
        assertNotNull("null did not cause assertion failure", fail1);

        AssertionError fail2 = null;
        try
        {
            StringAsserts.assertNotEmpty("");
        }
        catch (AssertionError ex)
        {
            fail2 = ex;
            assertEquals("expected exception to indicate value was empty", "expected not-empty", ex.getMessage());
        }
        assertNotNull("empty string did not cause assertion failure", fail2);

        AssertionError fail3 = null;
        try
        {
            StringAsserts.assertNotEmpty("example", "");
        }
        catch (AssertionError ex)
        {
            fail3 = ex;
            assertEquals("expected exception to start with user message", "example: expected not-empty", ex.getMessage());
        }
        assertNotNull("null did not cause assertion failure", fail3);
    }


    @Test
    public void testAssertSubstringCount0() throws Exception
    {
        StringAsserts.assertSubstringCount("foo", "bar", 0);
    }


    @Test
    public void testAssertSubstringCount1() throws Exception
    {
        StringAsserts.assertSubstringCount("bar", "bar", 1);
        StringAsserts.assertSubstringCount("foobarbaz", "bar", 1);
    }


    @Test
    public void testAssertSubstringCount2() throws Exception
    {
        StringAsserts.assertSubstringCount("barbar", "bar", 2);
        StringAsserts.assertSubstringCount("foobarbazbar", "bar", 2);
        StringAsserts.assertSubstringCount("foobarbazbarfoo", "bar", 2);
    }


    @Test
    public void testAssertSubstringFailure() throws Exception
    {
        try
        {
            StringAsserts.assertSubstringCount("foo", "bar", 1);
        }
        catch (AssertionError e)
        {
            return; // success
        }
        fail("assertion passed when it shouldn't");
    }


    @Test
    public void testRegex() throws Exception
    {
        StringAsserts.assertRegex("test", "test");
        StringAsserts.assertRegex("this.*test", "this is a test");
    }


    @Test
    public void testRegexFailure() throws Exception
    {
        try
        {
            StringAsserts.assertRegex("foo", "bar");
        }
        catch (AssertionError e)
        {
            return; // success
        }
        fail("assertion passed when it shouldn't");
    }


    @Test
    public void testContainsRegex() throws Exception
    {
        StringAsserts.assertContainsRegex("test", "this is a test");
        StringAsserts.assertContainsRegex("\\s", "this is a test");
    }


    @Test
    public void testContainsRegexFailure() throws Exception
    {
        try
        {
            StringAsserts.assertContainsRegex("^test", "this is a test");
        }
        catch (AssertionError e)
        {
            return; // success
        }
        fail("assertion passed when it shouldn't");
    }


    @Test
    public void testDoesntContainRegex() throws Exception
    {
        StringAsserts.assertDoesntContainRegex("foo", "this is a test");
    }


    @Test
    public void testDoesntContainRegexFailure() throws Exception
    {
        try
        {
            StringAsserts.assertDoesntContainRegex("test", "this is a test");
        }
        catch (AssertionError e)
        {
            return; // success
        }
        fail("assertion passed when it shouldn't");
    }


    @Test
    public void testAssertContainsAndRemove() throws Exception
    {
        String src = "foo bar baz bargle";

        String rslt1 = StringAsserts.assertContainsThenRemove(src, "baz");
        assertEquals("foo bar  bargle", rslt1);

        String rslt2 = StringAsserts.assertContainsThenRemove(rslt1, "bar");
        assertEquals("foo   bargle", rslt2);

        // verify that we don't have any index-out-of-bounds errors

        String rslt3 = StringAsserts.assertContainsThenRemove(rslt2, "f");
        assertEquals("oo   bargle", rslt3);

        String rslt4 = StringAsserts.assertContainsThenRemove(rslt3, "e");
        assertEquals("oo   bargl", rslt4);
    }


    @Test
    public void testAssertContainsAndRemoveFailure() throws Exception
    {
        try
        {
            StringAsserts.assertContainsThenRemove("foo bar baz", "argle");
        }
        catch (AssertionError e)
        {
            return; // success
        }
        fail("assertion passed when it shouldn't");
    }
}
