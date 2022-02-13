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

package com.kdgregory.kdgcommons.util;

import java.io.PrintWriter;
import java.util.Iterator;

import com.kdgregory.kdgcommons.lang.StringBuilderUtil;


/**
 *  Utility class to produce formatted hex output from arrays of bytes.
 *  <p>
 *  Output follows normal conventions, and includes the following parts:
 *  <ul>
 *  <li>    An optional offset, tracking the number of bytes that have been
 *          dumped by this object. Formatted as a hex value.
 *  <li>    One or more two-character hex codes representing the input
 *          bytes, separated by spaces. The number of bytes shown per line
 *          may be limited by a constructor parameter.
 *  <li>    An optional character representation of the bytes in the previous
 *          segment. This may be limited to printable ASCII characters only,
 *          in which case any non-printable/non-ASCII characters are replaced
 *          by a specified character.
 *  </ul>
 *  <p>
 *  There are also multiple ways to get output:
 *  <ul>
 *  <li>    As a single String, with lines of output separated by newlines.
 *  <li>    As an iterator, where each iteration returns a single String
 *          containing a line of output.
 *  <li>    Written directly to a Writer.
 *  </ul>
 *  <p>
 *  The dumper may be invoked with varying input buffers, which may contain more
 *  or less bytes than desired per line. In such cases, the output is broken or
 *  padded, such that the three segments occupy the same columns in each line (ie,
 *  if you have 16 bytes displayed in one line, and only 8 in the next, the second
 *  line will be padded so that the "printable characters" line up in both).
 */
public class HexDump
{
    private int bytesPerLine;
    private boolean showOffset;
    private int offset;
    private int offsetWidth;
    private int spacesAfterOffset;
    private boolean showChars;
    private int spacesBeforeChars;
    private boolean replaceNonAscii;
    private char replacement;


    /**
     *  Base constructor, with all sorts of options.
     *
     *  @param  bytesPerLine        The number of bytes that will be dumped per
     *                              "line" (aka iteration) of output.
     *  @param  showOffset          If <code>true</code>, the output lines will
     *                              start with a hex counter showing position of
     *                              the bytes in that line, relative to all bytes
     *                              dumped by this object.
     *  @param  offsetWidth         Number of characters for offset field. The
     *                              actual offset will be trimmed or left-zero-
     *                              padded to this size.
     *  @param  spacesAfterOffset   Number of spaces to insert between offset
     *                              field (if it's displayed) and hex bytes.
     *  @param  showChars           If <code>true</code>, the hex dump will be
     *                              followed by the characters represented by
     *                              those bytes.
     *  @param  spacesBeforeChars   If showing characters, the number of spaces
     *                              inserted between the hex dump and characters.
     *  @param  replaceNonAscii     If <code>true</code>, only bytes between 32
     *                              and 126 will be displayed in the character
     *                              output; all others will be replaced.
     *  @param  replacement         The character used to replace non-ASCII or
     *                              non-printable characters. May be '\0',
     *                              which may lead to ugly output.
     */
    public HexDump(int bytesPerLine,
                   boolean showOffset, int offsetWidth, int spacesAfterOffset,
                   boolean showChars, int spacesBeforeChars,
                   boolean replaceNonAscii, char replacement
                   )
    {
        this.bytesPerLine = bytesPerLine;
        this.showOffset = showOffset;
        this.offset = 0;
        this.offsetWidth = offsetWidth;
        this.spacesAfterOffset = spacesAfterOffset;
        this.showChars = showChars;
        this.spacesBeforeChars = spacesBeforeChars;
        this.replaceNonAscii = replaceNonAscii;
        this.replacement = replacement;
    }


