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

import java.nio.ByteBuffer;


/**
 *  Creates {@link BufferFacade} instances to wrap variety of buffer-like objects.
 *  In addition to simple wrappers, this class will also create "offset" wrappers:
 *  all requested locations are offset a fixed distance from the start of the
 *  underlying buffer.
 *  <p>
 *  There are two factory methods (which are overloaded by buffer type and whether
 *  the facade is offset):
 *  <ul>
 *  <li> <code>create()</code> creates a facade for single-threaded access
 *  <li> <code>createThreadsafe</code> creates a facade that will support concurrent
 *       access
 *  </ul>
 *  <p>
 *  If you don't like the factory method, the implementation classes are exposed.
 */
public class BufferFacadeFactory
{
    /**
     *  Creates an instance that accesses a standard <code>ByteBuffer</code>.
     *  All indexes are limited to <code>Integer.MAX_VALUE</code>.
     */
    public static BufferFacade create(ByteBuffer buf)
    {
        return new ByteBufferFacade(buf);
    }


    /**
     *  Creates an instance that accesses a standard <code>ByteBuffer</code>,
     *  with offsets relative to the specified base value. Although the base
     *  value is specified as a <code>long</code> (for consistency with other
     *  methods), it is limited to <code>Integer.MAX_VALUE</code> and all
     *  indexes are limited to <code>Integer.MAX_VALUE - base</code>.
     */
    public static BufferFacade create(ByteBuffer buf, long base)
    {
        return new ByteBufferFacade(buf, (int)base);
    }


    /**
     *  Creates a thread-safe instance that accesses a standard<code>ByteBuffer</code>.
     *  All indexes are limited to <code>Integer.MAX_VALUE</code>.
     *  <p>
     *  See {@link ByteBufferThreadLocal} for important caveats.
     */
    public static BufferFacade createThreadsafe(ByteBuffer buf)
    {
        return new ByteBufferTLFacade(buf);
    }


    /**
     *  Creates a thread-safe instance that accesses a standard <code>ByteBuffer</code>,
     *  with offsets relative to the specified base value. Although the base value is
     *  specified as a <code>long</code> (for consistency with other methods), it is
     *  limited to <code>Integer.MAX_VALUE</code> and all indexes are limited to
     *  <code>Integer.MAX_VALUE - base</code>.
     *  <p>
     *  Because this method will create independent buffers for each thread, the
     *  <code>limit()</code> method may return different values. Setting a limit on
     *  the underlying buffer will not affect buffers that have already been created.
     */
    public static BufferFacade createThreadsafe(ByteBuffer buf, long base)
    {
        return new ByteBufferTLFacade(buf, (int)base);
    }


    /**
     *  Creates an instance that accesses a {@link MappedFileBuffer}.
     */
    public static BufferFacade create(MappedFileBuffer buf)
    {
        // no need to wrap this
        return buf;
    }


    /**
     *  Creates an instance that accesses a {@link MappedFileBuffer}, with
     *  with offsets relative to the specified base value.
     */
    public static BufferFacade create(MappedFileBuffer buf, long base)
    {
        return new MappedFileBufferFacade(buf, base);
    }


    /**
     *  Creates a thread-safe instance that accesses a {@link MappedFileBuffer}.
     */
    public static BufferFacade createThreadsafe(MappedFileBuffer buf)
    {
        return new MappedFileBufferTLFacade(buf);
    }


    /**
     *  Creates a thread-safe instance that accesses a {@link MappedFileBuffer},
     *  with offsets relative to the specified base  value.
     */
    public static BufferFacade createThreadsafe(MappedFileBuffer buf, long base)
    {
        return new MappedFileBufferTLFacade(buf, (int)base);
    }

//----------------------------------------------------------------------------
//  Facade Implementation Classes
//----------------------------------------------------------------------------

