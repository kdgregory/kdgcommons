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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;


public class TestTeeOutputStream extends TestCase
{
    private byte[] testData;
    private ByteArrayOutputStream base;
    private ByteArrayOutputStream tee;
    private TeeOutputStream test;


    @Override
    protected void setUp() throws Exception
    {
        testData = "Hello, World".getBytes("UTF-8");
        base = new ByteArrayOutputStream();
        tee = new ByteArrayOutputStream();
        test = new TeeOutputStream(base, tee);
    }


    public void testWriteSingleByte() throws Exception
    {
        test.write(65);
        assertEquals(1, base.toByteArray().length);
        assertEquals(1, tee.toByteArray().length);
        assertEquals(65, base.toByteArray()[0]);
        assertEquals(65, tee.toByteArray()[0]);
    }


    public void testWriteFromBuffer() throws Exception
    {
        test.write(testData);
        byte[] baseData = base.toByteArray();
        byte[] teeData = tee.toByteArray();

        assertEquals(testData.length, baseData.length);
        assertEquals(testData.length, teeData.length);
        for (int ii = 0 ; ii < testData.length ; ii++)
        {
            assertEquals("byte " + ii, testData[ii], baseData[ii]);
            assertEquals("byte " + ii, testData[ii], teeData[ii]);
        }
    }


    public void testWriteFromBufferWithOffsetAndLength() throws Exception
    {
        test.write(testData, 2, 6);
        byte[] baseData = base.toByteArray();
        byte[] teeData = tee.toByteArray();

        assertEquals(6, baseData.length);
        assertEquals(6, teeData.length);
        for (int ii = 0 ; ii < 6 ; ii++)
        {
            assertEquals("byte " + ii, testData[ii + 2], baseData[ii]);
            assertEquals("byte " + ii, testData[ii + 2], teeData[ii]);
        }
    }



    public void testFlush() throws Exception
    {
        test = new TeeOutputStream(new BufferedOutputStream(base, 1024),
                                    new BufferedOutputStream(tee, 1024));

        test.write(testData);
        assertEquals(0, base.toByteArray().length);
        assertEquals(0, tee.toByteArray().length);

        test.flush();
        assertEquals(testData.length, base.toByteArray().length);
        assertEquals(testData.length, tee.toByteArray().length);
    }


    public void testAutoFlush() throws Exception
    {
        test = new TeeOutputStream(new BufferedOutputStream(base, 1024),
                                    new BufferedOutputStream(tee, 1024),
                                    true);

        test.write(testData);
        assertEquals(0, base.toByteArray().length);
        assertEquals(testData.length, tee.toByteArray().length);

        test.flush();
        assertEquals(testData.length, base.toByteArray().length);
        assertEquals(testData.length, tee.toByteArray().length);
    }

}
