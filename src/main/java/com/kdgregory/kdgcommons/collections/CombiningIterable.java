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

package com.kdgregory.kdgcommons.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 *  Produces an iterator that will iterate over an array of <code>Iterable</code>s.
 *  <p>
 *  The concurrent modification behavior of the iterators produced by this class is
 *  undefined. You should not rely on being able to modify a collection prior to
 *  its place in the sequence of iterators.
 *  <p>
 *  Removal via the produced iterators is dependent on the underlying collection's
 *  iterator. It is possible to mix collections that support removal with those
 *  that do not; the combined iterator will throw for part of the iteration, and
 *  not throw for another part. It's best not to use <code>remove()</code> unless
 *  you know that all underlying collections support it.
 *
 *  @since 1.0.1
 */
public class CombiningIterable<T>
implements Iterable<T>
{
    private List<Iterable<T>> iterables = new ArrayList<Iterable<T>>();


    /**
     *  Constructs an instance from zero or more explicit iterable objects.

     */
    @SuppressWarnings({"unchecked", "varargs"})
    public CombiningIterable(Iterable<T> ... iterables)
    {
        for (Iterable<T> iterable : iterables)
        {
            this.iterables.add(iterable);
        }
    }


    /**
     *  Constructs an instance from an iterable of iterables.
     *
     *  @since 1.0.18
     */
    public CombiningIterable(Iterable<Iterable<T>> iterables)
    {
        for (Iterable<T> iterable : iterables)
        {
            this.iterables.add(iterable);
        }
    }


    @Override
    public Iterator<T> iterator()
    {
        LinkedList<Iterator<T>> iterators = new LinkedList<Iterator<T>>();
        for (Iterable<T> iterable : iterables)
            iterators.add(iterable.iterator());
        return new CombiningIterator<T>(iterators);
    }


    /**
     *  Combines a list of iterators into a single iterator. Exposed for those
     *  callers that don't want to stick to <code>Iterable</code>s.
     */
    public static class CombiningIterator<E>
    implements Iterator<E>
    {
        private LinkedList<Iterator<E>> iterators;
        private Iterator<E> curItx;

        @SuppressWarnings({"unchecked", "varargs"})
        public CombiningIterator(Iterator<E>... iterators)
        {
            this.iterators = new LinkedList<Iterator<E>>();
            for (Iterator<E> itx : iterators)
                this.iterators.add(itx);
        }

        public CombiningIterator(LinkedList<Iterator<E>> iterators)
        {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext()
        {
            if ((curItx != null) && curItx.hasNext())
                return true;

            if (iterators.size() == 0)
                return false;

            curItx = iterators.removeFirst();
            return hasNext();

        }

        @Override
        public E next()
        {
            if (hasNext())
                return curItx.next();
            else
                throw new NoSuchElementException();
        }

        @Override
        public void remove()
        {
            if (curItx != null)
                curItx.remove();
        }
    }
}
