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


public class TestPoint extends TestCase
{
    public void testConstructor() throws Exception
    {
        Point p1 = new Point(39.95229, -75.1657517);
        assertEquals("lat",  39.95229,   p1.getLat(), 0.00001);
        assertEquals("lon", -75.1657517, p1.getLon(), 0.00001);

        // no exception means success for these two
        new Point(-90.0, -180.0);
        new Point(90.0, 180.0);

        try
        {
            new Point(-90.01, 0);
            fail("accepted latitude < -90");
        }
        catch (IllegalArgumentException ex)
        {
            StringAsserts.assertRegex("invalid lat.*-90.01.*", ex.getMessage());
        }

        try
        {
            new Point(90.01, 0);
            fail("accepted latitude > 90");
        }
        catch (IllegalArgumentException ex)
        {
            StringAsserts.assertRegex("invalid lat.*90.01.*", ex.getMessage());
        }

        try
        {
            new Point(0, -180.01);
            fail("accepted longitude < -180");
        }
        catch (IllegalArgumentException ex)
        {
            StringAsserts.assertRegex("invalid lon.*-180.01.*", ex.getMessage());
        }

        try
        {
            new Point(0, 180.01);
            fail("accepted longitude > 180");
        }
        catch (IllegalArgumentException ex)
        {
            StringAsserts.assertRegex("invalid lon.*180.01.*", ex.getMessage());
        }
    }


    public void testHashcodeAndEquals() throws Exception
    {
        Point p1 = new Point(39.95229, -75.1657517);
        Point p2 = new Point(39.95229, -75.1657517);
        Point p3 = new Point(39.95230, -75.1657517);
        Point p4 = new Point(39.95229, -75.165752);

        assertTrue("identity", p1.equals(p1));
        assertTrue("equality", p1.equals(p2));
        assertFalse("inequality of latitude",  p1.equals(p3));
        assertFalse("inequality of longitude", p1.equals(p4));

        assertEquals("hashcode of equal points", p1.hashCode(), p2.hashCode());

        // this has been observed to be true
        assertTrue("hashcode of unequal points", p1.hashCode() != p3.hashCode());
    }


    public void testToString() throws Exception
    {
        Point p = new Point(39.95229, -75.1657517);

        // note that there can be additional digits due to floating-point conversion
        StringAsserts.assertRegex("\\(39.95229\\d*,-75.1657517\\d*\\)", p.toString());
    }


    public void testComparable() throws Exception
    {
        // rather than individually test points, I'll put all possibilities in a list
        // and let sort() figure it out (note this requires a stable sort for the
        // equal points)

        Point p1 = new Point(-1, 0);
        Point p2 = new Point(0, 0);
        Point p3 = new Point(0, 0);
        Point p4 = new Point(1, -1);
        Point p5 = new Point(1, 0);
        Point p6 = new Point(1, 1);

        List<Point> unordered = Arrays.asList(p6, p5, p4, p3, p2, p1);
        List<Point> ordered = new ArrayList<Point>(unordered);
        Collections.sort(ordered);
        assertEquals(Arrays.asList(p1, p2, p3, p4, p5, p6), ordered);
    }
}