    /**
     *  A facade for a standard Java <code>ByteBuffer</code>.
     */
    public static class ByteBufferFacade
    implements BufferFacade
    {
        private ByteBuffer buf;
        private int base;

        public ByteBufferFacade(ByteBuffer buf)
        {
            this.buf = buf;
        }

        public ByteBufferFacade(ByteBuffer buf, int base)
        {
            this(buf);
            this.base = base;
        }

        @Override
        public byte get(long index)
        {
            return buf.get((int)index + base);
        }

        @Override
        public void put(long index, byte value)
        {
            buf.put((int)index + base, value);
        }

        @Override
        public short getShort(long index)
        {
            return buf.getShort((int)index + base);
        }

        @Override
        public void putShort(long index, short value)
        {
            buf.putShort((int)index + base, value);
        }

        @Override
        public int getInt(long index)
        {
            return buf.getInt((int)index + base);
        }

        @Override
        public void putInt(long index, int value)
        {
            buf.putInt((int)index + base, value);
        }

        @Override
        public long getLong(long index)
        {
            return buf.getLong((int)index + base);
        }

        @Override
        public void putLong(long index, long value)
        {
            buf.putLong((int)index + base, value);
        }

        @Override
        public float getFloat(long index)
        {
            return buf.getFloat((int)index + base);
        }

        @Override
        public void putFloat(long index, float value)
        {
            buf.putFloat((int)index + base, value);
        }

        @Override
        public double getDouble(long index)
        {
            return buf.getDouble((int)index + base);
        }

        @Override
        public void putDouble(long index, double value)
        {
            buf.putDouble((int)index + base, value);
        }

        @Override
        public char getChar(long index)
        {
            return buf.getChar((int)index + base);
        }

        @Override
        public void putChar(long index, char value)
        {
            buf.putChar((int)index + base, value);
        }

        @Override
        public byte[] getBytes(long index, int len)
        {
            buf.position((int)index + base);

            byte[] ret = new byte[len];
            buf.get(ret);
            return ret;
        }

        @Override
        public void putBytes(long index, byte[] value)
        {
            buf.position((int)index + base);
            buf.put(value);
        }

        @Override
        public ByteBuffer slice(long index)
        {
            buf.position((int)index + base);
            return buf.slice();
        }

        @Override
        public long capacity()
        {
            return buf.capacity() - base;
        }

        @Override
        public long limit()
        {
            return buf.limit() - base;
        }
    }


    /**
     *  A facade for a standard Java <code>ByteBuffer</code> that uses a
     *  thread-local to allow concurrent access.
     */
    public static class ByteBufferTLFacade
    implements BufferFacade
    {
        private ByteBufferThreadLocal tl;
        private int base;

        public ByteBufferTLFacade(ByteBuffer buf)
        {
            this.tl = new ByteBufferThreadLocal(buf);
        }

        public ByteBufferTLFacade(ByteBuffer buf, int base)
        {
            this(buf);
            this.base = base;
        }

        @Override
        public byte get(long index)
        {
            return tl.get().get((int)index + base);
        }

        @Override
        public void put(long index, byte value)
        {
            tl.get().put((int)index + base, value);
        }

        @Override
        public short getShort(long index)
        {
            return tl.get().getShort((int)index + base);
        }

        @Override
        public void putShort(long index, short value)
        {
            tl.get().putShort((int)index + base, value);
        }

        @Override
        public int getInt(long index)
        {
            return tl.get().getInt((int)index + base);
        }

        @Override
        public void putInt(long index, int value)
        {
            tl.get().putInt((int)index + base, value);
        }

        @Override
        public long getLong(long index)
        {
            return tl.get().getLong((int)index + base);
        }

        @Override
        public void putLong(long index, long value)
        {
            tl.get().putLong((int)index + base, value);
        }

        @Override
        public float getFloat(long index)
        {
            return tl.get().getFloat((int)index + base);
        }

        @Override
        public void putFloat(long index, float value)
        {
            tl.get().putFloat((int)index + base, value);
        }

        @Override
        public double getDouble(long index)
        {
            return tl.get().getDouble((int)index + base);
        }

        @Override
        public void putDouble(long index, double value)
        {
            tl.get().putDouble((int)index + base, value);
        }

        @Override
        public char getChar(long index)
        {
            return tl.get().getChar((int)index + base);
        }

        @Override
        public void putChar(long index, char value)
        {
            tl.get().putChar((int)index + base, value);
        }

        @Override
        public byte[] getBytes(long index, int len)
        {
            ByteBuffer buf = tl.get();
            buf.position((int)index + base);

            byte[] ret = new byte[len];
            buf.get(ret);
            return ret;
        }

        @Override
        public void putBytes(long index, byte[] value)
        {
            ByteBuffer buf = tl.get();
            buf.position((int)index + base);
            buf.put(value);
        }

        @Override
        public ByteBuffer slice(long index)
        {
            ByteBuffer buf = tl.get();
            buf.position((int)index + base);
            return buf.slice();
        }

        @Override
        public long capacity()
        {
            return tl.get().capacity() - base;
        }

        @Override
        public long limit()
        {
            return  tl.get().limit() - base;
        }
    }


