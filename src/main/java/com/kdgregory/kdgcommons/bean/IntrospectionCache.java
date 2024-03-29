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

package com.kdgregory.kdgcommons.bean;

import java.util.HashMap;
import java.util.Map;


/**
 *  A thread-safe cache of {@link Introspection} objects. May be constructed
 *  using either a local or shared (static) cache.
 *
 *  @since 1.0.5
 */
public class IntrospectionCache
{
    private static Map<Class<?>,Introspection> staticCache = new HashMap<Class<?>,Introspection>();
    private Map<Class<?>,Introspection> cache;


    /**
     *  Creates an instance that uses a local cache.
     */
    public IntrospectionCache()
    {
        this(false);
    }


    /**
     *  Creates an instance that will either use a local or shared (static) cache.
     */
    public IntrospectionCache(boolean shared)
    {
        this.cache = shared
                    ? staticCache
                    : new HashMap<Class<?>,Introspection>();
    }



    /**
     *  Returns an {@link Introspection} of the passed class.
     *
     *  @throws ConversionError if unable to introspect the class.
     */
    public synchronized Introspection lookup(Class<?> klass)
    {
        return lookup(klass,false);
    }


    /**
     *  Returns an {@link Introspection} of the passed class, optionally calling
     *  <code>setAccessible(true)</code> on all accessor methods.
     *  <p>
     *  Note: because introspections are cached, the <code>setAccessible</code>
     *  argument is ignored for the second and subsequent calls for the same class.
     *
     *  @since 1.0.12
     *
     *  @throws ConversionError if unable to introspect the class.
     */
    public synchronized Introspection lookup(Class<?> klass, boolean setAccessible)
    {
        Introspection result = cache.get(klass);
        if (result == null)
        {
            result = new Introspection(klass, setAccessible);
            cache.put(klass, result);
        }
        return result;
    }
}
