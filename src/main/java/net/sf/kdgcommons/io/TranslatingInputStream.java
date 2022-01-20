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

package net.sf.kdgcommons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;


/**
 *  A decorator stream that translates bytes from one character-set encoding to
 *  another. Can be configured to throw or replace untranslateable data.
 */
public class TranslatingInputStream
extends InputStream
{
    private InputStreamReader delegate;
    private CharBuffer charBuf;
    private ByteBuffer byteBuf;
    private CharsetEncoder encoder;


    /**
     *  Base constructor: optionally throws or ignores untranslateable data.
     */
    public TranslatingInputStream(InputStream delegate, Charset from, Charset to, boolean throwOnError)
    {
        this.delegate = new InputStreamReader(delegate, from);
        this.charBuf = CharBuffer.allocate(2);
        this.byteBuf = ByteBuffer.allocate(4);
        this.encoder = to.newEncoder();
        if (throwOnError)
        {
            this.encoder.onMalformedInput(CodingErrorAction.REPORT);
            this.encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        }
        else
        {
            this.encoder.onMalformedInput(CodingErrorAction.IGNORE);
            this.encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
        }

        // this will force first call to read() to pull a byte from source
        this.byteBuf.limit(0);
    }


    /**
     *  Creates an instance that ignores any data that can not be translated.
     */
    public TranslatingInputStream(InputStream delegate, Charset from, Charset to)
    {
        this(delegate, from, to, false);
    }


    /**
     *  Creates an instance that replaces any data that can not be translated
     */
    public TranslatingInputStream(InputStream delegate, Charset from, Charset to, char replacement)
    {
        this(delegate, from, to, false);
        encoder.replaceWith(encodeReplacement(to, replacement));
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }


//----------------------------------------------------------------------------
//  InputStream
//----------------------------------------------------------------------------

    @Override
    public int read() throws IOException
    {
        if (byteBuf.remaining() == 0)
            fillBuffer();

        if (byteBuf.remaining() == 0)
            return -1;

        return byteBuf.get() & 0xFF;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    // creates the byte array representing the replacement character in the
    // "to" character set; we create a new encoder to do this, so that we
    // don't muck with the state of the "real" encoder (in particular, whether
    // it will handle a BOM)
    private static byte[] encodeReplacement(Charset to, char replacement)
    {
        try
        {
            CharsetEncoder tempEncoder = to.newEncoder();
            CharBuffer src = CharBuffer.wrap(new char[] { replacement });
            ByteBuffer dst = tempEncoder.encode(src);
            dst.position(0);
            byte[] result = new byte[dst.remaining()];
            dst.get(result);
            return result;
        }
        catch (CharacterCodingException e)
        {
            throw new IllegalArgumentException("illegal replacement character: " + (int)replacement);
        }
    }


    /**
     *  Fills the buffer that's used to satisfy {@link #read}.
     */
    private void fillBuffer()
    throws IOException
    {
        byteBuf.clear();
        while (byteBuf.position() == 0)
        {
            fillCharBuf();
            if (charBuf.limit() == 0)
            {
                byteBuf.limit(0);
                return;
            }

            encoder.reset();
            CoderResult rslt = encoder.encode(charBuf, byteBuf, true);
            if (rslt.isUnmappable())
            {
                StringBuilder sb = new StringBuilder(16);
                for (int ii = 0 ; ii < charBuf.limit() ; ii++)
                {
                    sb.append(Integer.toString(charBuf.get(ii), 16)).append(" ");
                }
                throw new RuntimeException("unmappable character: " + sb);
            }
            encoder.flush(byteBuf);
        }

        byteBuf.limit(byteBuf.position());
        byteBuf.position(0);
    }


    private void fillCharBuf()
    throws IOException
    {
        charBuf.clear();
        int limit = 0;
        int c = 0;
        do
        {
            c = delegate.read();
            if (c >= 0)
            {
                charBuf.put((char)c);
                limit++;
            }
        }
        while ((c >= 0xD800) && (c <= 0xDBFF));

        charBuf.position(0);
        charBuf.limit(limit);
    }
}
