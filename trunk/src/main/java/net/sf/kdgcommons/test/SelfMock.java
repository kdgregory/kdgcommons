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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.kdgcommons.util.Counters;


/**
 *  A reflection-based proxy that invokes requests on itself. This class is
 *  intended to be subclassed, with the subclass implementing the methods
 *  that are to be mocked.
 *  <p>
 *  To use, construct with the interface to be mocked (only one allowed, due to
 *  Java parameterization), then call {@link #getInstance} to create the proxy
 *  instance.
 *  <p>
 *  By default, all invocations are counted and the invocation arguments retained.
 *  See {@link #getInvocationCount} and {@link #getInvocationArgument} for more
 *  information.
 */
public abstract class SelfMock<MockedType>
implements InvocationHandler
{
    private Class<MockedType> klass;

    private Counters<String> invocationCounts = new Counters<String>();
    private ConcurrentHashMap<String,ArrayList<Object[]>> invocationArgs = new ConcurrentHashMap<String,ArrayList<Object[]>>();


    public SelfMock(Class<MockedType> klass)
    {
        this.klass = klass;
    }

//----------------------------------------------------------------------------
//  Public API
//----------------------------------------------------------------------------

    /**
     *  Returns a proxy instance that passes invocations to this mock. Multiple
     *  calls will return different proxy instances.
     */
    public MockedType getInstance()
    {
        return klass.cast(
                Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[] {klass},
                    this));
    }


    /**
     *  Returns the number of times the named function was invoked. This does
     *  not differentiate between overloaded methods: all methods with the same
     *  name is counted together.
     */
    public int getInvocationCount(String methodName)
    {
        return invocationCounts.getInt(methodName);
    }


    /**
     *  Returns the arguments passed to a particular invocation of the named method
     *  (using zero-based counting). This is primarily useful when dealing with
     *  overloaded methods, where you might not know what types the arguments are.
     *  If you know the arguments, {@link #getInvocationArgAs} is a better choice.
     *  <p>
     *  Note: this method returns the actual argument array that was passed to the
     *  invocation handler. Don't modify it unless you want to invalidate your tests.
     *
     *  @throws IndexOutOfBoundsException if accessing a call that was never made.
     */
    public Object[] getInvocationArgs(String methodName, int invocationIndex)
    {
        return invocationHistoryFor(methodName).get(invocationIndex);
    }


    /**
     *  Returns a specific invocation argument, cast to a particular type. Both
     *  indexes are zero-based.
     */
    public <T> T getInvocationArg(String methodName, int invocationIndex, int argumentIndex, Class<T> argType)
    {
        return argType.cast(getInvocationArgs(methodName, invocationIndex)[argumentIndex]);
    }


    /**
     *  Returns the arguments passed to the most recent invocation of the named
     *  method.This is primarily useful when dealing with overloaded methods,
     *  where you might not know what types the arguments are. If you know the
     *  arguments, {@link #getInvocationArgAs} is a better choice.
     *  <p>
     *  Note: this method returns the actual argument array that was passed to the
     *  invocation handler. Don't modify it unless you want to invalidate your tests.
     */
    public Object[] getMostRecentInvocationArgs(String methodName)
    {
        int index = getInvocationCount(methodName) - 1;
        return (index < 0)
             ? null
             : getInvocationArgs(methodName, index);
    }


    /**
     *  The a specific argument from the most recent invocation, cast to a particular
     *  type.
     */
    public <T> T getMostRecentInvocationArg(String methodName, int argumentIndex, Class<T> argType)
    {
        Object[] args = getMostRecentInvocationArgs(methodName);
        return (args == null)
             ? null
             : argType.cast(args[argumentIndex]);
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
    {
        String methodName = method.getName();
        invocationCounts.increment(methodName);

        synchronized (this)
        {
            invocationHistoryFor(methodName).add(args);
        }

        try
        {
            Method selfMethod = getClass().getMethod(methodName, method.getParameterTypes());
            selfMethod.setAccessible(true);
            return selfMethod.invoke(this, args);
        }
        catch (NoSuchMethodException ex)
        {
            throw new UnsupportedOperationException("mock does not implement method: " + methodName
                                                    + "(" + Arrays.asList(method.getParameterTypes()) + ")");
        }
        catch (SecurityException ex)
        {
            throw new RuntimeException("security exception when invoking: " + methodName, ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException("illegal access exception when invoking: " + methodName, ex);
        }
        catch (InvocationTargetException ex)
        {
            // this is an exception thrown by the mock instance, which is probably intentional
            throw ex.getCause();
        }
    }


    /**
     *  Returns the list of historical arguments for the named method, creating it
     *  if necessary. May be called without synchronization, although changes to the
     *  returned array should be synchronized.
     */
    private ArrayList<Object[]> invocationHistoryFor(String methodName)
    {
        ArrayList<Object[]> history = invocationArgs.get(methodName);
        if (history == null)
        {
            // this could be invoked concurrently, and someone else could create the list
            invocationArgs.putIfAbsent(methodName, new ArrayList<Object[]>());
            history = invocationArgs.get(methodName);
        }
        return history;
    }
}
