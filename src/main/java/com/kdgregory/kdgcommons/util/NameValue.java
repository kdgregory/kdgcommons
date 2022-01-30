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

import java.io.Serializable;

import com.kdgregory.kdgcommons.lang.ObjectUtil;


/**
 *  An immutable 2-tuple that associates a name with a value. This is
 *  particularly useful for programs that perform database operations,
 *  as a way of managing the data coming back from JDBC.
 */
public class NameValue<T>
implements Comparable<NameValue<T>>, Serializable
{
    private static final long serialVersionUID = 1L;

    private String  name;
    private T  value;


    public NameValue(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    public String getName()
    {
        return name;
    }


    public T getValue()
    {
        return value;
    }

//----------------------------------------------------------------------------
//  Overrides of Object
//----------------------------------------------------------------------------

    /**
     *  Two <code>NameValue</code> instances are considered equal if both
     *  name and value components are equal.
     */
    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof NameValue)
        {
            NameValue<T> that = (NameValue<T>)obj;
            return ObjectUtil.equals(name, that.name)
                   && ObjectUtil.equals(value, that.value);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return ObjectUtil.hashCode(name) * 31 + ObjectUtil.hashCode(value);
    }


    /**
     *  Returns the string representation of this object, in the form
     *  <code>[NAME=VALUE]</code>.
     */
    @Override
    public String toString()
    {
        return "[" + name + "=" + String.valueOf(value) + "]";
    }

//----------------------------------------------------------------------------
//  Implementation of Comparable
//----------------------------------------------------------------------------

    /**
     *  Compares two <CODE>NameValue</CODE> instances. Instances are ordered
     *  by name first. If two instances have the same name, then the value is
     *  examined. If the value implements Comparable, this is straightforward;
     *  if not, the values are converted to strings and then compared.
     */
    @Override
    public int compareTo(NameValue<T> that)
    {
        int cmp = name.compareTo(that.name);
        if (cmp != 0)
            return cmp;

        if (value instanceof Comparable)
            return ((Comparable<T>)value).compareTo(that.value);

        if (ObjectUtil.equals(value, that.value))
            return 0;

        return (String.valueOf(value).compareTo(String.valueOf(that.value)));
    }
}
