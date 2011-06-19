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

package net.sf.kdgcommons.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import junit.framework.TestCase;

import net.sf.kdgcommons.test.SimpleMock;


public class TestIOUtil
extends TestCase
{
    public void testCloseQuietly() throws Exception
    {
        SimpleMock proxy = new SimpleMock();

        Closeable mock = proxy.getInstance(Closeable.class);
        IOUtil.closeQuietly(mock);

        proxy.assertCallCount(1);
        proxy.assertCall(0, "close");
    }


    public void testCloseQuietlyWithException() throws Exception
    {
        Closeable mock = new Closeable()
        {
            public void close() throws IOException
            {
                throw new IOException();
            }
        };

        // getting through here is sufficient
        IOUtil.closeQuietly(mock);
    }


    public void testCloseQuietlyWithNull() throws Exception
    {
        // getting through here is sufficient
        IOUtil.closeQuietly(null);
    }


    public void testOpenFile() throws Exception
    {
        File file = File.createTempFile("TestIOUtil", ".tmp");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        out.write("test".getBytes());
        out.close();

        // by using the file's name, we test two functions for the price of one
        // ... and there's no good reason to read more than one byte, we're not
        // testing file writing here
        InputStream in = IOUtil.openFile(file.getPath());
        assertEquals('t', in.read());
        in.close();
    }


    public void testOpenGZippedFile() throws Exception
    {
        File file = File.createTempFile("TestIOUtil", ".tmp.gz");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        GZIPOutputStream out = new GZIPOutputStream(fos);
        out.write("test".getBytes());
        out.close();

        InputStream in = IOUtil.openFile(file.getPath());
        assertEquals('t', in.read());
        in.close();
    }


    // this test is for coverage ... I don't know any way to track the number of
    // open file descriptors from the JDK, and looping until FD exhaustion seems
    // like a bad test
    public void testOpenFileFailure() throws Exception
    {
        File file = File.createTempFile("TestIOUtil", ".tmp.gz");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        out.write("test".getBytes());
        out.close();

        try
        {
            // we claim to be GZipped but aren't; the GZIPInputStream ctor will throw
            IOUtil.openFile(file.getPath());
            fail("did not throw on invalid file format");
        }
        catch (IOException ex)
        {
            // success ... now we just need to measure open FDs
        }
    }


    // this doesn't fully test the method; we'd have to restart the JVM to do that
    // ... but there's at least some value in demonstrating that it does indeed
    // create a file
    public void testCreateTempFile() throws Exception
    {
        File file = IOUtil.createTempFile("testCreateTempFile", ".tmp");
        assertTrue(file.exists());
        assertEquals(0, file.length());
    }
}
