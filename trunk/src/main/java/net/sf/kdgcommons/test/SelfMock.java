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

package net.sf.kdgcommons.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;


/**
 *  A reflection-based proxy that invokes requests on itself. This class is
 *  intended to be subclassed, with the subclass implementing the methods
 *  that are to be mocked.
 *  <p>
 *  To use, construct with the interface to be mocked (only one allowed), then
 *  call {@link #getInstance} to create the proxy instance.
 *  <p>
 *
 */
public abstract class SelfMock<T>
implements InvocationHandler
{
    private Class<T> klass;

    public SelfMock(Class<T> klass)
    {
        this.klass = klass;
    }


    public T getInstance()
    {
        return klass.cast(
                Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[] {klass},
                    this));
    }


    public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
    {
        try
        {
            Method selfMethod = getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            selfMethod.setAccessible(true);
            return selfMethod.invoke(this, args);
        }
        catch (NoSuchMethodException ex)
        {
            throw new UnsupportedOperationException("mock does not implement method: " + method.getName()
                                                    + "(" + Arrays.asList(method.getParameterTypes()) + ")");
        }
        catch (SecurityException ex)
        {
            throw new RuntimeException("security exception when invoking: " + method.getName(), ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException("illegal access exception when invoking: " + method.getName(), ex);
        }
        catch (InvocationTargetException ex)
        {
            throw ex.getCause();
        }
    }
}
