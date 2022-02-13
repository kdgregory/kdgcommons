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
 *  A decorator <code>InputStream</code> that invokes a {@link #progress} method
 *  after each read, reporting the number of bytes processed. Applications can
 *  subclass and override this method to provide features such as a progress
 *  bar.
 */
public class MonitoredInputStream
extends InputStream
{
    private InputStream delegate;
    private long total;

    public MonitoredInputStream(InputStream delegate)
    {
        this.delegate = delegate;
    }

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    /**
     *  Returns the number of bytes available from the underlying stream. Has
     *  no effect on monitoring.
     */
    @Override
    public int available() throws IOException
    {
        return delegate.available();
    }


    /**
     *  Closes the underlying stream.
     */
    @Override
    public void close() throws IOException
    {
        delegate.close();
    }


    /**
     *  Reads a single byte from the underlying stream, blocking if necessary.
     *  Calls {@link #progress} after the byte has been read, reporting either
     *  1 or 0 (depending on whether the stream is at its end).
     */
    @Override
    public int read() throws IOException
    {
        int b = delegate.read();

        long bytesRead = (b < 0) ? 0 : 1;
        total += bytesRead;
        progress(bytesRead, total);

        return b;
    }


    /**
     *  Reads up bytes from the underlying stream into the passed buffer,
     *  blocking if necessary.  Calls {@link #progress} after the read
     *  completes, reporting the actual number of bytes read.
     */
    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }


    /**
     *  Reads up to the specified number of bytes from the underlying stream,
     *  blocking if necessary.  Calls {@link #progress} after the read completes,
     *  reporting the actual number of bytes read.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int count = delegate.read(b, off, len);

        long bytesRead = (count < 0) ? 0 : count;
        total += bytesRead;
        progress(bytesRead, total);

        return count;
    }


    /**
     *  Returns whether the underlying stream supports {@link #mark}.
     */
    @Override
    public boolean markSupported()
    {
        return delegate.markSupported();
    }


    /**
     *  Sets a mark on the underlying stream.
     */
    @Override
    public synchronized void mark(int readlimit)
    {
        delegate.mark(readlimit);
    }


    /**
     *  Resets the underlying stream to a previous mark. Does not reset the
     *  monitoring counts; these will show total number of bytes read.
     */
    @Override
    public synchronized void reset() throws IOException
    {
        delegate.reset();
    }


    /**
     *  Skips up to a specified number of bytes in the underlying stream. Will
     *  call {@link #progress} after the skip completes, with the number of
     *  bytes skipped.
     */
    @Override
    public long skip(long n) throws IOException
    {
        long skipped = delegate.skip(n);

        total += skipped;
        progress(skipped, total);

        return skipped;
    }

//----------------------------------------------------------------------------
//  Other public methods
//----------------------------------------------------------------------------

    /**
     *  Subclasses override this to monitor progress. The default implementation
     *  of this method is a no-op.
     *
     *  @param  lastRead    Number of bytes in last read operation. Note that
     *                      this may be 0.
     *  @param  totalBytes  Total number of bytes read or skipped since this
     *                      object was instantiated.
     */
    public void progress(long lastRead, long totalBytes)
    {
        // nothing here
    }
}
