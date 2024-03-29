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

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestExceptionMock
{
    @Test
    @SuppressWarnings("resource")
    public void testThrowExplicitInstance() throws Exception
    {
        Exception myException = new IOException();
        ExceptionMock mock = new ExceptionMock(myException);
        Closeable stub = mock.getInstance(Closeable.class);

        try
        {
            stub.close();
            fail("did not throw");
        }
        catch (IOException ex)
        {
            assertSame("threw different exception instance", myException, ex);
        }
    }


    @Test
    @SuppressWarnings("resource")
    public void testThrowNewInstance() throws Exception
    {
        ExceptionMock mock = new ExceptionMock(IOException.class);
        Closeable stub = mock.getInstance(Closeable.class);

        try
        {
            stub.close();
            fail("did not throw");
        }
        catch (IOException ex)
        {
            // success
        }
    }
}
