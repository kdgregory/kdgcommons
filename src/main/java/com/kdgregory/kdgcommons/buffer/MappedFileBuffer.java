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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.kdgregory.kdgcommons.io.IOUtil;
import com.kdgregory.kdgcommons.lang.UnreachableCodeException;


/**
 *  A wrapper for memory-mapped files that generally preserves the semantics of
 *  <code>ByteBuffer</code>, while supporing files larger than 2 GB. Unlike
 *  normal byte buffers, all access via absolute index, and indexes are
 *  <code>long</code> values.
 *  <p>
 *  This is achieved using a set of overlapping buffers, based on the "segment
 *  size" passed during construction. Segment size is the largest contiguous
 *  sub-buffer that may be accessed (via {@link #getBytes} and {@link #putBytes}),
 *  and may be no larger than 1 GB.
 *  <p>
 *  <strong>Warning:</strong>
 *  This class is not thread-safe. Caller must explicitly synchronize access,
 *  or call {@link #clone} to create a distinct buffer for each thread.
 */
public class MappedFileBuffer
implements BufferFacade, Cloneable
{
    private final static int MAX_SEGMENT_SIZE = 0x8000000; // 1 GB, assures alignment

    private File file;
    private boolean isWritable;
    private long segmentSize;              // long because it's used in long expressions
    private MappedByteBuffer[] buffers;


    /**
     *  Opens and memory-maps the specified file for read-only access, using
     *  the maximum segment size.
     *
     *  @param  file        The file to open; must be accessible to user.
     *
     *  @throws IllegalArgumentException if <code>segmentSize</code> is > 1GB.
     */
    public MappedFileBuffer(File file)
    throws IOException
    {
        this(file, MAX_SEGMENT_SIZE, false);
    }


    /**
     *  Opens and memory-maps the specified file for read-only or read-write
     *  access, using the maximum segment size.
     *
     *  @param  file        The file to open; must be accessible to user.
     *  @param  readWrite   Pass <code>true</code> to open the file with
     *                      read-write access, <code>false</code> to open
     *                      with read-only access.
     *
     *  @throws IllegalArgumentException if <code>segmentSize</code> is > 1GB.
     */
    public MappedFileBuffer(File file, boolean readWrite)
    throws IOException
    {
        this(file, MAX_SEGMENT_SIZE, readWrite);
    }


    /**
     *  Opens and memory-maps the specified file, for read-only or read-write
     *  access, with a specified segment size.
     *
     *  @param  file        The file to open; must be accessible to user.
     *  @param  segmentSize The largest contiguous sub-buffer that can be
     *                      created using {@link #slice}. The maximum size
     *                      is 2^30 - 1.
     *  @param  readWrite   Pass <code>true</code> to open the file with
     *                      read-write access, <code>false</code> to open
     *                      with read-only access.
     *
     *  @throws IllegalArgumentException if <code>segmentSize</code> is > 1GB.
     */
    public MappedFileBuffer(File file, int segmentSize, boolean readWrite)
    throws IOException
    {
        if (segmentSize > MAX_SEGMENT_SIZE)
            throw new IllegalArgumentException(
                    "segment size too large (max is " + MAX_SEGMENT_SIZE + "): " + segmentSize);

        this.file = file;
        this.isWritable = readWrite;
        this.segmentSize = segmentSize;

        RandomAccessFile mappedFile = null;
        try
        {
            String mode = readWrite ? "rw" : "r";
            MapMode mapMode = readWrite ? MapMode.READ_WRITE : MapMode.READ_ONLY;

            mappedFile = new RandomAccessFile(file, mode);
            FileChannel channel = mappedFile.getChannel();

            long fileSize = file.length();

            int bufArraySize = (int)(fileSize / segmentSize)
                             + ((fileSize % segmentSize != 0) ? 1 : 0);
            buffers = new MappedByteBuffer[bufArraySize];
            int bufIdx = 0;
            for (long offset = 0 ; offset < fileSize ; offset += segmentSize)
            {
                long remainingFileSize = fileSize - offset;
                long thisSegmentSize = Math.min(2L * segmentSize, remainingFileSize);
                buffers[bufIdx++] = channel.map(mapMode, offset, thisSegmentSize);
            }
        }
        finally
        {
            IOUtil.closeQuietly(mappedFile);
        }
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the buffer's capacity -- the size of the mapped file.
     */
    @Override
    public long capacity()
    {
        return file.length();
    }


    /**
     *  Returns the buffer's limit -- the maximum index in the buffer + 1.
     *  <p>
     *  This returns the same value as {@link #capacity}; it exists as part of
     *  the {@link BufferFacade} interface.
     */
    @Override
    public long limit()
    {
        return capacity();
    }


    /**
     *  Returns the file that is mapped by this buffer.
     */
    public File file()
    {
        return file;
    }


    /**
     *  Indicates whether this buffer is read-write or read-only.
     */
    public boolean isWritable()
    {
        return isWritable;
    }


    /**
     *  Returns the byte-order of this buffer (actually, the order of the first
     *  child buffer; they should all be the same).
     */
    public ByteOrder getByteOrder()
    {
        return buffers[0].order();
    }


    /**
     *  Sets the order of this buffer (propagated to all child buffers).
     */
    public void setByteOrder(ByteOrder order)
    {
        for (ByteBuffer child : buffers)
            child.order(order);
    }


    /**
     *  Retrieves a single byte from the specified index.
     */
    @Override
    public byte get(long index)
    {
        return buffer(index).get();
    }


    /**
     *  Stores a single byte at the specified index.
     */
    @Override
    public void put(long index, byte value)
    {
        buffer(index).put(value);
    }


    /**
     *  Retrieves a four-byte integer starting at the specified index.
     */
    @Override
    public int getInt(long index)
    {
        return buffer(index).getInt();
    }


    /**
     *  Stores a four-byte integer starting at the specified index.
     */
    @Override
    public void putInt(long index, int value)
    {
        buffer(index).putInt(value);
    }


    /**
     *  Retrieves an eight-byte integer starting at the specified index.
     */
    @Override
    public long getLong(long index)
    {
        return buffer(index).getLong();
    }


    /**
     *  Stores an eight-byte integer starting at the specified index.
     */
    @Override
    public void putLong(long index, long value)
    {
        buffer(index).putLong(value);
    }


    /**
     *  Retrieves a four-byte integer starting at the specified index.
     */
    @Override
    public short getShort(long index)
    {
        return buffer(index).getShort();
    }


    /**
     *  Stores a four-byte integer starting at the specified index.
     */
    @Override
    public void putShort(long index, short value)
    {
        buffer(index).putShort(value);
    }


    /**
     *  Retrieves a four-byte floating-point number starting at the specified
     *  index.
     */
    @Override
    public float getFloat(long index)
    {
        return buffer(index).getFloat();
    }


    /**
     *  Stores a four-byte floating-point number starting at the specified
     *  index.
     */
    @Override
    public void putFloat(long index, float value)
    {
        buffer(index).putFloat(value);
    }


    /**
     *  Retrieves an eight-byte floating-point number starting at the specified
     *  index.
     */
    @Override
    public double getDouble(long index)
    {
        return buffer(index).getDouble();
    }


    /**
     *  Stores an eight-byte floating-point number starting at the specified
     *  index.
     */
    @Override
    public void putDouble(long index, double value)
    {
        buffer(index).putDouble(value);
    }


    /**
     *  Retrieves a two-byte character starting at the specified  index (note
     *  that a Unicode code point may require calling this method twice).
     */
    @Override
    public char getChar(long index)
    {
        return buffer(index).getChar();
    }


    /**
     *  Stores a two-byte character starting at the specified  index.
     */
    @Override
    public void putChar(long index, char value)
    {
        buffer(index).putChar(value);
    }


    /**
     *  Retrieves <code>len</code> bytes starting at the specified index,
     *  storing them in a newly created <code>byte[]</code>. Will span
     *  segments if necessary to retrieve the requested number of bytes.
     *
     *  @throws IndexOutOfBoundsException if the request would read past
     *          the end of file.
     */
    @Override
    public byte[] getBytes(long index, int len)
    {
        byte[] ret = new byte[len];
        return getBytes(index, ret, 0, len);
    }


    /**
     *  Retrieves <code>len</code> bytes starting at the specified index,
     *  storing them in an existing <code>byte[]</code> at the specified
     *  offset. Returns the array as a convenience. Will span segments as
     *  needed.
     *
     *  @throws IndexOutOfBoundsException if the request would read past
     *          the end of file.
     */
    public byte[] getBytes(long index, byte[] array, int off, int len)
    {
        while (len > 0)
        {
            ByteBuffer buf = buffer(index);
            int count = Math.min(len, buf.remaining());
            buf.get(array, off, count);
            index += count;
            off += count;
            len -= count;
        }
        return array;
    }


    /**
     *  Stores the contents of the passed byte array, starting at the given index.
     *  Will span segments as needed.
     *
     *  @throws IndexOutOfBoundsException if the request would write past
     *          the end of file.
     */
    @Override
    public void putBytes(long index, byte[] value)
    {
        putBytes(index, value, 0, value.length);
    }


    /**
     *  Stores a section of the passed byte array, defined by <code>off</code> and
     *  <code>len</code>, starting at the given index. Will span segments as needed.
     *
     *  @throws IndexOutOfBoundsException if the request would write past
     *          the end of file.
     */
    public void putBytes(long index, byte[] value, int off, int len)
    {
        while (len > 0)
        {
            ByteBuffer buf = buffer(index);
            int count = Math.min(len, buf.remaining());
            buf.put(value, off, count);
            index += count;
            off += count;
            len -= count;
        }
    }


    /**
     *  Creates a new buffer, whose size will be >= segment size, starting at
     *  the specified offset.
     */
    @Override
    public ByteBuffer slice(long index)
    {
        return buffer(index).slice();
    }


    /**
     *  Iterates through the underlying buffers, calling <code>force()</code>
     *  on each; this will cause the buffers' contents to be written to disk.
     *  Note, however, that the OS may not physically write the buffers until
     *  a future time.
     */
    public void force()
    {
        for (MappedByteBuffer buf : buffers)
            buf.force();
    }


    /**
     *  Creates a new buffer referencing the same file, but with a copy of the
     *  original underlying mappings. The new and old buffers may be accessed
     *  by different threads.
     */
    @Override
    public MappedFileBuffer clone()
    {
        try
        {
            MappedFileBuffer that = (MappedFileBuffer)super.clone();
            that.buffers = new MappedByteBuffer[buffers.length];
            for (int ii = 0 ; ii < buffers.length ; ii++)
            {
                // if the file is a multiple of the segment size, we
                // can end up with an empty slot in the buffer array
                if (buffers[ii] != null)
                    that.buffers[ii] = (MappedByteBuffer)buffers[ii].duplicate();
            }
            return that;
        }
        catch (CloneNotSupportedException ex)
        {
            throw new UnreachableCodeException("I used to implement Cloneable, why don't I now?");
        }
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    // this is exposed for a white-box test of cloning
    protected ByteBuffer buffer(long index)
    {
        ByteBuffer buf = buffers[(int)(index / segmentSize)];
        buf.position((int)(index % segmentSize));
        return buf;
    }
}
