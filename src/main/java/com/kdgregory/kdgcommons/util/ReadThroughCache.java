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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;


/**
 *  A size-limited LRU cache that uses a retriever function to load values. Instances are
 *  thread-safe (provided the retriever is thread-safe) and provide a variety of blocking
 *  options for retrieval.
 *  <p>
 *  Note that the cache itself implements the {@link Retriever} interface; caches may be
 *  stacked.
 *
 *  @since 1.0.15
 */
public class ReadThroughCache<K,V>
{
    /**
     *  Options for controlling concurrent retrieval.
     */
    public enum Synchronization
    {
        /**
         *  No synchronization: concurrent requests for the same key will invoke concurrent
         *  retrievals. The first value returned is cached, subsequent values are discarded.
         */
        NONE,

        /**
         *  Per-key synchronization: each retrieval request establishes a lock that is cleared
         *  when the retrieval completes. Subsequent retrieves for the same key will block until
         *  the first returns. This is the default behavior.
         */
        BY_KEY,

        /**
         *  Single-threaded: only one invocation of the retriever will take place at a time; all
         *  subsequent requests will block until it completes. This should only be used if the
         *  retriever is not thread-safe.
         */
        SINGLE_THREADED
    }


//----------------------------------------------------------------------------
//  Constructors and instance variables
//----------------------------------------------------------------------------

    private Function<K,V> retriever;
    private Object cacheLock = new Object();
    private Map<K,V> cache = null;

    /**
     *  Base constructor.
     *
     *  @param size         Maximum number of items in the cache; the least recently used
     *                      item will be evicted if retrieval would exceed this limit. To
     *                      prevent resizing the underlying hash table, this value is also
     *                      used as the map's capacity.
     *  @param retriever    The function to retrieve items.
     *  @param syncOpt      The synchronization strategy.
     */
    public ReadThroughCache(final int size, Function<K,V> retriever, Synchronization syncOpt)
    {
        switch (syncOpt)
        {
            case NONE :
                this.retriever = new UnsynchronizedRetriever(retriever);
                break;
            case BY_KEY :
                this.retriever = new ByKeyRetriever(retriever);
                break;
            case SINGLE_THREADED:
                this.retriever = new SingleThreadedRetriever(retriever);
                break;
            default :
                throw new IllegalArgumentException("invalid synchronization option: " + syncOpt);
        }


        cache = new LinkedHashMap<K,V>(size, 0.75f, true)
        {
             private static final long serialVersionUID = 1L;

             @Override
             protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
                return this.size() > size;
             }
        };
    }


    /**
     *  Convenience constructor: creates an instance with specified size and retriever,
     *  using per-key synchronization.
     */
    public ReadThroughCache(int size, Function<K,V> retriever)
    {
        this(size, retriever, Synchronization.BY_KEY);
    }

//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    public V retrieve(K key) throws InterruptedException
    {
        // all the intelligence happens in the retriever decorators
        return retriever.apply(key);
    }


    /**
     *  Returns the count of mappings currently in the cache.
     */
    public int size()
    {
        synchronized (cacheLock)
        {
            return cache.size();
        }
    }


    /**
     * Removes all cached values.
     */
    public void clear()
    {
        synchronized (cacheLock)
        {
            cache.clear();
        }
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private abstract class AbstractDelegatingRetriever
    implements Function<K,V>
    {
        protected Function<K,V> delegate;

        protected AbstractDelegatingRetriever(Function<K,V> delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public V apply(K key)
        {
            synchronized (cacheLock)
            {
                if (cache.containsKey(key))
                    return cache.get(key);
            }
            return delegate.apply(key);
        }


        protected V cache(K key, V value)
        {
            synchronized (cacheLock)
            {
                cache.put(key, value);
                return value;
            }
        }
    }


    private class UnsynchronizedRetriever
    extends AbstractDelegatingRetriever
    {
        public UnsynchronizedRetriever(Function<K,V> delegate)
        {
            super(delegate);
        }

        @Override
        public V apply(K key)
        {
            V value = super.apply(key);

            synchronized (cacheLock)
            {
                if (cache.containsKey(key))
                    return cache.get(key);
                else
                    return cache(key, value);
            }
        }
    }


    private class ByKeyRetriever
    extends AbstractDelegatingRetriever
    {
        private Object internalLock = new Object();
        private Map<K,ReentrantLock> keyLocks = new HashMap<K,ReentrantLock>();

        public ByKeyRetriever(Function<K,V> delegate)
        {
            super(delegate);
        }

        @Override
        public V apply(K key)
        {
            ReentrantLock lock = null;
            try
            {
                lock = acquireLock(key);
            }
            catch (InterruptedException ex)
            {
                throw new RuntimeException("interrupted while waiting for lock");
            }

            try
            {
                V value = super.apply(key);
                return cache(key, value);
            }
            finally
            {
                releaseLock(lock, key);
            }
        }

        private ReentrantLock acquireLock(K key) throws InterruptedException
        {
            ReentrantLock lock = null;
            synchronized (internalLock)
            {
                lock = keyLocks.get(key);
                if (lock == null)
                {
                    lock = new ReentrantLock();
                    keyLocks.put(key, lock);
                    lock.lock();
                    return lock;
                }
            }

            // here's a race condition here that I don't think I can fix: if T1 deletes
            // the map entry between the time that T2 gets the lock and locks it, then
            // T3 can come in and think that nobody is locking the key.
            //
            // the only guaranteed solution is to make the keylocks have the same lifetime as
            // entries in the cache, but that would mean that the cache could no longer be
            // written to take a Function for retrieval; it would have to be a custom class

            lock.lockInterruptibly();
            return lock;
        }


        private void releaseLock(ReentrantLock lock, K key)
        {
            synchronized (internalLock)
            {
                if (! lock.hasQueuedThreads())
                {
                    keyLocks.remove(key);
                }
                lock.unlock();
            }
        }
    }


    private class SingleThreadedRetriever
    extends AbstractDelegatingRetriever
    {
        public SingleThreadedRetriever(Function<K,V> delegate)
        {
            super(delegate);
        }

        @Override
        public synchronized V apply(K key)
        {
            V value = super.apply(key);
            return cache(key, value);
        }
    }
}
