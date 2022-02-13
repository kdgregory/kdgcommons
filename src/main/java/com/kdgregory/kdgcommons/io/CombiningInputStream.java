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
 *  This class combines multiple source streams into a single stream. This can
 *  be used for purposes ranging from combining files that were previously
 *  split, to inserting a standard prologue before a stream.
 *  <p>
 *  The behavior of several of the standard <code>InputStream</code> methods
 *  changes; see {@link #available} and {@link #mark} for details. Also, note
 *  that this class will not close its consituent streams unless you call
 *  {@link #close}.
 *  <p>
 *  This class is not thread-safe.
 */
public class CombiningInputStream
extends InputStream
{
    private InputStream[] constituents;
    private int current;
    private int marked = -1;
    private int markLimit = -1;

    // TODO - add a collection-based constructor

    /**
     *  Combines one or more constituent streams.
     *  <p>
     *  Note that we directly use the passed array. If you want to play games
     *  with changing its elements after constructing the stream, you're on
     *  your own.
     */
    public CombiningInputStream(InputStream... constituents)
    {
        this.constituents = constituents;
    }

//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    /**
     *  Returns the number of bytes available without blocking, <em>from the
     *  current stream</em>. Will not look at subsequent streams, because
     *  the current stream may have unreported data and would block when
     *  trying to read that data.
     */
    @Override
    public int available() throws IOException
    {
        return isEOF() ? 0 : getCurrent().available();
    }


    /**
     *  Attempts to close all constituent streams. If any consituent stream
     *  throws an exception, will preserve that exception and continue to
     *  close other streams. If multiple streams throw, only the last will
     *  be preserved.
     */
    @Override
    public void close() throws IOException
    {
        IOException thrown = null;
        for (int ii = 0 ; ii < constituents.length ; ii++)
        {
            try
            {
                constituents[ii].close();
            }
            catch (IOException e)
            {
                thrown = e;
            }
        }
        if (thrown != null)
            throw thrown;
    }


    /**
     *  Reads a single byte of data, transparently switching between input
     *  streams as necessary. Will block until either data is available,
     *  EOF is reached on the last stream, or an exception is thrown.
     *
     *  @return The byte read, or -1 when EOF is reached on the last stream.
     */
    @Override
    public int read() throws IOException
    {
        if (isEOF())
            return -1;

        int i = getCurrent().read();
        if (i < 0)
        {
            switchStreams();
            return read();
        }
        markLimit--;
        return i;
    }


    /**
     *  Reads up to <code>len</code> bytes, transparently swtching between
     *  input streams as necessary. Will block when reading from the current
     *  stream, but <em>does not block</em> when switching streams; if no
     *  data is available from the new stream, will not attempt to read it.
     *
     *  @return The number of bytes read from all streams, -1 if at EOF on
     *          the last stream.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (isEOF())
            return -1;

        int total = 0;
        while (len > 0)
        {
            int read = getCurrent().read(b, off, len);
            if (read < 0)
            {
                if (switchStreams() || (getCurrent().available() == 0))
                    break;
            }
            else
            {
                total += read;
                off += read;
                len -= read;
                markLimit -= read;
            }
        }
        return total;
    }


    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }


    /**
     *  Determines whether a mark can be placed at this point in the stream.
     *  Examines the current and all subsequent streams, and returns true if
     *  all support marking. This means that you can set a mark if one of the
     *  constituent streams does not support marking, but you have already
     *  exhausted it.
     *  <p>
     *  Note that this will return false if you have exhausted a stream that
     *  does not support marking, but have not yet performed a read that
     *  switches streams. This method will not block.
     */
    @Override
    public boolean markSupported()
    {
        boolean supported = true;
        for (int ii = current ; ii < constituents.length ; ii++)
        {
            supported &= constituents[ii].markSupported();
        }
        return supported;
    }


    /**
     *  Sets a mark on the current stream. The current stream and all
     *  subsequent streams must support marking; if not, the failure
     *  will occur during {@link #reset}.
     */
    @Override
    public void mark(int readlimit)
    {
        if (!isEOF())
        {
            marked = current;
            markLimit = readlimit;
            getCurrent().mark(readlimit);
        }
    }


    @Override
    public void reset() throws IOException
    {
        if (marked < 0)
            throw new IOException("no mark set");

        if (isEOF())
            current--;

        while (current > marked)
        {
            getCurrent().reset();
            current--;
        }

        getCurrent().reset();
    }


    /**
     *  Attempts to skip up to <i>N</i> bytes, transparently switching streams
     *  as needed. Will return -1 if already at EOF, otherwise returns the
     *  number of bytes actually skipped.
     *  <p>
     *  This method is implemented by repeatedly calling <code>skip()</code>
     *  on the constituent streams. If any such call returns 0, we try to
     *  read from the stream to verify EOF. This can lead to an inefficient
     *  loop, in which <code>skip()</code> constantly indicates zero bytes,
     *  but we're able to read from the file. Shouldn't happen in practice.
     */
    @Override
    public long skip(long n) throws IOException
    {
        if (isEOF())
            return -1;

        long total = 0;
        while ((n > 0) && !isEOF())
        {
            long bytes = getCurrent().skip(n);
            if (bytes <= 0)
            {
                int check = read();
                if (check < 0)
                    break;
                bytes = 1;
            }
            total += bytes;
            n -= bytes;
        }
        return total;
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private boolean isEOF()
    {
        return current == constituents.length;
    }


    private boolean switchStreams()
    {
        current++;
        if (!isEOF() && (markLimit > 0))
        {
            getCurrent().mark(markLimit);
        }
        return isEOF();
    }


    private InputStream getCurrent()
    {
        return isEOF() ? null : constituents[current];
    }
}
