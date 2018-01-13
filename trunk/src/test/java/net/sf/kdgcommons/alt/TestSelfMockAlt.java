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

package net.sf.kdgcommons.alt;

import junit.framework.TestCase;

import net.sf.kdgcommons.test.SelfMock;


public class TestSelfMockAlt extends TestCase
{
    // there was a bug where methods in anonymous classes in a different package
    // (ie, not net.sf.kdgcommons.test) were unaccessible and needed to be set
    // accessible to be invoked
    public void testAnonymousImplementationClassInDifferentPackage()
    {
        CharSequence instance = new SelfMock<CharSequence>(CharSequence.class)
        {
            @SuppressWarnings("unused")
            public int length()
            {
                return 123;
            }
        }.getInstance();

        assertEquals(123, instance.length());
    }
}
