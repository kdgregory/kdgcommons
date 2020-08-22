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

package net.sf.kdgcommons.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import net.sf.kdgcommons.test.StringAsserts;


public class TestTimestampedPoint extends TestCase
{
    public void testConstructor() throws Exception
    {
        TimestampedPoint p1 = new TimestampedPoint(123456789012L, 39.95229, -75.1657517);
        assertEquals("timestamp", 123456789012L, p1.getTimestamp());
        assertEquals("lat",        39.95229,     p1.getLat(), 0.00001);
        assertEquals("lon",       -75.1657517,   p1.getLon(), 0.00001);

        try
        {
            new TimestampedPoint(-1, 0, 0);
            fail("accepted timestamp < 0");
        }
        catch (IllegalArgumentException ex)
        {
            StringAsserts.assertRegex("invalid timestamp.*-1.*", ex.getMessage());
        }
    }


    public void testHashcodeAndEquals() throws Exception
    {
        TimestampedPoint p1 = new TimestampedPoint(123456789012L, 39.95229, -75.1657517);
        TimestampedPoint p2 = new TimestampedPoint(123456789012L, 39.95229, -75.1657517);
        TimestampedPoint p3 = new TimestampedPoint(123456789013L, 39.95229, -75.1657517);
        TimestampedPoint p4 = new TimestampedPoint(123456789012L, 42.3554833,-71.0669649);

        assertTrue("identity", p1.equals(p1));
        assertTrue("equality", p1.equals(p2));
        assertFalse("inequality of timestamp", p1.equals(p3));
        assertFalse("inequality of location",  p1.equals(p4));

        assertEquals("hashcode of equal points", p1.hashCode(), p2.hashCode());

        // this has been observed to be true
        assertTrue("hashcode of unequal points", p1.hashCode() != p4.hashCode());
    }


    public void testToString() throws Exception
    {
        TimestampedPoint p = new TimestampedPoint(123456789012L, 39.95229, -75.1657517);

        // note that there can be additional digits due to floating-point conversion
        StringAsserts.assertRegex("\\(123456789012: 39.95229\\d*,-75.1657517\\d*\\)", p.toString());
    }


    public void testComparable() throws Exception
    {
        // rather than individually test points, I'll put all possibilities in a list
        // and let sort() figure it out (note this requires a stable sort for the
        // equal points)

        TimestampedPoint p1 = new TimestampedPoint(123456789012L, -1, 0);
        TimestampedPoint p2 = new TimestampedPoint(123456789012L, 0, 0);
        TimestampedPoint p3 = new TimestampedPoint(123456789012L, 0, 0);
        TimestampedPoint p4 = new TimestampedPoint(123456789012L, 1, -1);
        TimestampedPoint p5 = new TimestampedPoint(123456789012L, 1, 0);
        TimestampedPoint p6 = new TimestampedPoint(123456789012L, 1, 1);
        TimestampedPoint p7 = new TimestampedPoint(123456789013L, 0, 0);

        List<TimestampedPoint> unordered = Arrays.asList(p7, p6, p5, p4, p3, p2, p1);
        List<TimestampedPoint> ordered = new ArrayList<TimestampedPoint>(unordered);
        Collections.sort(ordered);
        assertEquals(Arrays.asList(p1, p2, p3, p4, p5, p6, p7), ordered);
    }
}
