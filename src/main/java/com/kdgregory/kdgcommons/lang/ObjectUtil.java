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

import java.lang.reflect.Array;
import java.util.function.Supplier;


/**
 *  Static utility methods for working with objects.
 */
public class ObjectUtil
{
    private ObjectUtil()
    {
        // this is here to prevent instantiation
    }


    /**
     *  Tests two objects for equality. Properly handles <code>null</code>, and also
     *  handles arrays (something that Jakarta <code>ObjectUtils.equals()</code> does
     *  not do).
     */
    public static boolean equals(Object o1, Object o2)
    {
        if (o1 == o2)
            return true;

        if ((o1 == null) && (o2 != null))
            return false;

        if ((o1 != null) && (o2 == null))
            return false;

        if (!o1.getClass().isArray() || !o2.getClass().isArray())
            return o1.equals(o2);

        if (o1.getClass() != o2.getClass())
            return false;

        int s1 = Array.getLength(o1);
        int s2 = Array.getLength(o2);
        if (s1 != s2)
            return false;

        for (int ii = 0 ; ii < s1 ; ii++)
        {
            if (!equals(Array.get(o1, ii), Array.get(o2, ii)))
                return false;
        }
        return true;
    }


    /**
     *  Tests for equality between an <code>int</code> and an <code>Integer</code>.
     *  <p>
     *  This method and its sibling exists to avoid the accidental derefencing of a
     *  null for primitive comparison, or the needless creation of an auto-boxed
     *  wrapper for object comparison.
     *
     *  @since 1.0.11
     */
    public static boolean equals(int v1, Integer v2)
    {
        return (v2 == null) ? false : v1 == v2.intValue();
    }


    /**
     *  Tests for equality between an <code>Integer</code> and an <code>int</code>.
     *  <p>
     *  This method and its sibling exists to avoid the accidental derefencing of a
     *  null for primitive comparison, or the needless creation of an auto-boxed
     *  wrapper for object comparison.
     *
     *  @since 1.0.11
     */
    public static boolean equals(Integer v1, int v2)
    {
        return (v1 == null) ? false : v1.intValue() == v2;
    }


    /**
     *  Tests for equality between two <code>int</code> values.
     *  <p>
     *  This method exists as a complement to the object/primitive tests, so that the
     *  same method name can be used regardless of the arguments.
     *
     *  @since 1.0.11
     */
    public static boolean equals(int v1, int v2)
    {
        return v1 == v2;
    }


    /**
     *  Tests for equality between a <code>long</code> and a <code>Long</code>.
     *  <p>
     *  This method and its sibling exists to avoid the accidental derefencing of a
     *  null for primitive comparison, or the needless creation of an auto-boxed
     *  wrapper for object comparison.
     *
     *  @since 1.0.11
     */
    public static boolean equals(long v1, Long v2)
    {
        return (v2 == null) ? false : v1 == v2.longValue();
    }


    /**
     *  Tests for equality between a <code>Long</code> and a <code>long</code>.
     *  <p>
     *  This method and its sibling exists to avoid the accidental derefencing of a
     *  null for primitive comparison, or the needless creation of an auto-boxed
     *  wrapper for object comparison.
     *
     *  @since 1.0.11
     */
    public static boolean equals(Long v1, long v2)
    {
        return (v1 == null) ? false : v1.longValue() == v2;
    }


    /**
     *  Tests for equality between two <code>long</code> values.
     *  <p>
     *  This method exists as a complement to the object/primitive tests, so that the
     *  same method name can be used regardless of the arguments.
     *
     *  @since 1.0.11
     */
    public static boolean equals(long v1, long v2)
    {
        return v1 == v2;
    }


    /**
     *  Returns the hashcode of the passed object, 0 if passed <code>null</code>.
     *  This is a direct replacement for Jakarta's <code>ObjectUtils.hashCode()</code>
     *  method, existing simply to eliminate a dependency.
     */
    public static int hashCode(Object obj)
    {
        return (obj == null) ? 0 : obj.hashCode();
    }


    /**
     *  Null-safe comparison of two arbitrary comparables, in which <code>null</code>
     *  is considered less than any not-null value.
     *
     *  @since 1.0.14
     *
     *  @throws ClassCastException if objects are not directly comparable.
     */
    @SuppressWarnings("rawtypes")
    public static int compare(Comparable v1, Comparable v2)
    {
        return compare(v1, v2, true);
    }


    /**
     *  Null-safe comparison of two arbitrary comparables. Null are treated as higher or
     *  lower than not-null values based on <code>nullIsLow</code>.
     *
     *  @since 1.0.14
     *
     *  @throws ClassCastException if objects are not directly comparable.
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static int compare(Comparable v1, Comparable v2, boolean nullIsLow)
    {
        return ((v1 == null) && (v2 == null)) ? 0
             : ((v1 == null) && (v2 != null)) ? (nullIsLow ? -1 : 1)
             : ((v1 != null) && (v2 == null)) ? (nullIsLow ? 1  : -1)
             : v1.compareTo(v2);
    }


    /**
     *  Returns a string value of an object similar to that returned by <code>
     *  Object.toString()</code>. The primary difference is that the numeric
     *  part of the string is its identity hashcode, and it's in decimal (so as
     *  not to be confused with the default <code>toString()</code>).
     *  <p>
     *  This method is null-safe: if passed <code>null</code>, it returns
     *  "null".
     */
    public static String identityToString(Object obj)
    {
        return (obj == null)
               ? "null"
               : obj.getClass().getName() + "@" + System.identityHashCode(obj);
    }


    /**
     *  Returns the passed <code>value</code>, unless it's null, in which case
     *  the <code>defaultValue</code> is returned.
     *
     *  @since 1.0.6
     */
    public static <T> T defaultValue(T value, T defaultValue)
    {
        return (value != null) ? value : defaultValue;
    }


    /**
     *  Returns the passed <code>value</code>, unless it's null, in which case
     *  the supplier operation is called.
     *
     *  @since 2.0.0
     */
    public static <T> T defaultValue(T value, Supplier<T> factory)
    {
        return (value != null) ? value : factory.get();
    }
}
