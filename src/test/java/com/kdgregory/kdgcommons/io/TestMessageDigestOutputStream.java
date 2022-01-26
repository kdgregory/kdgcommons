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

package com.kdgregory.kdgcommons.io;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

import com.kdgregory.kdgcommons.test.ArrayAsserts;


public class TestMessageDigestOutputStream
extends TestCase
{
    @SuppressWarnings("resource")
    public void testBasicOperation() throws Exception
    {
        byte[] data = "This is some test data".getBytes("UTF-8");

        MessageDigest digester = MessageDigest.getInstance("MD5");
        MessageDigestOutputStream out = new MessageDigestOutputStream("MD5");

        digester.update(data[0]);
        out.write(data[0]);

        digester.update(data);
        out.write(data);

        digester.update(data, 3, 12);
        out.write(data, 3, 12);

        byte[] expectedDigest = digester.digest();

        ArrayAsserts.assertEquals("digests matches check",      expectedDigest,                     out.digest());
        ArrayAsserts.assertEquals("can digest multiple times",  expectedDigest,                     out.digest());
        assertEquals("string digest matches precomputed",       "07e3ef6e12a61cc291789f3728d57e90", out.digestAsString());
    }


    @SuppressWarnings("resource")
    public void testInvalidAlgorithm() throws Exception
    {
        try
        {
            new MessageDigestOutputStream("MD5000");
        }
        catch (RuntimeException ex)
        {
            assertTrue("message contains algorithm (was: " + ex.getMessage() + ")", ex.getMessage().contains("MD5000"));
            assertSame("wraps original exception",  NoSuchAlgorithmException.class, ex.getCause().getClass());
        }
    }


    @SuppressWarnings("resource")
    public void testWriteAfterDigest() throws Exception
    {
        MessageDigestOutputStream out = new MessageDigestOutputStream("MD5");
        out.write("test".getBytes());
        out.digest();

        try
        {
            out.write('x');
            fail("able to write after digest; single byte");
        }
        catch (IllegalStateException ex)
        {
            // success
        }

        try
        {
            out.write("test2".getBytes());
            fail("able to write after digest; array");
        }
        catch (IllegalStateException ex)
        {
            // success
        }

        try
        {
            out.write("test2".getBytes(), 1, 2);
            fail("able to write after digest; array section");
        }
        catch (IllegalStateException ex)
        {
            // success
        }
    }
}
