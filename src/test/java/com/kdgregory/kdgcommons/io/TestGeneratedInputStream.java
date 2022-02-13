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

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestGeneratedInputStream
{

//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    @Test
    @SuppressWarnings("resource")
    public void testSingleByteReadOneBuffer() throws Exception
    {
        InputStream in = new TestStream(3, 1);
        assertEquals('A', in.read());
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals(-1,  in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testSingleByteReadMultipleBuffers() throws Exception
    {
        InputStream in = new TestStream(2, 3);
        assertEquals('A', in.read());
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals('D', in.read());
        assertEquals('E', in.read());
        assertEquals('F', in.read());
        assertEquals(-1,  in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testBlockReadOneBuffer() throws Exception
    {
        byte[] buf = new byte[1024];
        InputStream in = new TestStream(3, 1);
        assertEquals(3, in.read(buf));

        assertEquals('A', buf[0]);
        assertEquals('B', buf[1]);
        assertEquals('C', buf[2]);
    }


    @Test
    @SuppressWarnings("resource")
    public void testBlockReadMultipleBuffers() throws Exception
    {
        byte[] buf = new byte[1024];
        InputStream in = new TestStream(2, 3);
        assertEquals(6, in.read(buf));

        assertEquals('A', buf[0]);
        assertEquals('B', buf[1]);
        assertEquals('C', buf[2]);
        assertEquals('D', buf[3]);
        assertEquals('E', buf[4]);
        assertEquals('F', buf[5]);
    }


    @Test
    @SuppressWarnings("resource")
    public void testPartialBlockReadOneBuffer() throws Exception
    {
        byte[] buf = new byte[1024];
        InputStream in = new TestStream(3, 1);
        assertEquals(3, in.read(buf, 4, 20));

        assertEquals('A', buf[4]);
        assertEquals('B', buf[5]);
        assertEquals('C', buf[6]);
    }


    @Test
    @SuppressWarnings("resource")
    public void testPartialBlockReadMultipleBuffers() throws Exception
    {
        byte[] buf = new byte[1024];
        InputStream in = new TestStream(2, 3);
        assertEquals(6, in.read(buf, 4, 20));

        assertEquals('A', buf[4]);
        assertEquals('B', buf[5]);
        assertEquals('C', buf[6]);
        assertEquals('D', buf[7]);
        assertEquals('E', buf[8]);
        assertEquals('F', buf[9]);
    }


    @Test
    @SuppressWarnings("resource")
    public void testAvailable() throws Exception
    {
        InputStream in = new TestStream(3, 1);

        // there's no buffer until the first read
        assertEquals(0, in.available());

        in.read();
        assertEquals(2, in.available());
    }


    @Test
    @SuppressWarnings("resource")
    public void testThrowsAfterClose() throws Exception
    {
        InputStream in = new TestStream(3, 1);

        in.close();
        try
        {
            in.read();
            fail("able to read after closing stream");
        }
        catch (IOException ee)
        {
            // success
        }
    }


    @Test
    @SuppressWarnings("resource")
    public void testMarkAndReset() throws Exception
    {
        InputStream in = new TestStream(3, 1);

        assertFalse(in.markSupported());

        // by default, mark is ignored
        in.mark(10);

        // but reset will throw
        try
        {
            in.reset();
            fail("reset() didn't throw");
        }
        catch (IOException ee)
        {
            // success
        }
    }


    @Test
    @SuppressWarnings("resource")
    public void testSkip() throws Exception
    {
        InputStream in = new TestStream(10, 1);

        assertEquals(5L, in.skip(5L));
        assertEquals('F', in.read());

        assertEquals(4L, in.skip(5L));
        assertEquals(-1, in.read());
    }


    // cross-library regression test
    @Test
    @SuppressWarnings("resource")
    public void testSingleByteReadDoesNotSignExtend() throws Exception
    {
        InputStream in = new GeneratedInputStream()
        {
            @Override
            protected byte[] nextBuffer()
            {
                return new byte[] { (byte)0xFF, 0x01 };
            }
        };

        assertEquals(0xFF, in.read());
        assertEquals(0x01, in.read());
        // will never return a real EOF
    }

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  The test stream: produces fixed-size blocks containing sequential
     *  characters in the range A..Z, up to a specified number of calls.
     */
    private static class TestStream
    extends GeneratedInputStream
    {
        private int bufferSize;
        private int numCalls;
        private int curByte = 0;

        public TestStream(int bufferSize, int numCalls)
        {
            this.bufferSize = bufferSize;
            this.numCalls = numCalls;
        }

        @Override
        protected byte[] nextBuffer()
        {
            if (numCalls-- <= 0)
                return null;

            byte[] buf = new byte[bufferSize];
            for (int ii = 0 ; ii < bufferSize ; ii++)
                buf[ii] = (byte)('A' + curByte++ % 26);
            return buf;
        }
    }

}
