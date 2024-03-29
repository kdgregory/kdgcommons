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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestTranslatingInputStream
{

//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    @Test
    public void testEmptyStream() throws Exception
    {
        byte[] src = new byte[0];
        byte[] expected = new byte[0];

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-8"),
                Charset.forName("UTF-8"));
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf8PassThrough() throws Exception
    {
        String text = "\u2724Some t\u00EBst data\u2734";
        byte[] src = text.getBytes("UTF-8");
        byte[] expected = src;

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-8"),
                Charset.forName("UTF-8"));
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf8ToISO8859PassThrough() throws Exception
    {
        String text = "\u00A1Some t\u00EBst \u00ABdata\u00BB";
        byte[] src = text.getBytes("UTF-8");
        byte[] expected = text.getBytes("ISO-8859-1");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-8"),
                Charset.forName("ISO-8859-1"));
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf8ToISO8859IgnoringUnmappableCharacters() throws Exception
    {
        byte[] src = "\u2724Some t\u00EBst data\u2734".getBytes("UTF-8");
        byte[] expected = "Some t\u00EBst data".getBytes("ISO-8859-1");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-8"),
                Charset.forName("ISO-8859-1"));
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf8ToISO8859ReplacingUnmappableCharacters() throws Exception
    {
        byte[] src = "\u2724Some t\u00EBst data\u2734".getBytes("UTF-8");
        byte[] expected = "!Some t\u00EBst data!".getBytes("ISO-8859-1");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-8"),
                Charset.forName("ISO-8859-1"),
                '!');
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf8ToISO8859ThrowingOnUnmappableCharacters() throws Exception
    {
        byte[] src = "\u2724Some t\u00EBst data\u2734".getBytes("UTF-8");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-8"),
                Charset.forName("ISO-8859-1"),
                true);
        try
        {
            IOUtil.readFully(in, new byte[1024]);
            fail("should have thrown on unmappable character");
        }
        catch (RuntimeException ex)
        {
            assertTrue("exception message includes unmappable character (was: " + ex.getMessage() + ")",
                       ex.getMessage().contains("2724"));
        }
    }


    @Test
    public void testUtf16ToUtf8() throws Exception
    {
        String text = "\u2724Some t\u00EBst data\u2734";
        byte[] src = text.getBytes("UTF-16");
        byte[] expected = text.getBytes("UTF-8");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-16"),
                Charset.forName("UTF-8"));
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf16ToUtf8WithEmbeddedBOM() throws Exception
    {
        // 0xFEFF is zero-width non-breaking space unless it appears at
        // the start of the text
        String text = "Some t\uFEFFst data\uFEFF";
        byte[] src = text.getBytes("UTF-16");
        byte[] expected = text.getBytes("UTF-8");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-16"),
                Charset.forName("UTF-8"));
        assertStreamContent(expected, in);
    }


    @Test
    public void testUtf16ToUtf8WithSurrogatePairs() throws Exception
    {
        // "old italic" characters
        String text = new StringBuilder()
                      .appendCodePoint(0x10301).appendCodePoint(0x10311).appendCodePoint(0x10321)
                      .toString();
        byte[] src = text.getBytes("UTF-16");
        byte[] expected = text.getBytes("UTF-8");

        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(src),
                Charset.forName("UTF-16"),
                Charset.forName("UTF-8"));
        assertStreamContent(expected, in);
    }


    // cross-library regression test
    @Test
    @SuppressWarnings("resource")
    public void testSingleByteReadDoesNotSignExtend() throws Exception
    {
        TranslatingInputStream in = new TranslatingInputStream(
                new ByteArrayInputStream(new byte[] { (byte)0xA4 }),
                Charset.forName("ISO-8859-1"),
                Charset.forName("UTF-8"));

        assertEquals(0xC2, in.read());
        assertEquals(0xA4, in.read());
        assertEquals(-1, in.read());
    }

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    // TODO - replace loop with function that reads stream to bytes?
    private static void assertStreamContent(byte[] expected, InputStream in)
    throws IOException
    {
        for (int ii = 0 ; ii < expected.length ; ii++)
        {
            int e = expected[ii] & 0xFF;
            int a = in.read();
            assertEquals("byte " + ii, e, a);
        }

        assertTrue("EOF", in.read() == -1);
    }

}
