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

package net.sf.kdgcommons.collections;


/**
 *  A wrapper for an object that provides <code>equals()</code> and <code>hashCode()</code>
 *  based on identity of the underlying object. Used to store objects in hashed collections
 *  where the object's internal implementations would result in deduplication.
 *  <p>
 *  This class is not serializable. Serialization could create a new instance of the
 *  underlying object, meaning that a serialized <code>IdentityKey</code> would not
 *  provide identity comparisons to the original key.
 */
public class IdentityKey
{
    Object _realKey;

    public IdentityKey(Object key)
    {
        _realKey = key;
    }

    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof IdentityKey)
        {
            return _realKey == ((IdentityKey)obj)._realKey;
        }
        return false;

    }

    @Override
    public final int hashCode()
    {
        return System.identityHashCode(_realKey);
    }
}
