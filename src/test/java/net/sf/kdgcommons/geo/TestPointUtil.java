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

import junit.framework.TestCase;


public class TestPointUtil extends TestCase
{
    public void testCorrectedLongitude() throws Exception
    {
        // note: 100,000 chosen to be in same scale as base degree length on Earth

        assertEquals("0",   100000.0, PointUtil.correctedLongitude(100000.0,  0), 0.1);

        assertEquals("45S",  70710.7, PointUtil.correctedLongitude(100000.0, -45), 0.1);
        assertEquals("45N",  70710.7, PointUtil.correctedLongitude(100000.0,  45), 0.1);

        assertEquals("90S",      0.0, PointUtil.correctedLongitude(100000.0, -90), 0.1);
        assertEquals("90N",      0.0, PointUtil.correctedLongitude(100000.0,  90), 0.1);
    }


    public void testPythagoreanDistance() throws Exception
    {
        // verify the base calculation: as long as this is right any values should work

        double d1 = PointUtil.pythagorean(45, 75, 46, 76, 100000);
        assertEquals("base calculation", 122474.5, d1, 0.1);

        // verify the chain of alls and base Earth degree length

        Point p2a = new Point(45, 75);
        Point p2b = new Point(46, 76);
        double d2 = PointUtil.pythagorean(p2a, p2b);
        assertEquals("point calculation, on Earth", 136108.4, d2, 0.1);
    }


    public void testGreatCircleDistance() throws Exception
    {
        // rather than work through the calculation for arbitrary radius (and likely making
        // a mistake), I used an online source for Earth distance. However, the available
        // resolution for that source was kilometers, so I'm adjusted assertion to correspond

        Point p1 = new Point(-15, -15);
        Point p2 = new Point(15, 15);
        double d = PointUtil.greatCircle(p1, p2) / 1000;
        assertEquals("calculation on Earth", 4690.0, d, 0.5);
    }


    public void testVelocity() throws Exception
    {
        TimestampedPoint p1 = new TimestampedPoint(0, 45, 75);
        TimestampedPoint p2 = new TimestampedPoint(2000, 46, 76);
        assertEquals("meters/second",  68054.2, PointUtil.velocity(p1, p2), 0.1);
        assertEquals("meters/second",  152232.6, PointUtil.velocityMPH(p1, p2), 0.1);
    }


    public void testMidpoint() throws Exception
    {
        Point p1 = new Point(-1,  1);
        Point p2 = new Point( 1, -1);
        assertEquals("simple point",            new Point(0, 0), PointUtil.midpoint(p1, p2));
        assertEquals("simple point, reversed",  new Point(0, 0), PointUtil.midpoint(p2, p1));

        TimestampedPoint p3 = new TimestampedPoint(1000, -1,  1);
        TimestampedPoint p4 = new TimestampedPoint(2000,  1, -1);
        assertEquals("timestamped point",            new TimestampedPoint(1500, 0, 0), PointUtil.midpoint(p3, p4));
        assertEquals("timestamped point, reversed",  new TimestampedPoint(1500, 0, 0), PointUtil.midpoint(p4, p3));

        assertEquals("mixed",           new Point(0, 0), PointUtil.midpoint(p1, p4));
        assertEquals("mixed, reversed", new Point(0, 0), PointUtil.midpoint(p4, p1));
    }

}
