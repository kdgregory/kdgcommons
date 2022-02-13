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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;


/**
 *  An N-tuple that provides <code>equals()</code> and <code>hashCode()</code>,
 *  used to hold a multi-element key for <code>HashMap</code>s.
 */
public final class CompoundKey
implements Iterable<Object>, Serializable
{
        private static final long serialVersionUID = 1L;

        private Object[] components;
        private int hashCode;
        private String stringValue;


        public CompoundKey(Object... components)
        {
            // FIXME - make a defensive copy
            this.components = components;
            for (Object obj : components)
            {
                if (obj != null)
                    this.hashCode = hashCode * 37 + obj.hashCode();
            }
        }


        /**
         *  Returns an iterator over the components of this key.
         */
        @Override
        public Iterator<Object> iterator()
        {
            return Arrays.asList(components).iterator();
        }


        /**
         *  Produces a string of the form "[COMP0,COMP1,...]" where COMP0 et al
         *  come from calling <code>String.valueOf()</code> on the component.
         */
        @Override
        public String toString()
        {
            if (stringValue == null)
            {
                StringBuilder buf = new StringBuilder(components.length * 16);
                buf.append("[");
                for (Object comp : components)
                {
                    if (buf.length() > 1)
                        buf.append(",");
                    buf.append(comp);
                }
                buf.append("]");
                stringValue = buf.toString();
            }
            return stringValue;
        }


        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;

            if (obj instanceof CompoundKey)
            {
                return Arrays.equals(components, ((CompoundKey)obj).components);
            }
            return false;
        }


        @Override
        public int hashCode()
        {
            return hashCode;
        }
}
