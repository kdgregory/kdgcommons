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

import junit.framework.Assert;


/**
 *  Static assertion methods for numeric values (any data type).
 */
public class NumericAsserts
{
    /**
     *  Asserts that the actual value is within the expected, plus/minus the
     *  specified percentage. This is a reasonable approach to probabilistic
     *  testing.
     */
    public static void assertApproximate(int expected, int actual, int deltaPercent)
    {
        int delta = (int)(((long)expected * deltaPercent) / 100);
        assertInRange(expected - delta, expected + delta, actual);
    }

    /**
     *  Asserts that the actual value is within the expected, plus/minus the
     *  specified percentage. This is a reasonable approach to probabilistic
     *  testing.
     */
    public static void assertApproximate(long expected, long actual, int deltaPercent)
    {
        // to avoid overflow, we swap the divide and multiply depending on the size of
        // the value -- assumption is that range of error is minimal compared to delta
        long delta = (expected > Integer.MAX_VALUE * 100)
                   ? (expected / 100) * deltaPercent
                   : (expected * deltaPercent) / 100;
        assertInRange(expected - delta, expected + delta, actual);
    }

    /**
     *  Asserts that the actual value is within the expected, plus/minus the
     *  specified percentage. This is a reasonable approach to probabilistic
     *  testing.
     */
    public static void assertApproximate(double expected, double actual, double deltaPercent)
    {
        // to avoid overflow, we swap the divide and multiply depending on the size of
        // the value -- assumption is that range of error is minimal compared to delta
        double delta = (expected * deltaPercent) / 100;
        assertInRange(expected - delta, expected + delta, actual);
    }

    /**
     *  Asserts that the actual value is within an arbitrary range +/- the expected value.
     */
    public static void assertInRange(int expectedLow, int expectedHigh, int actual)
    {
        Assert.assertTrue("expected >= " + expectedLow + ", was " + actual, actual >= expectedLow);
        Assert.assertTrue("expected <= " + expectedHigh + ", was " + actual, actual <= expectedHigh);
    }

    /**
     *  Asserts that the actual value is within an arbitrary range +/- the expected value.
     */
    public static void assertInRange(long expectedLow, long expectedHigh, long actual)
    {
        Assert.assertTrue("expected >= " + expectedLow + ", was " + actual, actual >= expectedLow);
        Assert.assertTrue("expected <= " + expectedHigh + ", was " + actual, actual <= expectedHigh);
    }

    /**
     *  Asserts that the actual value is within an arbitrary range +/- the expected value.
     */
    public static void assertInRange(double expectedLow, double expectedHigh, double actual)
    {
        Assert.assertTrue("expected >= " + expectedLow + ", was " + actual, actual >= expectedLow);
        Assert.assertTrue("expected <= " + expectedHigh + ", was " + actual, actual <= expectedHigh);
    }
}
