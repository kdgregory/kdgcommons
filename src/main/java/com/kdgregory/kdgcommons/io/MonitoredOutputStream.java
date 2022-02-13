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
 *  A decorator <code>OutputStream</code> that invokes a {@link #progress} method
 *  after each write, reporting the number of bytes processed. Applications can
 *  subclass and override this method to provide features such as a progress bar.
 */
public class MonitoredOutputStream
extends OutputStream
{
    private OutputStream delegate;
    private long total;

    public MonitoredOutputStream(OutputStream delegate)
    {
        this.delegate = delegate;
    }

//----------------------------------------------------------------------------
//  OutputStream
//----------------------------------------------------------------------------

    @Override
    public void write(int b) throws IOException
    {
        delegate.write(b);

        total++;
        progress(1, total);
    }


    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        delegate.write(b, off, len);

        total += len;
        progress(len, total);
    }


    @Override
    public void flush() throws IOException
    {
        delegate.flush();
    }


    @Override
    public void close() throws IOException
    {
        delegate.close();
    }


//----------------------------------------------------------------------------
//  OutputStream
//----------------------------------------------------------------------------
    /**
     *  Subclasses override this to monitor progress. The default implementation
     *  of this method is a no-op.
     *
     *  @param  lastWrite   Number of bytes in last write operation. Note that
     *                      this may be 0.
     *  @param  totalBytes  Total number of bytes written since this object was
     *                      instantiated.
     */
    public void progress(long lastWrite, long totalBytes)
    {
        // nothing here
    }
}
