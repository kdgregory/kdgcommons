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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.kdgcommons.test.StringAsserts;


public class TestFunctionInputStream
{
    @Test
    @SuppressWarnings("resource")
    public void testSingleByteReads() throws Exception
    {
        DataSource dataSource = new DataSource(TEST_DATA, 2);
        FunctionInputStream in = new FunctionInputStream(dataSource);

        byte[] data = new byte[TEST_DATA.length];
        for (int ii = 0, b = 0 ; (b = in.read()) >= 0 ; ii++)
        {
            data[ii] = (byte)b;
        }

        assertArrayEquals("read test test data correctly", TEST_DATA, data);
        assertEquals("number of calls to function", TEST_DATA.length / 2 + 1, dataSource.getNumCalls());
        assertEquals("stream offset", data.length, in.getStreamOffset());

        assertEquals("read after end-of-stream", -1, in.read());
    }


    @Test
    @SuppressWarnings("resource")
    public void testMultiByteReads() throws Exception
    {
        int bufSize = 5;  // not an even multiple of test data size
        DataSource dataSource = new DataSource(TEST_DATA, bufSize);
        FunctionInputStream in = new FunctionInputStream(dataSource);

        // used accumulate each read to verify that we got everything
        ByteArrayOutputStream acc = new ByteArrayOutputStream();

        // note that we only read what's available, regardless of the size of the array
        byte[] buf = new byte[10];
        int bytesRead = in.read(buf);
        assertEquals("only read number of bytes available", bufSize, bytesRead);
        assertEquals("stream offset", bufSize, in.getStreamOffset());

        while (bytesRead >= 0)
        {
            acc.write(buf, 0, bytesRead);
            bytesRead = in.read(buf);
        }

        assertArrayEquals("read test test data correctly", TEST_DATA, acc.toByteArray());
    }


    @Test
    @SuppressWarnings("resource")
    public void testAvailable() throws Exception
    {
        int bufSize = 5;
        DataSource dataSource = new DataSource(TEST_DATA, bufSize);
        InputStream in = new FunctionInputStream(dataSource);

        assertEquals("when stream first opened", 0, in.available());

        in.read();
        assertEquals("after first byte read", bufSize - 1, in.available());
    }


    @Test
    @SuppressWarnings("resource")
    public void testClose() throws Exception
    {
        DataSource dataSource = new DataSource(TEST_DATA, 5);
        InputStream in = new FunctionInputStream(dataSource);

        in.read();
        in.close();

        try
        {
            in.available();
            fail("available() should have thrown");
        }
        catch (IOException ex)
        {
            // success
        }

        try
        {
            in.read();
            fail("read() should have thrown");
        }
        catch (IOException ex)
        {
            // success
        }

        try
        {
            in.read(new byte[1024]);
            fail("read(buf) should have thrown");
        }
        catch (IOException ex)
        {
            // success
        }

        try
        {
            in.read(new byte[1024], 0, 10);
            fail("read(buf, off, len) should have thrown");
        }
        catch (IOException ex)
        {
            // success
        }
    }


    @Test
    @SuppressWarnings("resource")
    public void testFunctionReturnsEmptyBufferSingleByteRead() throws Exception
    {
        DataSource dataSource = new DataSource(TEST_DATA, 5)
        {
            @Override
            public byte[] apply(Long t)
            {
                if (numCalls++ == 0)
                    return new byte[0];
                else
                    return super.apply(t);
            }
        };
        InputStream in = new FunctionInputStream(dataSource);

        int b = in.read();
        assertEquals("retried on empty buffer", TEST_DATA[0], b);
    }


    @Test
    @SuppressWarnings("resource")
    public void testFunctionReturnsEmptyBufferMultiByteRead() throws Exception
    {
        final int batchSize = 5;
        DataSource dataSource = new DataSource(TEST_DATA, batchSize)
        {
            @Override
            public byte[] apply(Long t)
            {
                if (numCalls++ == 0)
                    return new byte[0];
                else
                    return super.apply(t);
            }
        };
        InputStream in = new FunctionInputStream(dataSource);

        byte[] sink = new byte[1024];

        int c = in.read(sink);
        assertEquals("first read returns zero bytes", 0, c);

        c = in.read(sink);
        assertEquals("second read returns data", batchSize, c);
    }


    @Test
    @SuppressWarnings("resource")
    public void testMultiByteReadZeroLength() throws Exception
    {
        DataSource dataSource = new DataSource(TEST_DATA, 5);
        InputStream in = new FunctionInputStream(dataSource);

        byte[] sink = new byte[1024];
        int c = in.read(sink, 0, 0);
        assertEquals("read returns zero bytes", 0, c);
    }


    @Test
    @SuppressWarnings("resource")
    public void testFunctionThrows() throws Exception
    {
        final RuntimeException ex0 = new RuntimeException("anything");
        DataSource dataSource = new DataSource(TEST_DATA, 5)
        {
            @Override
            public byte[] apply(Long t)
            {
                throw ex0;
            }
        };
        InputStream in = new FunctionInputStream(dataSource);

        try
        {
            in.read();
            fail("should have thrown");
        }
        catch (IOException ex)
        {
            StringAsserts.assertRegex("exception message (was: " + ex.getMessage() + ")",
                                      "exception in function:.*anything",
                                      ex.getMessage());
            assertSame("original exception is cause", ex0, ex.getCause());
        }
    }


    @Test
    @SuppressWarnings("resource")
    public void testFunctionThrowsAndRecovers() throws Exception
    {
        final RuntimeException ex0 = new RuntimeException("anything");
        DataSource dataSource = new DataSource(TEST_DATA, 5)
        {
            @Override
            public byte[] apply(Long t)
            {
                if (numCalls++ == 0)
                    throw ex0;
                else
                    return super.apply(t);
            }
        };
        InputStream in = new FunctionInputStream(dataSource);

        try
        {
            in.read();
            fail("should have thrown");
        }
        catch (IOException ex)
        {
            assertSame("original exception is cause", ex0, ex.getCause());
        }

        // second try should succeed
        int b = in.read();
        assertEquals("successfully read first byte of data", TEST_DATA[0], b);
    }

//----------------------------------------------------------------------------
//  Helpers
//----------------------------------------------------------------------------

    /**
     *  Some simple data for testing
     */
    private final static byte[] TEST_DATA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.US_ASCII);


    /**
     *  A data source that returns fixed-size chunks of data.
     */
    private static class DataSource
    implements Function<Long,byte[]>
    {
        private byte[] data;
        private int blockSize;

        protected int numCalls;

        public DataSource(byte[] data, int blockSize)
        {
            this.data = data;
            this.blockSize = blockSize;
        }

        @Override
        public byte[] apply(Long t)
        {
            numCalls++;
            int offset = t.intValue();
            int len = Math.min(blockSize, data.length - offset);
            if (len <= 0)
            {
                return null;
            }
            else
            {
                byte[] result = new byte[len];
                System.arraycopy(data, offset, result, 0, len);
                return result;
            }
        }


        public int getNumCalls()
        {
            return numCalls;
        }
    }

}