    /**
     *  Convenience constructor, which emits 78-character-wide lines with
     *  the following characteristics:
     *  <ul>
     *  <li> Offset, as an 8-character hex value, followed by 2 spaces.
     *  <li> 16 bytes per line, followed by 4 spaces.
     *  <li> Characters, limited to ASCII, with replacement by spaces.
     *  </ul>
     */
    public HexDump()
    {
        this(16, true, 8, 2, true, 4, true, ' ');
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Resets the offset counter to a specified value. Useful if the same
     *  dumper is reused for different output.
     */
    public void setOffset(int value)
    {
        offset = value;
    }


    /**
     *  Creates an iterator over the entire passed buffer, where each call
     *  to <code>next</code> returns a line of output.
     */
    public Iterator<String> iterator(byte[] buf)
    {
        return iterator(buf, 0, buf.length);
    }


    /**
     *  Creates an iterator over a portion of the passed buffer, where each
     *  call to <code>next</code> returns a line of output.
     */
    public Iterator<String> iterator(byte[] buf, int off, int len)
    {
        return new Dumper(buf, off, len);
    }


    /**
     *  Returns a string containing the a dump of the entire passed
     *  buffer, with newlines separating each line of output.
     */
    public String stringValue(byte[] buf)
    {
        return stringValue(buf, 0, buf.length);
    }


    /**
     *  Returns a string containing the a dump of the specified portion
     *  of a passed buffer, with newlines separating each line of output.
     */
    public String stringValue(byte[] buf, int off, int len)
    {
        int capacity = ((len / bytesPerLine) + 1)
                     * (1 + offsetWidth + spacesAfterOffset + 4 * bytesPerLine);
        StringBuilder result = new StringBuilder(capacity);
        Iterator<String> itx = iterator(buf, off, len);
        while (itx.hasNext())
        {
            if (result.length() > 0)
                result.append('\n');
            result.append(itx.next());
        }
        return result.toString();
    }


    /**
     *  Writes an entire buffer to the specified PrintWriter, as separate
     *  lines of output.
     *  <p>
     *  Caller is responsible for setting auto-flush if needed, as well as
     *  selecting an appropriate character set if using character displays
     *  (picking UTF-8 is a bad idea when outputting raw characters).
     */
    public void write(PrintWriter out, byte[] buf)
    {
        write(out, buf, 0, buf.length);
    }


    /**
     *  Writes a portion of the passed buffer to the specified PrintWriter,
     *  as separate lines of output.
     *  <p>
     *  Caller is responsible for setting auto-flush if needed, as well as
     *  selecting an appropriate character set if using character displays
     *  (picking UTF-8 is a bad idea when outputting raw characters).
     */
    public void write(PrintWriter out, byte[] buf, int off, int len)
    {
        Iterator<String> itx = iterator(buf, off, len);
        while (itx.hasNext())
        {
            out.println(itx.next());
        }
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  All the real work happens in this class.
     */
    private class Dumper
    implements Iterator<String>
    {
        private byte[] buf;
        private int off;
        private int len;
        private int lenThisTime;

        public Dumper(byte[] buf, int off, int len)
        {
            this.buf = buf;
            this.off = off;
            this.len = len;
        }

        @Override
        public boolean hasNext()
        {
            return len > 0;
        }

        @Override
        public String next()
        {
            StringBuilder sb = new StringBuilder();
            if (showOffset)
                appendOffset(sb);
            appendBytes(sb);
            if (showChars)
                appendChars(sb);
            updateOffsets();
            return sb.toString();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException(
                    "this iterator isn't backed by a collection");
        }

        private void appendOffset(StringBuilder sb)
        {
            StringBuilderUtil.appendHex(sb, offset, offsetWidth);
            StringBuilderUtil.appendRepeat(sb, ' ', spacesAfterOffset);
        }

        private void appendBytes(StringBuilder sb)
        {
            lenThisTime = Math.min(bytesPerLine, len);
            for (int ii = 0 ; ii < lenThisTime ; ii++)
            {
                if (ii > 0)
                    sb.append(' ');
                StringBuilderUtil.appendHex(sb, buf[off + ii], 2);
            }
        }

        private void appendChars(StringBuilder sb)
        {
            int spaceThisTime = (bytesPerLine - lenThisTime) * 3 + spacesBeforeChars;
            StringBuilderUtil.appendRepeat(sb, ' ', spaceThisTime);

            for (int ii = 0 ; ii < lenThisTime ; ii++)
            {
                byte b = buf[off + ii];
                if (replaceNonAscii && ((b < 32) || (b > 126)))
                    sb.append(replacement);
                else
                    sb.append((char)(b & 0xFF));
            }
        }

        private void updateOffsets()
        {
            offset += lenThisTime;
            off += lenThisTime;
            len -= lenThisTime;
        }
    }
}
