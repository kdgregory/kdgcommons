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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestCombiningInputStream
{

//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    @Test
    @SuppressWarnings("resource")
    public void testSingleStreamSimpleRead() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}));
        assertEquals('A', in.read());
        assertEquals('B', in.read());
        assertEquals(-1, in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testMultipleStreamsSimpleRead() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertEquals('A', in.read());
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals('D', in.read());
        assertEquals('E', in.read());
        assertEquals('F', in.read());
        assertEquals(-1, in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testSingleStreamBufRead() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}));

        byte[] buf = new byte[1024];
        assertEquals(2, in.read(buf));
        assertEquals('A', buf[0]);
        assertEquals('B', buf[1]);

        assertEquals(-1, in.read(buf));
    }


    @Test
    @SuppressWarnings("resource")
    public void testMultipleStreamsBufRead() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));

        byte[] buf = new byte[1024];
        assertEquals(6, in.read(buf));
        assertEquals('A', buf[0]);
        assertEquals('B', buf[1]);
        assertEquals('C', buf[2]);
        assertEquals('D', buf[3]);
        assertEquals('E', buf[4]);
        assertEquals('F', buf[5]);

        assertEquals(-1, in.read(buf));
    }


    @Test
    @SuppressWarnings("resource")
    public void testMultipleStreamsBufReadWithZeroAvailable() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new WouldBlockInputStream(
                                    new byte[] {'A', 'B'}),
                            new WouldBlockInputStream(
                                    new byte[] {'C', 'D'}));

        byte[] buf = new byte[1024];
        assertEquals(2, in.read(buf));
        assertEquals('A', buf[0]);
        assertEquals('B', buf[1]);

        assertEquals(2, in.read(buf));
        assertEquals('C', buf[0]);
        assertEquals('D', buf[1]);

        assertEquals(-1, in.read(buf));
    }


    @Test
    @SuppressWarnings("resource")
    public void testAvailable() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}));

        assertEquals(2, in.available());
        assertEquals(2, in.read(new byte[2]));
        assertEquals(0, in.available());

        assertEquals(1, in.read(new byte[1]));
        assertEquals(1, in.available());
    }


    @Test
    @SuppressWarnings("resource")
    public void testClose() throws Exception
    {
        CloseAssertingInputStream sub1 = new CloseAssertingInputStream(false);
        CloseAssertingInputStream sub2 = new CloseAssertingInputStream(false);
        InputStream in = new CombiningInputStream(sub1, sub2);

        in.close();
        assertTrue(sub1.isClosed());
        assertTrue(sub2.isClosed());
    }


    @Test
    @SuppressWarnings("resource")
    public void testCloseWithException() throws Exception
    {
        CloseAssertingInputStream sub1 = new CloseAssertingInputStream(true);
        CloseAssertingInputStream sub2 = new CloseAssertingInputStream(false);
        InputStream in = new CombiningInputStream(sub1, sub2);

        try
        {
            in.close();
            fail("should have thrown");
        }
        catch (IOException e)
        {
            // success
        }

        assertFalse(sub1.isClosed());
        assertTrue(sub2.isClosed());
    }


    @Test
    @SuppressWarnings("resource")
    public void testMarkSupported() throws Exception
    {
        // everyone supports
        InputStream in1 = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertTrue(in1.markSupported());

        // someone doesn't support, and we're before it
        InputStream in2 = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new MarkNotSupportedInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertFalse(in2.markSupported());

        // someone doesn't support, but we're after it
        InputStream in3 = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new MarkNotSupportedInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertEquals(5, in3.read(new byte[5]));
        assertTrue(in3.markSupported());assertFalse(in2.markSupported());

        // someone doesn't support, and we're on the boundary
        InputStream in4 = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new MarkNotSupportedInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertEquals(4, in4.read(new byte[4]));
        assertFalse(in4.markSupported());
    }


    @Test
    @SuppressWarnings("resource")
    public void testMarkResetSingleConstituent() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B', 'C'}));
        assertEquals('A', in.read());
        in.mark(10);

        assertEquals('B', in.read());
        in.reset();
        assertEquals('B', in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testMarkResetMultipleComponents() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertEquals('A', in.read());
        in.mark(10);

        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals('D', in.read());
        assertEquals('E', in.read());

        in.reset();
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals('D', in.read());
        assertEquals('E', in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testMarkResetAtEOF() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));
        assertEquals('A', in.read());
        in.mark(10);

        assertEquals(5, in.read(new byte[1024]));
        assertEquals(-1, in.read());

        in.reset();
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals('D', in.read());
        assertEquals('E', in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testSkip() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));

        assertEquals('A', in.read());
        assertEquals(4, in.skip(4));
        assertEquals('F', in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testSkipAtEOF() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}));

        assertEquals('A', in.read());
        assertEquals(3, in.skip(3));
        assertEquals(-1, in.read());
        assertEquals(-1, in.skip(4));
    }


    @Test
    @SuppressWarnings("resource")
    public void testSkipWithReset() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {'A', 'B'}),
                            new ByteArrayInputStream(
                                    new byte[] {'C', 'D'}),
                            new ByteArrayInputStream(
                                    new byte[] {'E', 'F'}));

        assertEquals('A', in.read());
        in.mark(10);
        assertEquals(4, in.skip(4));
        assertEquals('F', in.read());

        in.reset();
        assertEquals('B', in.read());
        assertEquals('C', in.read());
        assertEquals('D', in.read());
        assertEquals('E', in.read());
    }


    // cross-library regression test (did not find failure)
    @Test
    @SuppressWarnings("resource")
    public void testSingleByteReadDoesNotSignExtend() throws Exception
    {
        InputStream in = new CombiningInputStream(
                            new ByteArrayInputStream(
                                    new byte[] {0x01, (byte)0xFF}),
                            new ByteArrayInputStream(
                                    new byte[] {0x02}));
        assertEquals(0x01, in.read());
        assertEquals(0xFF, in.read());
        assertEquals(0x02, in.read());
        assertEquals(-1, in.read());
    }

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  This class always reports 0 bytes available, simulating a stream that
     *  would block if it were read.
     */
    private static class WouldBlockInputStream
    extends ByteArrayInputStream
    {
        public WouldBlockInputStream(byte[] data)
        {
            super(data);
        }

        @Override
        public synchronized int available()
        {
            return 0;
        }
    }


    /**
     *  This class overrides the default <code>markSupported()</code> to
     *  always return <code>false</code>.
     */
    private static class MarkNotSupportedInputStream
    extends ByteArrayInputStream
    {
        public MarkNotSupportedInputStream(byte[] data)
        {
            super(data);
        }

        @Override
        public boolean markSupported()
        {
            return false;
        }
    }


    /**
     *  A stream where we can assert that <code>close()</code> was called.
     *  and also cause an exception when closing.
     */
    private static class CloseAssertingInputStream
    extends ByteArrayInputStream
    {
        boolean _doThrow;
        boolean _closed;

        public CloseAssertingInputStream(boolean doThrow)
        {
            super(new byte[0]);
            _doThrow = doThrow;
        }

        @Override
        public void close() throws IOException
        {
            if (_doThrow)
                throw new IOException("testing close failure");
            _closed = true;
        }

        public boolean isClosed()
        {
            return _closed;
        }
    }
}
