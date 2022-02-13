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

package com.kdgregory.kdgcommons.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 *  Instances of this class act as factories for stub objects that always throw.
 *  It can be used in one of two ways: throwing an explicit exception instance,
 *  or creating a new exception of a given type. The latter is useful when you
 *  just need to verify that an exception was thrown, the former when you need
 *  to verify that a certain path threw (use <code>assertSame()</code> to test).
 *  <p>
 *  To use, construct around the desired exception, and call {@link #getInstance}
 *  to create the stub. Since this class does not maintain any state (other than
 *  the configured exception), you can create many stubs from a single mock.
 */
public class ExceptionMock
implements InvocationHandler
{
    private Throwable specific;
    private Class<? extends Throwable> generic;


    /**
     *  Creates a factory for stub objects that will always throw a specific
     *  exception.
     */
    public ExceptionMock(Throwable ex)
    {
        this.specific = ex;
    }


    /**
     *  Creates a factory for stub objects that will throw new instances of the
     *  specified exception class.
     *  <p>
     *  <em>Note:</em> instances are created by reflection. If unable to create
     *  an exception instance (typically due to protection/missing class), will
     *  throw a reflection exception instead (and this will potentially be
     *  captured by the proxy and wrapped in a proxy exception).
     */
    public ExceptionMock(Class<? extends Throwable> klass)
    {
        generic = klass;
    }


    public <T> T getInstance(Class<T> classToMock)
    {
        return classToMock.cast(
                Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[] {classToMock},
                    this));
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
    {
        if (specific != null)
            throw specific;
        else
            throw generic.newInstance();
    }
}
