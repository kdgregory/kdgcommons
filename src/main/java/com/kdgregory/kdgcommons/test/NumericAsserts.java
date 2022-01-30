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

import junit.framework.Assert;


/**
 *  Static assertion methods for numeric values (any data type).
 */
public class NumericAsserts
{
    private NumericAsserts()
    {
        // this is here to prevent instantiation
    }


    /**
     *  Asserts that the actual value is equal to the expected, plus or minus a
     *  specified percentage (useful for probabilistic testing).
     */
    public static void assertApproximate(int expected, int actual, int deltaPercent)
    {
        assertApproximate(null, expected, actual, deltaPercent);
    }


    /**
     *  Asserts that the actual value is equal to the expected, plus or minus a
     *  specified percentage (useful for probabilistic testing). On failure,
     *  prepends the supplied message (if any) to a description of the failure.
     */
    public static void assertApproximate(String message, int expected, int actual, int deltaPercent)
    {
        int delta = (int)(((long)expected * deltaPercent) / 100);
        assertInRange(message, expected - delta, expected + delta, actual);
    }


    /**
     *  Asserts that the actual value is equal to the expected, plus or minus a
     *  specified percentage (useful for probabilistic testing).
     */
    public static void assertApproximate(long expected, long actual, int deltaPercent)
    {
        assertApproximate(null, expected, actual, deltaPercent);
    }


    /**
     *  Asserts that the actual value is equal to the expected, plus or minus a
     *  specified percentage (useful for probabilistic testing). On failure,
     *  prepends the supplied message (if any) to a description of the failure.
     */
    public static void assertApproximate(String message, long expected, long actual, int deltaPercent)
    {
        // to avoid overflow, we swap the divide and multiply depending on the size of
        // the value -- assumption is that range of error is minimal compared to delta
        long delta = (expected > Integer.MAX_VALUE * 100)
                   ? (expected / 100) * deltaPercent
                   : (expected * deltaPercent) / 100;
        assertInRange(message, expected - delta, expected + delta, actual);
    }


    /**
     *  Asserts that the actual value is equal to the expected, plus or minus a
     *  specified percentage (useful for probabilistic testing).
     */
    public static void assertApproximate(double expected, double actual, double deltaPercent)
    {
        assertApproximate(null, expected, actual, deltaPercent);
    }


    /**
     *  Asserts that the actual value is equal to the expected, plus or minus a
     *  specified percentage (useful for probabilistic testing). On failure,
     *  prepends the supplied message (if any) to a description of the failure.
     */
    public static void assertApproximate(String message, double expected, double actual, double deltaPercent)
    {
        double delta = (expected * deltaPercent) / 100;
        assertInRange(message, expected - delta, expected + delta, actual);
    }


    /**
     *  Asserts that the actual value is within an arbitrary range.
     */
    public static void assertInRange(int expectedLow, int expectedHigh, int actual)
    {
        assertInRange(null, expectedLow, expectedHigh, actual);
    }


    /**
     *  Asserts that the actual value is within an arbitrary range. On failure,
     *  prepends the supplied message (if any) to a description of the failure.
     */
    public static void assertInRange(String message, int expectedLow, int expectedHigh, int actual)
    {
        if ((actual < expectedLow) || (actual > expectedHigh))
        {
            String baseMessage = "value not in expected range: was " + actual + ", expected between " + expectedLow + " and " + expectedHigh;
            String actualMessage = (message != null)
                                 ? message + ": " + baseMessage
                                 : baseMessage;
            Assert.fail(actualMessage);
        }
    }


    /**
     *  Asserts that the actual value is within an arbitrary range.
     */
    public static void assertInRange(long expectedLow, long expectedHigh, long actual)
    {
        assertInRange(null, expectedLow, expectedHigh, actual);
    }


    /**
     *  Asserts that the actual value is within an arbitrary range. On failure,
     *  prepends the supplied message (if any) to a description of the failure.
     */
    public static void assertInRange(String message, long expectedLow, long expectedHigh, long actual)
    {
        if ((actual < expectedLow) || (actual > expectedHigh))
        {
            String baseMessage = "value not in expected range: was " + actual + ", expected between " + expectedLow + " and " + expectedHigh;
            String actualMessage = (message != null)
                                 ? message + ": " + baseMessage
                                 : baseMessage;
            Assert.fail(actualMessage);
        }
    }


    /**
     *  Asserts that the actual value is within an arbitrary range.
     */
    public static void assertInRange(double expectedLow, double expectedHigh, double actual)
    {
        assertInRange(null, expectedLow, expectedHigh, actual);
    }


    /**
     *  Asserts that the actual value is within an arbitrary range. On failure,
     *  prepends the supplied message (if any) to a description of the failure.
     */
    public static void assertInRange(String message, double expectedLow, double expectedHigh, double actual)
    {
        if ((actual < expectedLow) || (actual > expectedHigh))
        {
            String baseMessage = "value not in expected range: was " + actual + ", expected between " + expectedLow + " and " + expectedHigh;
            String actualMessage = (message != null)
                                 ? message + ": " + baseMessage
                                 : baseMessage;
            Assert.fail(actualMessage);
        }
    }
}
