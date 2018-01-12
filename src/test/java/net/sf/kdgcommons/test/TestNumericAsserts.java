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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;


public class TestNumericAsserts extends TestCase
{
    public void testAssertApproximateInt() throws Exception
    {
        NumericAsserts.assertApproximate(100, 100, 0);
        NumericAsserts.assertApproximate(100, 101, 1);
        NumericAsserts.assertApproximate(100,  99, 1);

        AssertionFailedError last = null;
        try
        {
            NumericAsserts.assertApproximate(100, 98, 1);
        }
        catch (AssertionFailedError ee)
        {
            last = ee;
        }
        assertNotNull("did not assert for < delta %", last);

        try
        {
            NumericAsserts.assertApproximate(100, 102, 1);
        }
        catch (AssertionFailedError ee)
        {
            last = ee;
        }
        assertNotNull("did not assert for > delta %", last);
    }


    public void testAssertApproximateLong() throws Exception
    {
        NumericAsserts.assertApproximate(100L, 100L, 0);
        NumericAsserts.assertApproximate(100L, 101L, 1);
        NumericAsserts.assertApproximate(100L,  99L, 1);

        AssertionFailedError last = null;
        try
        {
            NumericAsserts.assertApproximate(100L, 98L, 1);
        }
        catch (AssertionFailedError ee)
        {
            last = ee;
        }
        assertNotNull("did not assert for < delta %", last);

        try
        {
            NumericAsserts.assertApproximate(100L, 102L, 1);
        }
        catch (AssertionFailedError ee)
        {
            last = ee;
        }
        assertNotNull("did not assert for > delta %", last);
    }


    public void testAssertApproximateDouble() throws Exception
    {
        NumericAsserts.assertApproximate(100.0, 100.0, 0);
        NumericAsserts.assertApproximate(100.0, 101.0, 1);
        NumericAsserts.assertApproximate(100.0,  99.0, 1);

        AssertionFailedError last = null;
        try
        {
            NumericAsserts.assertApproximate(100.0, 98.0, 1);
        }
        catch (AssertionFailedError ee)
        {
            last = ee;
        }
        assertNotNull("did not assert for < delta %", last);

        try
        {
            NumericAsserts.assertApproximate(100.0, 102.0, 1);
        }
        catch (AssertionFailedError ee)
        {
            last = ee;
        }
        assertNotNull("did not assert for > delta %", last);
    }
}
