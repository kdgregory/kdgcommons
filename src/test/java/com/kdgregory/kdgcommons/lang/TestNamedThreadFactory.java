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

package com.kdgregory.kdgcommons.lang;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestNamedThreadFactory
{
    // these variables let created threads report back to the main thread
    private volatile String threadName;
    private volatile ThreadGroup threadGroup;
    private volatile int priority;
    private volatile boolean isDaemon;

    // the main thread uses this latch to ensure that the created thread runs
    private CountDownLatch syncLatch = new CountDownLatch(1);

    // and this runnable does the work
    Runnable task = new Runnable()
    {
        @Override
        public void run()
        {
            threadName = Thread.currentThread().getName();
            threadGroup = Thread.currentThread().getThreadGroup();
            priority = Thread.currentThread().getPriority();
            isDaemon = Thread.currentThread().isDaemon();
            syncLatch.countDown();
        }
    };


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testSimpleConstructor() throws Exception
    {
        ThreadFactory fact = new NamedThreadFactory("foo");

        fact.newThread(task).start();
        syncLatch.await();

        assertEquals("name",       "foo-thread-0", threadName);
        assertSame("thread group", Thread.currentThread().getThreadGroup(), threadGroup);
        assertEquals("priority",   Thread.NORM_PRIORITY, priority);
        assertTrue("isDaemon",     isDaemon);
    }


    @Test
    public void testFactoryRetainsThreadGroupOfCreator() throws Exception
    {
        ThreadGroup factGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup altGroup = new ThreadGroup("foo");
        final ThreadFactory fact = new NamedThreadFactory("foo");

        new Thread(altGroup, new Runnable()
        {
            @Override
            public void run()
            {
                fact.newThread(task).start();
            }
        }).start();
        syncLatch.await();

        assertSame("thread group", factGroup, threadGroup);
    }


    @Test
    public void testFullConstructor() throws Exception
    {
        ThreadGroup altGroup = new ThreadGroup("foo");
        final ThreadFactory fact = new NamedThreadFactory("foo", altGroup, Thread.MIN_PRIORITY, false);

        fact.newThread(task).start();
        syncLatch.await();

        assertEquals("name",       "foo-thread-0", threadName);
        assertSame("thread group", altGroup, threadGroup);
        assertEquals("priority",   Thread.MIN_PRIORITY, priority);
        assertFalse("isDaemon",    isDaemon);
    }
}
