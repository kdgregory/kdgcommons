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


public class TestDistanceUtil extends TestCase
{
    public void testCorrectedLongitude() throws Exception
    {
        // note: 100,000 chosen to be in same scale as base degree length on Earth

        assertEquals("0",   100000.0, DistanceUtil.correctedLongitude(100000.0,  0), 0.1);

        assertEquals("45S",  70710.7, DistanceUtil.correctedLongitude(100000.0, -45), 0.1);
        assertEquals("45N",  70710.7, DistanceUtil.correctedLongitude(100000.0,  45), 0.1);

        assertEquals("90S",      0.0, DistanceUtil.correctedLongitude(100000.0, -90), 0.1);
        assertEquals("90N",      0.0, DistanceUtil.correctedLongitude(100000.0,  90), 0.1);
    }


    public void testPythagoreanDistance() throws Exception
    {
        // verify the base calculation: as long as this is right any values should work

        double d1 = DistanceUtil.pythagorean(45, 75, 46, 76, 100000);
        assertEquals("base calculation", 122474.5, d1, 0.1);

        // verify the chain of alls and base Earth degree length

        Point p2a = new Point(45, 75);
        Point p2b = new Point(46, 76);
        double d2 = DistanceUtil.pythagorean(p2a, p2b);
        assertEquals("point calculation, on Earth", 136108.4, d2, 0.1);
    }


    public void testGreatCircleDistance() throws Exception
    {
        // rather than work through the calculation for arbitrary radius (and likely making
        // a mistake), I used an online source for Earth distance. However, the available
        // resolution for that source was kilometers, so I'm adjusting calculated distance
        // to correspond

        Point p1 = new Point(-15, -15);
        Point p2 = new Point(15, 15);
        double d = DistanceUtil.greatCircle(p1, p2) / 1000;
        assertEquals("calculation on Earth", 4690.0, d, 0.5);
    }
}
