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
 *  This class is used to wrap a UTF8-encoded stream that includes an encoded
 *  Byte Order Mark (BOM, 0xFEFF encoded as 0xEF 0xBB 0xBF) as its first bytes.
 *  Such streams are produced by various Microsoft applications. This class
 *  will automatically skip these bytes and return the subsequent byte as the
 *  first byte in the stream.
 *  <p>
 *  If the first byte in the stream is 0xEF, this class will attempt to read
 *  the next two bytes. Results are undefined in the stream does not contain
 *  UTF-8 encoded data, as these next two bytes may not exist.
 */
public class BOMExclusionInputStream
extends InputStream
{
    private InputStream delegate;
    private int[] firstBytes;
    private int fbLen;
    private int fbIndex;
    private boolean markedAtStart;


    public BOMExclusionInputStream(InputStream delegate)
    {
        this.delegate = delegate;
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  This method reads and either preserves or skips the first bytes in the
     *  stream. It behaves like the single-byte <code>read()</code> method,
     *  either returning a valid byte or -1 to indicate that the initial bytes
     *  have been processed already.
     */
    private int readFirstBytes()
    throws IOException
    {
        if (firstBytes == null)
        {
            firstBytes = new int[3];
            int b0 = delegate.read();
            if ((b0 < 0) || (b0 != 0xEF))
                return b0;

            int b1 = delegate.read();
            int b2 = delegate.read();
            if ((b1 == 0xBB) && (b2 == 0xBF))
                return delegate.read();

            // if the stream isn't valid UTF-8, this is where things get weird
            firstBytes[fbLen++] = b0;
            firstBytes[fbLen++] = b1;
            firstBytes[fbLen++] = b2;
        }

        return (fbIndex < fbLen) ? firstBytes[fbIndex++] : -1;
    }

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    /**
     *  Returns the number of bytes available in the stream, <em>including
     *  the BOM</em>. This operation does not attempt to read the stream.
     */
    @Override
    public int available() throws IOException
    {
        return delegate.available();
    }


    @Override
    public void close() throws IOException
    {
        delegate.close();
    }


    @Override
    public int read() throws IOException
    {
        int b = readFirstBytes();
        return (b >= 0) ? b : delegate.read();
    }


    @Override
    public int read(byte[] buf, int off, int len)
    throws IOException
    {
        int firstCount = 0;
        int b = 0;
        while ((len > 0) && (b >= 0))
        {
            b = readFirstBytes();
            if (b >= 0)
            {
                buf[off++] = (byte)(b & 0xFF);
                len--;
                firstCount++;
            }
        }
        int secondCount = delegate.read(buf, off, len);
        return (secondCount < 0) ? firstCount
                                 : firstCount + secondCount;
    }


    @Override
    public int read(byte[] buf) throws IOException
    {
        return read(buf, 0, buf.length);
    }


    @Override
    public boolean markSupported()
    {
        return delegate.markSupported();
    }


    @Override
    public synchronized void mark(int readlimit)
    {
        markedAtStart = firstBytes == null;
        delegate.mark(readlimit);
    }


    @Override
    public synchronized void reset() throws IOException
    {
        if (markedAtStart)
        {
            firstBytes = null;
        }

        delegate.reset();
    }


    @Override
    public long skip(long n) throws IOException
    {
        while ((n > 0) && (readFirstBytes() >= 0))
        {
            n--;
        }
        return delegate.skip(n);
    }

}
