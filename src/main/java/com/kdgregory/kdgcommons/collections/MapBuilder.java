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

import java.util.Map;


/**
 *  A utility class that allows maps to be created and populated with a single
 *  expression. Useful when you want to pass some value to <code>super()</code>,
 *  or when you just don't want independent calls to <code>put()</code>.
 *
 *  @since 1.0.5
 */
public class MapBuilder<K,V>
{
    private Map<K,V> _map;


    /**
     *  Provides the map that will be populated by this object.
     */
    public MapBuilder(Map<K,V> map)
    {
        _map = map;
    }


    /**
     *  Adds an item to the map, returning <code>this</code> so that calls can be chained.
     */
    public MapBuilder<K,V> put(K key, V value)
    {
        _map.put(key, value);
        return this;
    }


    /**
     *  Returns the (now-populated) map.
     */
    public Map<K,V> toMap()
    {
        return _map;
    }
}
