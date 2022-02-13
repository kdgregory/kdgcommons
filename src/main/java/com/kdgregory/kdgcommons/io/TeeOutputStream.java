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
import java.io.OutputStream;


/**
 *  A decorator <code>OutputStream</code> that writes all bytes to two
 *  <code>OutputStream</code>s: a base and a tee.
 */
public class TeeOutputStream
extends OutputStream
{
    private OutputStream base;
    private OutputStream tee;
    private boolean flushEveryWrite;


    /**
     *  Basic constructor. Stream is a simple pass-through.
     */
    public TeeOutputStream(OutputStream base, OutputStream tee)
    {
        this.base = base;
        this.tee = tee;
    }


    /**
     *  Constructs a stream that optionally flushes the tee after every write.
     *  This is useful when the tee is a logger.
     */
    public TeeOutputStream(OutputStream base, OutputStream tee, boolean flushTee)
    {
        this(base, tee);
        this.flushEveryWrite = flushTee;
    }

//----------------------------------------------------------------------------
//  OutputStream
//----------------------------------------------------------------------------

    /**
     *  Closes the base stream, but <em>not</em> the tee.
     */
    @Override
    public void close() throws IOException
    {
        base.close();
    }


    /**
     *  Flushes all characters currently buffered in the stream. This will
     *  flush both the base stream and the tee.
     */
    @Override
    public void flush() throws IOException
    {
        base.flush();
        tee.flush();
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        base.write(b, off, len);
        tee.write(b, off, len);

        if (flushEveryWrite)
            tee.flush();
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        base.write(b);
        tee.write(b);

        if (flushEveryWrite)
            tee.flush();
    }


    /**
     *  Writes a single byte to both the base and tee.
     */
    @Override
    public void write(int b) throws IOException
    {
        base.write(b);
        tee.write(b);

        if (flushEveryWrite)
            tee.flush();
    }
}
