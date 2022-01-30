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
import java.io.OutputStream;


/**
 *  A decorator <code>InputStream</code> that writes all bytes read to a
 *  specified <code>OutputStream</code>.
 */
public class TeeInputStream
extends InputStream
{
    private InputStream base;
    private OutputStream tee;

    public TeeInputStream(InputStream base, OutputStream tee)
    {
        this.base = base;
        this.tee = tee;
    }

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    /**
     *  Returns the number of bytes available from the base stream. Has
     *  no effect on the tee.
     */
    @Override
    public int available() throws IOException
    {
        return base.available();
    }


    /**
     *  Closes the base stream, but <em>not</em> the tee.
     */
    @Override
    public void close() throws IOException
    {
        base.close();
    }


    /**
     *  Reads a single byte from the base, writing it to the tee. If at
     *  end-of-file on the base, has no effect on the tee.
     */
    @Override
    public int read() throws IOException
    {
        int b = base.read();
        if (b >= 0)
            tee.write(b);
        return b;
    }


    /**
     *  Reads multiple bytes from the base, into an arbitrary position
     *  in the specified buffer, and writes the same bytes to the tee.
     *  If no bytes read, has no effect on the tee.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int count = base.read(b, off, len);
        if (count > 0)
            tee.write(b, off, count);
        return count;
    }


    /**
     *  Reads as many bytes as possible from the base, up to the size of
     *  the passed buffer, and writes them to the tee. If no bytes read,
     *  has no effect on the tee.
     */
    @Override
    public int read(byte[] b) throws IOException
    {
        int count = base.read(b);
        if (count > 0)
            tee.write(b, 0, count);
        return count;
    }


    /**
     *  Returns whether the base stream supports {@link #mark}. Has no
     *  effect on the tee.
     */
    @Override
    public boolean markSupported()
    {
        return base.markSupported();
    }


    /**
     *  Sets a mark on the base stream. Has no effect on the tee.
     */
    @Override
    public synchronized void mark(int readlimit)
    {
        base.mark(readlimit);
    }


    /**
     *  Resets the base stream to a preset mark. Has no effect on
     *  the tee.
     */
    @Override
    public synchronized void reset() throws IOException
    {
        base.reset();
    }


    /**
     *  Skips a specified number of bytes in the input stream. Has
     *  no effect on the tee.
     */
    @Override
    public long skip(long n) throws IOException
    {
        return base.skip(n);
    }
}
