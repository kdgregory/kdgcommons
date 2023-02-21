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

import org.junit.Test;
import static org.junit.Assert.*;


public class TestNumericAsserts
{
    @Test
    public void testAssertApproximateInt() throws Exception
    {
        NumericAsserts.assertApproximate(100, 100, 0);
        NumericAsserts.assertApproximate(100, 101, 1);
        NumericAsserts.assertApproximate(100,  99, 1);

        // note: we can't use fail() inside the try block (because it throws
        //       AssertionFailedError) so must capture the exception and
        //       assert on it afterward
        AssertionError lastAssertionResult;

        try
        {
            NumericAsserts.assertApproximate("example", 100, 98, 1);
            lastAssertionResult = null;
        }
        catch (AssertionError ee)
        {
            lastAssertionResult = ee;
        }
        assertNotNull("did not assert for < delta %",   lastAssertionResult);
        assertTrue("message contained passed title",    lastAssertionResult.getMessage().contains("example: "));
        assertTrue("message identified original value", lastAssertionResult.getMessage().contains("98"));
        assertTrue("message identified expected low",   lastAssertionResult.getMessage().contains("99"));
        assertTrue("message identified expected high",  lastAssertionResult.getMessage().contains("101"));

        try
        {
            NumericAsserts.assertApproximate("example", 100, 102, 1);
            lastAssertionResult = null;
        }
        catch (AssertionError ee)
        {
            lastAssertionResult = ee;
        }
        assertNotNull("did not assert for > delta %",   lastAssertionResult);
        assertTrue("message contained passed title",    lastAssertionResult.getMessage().contains("example: "));
        assertTrue("message identified original value", lastAssertionResult.getMessage().contains("102"));
        assertTrue("message identified expected low",   lastAssertionResult.getMessage().contains("99"));
        assertTrue("message identified expected high",  lastAssertionResult.getMessage().contains("101"));
    }


    @Test
    public void testAssertApproximateLong() throws Exception
    {
        NumericAsserts.assertApproximate(100L, 100L, 0);
        NumericAsserts.assertApproximate(100L, 101L, 1);
        NumericAsserts.assertApproximate(100L,  99L, 1);

        // note: we can't use fail() inside the try block (because it throws
        //       AssertionFailedError) so must capture the exception and
        //       assert on it afterward
        AssertionError lastAssertionResult;

        try
        {
            NumericAsserts.assertApproximate("example", 100L, 98L, 1);
            lastAssertionResult = null;
        }
        catch (AssertionError ee)
        {
            lastAssertionResult = ee;
        }
        assertNotNull("did not assert for < delta %",   lastAssertionResult);
        assertTrue("message contained passed title",    lastAssertionResult.getMessage().contains("example: "));
        assertTrue("message identified original value", lastAssertionResult.getMessage().contains("98"));
        assertTrue("message identified expected low",   lastAssertionResult.getMessage().contains("99"));
        assertTrue("message identified expected high",  lastAssertionResult.getMessage().contains("101"));

        try
        {
            NumericAsserts.assertApproximate("example", 100L, 102L, 1);
            lastAssertionResult = null;
        }
        catch (AssertionError ee)
        {
            lastAssertionResult = ee;
        }
        assertNotNull("did not assert for > delta %",   lastAssertionResult);
        assertTrue("message contained passed title",    lastAssertionResult.getMessage().contains("example: "));
        assertTrue("message identified original value", lastAssertionResult.getMessage().contains("102"));
        assertTrue("message identified expected low",   lastAssertionResult.getMessage().contains("99"));
        assertTrue("message identified expected high",  lastAssertionResult.getMessage().contains("101"));
    }


    @Test
    public void testAssertApproximateDouble() throws Exception
    {
        NumericAsserts.assertApproximate(100.0, 100.0, 0);
        NumericAsserts.assertApproximate(100.0, 101.0, 1);
        NumericAsserts.assertApproximate(100.0,  99.0, 1);

        // note: we can't use fail() inside the try block because it throws
        //       AssertionFailedError so must use another way to capture an
        //       assertion that was expected to fail but didn't
        boolean assertionSucceeded = false;

        try
        {
            NumericAsserts.assertApproximate("example", 100.0, 98.0, 1);
            assertionSucceeded = true;
        }
        catch (AssertionError ex)
        {
            assertTrue("message identified original value", ex.getMessage().contains("98"));
            assertTrue("message identified expected low",   ex.getMessage().contains("99"));
            assertTrue("message identified expected high",  ex.getMessage().contains("101"));
            assertTrue("message contained passed title",    ex.getMessage().contains("example: "));
        }
        assertFalse("assertion should have failed for < delta %", assertionSucceeded);

        try
        {
            NumericAsserts.assertApproximate("example", 100.0, 102.0, 1);
            assertionSucceeded = true;
        }
        catch (AssertionError ex)
        {
            assertTrue("message contained passed title",    ex.getMessage().contains("example: "));
            assertTrue("message identified original value", ex.getMessage().contains("102"));
            assertTrue("message identified expected low",   ex.getMessage().contains("99"));
            assertTrue("message identified expected high",  ex.getMessage().contains("101"));
        }
        assertFalse("assertion should have failed for > delta %", assertionSucceeded);
    }


