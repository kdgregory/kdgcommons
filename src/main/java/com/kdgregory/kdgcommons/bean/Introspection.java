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

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *  Introspects bean-style classes, identifying property names and the methods
 *  to get/set those properties. Each instance introspects a single bean; you
 *  can use {@link IntrospectionCache} to hold multiple instances.
 *
 *  Instances of this class are read-only (and thus threadsafe) once constructed.
 *
 *  @since 1.0.5
 */
public class Introspection
{
    private boolean setAccessible;
    private Set<String> propNames;
    private Set<String> propNamesPublic;
    private Map<String,Method> getters;
    private Map<String,Method> setters;


    /**
     *  Introspects the specified class, per the rules above.
     *
     *  @throws IntrospectionException on any error (this will always wrap
     *          an underlying exception, typically one of the checked exceptions
     *          thrown by the reflection mechanism).
     */
    public Introspection(Class<?> klass)
    {
        this(klass, false);
    }


    /**
     *  Introspects the specified class, per the rules above. Optionally sets
     *  each introspected method as accessible. This avoids exceptions caused
     *  by public methods in private classes, but will throw a security
     *  exception if running in a sandbox.
     *
     *  @since 1.0.12
     */
    public Introspection(Class<?> klass, boolean setAccessible)
    {
        this.setAccessible = setAccessible;
        this.propNames = new HashSet<String>();
        this.propNamesPublic = Collections.unmodifiableSet(propNames);
        this.getters = new HashMap<String,Method>();
        this.setters = new HashMap<String,Method>();

        introspect(klass);
    }

//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns the property names for the specified class. These names are
     *  generated from getter methods -- any method beginning with "get"
     *  or "is". The returned set is unmodifiable, and will be empty if
     *  there are no properties with bean-style getter methods.
     *  <p>
     *  Names are processed by <code>Introspector.decapitalize()</code>, so
     *  will be consistent with the bean specification.
     */
    public Set<String> propertyNames()
    {
        return propNamesPublic;
    }


    /**
     *  Returns the getter method for the named property, <code>null</code>
     *  no method is known (all properties returned by {@link #propertyNames}
     *  must have getters, but may not have setters).
     */
    public Method getter(String propName)
    {
        return getters.get(propName.toLowerCase());
    }


    /**
     *  Returns the setter method for the named property, <code>null</code>
     *  if unable to find a method.
     */
    public Method setter(String propName)
    {
        return setters.get(propName.toLowerCase());
    }


    /**
     *  Returns the type of the named property, taken from the return type
     *  of the property's getter. Will be <code>null</code> if the property
     *  isn't known.
     */
    public Class<?> type(String propName)
    {
        Method getter = getter(propName);
        return (getter == null)
             ? null
             : getter.getReturnType();
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void introspect(Class<?> klass)
    {
        try
        {
            for (Method method : klass.getMethods())
            {
                if (method.getDeclaringClass() == Object.class)
                    continue;

                String methodName = method.getName();
                int paramCount = method.getParameterTypes().length;

                if ((methodName.startsWith("get")) && (paramCount == 0))
                {
                    String propName = extractAndSavePropName(methodName, 3);
                    saveGetter(propName, method);
                }
                else if ((methodName.startsWith("is")) && (paramCount == 0))
                {
                    String propName = extractAndSavePropName(methodName, 2);
                    saveGetter(propName, method);
                }
                else if ((methodName.startsWith("set")) && (paramCount == 1))
                {
                    String propName = extractAndSavePropName(methodName, 3);
                    saveSetter(propName, method);
                }
            }
        }
        catch (Exception ee)
        {
            throw new IntrospectionException("unable to introspect", ee);
        }
    }


    private String extractAndSavePropName(String methodName, int pos)
    {
        String propName = methodName.substring(pos);
        propNames.add(Introspector.decapitalize(propName));
        return propName.toLowerCase();
    }


    private void saveGetter(String propName, Method method)
    {
        if (setAccessible)
            method.setAccessible(true);

        Method existing = getters.get(propName);
        if (existing == null)
        {
            getters.put(propName, method);
            return;
        }

        Class<?> methodClass = method.getReturnType();
        Class<?> existingClass = existing.getReturnType();
        if (existingClass.isAssignableFrom(methodClass))
        {
            getters.put(propName, method);
            return;
        }
    }


    private void saveSetter(String propName, Method method)
    {
        if (setAccessible)
            method.setAccessible(true);

        Method existing = setters.get(propName);
        if (existing == null)
        {
            setters.put(propName, method);
            return;
        }

        Class<?> methodClass = method.getDeclaringClass();
        Class<?> existingClass = existing.getDeclaringClass();
        if (!existingClass.isAssignableFrom(methodClass))
            return; // existing is subclass, keep it

        if (methodClass != existingClass)
        {
            // existing is superclass, take subclass
            setters.put(propName, method);
            return;
        }

        if (setterRank(method) < setterRank(existing))
        {
            setters.put(propName, method);
            return;
        }
    }


    private static int setterRank(Method method)
    {
        Class<?> parmClass = method.getParameterTypes()[0];
        if (parmClass.isPrimitive())
            return 1;
        if (Number.class.isAssignableFrom(parmClass))
            return 2;
        if (String.class.isAssignableFrom(parmClass))
            return 3;
        return 4;
    }
}
