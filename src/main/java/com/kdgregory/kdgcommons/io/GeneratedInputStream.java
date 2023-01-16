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


/**  
 *  An <code>InputStream</code> that calls a method to generate blocks of data.
 *  This can be used to replace a pipe in many situations where you can't spin
 *  up a new thread. It's also useful for performing ranged retrieval from a
 *  web-service and wrapping it in a stream.
 *  <p>
 *  To use, implement {@link #nextBuffer}. Each time this method is called, it
 *  should return an arbitrary-size <code>byte[]</code> containing the next
 *  chunk of data. At EOF, it returns <code>null</code>.
 *  <p>
 *  {@link FunctionInputStream} is a Java-8 alternative to this class, in which
 *  you provide a <code>java.util.Function</code> that returns the buffer.
 */
public abstract class GeneratedInputStream
extends InputStream
{
    private byte[] buf;
    private int bufOffset;
    private boolean isClosed;

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    /**
     *  Returns the number of bytes available in the current buffer &mdash;
     *  assumes that a call to {@link #nextBuffer} will block.
     */
    @Override
    public int available() throws IOException
    {
        return (buf == null)
             ? 0
             : buf.length - bufOffset;
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
     *  By default this method throws <code>IOException</code>. Subclasses may
     *  override.
     */
    @Override
    public synchronized void reset() throws IOException
    {
        throw new IOException("mark/reset not supported");
    }


    @Override
    public int read() throws IOException
    {
        if (isAvailable())
            return buf[bufOffset++] & 0xFF;
        else
            return -1;
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int bytesRead = 0;
        while ((len > 0) && isAvailable())
        {
            int bytesToCopy = Math.min(
                                buf.length - bufOffset,
                                len - bytesRead);
            System.arraycopy(buf, bufOffset, b, off, bytesToCopy);
            bytesRead += bytesToCopy;
            off += bytesToCopy;
            len -= bytesToCopy;
            bufOffset += bytesToCopy;
        }
        return bytesRead;
    }


    /**
     *  Attempts to completely fill the passed buffer, calling {@link #nextBuffer}
     *  until it is either full or EOF is reached. Note that this differs from a
     *  "normal" stream, which makes a single attempt to read the underlying
     *  data.
     */
    @Override
    public int read(byte[] b)
    throws IOException
    {
        return read(b, 0, b.length);
    }


    /**
     *  This method calls {@link #read} until either the requisite number of
     *  bytes have been read, or EOF is reached.
     */
    @Override
    public long skip(long n) throws IOException
    {
        long bytesSkipped = 0;
        byte[] sink = new byte[1024];
        while ((n > 0) && isAvailable())
        {
            int bytesRead = read(sink, 0, (int)Math.min(sink.length, n));
            n -= bytesRead;
            bytesSkipped += bytesRead;
        }
        return bytesSkipped;
    }

//----------------------------------------------------------------------------
//  Methods for subclasses to implement
//----------------------------------------------------------------------------

    /**
     *  Returns the next buffer of generated data, <code>null</code> when
     *  there's no more data. Buffers may be any size.
     */
    protected abstract byte[] nextBuffer() throws IOException;

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Buffer management happens here. Returns <code>true</code> if data is
     *  available in the current buffer (potentially reading a new buffer),
     *  <code>false</code> if not.
     */
    private boolean isAvailable()
    throws IOException
    {
        if (isClosed)
            throw new IOException("stream is closed");

        if ((buf == null) || (bufOffset >= buf.length))
        {
            buf = nextBuffer();
            bufOffset = 0;
        }

        return (buf != null);
    }
}
