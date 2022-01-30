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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;


/**
 *  An <code>OutputStream</code> that writes to an existing <code>ByteBuffer</code>.
 *  The buffer is repositioned at construction, and written sequentially. Attempts
 *  to write past the capacity of the buffer will result in <code>IOException</code>.
 *  <p>
 *  <em>Warnings:</em>
 *  Because this class explicitly repositions the buffer, you should not create
 *  two instances around the same buffer; <code>slice()</code> the buffer
 *  instead. Instances of this class are only as thread-safe as the underlying
 *  buffer (and the JavaDocs don't have much to say about that).
 */
public class ByteBufferOutputStream
extends OutputStream
{
    private ByteBuffer buf;
    private boolean isClosed;


    /**
     *  Creates an instance that repositions the passed buffer to its start.
     */
    public ByteBufferOutputStream(ByteBuffer buf)
    {
        this(buf, 0);
    }


    /**
     *  Creates an instance that repositions the passed buffer to the specified
     *  index.
     */
    public ByteBufferOutputStream(ByteBuffer buf, int index)
    {
        this.buf = buf;
        this.buf.position(index);
    }

//----------------------------------------------------------------------------
//  OutputStream
//----------------------------------------------------------------------------

    @Override
    public void close() throws IOException
    {
        isClosed = true;
    }


    @Override
    public void flush() throws IOException
    {
        if (buf instanceof MappedByteBuffer)
            ((MappedByteBuffer)buf).force();
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (isClosed)
            throw new IOException("buffer is closed");

        if (len > buf.remaining())
            throw new IOException("write too large: " + len + " bytes, " + buf.remaining() + " remaining in buffer");

        buf.put(b, off, len);
    }


    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }


    @Override
    public void write(int b) throws IOException
    {
        if (isClosed)
            throw new IOException("buffer is closed");

        if (buf.remaining() == 0)
            throw new IOException("no space left in buffer");

        buf.put((byte)b);
    }
}
