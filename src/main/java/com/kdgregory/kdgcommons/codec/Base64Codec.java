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

package com.kdgregory.kdgcommons.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.kdgregory.kdgcommons.lang.StringUtil;


/**
 *  Converts binary data to/from a string consisting of base-64 characters.
 *  <p>
 *  There are several standard options for string conversion line breaks, defined
 *  by the {@link Base64Codec.Option} enum. You can also specify your own breaks
 *   and maximum line length.
 *  <p>
 *  Conversion to byte arrays always ignores whitespace, and will also ignore
 *  any specified start/end/separation strings (the separation string may appear
 *  any where in the source string, the start/end strings must appear in their
 *  respective locations).
 *
 *  @since 1.0.14
 */
public class Base64Codec
extends Codec
{
    /**
     *  Different standard construction options: each value specifies a combination of
     *  maximum line length and separator characters.
     */
    public enum Option
    {
        /** Produces an unbroken string of base-64 characters. */
        UNBROKEN(Integer.MAX_VALUE, null, defaultCharLookup, defaultValueLookup, '='),

        /** RFC-1421: 64 characters, CR+LF separator */
        RFC1421(64, new byte[] { 13, 10 }, defaultCharLookup, defaultValueLookup, '='),

        /**
         *  RFC4648 <a href="http://tools.ietf.org/html/rfc4648#section-5">filename</a> format:
         *  an unbroken string using filename-safe symbolic encoding, without pad characters.
         */
        FILENAME(Integer.MAX_VALUE, null, filenameCharLookup, filenameValueLookup, '\0');


        private final int lineLength;
        private final byte[] separator;
        private final char[] charLookup;
        private final HashMap<Character,Integer> valueLookup;
        private final char padChar;

        private Option(int lineLength, byte[] separator, char[] charLookup, HashMap<Character,Integer> valueLookup, char padChar)
        {
            this.lineLength = lineLength;
            this.separator = separator;
            this.charLookup = charLookup;
            this.valueLookup = valueLookup;
            this.padChar = padChar;
        }
    }

//----------------------------------------------------------------------------
//  Encoding Tables
//----------------------------------------------------------------------------

    private static char[] defaultCharLookup =
    {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };


    private static HashMap<Character,Integer> defaultValueLookup = new HashMap<Character,Integer>(64);
    static
    {
        defaultValueLookup.put('=', -1);
        for (int ii = 0 ; ii < defaultCharLookup.length ; ii++)
            defaultValueLookup.put(Character.valueOf(defaultCharLookup[ii]), Integer.valueOf(ii));
    }


    private static char[] filenameCharLookup =
    {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
    };


    private static HashMap<Character,Integer> filenameValueLookup = new HashMap<Character,Integer>(64);
    static
    {
        filenameValueLookup.put('=', -1);
        for (int ii = 0 ; ii < filenameCharLookup.length ; ii++)
            filenameValueLookup.put(Character.valueOf(filenameCharLookup[ii]), Integer.valueOf(ii));
    }

//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private int lineLength;
    private byte[] separator;
    private char[] charLookup;
    private HashMap<Character,Integer> valueLookup;
    private char padChar;
    private boolean paddingRequired;


    /**
     *  Default constructor; equivalent to <code>Option.UNBROKEN</code>.
     */
    public Base64Codec()
    {
        this(Option.UNBROKEN);
    }


    /**
     *  Constructs an instance using predefined output configurations.
     */
    public Base64Codec(Option option)
    {
        this(option.lineLength, option.separator, option.charLookup, option.valueLookup, option.padChar);
    }


    /**
     *  Creates an instance with custom line length and separator, where
     *  the separator is specified as a string (and converted via UTF-8).
     */
    public Base64Codec(int lineLength, String separator)
    {
        this(lineLength, StringUtil.toUTF8(separator));
    }


    /**
     *  Creates an instance that uses standard encoding, but custom line
     *  length and separator.
     */
    public Base64Codec(int lineLength, byte[] separator)
    {
        this(lineLength, separator, defaultCharLookup, defaultValueLookup, '=');
    }


    /**
     *  Internal constructor that specifies all values, including lookup tables.
     */
    private Base64Codec(int lineLength, byte[] separator, char[] charLookup, HashMap<Character,Integer> valueLookup, char padChar)
    {
        this.lineLength = lineLength;
        this.separator = separator;
        this.charLookup = charLookup;
        this.valueLookup = valueLookup;
        this.padChar = padChar;
        this.paddingRequired = (padChar != '\0');
    }

//----------------------------------------------------------------------------
//  Implementation of Codec
//----------------------------------------------------------------------------

    @Override
    public void encode(InputStream in, OutputStream out)
    {
        new Encoder(in, out).encode();
    }


    @Override
    public void decode(InputStream in, OutputStream out)
    {
        new Decoder(in, out).decode();
    }

//----------------------------------------------------------------------------
//  Convenience methods
//----------------------------------------------------------------------------

    /**
     *  Encodes the passed array and returns it as a string.
     */
    public String toString(byte[] data)
    {
        if ((data == null) || (data.length == 0)) return "";
        byte[] encoded = encode(data);
        return StringUtil.fromUTF8(encoded);
    }


    /**
     *  Decodes the passed string and returns it as a byte array.
     */
    public byte[] toBytes(String str)
    {
        if (StringUtil.isEmpty(str)) return EMPTY_ARRAY;

        byte[] bytes = StringUtil.toUTF8(str);
        return decode(bytes);
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private class Encoder
    {
        private InputStream in;
        private OutputStream out;
        private int breakCount;

        public Encoder(InputStream in, OutputStream out)
        {
            this.in = in;
            this.out = out;
        }

        public void encode()
        {
            try
            {
                while (true)
                {
                    int b1 = in.read();
                    int b2 = in.read();
                    int b3 = in.read();
                    if (b1 < 0) return;

                    insertBreakIfNeeded();
                    encodeGroup(b1, b2, b3);
                }
            }
            catch (CodecException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new CodecException("unable to encode", ex);
            }
        }

        private void insertBreakIfNeeded()
        throws IOException
        {
            if ((separator != null) && (breakCount >= lineLength))
            {
                out.write(separator);
                breakCount = 0;
            }
        }

        private boolean encodeGroup(int b1, int b2, int b3)
        throws IOException
        {
            int e1 = b1 >>> 2;

            int e2 = (b1 & 0x03) << 4;
            if (b2 >= 0)
                e2 |= b2 >> 4;

            int e3 = (b2 & 0xF) << 2;
            if (b3 >= 0)
                e3 |= b3 >> 6;

            int e4 = b3 & 0x3F;

            out.write(charLookup[e1]);
            out.write(charLookup[e2]);
            writeOrPad(b2, e3);
            writeOrPad(b3, e4);

            breakCount += 4;
            return true;
        }


        private void writeOrPad(int byteVal, int encVal)
        throws IOException
        {
            if (byteVal >= 0)
                out.write(charLookup[encVal]);
            else if (padChar != '\0')
                out.write(padChar);
        }

    }


    private class Decoder
    {
        private InputStream in;
        private OutputStream out;

        public Decoder(InputStream in, OutputStream out)
        {
            this.in = in;
            this.out = out;
        }

        public void decode()
        {
            try
            {
                do
                {
                    skipIfSeparator(in, separator);
                }
                while (decodeGroup());
            }
            catch (CodecException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new CodecException("unable to decode", ex);
            }
        }

        private boolean decodeGroup()
        throws IOException
        {
            int e1 = next(true);
            if (e1 < 0) return false;

            int e2 = next(! paddingRequired);
            int e3 = next(! paddingRequired);
            int e4 = next(! paddingRequired);

            out.write((e1 << 2) | ((e2 & 0x30) >> 4));
            if (e3 < 0) return false;

            out.write(((e2 & 0x0F) << 4) | ((e3 & 0x3C) >> 2));
            if (e4 < 0) return false;

            out.write(((e3 & 0x03) << 6) | e4);
            return true;
        }

        private int next(boolean eofAllowed)
        throws IOException
        {
            int b = nextNonWhitespace(in);
            if (b < 0)
            {
                if (eofAllowed) return -1;
                else throw new CodecException("unexpected EOF");
            }

            if (b == padChar) return -1;

            Integer val = valueLookup.get(Character.valueOf((char)b));
            if (val == null) throw new InvalidSourceByteException(b);

            return val.intValue();
        }
    }
}
