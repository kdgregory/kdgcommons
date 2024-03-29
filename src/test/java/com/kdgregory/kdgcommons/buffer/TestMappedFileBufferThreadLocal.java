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

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestMappedFileBufferThreadLocal
{
    private MappedFileBufferThreadLocal tl;

    private final int testLoc = 4;
    private final int testVal = 0x12345678;

//----------------------------------------------------------------------------
//  Test Case -- there's really only one, and it doesn't truly test
//               concurrent access
//----------------------------------------------------------------------------

    @Test
    public void testOperation()
    throws Exception
    {
        File tempFile = File.createTempFile("TestMappedFileBufferThreadLocal", "tmp");
        tempFile.deleteOnExit();
        try (FileOutputStream tempOut = new FileOutputStream(tempFile))
        {
            tempOut.write(new byte[1024]);
        }

        MappedFileBuffer buf = new MappedFileBuffer(tempFile, true);
        buf.putInt(testLoc, testVal);

        tl = new MappedFileBufferThreadLocal(buf);

        MyRunnable r1 = new MyRunnable();
        Thread t1 = new Thread(r1);

        MyRunnable r2 = new MyRunnable();
        Thread t2 = new Thread(r2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNotNull(r1.myBuffer);
        assertNotNull(r2.myBuffer);

        assertNotSame(buf, r1.myBuffer);
        assertNotSame(buf, r2.myBuffer);
        assertNotSame(r1.myBuffer, r2.myBuffer);

        assertEquals(testVal, r1.myValue);
        assertEquals(testVal, r2.myValue);
    }

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private class MyRunnable
    implements Runnable
    {
        public MappedFileBuffer myBuffer;
        public int myValue;

        @Override
        public void run()
        {
            myBuffer = tl.get();
            myValue = myBuffer.getInt(testLoc);
        }
    }
}