    /**
     *  A facade for a {@link MappedFileBuffer}. This is only needed when creating
     *  an offset facade, as <code>MappedFileBuffer</code> already implements
     *  <code>BufferFacade</code>.
     */
    private static class MappedFileBufferFacade
    implements BufferFacade
    {
        private MappedFileBuffer buf;
        private long base;

        public MappedFileBufferFacade(MappedFileBuffer buf)
        {
            this.buf = buf;
        }

        public MappedFileBufferFacade(MappedFileBuffer buf, long base)
        {
            this(buf);
            this.base = base;
        }

        @Override
        public byte get(long index)
        {
            return buf.get(index + base);
        }

        @Override
        public void put(long index, byte value)
        {
            buf.put(index + base, value);
        }

        @Override
        public short getShort(long index)
        {
            return buf.getShort(index + base);
        }

        @Override
        public void putShort(long index, short value)
        {
            buf.putShort(index + base, value);
        }

        @Override
        public int getInt(long index)
        {
            return buf.getInt(index + base);
        }

        @Override
        public void putInt(long index, int value)
        {
            buf.putInt(index + base, value);
        }

        @Override
        public long getLong(long index)
        {
            return buf.getLong(index + base);
        }

        @Override
        public void putLong(long index, long value)
        {
            buf.putLong(index + base, value);
        }

        @Override
        public float getFloat(long index)
        {
            return buf.getFloat(index + base);
        }

        @Override
        public void putFloat(long index, float value)
        {
            buf.putFloat(index + base, value);
        }

        @Override
        public double getDouble(long index)
        {
            return buf.getDouble(index + base);
        }

        @Override
        public void putDouble(long index, double value)
        {
            buf.putDouble(index + base, value);
        }

        @Override
        public char getChar(long index)
        {
            return buf.getChar(index + base);
        }

        @Override
        public void putChar(long index, char value)
        {
            buf.putChar(index + base, value);
        }

        @Override
        public byte[] getBytes(long index, int len)
        {
            return buf.getBytes(index + base, len);
        }

        @Override
        public void putBytes(long index, byte[] value)
        {
            buf.putBytes(index + base, value);
        }

        @Override
        public ByteBuffer slice(long index)
        {
            return buf.slice(index + base);
        }

        @Override
        public long capacity()
        {
            return buf.capacity() - base;
        }

        @Override
        public long limit()
        {
            return  buf.limit() - base;
        }
    }


    /**
     *  A facade for a {@link MappedFileBuffer} that uses a thread-local to
     *  allow concurrent access.
     */
    public static class MappedFileBufferTLFacade
    implements BufferFacade
    {
        private MappedFileBufferThreadLocal tl;
        private long base;

        public MappedFileBufferTLFacade(MappedFileBuffer buf)
        {
            this.tl = new MappedFileBufferThreadLocal(buf);
        }

        public MappedFileBufferTLFacade(MappedFileBuffer buf, long base)
        {
            this(buf);
            this.base = base;
        }

        @Override
        public byte get(long index)
        {
            return tl.get().get(index + base);
        }

        @Override
        public void put(long index, byte value)
        {
            tl.get().put(index + base, value);
        }

        @Override
        public short getShort(long index)
        {
            return tl.get().getShort(index + base);
        }

        @Override
        public void putShort(long index, short value)
        {
            tl.get().putShort(index + base, value);
        }

        @Override
        public int getInt(long index)
        {
            return tl.get().getInt(index + base);
        }

        @Override
        public void putInt(long index, int value)
        {
            tl.get().putInt(index + base, value);
        }

        @Override
        public long getLong(long index)
        {
            return tl.get().getLong(index + base);
        }

        @Override
        public void putLong(long index, long value)
        {
            tl.get().putLong(index + base, value);
        }

        @Override
        public float getFloat(long index)
        {
            return tl.get().getFloat(index + base);
        }

        @Override
        public void putFloat(long index, float value)
        {
            tl.get().putFloat(index + base, value);
        }

        @Override
        public double getDouble(long index)
        {
            return tl.get().getDouble(index + base);
        }

        @Override
        public void putDouble(long index, double value)
        {
            tl.get().putDouble(index + base, value);
        }

        @Override
        public char getChar(long index)
        {
            return tl.get().getChar(index + base);
        }

        @Override
        public void putChar(long index, char value)
        {
            tl.get().putChar(index + base, value);
        }

        @Override
        public byte[] getBytes(long index, int len)
        {
            return tl.get().getBytes(index + base, len);
        }

        @Override
        public void putBytes(long index, byte[] value)
        {
            tl.get().putBytes(index + base, value);
        }

        @Override
        public ByteBuffer slice(long index)
        {
            return tl.get().slice(index + base);
        }

        @Override
        public long capacity()
        {
            return tl.get().capacity() - base;
        }

        @Override
        public long limit()
        {
            return  tl.get().limit() - base;
        }
    }
}