    @Test
    public void testAssertFloatArray() throws Exception
    {
        float[] arr1  = new float[] { 1.1f, 1.2f, 1.3f };
        float[] arr1b = new float[] { 1.1f, 1.2f, 1.3f };
        float[] arr2  = new float[] { 1.1f, 1.2f };
        float[] arr3  = new float[] { 1.1f, 1.2f, 1.4f };

        NumericAsserts.assertEqual(arr1, arr1b, Float.MIN_VALUE);

        // note: we can't use fail() inside the try block because it throws
        //       AssertionFailedError so must use another way to capture an
        //       assertion that was expected to fail but didn't
        boolean assertionSucceeded = false;

        try
        {
            NumericAsserts.assertEqual("example", arr1, arr2, Float.MIN_VALUE);
            assertionSucceeded = true;
        }
        catch (AssertionError ex)
        {
            assertTrue("message contained passed title",    ex.getMessage().contains("example: "));
            assertTrue("message identified failure",        ex.getMessage().contains("arrays have different length"));
            assertTrue("message identified expected value", ex.getMessage().contains("<3>"));
            assertTrue("message identified actual value",   ex.getMessage().contains("<2>"));
        }
        assertFalse("assertion should have failed for different lengths", assertionSucceeded);

        try
        {
            NumericAsserts.assertEqual("example", arr1, arr3, Float.MIN_VALUE);
            assertionSucceeded = true;
        }
        catch (AssertionError ex)
        {
            assertTrue("message contained passed title",    ex.getMessage().contains("example: "));
            assertTrue("message identified failure",        ex.getMessage().contains("arrays differ at element 2"));
            assertTrue("message identified expected value", ex.getMessage().contains("1.3"));
            assertTrue("message identified actual value",   ex.getMessage().contains("1.4"));
        }
        assertFalse("assertion should have failed for values not within delta", assertionSucceeded);

        // same assertion, bigger delta
        NumericAsserts.assertEqual("example", arr1, arr3, 0.15f);
    }


    @Test
    public void testAssertDoubleArray() throws Exception
    {
        double[] arr1  = new double[] { 1.1, 1.2, 1.3 };
        double[] arr1b = new double[] { 1.1, 1.2, 1.3 };
        double[] arr2  = new double[] { 1.1, 1.2 };
        double[] arr3  = new double[] { 1.1, 1.2, 1.4 };

        NumericAsserts.assertEqual(arr1, arr1b, Double.MIN_VALUE);

        // note: we can't use fail() inside the try block because it throws
        //       AssertionFailedError so must use another way to capture an
        //       assertion that was expected to fail but didn't
        boolean assertionSucceeded = false;

        try
        {
            NumericAsserts.assertEqual("example", arr1, arr2, Double.MIN_VALUE);
            assertionSucceeded = true;
        }
        catch (AssertionError ex)
        {
            assertTrue("message contained passed title",    ex.getMessage().contains("example: "));
            assertTrue("message identified failure",        ex.getMessage().contains("arrays have different length"));
            assertTrue("message identified expected value", ex.getMessage().contains("<3>"));
            assertTrue("message identified actual value",   ex.getMessage().contains("<2>"));
        }
        assertFalse("assertion should have failed for different lengths", assertionSucceeded);

        try
        {
            NumericAsserts.assertEqual("example", arr1, arr3, Double.MIN_VALUE);
            assertionSucceeded = true;
        }
        catch (AssertionError ex)
        {
            assertTrue("message contained passed title",    ex.getMessage().contains("example: "));
            assertTrue("message identified failure",        ex.getMessage().contains("arrays differ at element 2"));
            assertTrue("message identified expected value", ex.getMessage().contains("1.3"));
            assertTrue("message identified actual value",   ex.getMessage().contains("1.4"));
        }
        assertFalse("assertion should have failed for values not within delta", assertionSucceeded);

        // same assertion, bigger delta
        NumericAsserts.assertEqual("example", arr1, arr3, 0.15);
    }
}
