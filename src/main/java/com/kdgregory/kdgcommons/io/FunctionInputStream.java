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
import java.util.function.Function;


/**
 *  An <code>InputStream</code> that calls a Java8 <code>Function</code>  to
 *  generate blocks of data. This can be used to replace a pipe in situations
 *  where you can't spin up a new thread. It's also useful for performing ranged
 *  retrieval from a web-service and wrapping it in a stream.
 *  <p>
 *  The provided function is given the current stream offset (useful for ranged
 *  queries), and may return an array of any size (including zero). When the
 *  source is exhausted, it must return null to indicate end-of-stream.
 *  <p>
 *  If a read operation can retrieve at least one byte from the current buffer
 *  then it will do so. Otherwise, it will call the function to provide a new
 *  buffer to satisfy the read. If this function returns an empty buffer, then
 *  the behavior depends on the variant of the <code>read()</code> method used:
 *  the single-byte variant will continue to call the function until it provides
 *  at least one byte, while the multi-byte variant will return immediately and
 *  report 0 bytes read (this differs from a typical <code>InputStream</code>,
 *  but does meet the documented contract, which allows reading 0 bytes).
 *  <p>
 *  Since the function is only invoked when <code>read()</code> has fully
 *  processed the buffer, it may reuse the buffer. However, it may not return a
 *  partially-filled buffer.
 *  <p>
 *  This class is not thread-safe.
 */
public class FunctionInputStream
extends InputStream
{
    private Function<Long,byte[]> function;

    private long streamOffset;
    private byte[] buf;
    private int bufOffset;
    private boolean isClosed;


    public FunctionInputStream(Function<Long,byte[]> function)
    {
        this.function = function;
        this.buf = new byte[0];     // triggers read on first call
    }

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    /**
     *  Returns the number of bytes available in the current buffer.
     *
     *  @throws IOException if the stream is closed.
     */
    @Override
    public int available() throws IOException
    {
        checkClosed();
        return (buf.length - bufOffset);
    }


    /**
     *  By default, closing the stream simply sets a flag such that all
     *  subsequent reads will throw. Subclasses may chose to override.
     */
    @Override
    public void close() throws IOException
    {
        isClosed = true;
    }


    /**
     *  By default, this method does nothing. Subclasses may override.
     */
    @Override
    public synchronized void mark(int readlimit)
    {
        // nothing to see here, move along
    }


    /**
     *  By default this method returns <code>false</code>. Subclasses may
     *  override.
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }


    /**
     *  By default this method throws. Subclasses may override.
     */
    @Override
    public synchronized void reset() throws IOException
    {
        throw new IOException("mark/reset not supported");
    }


    /**
     *  Attempts to read a single byte from the source, repeatedly calling the
     *  provider function until either (1) it returns a non-empty buffer, or
     *  (2) it returns <code>null</code> (indicating end-of-stream).
     *
     *  @throws IOException if the stream is closed.
     */
    @Override
    public int read() throws IOException
    {
        checkClosed();
        while (isAvailable())
        {
            if (buf.length > 0)
            {
                streamOffset++;
                return buf[bufOffset++] & 0xFF;
            }
        }
        return -1;
    }


    /**
     *  Makes a best-effort to read the requested number of bytes, calling the
     *  provider function at most once.
     *  <p>
     *  If bytes are available in the internal buffer, then those bytes will be
     *  consumed up to the requested length, and the function is <em>not</em>
     *  called. If the internal bufffer is empty at the time of call, then the
     *  function will be called to fill it.
     *
     *  @return The number of bytes copied into the provided array. May be zero,
     *          if either (1) <code>len</code> is 0, or (2) the function returns
     *          an empty buffer.
     *
     *  @throws IOException if the stream is closed.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        checkClosed();
        if (len == 0)
            return 0;
        int bytesRead = -1;
        if ((len > 0) && isAvailable())
        {
            bytesRead = Math.min(len, buf.length - bufOffset);
            System.arraycopy(buf, bufOffset, b, off, bytesRead);
            bufOffset += bytesRead;
            streamOffset += bytesRead;
        }
        return bytesRead;
    }

    /**
     *  Makes a best-effort to populate the provided array, calling the provider
     *  function at most once.
     *  <p>
     *  If bytes are available in the internal buffer, then those bytes will be
     *  consumed up to the size of the array, and the function is <em>not</em>
     *  called. If the internal bufffer is empty at the time of call, then the
     *  function will be called to fill it.
     *
     *  @return The number of bytes copied into the provided array. May be zero,
     *          if the function returns an empty buffer.
     *
     *  @throws IOException if the stream is closed.
     */
    @Override
    public int read(byte[] b)
    throws IOException
    {
        return read(b, 0, b.length);
    }

//----------------------------------------------------------------------------
//  Other public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the current position in the stream (the number of bytes that
     *  have been returned from reads).
     */
    public long getStreamOffset()
    {
        return streamOffset;
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Reads a buffer if necessary, and lets the caller know whether data is
     *  available.
     */
    private boolean isAvailable()
    throws IOException
    {
        try
        {
            if ((buf != null) && (bufOffset >= buf.length))
            {
                buf = function.apply(streamOffset);
                bufOffset = 0;
            }

            return (buf != null);
        }
        catch (Throwable ex)
        {
            throw new IOException("exception in function: " + ex.getMessage(), ex);
        }
    }


    /**
     *  Check before reads, to throw if the stream is closed.
     */
    private void checkClosed()
    throws IOException
    {
        if (isClosed)
            throw new IOException("stream is closed");
    }
}
