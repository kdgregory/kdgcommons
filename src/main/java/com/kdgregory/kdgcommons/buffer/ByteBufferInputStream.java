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

package com.kdgregory.kdgcommons.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 *  An <code>InputStream</code> that reads from an existing <code>ByteBuffer</code>.
 *  Will reposition the buffer at construction, and read sequentially from that
 *  point. Attempting to read past the buffer's limit will return end-of-file.
 *  <p>
 *  <em>Warnings:</em>
 *  Because this class explicitly repositions the buffer, you should not create
 *  two instances around the same buffer; <code>slice()</code> the buffer
 *  instead. Instances of this class are only as thread-safe as the underlying
 *  buffer (and the JavaDocs don't have much to say about that).
 */
public class ByteBufferInputStream
extends InputStream
{
    private ByteBuffer  buf;
    private int mark = -1;
    private boolean isClosed = false;


    /**
     *  Creates an instance that positions the stream at the start of the
     *  buffer.
     */
    public ByteBufferInputStream(ByteBuffer buf)
    {
        this(buf, 0);
    }


    /**
     *  Creates an instance that positions the stream at the specified offset.
     */
    public ByteBufferInputStream(ByteBuffer buf, int off)
    {
        this.buf = buf;
        this.buf.position(off);
    }

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    @Override
    public int available() throws IOException
    {
        return buf.limit() - buf.position();
    }


    @Override
    public void close() throws IOException
    {
        isClosed = true;
    }


    @Override
    public synchronized void mark(int readlimit)
    {
        mark = buf.position();
    }


    @Override
    public boolean markSupported()
    {
        return true;
    }


    @Override
    public int read() throws IOException
    {
        if (isClosed)
            throw new IOException("stream is closed");

        if (available() <= 0)
            return -1;

        return buf.get() & 0xFF;
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (isClosed)
            throw new IOException("stream is closed");

        int bytes = Math.min(len, available());
        if (bytes == 0)
            return -1;

        buf.get(b, off, bytes);
        return bytes;
    }


    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }


    @Override
    public synchronized void reset() throws IOException
    {
        if (mark < 0)
            throw new IOException("mark not set");

        buf.position(mark);
    }


    @Override
    public long skip(long n) throws IOException
    {
        int n2 = (n > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)n;
        int n3 = Math.min(n2, available());
        int newPos = n3 + buf.position();
        buf.position(newPos);
        return (long)n3;
    }
}
