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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;


public class TestSegmentUtil extends TestCase
{
    public void testPythagoreanDistance() throws Exception
    {
        assertEquals("null segment", 0.0, SegmentUtil.distance(null), 0.0);

        assertEquals("empty segment", 0.0, SegmentUtil.distance(Collections.<Point>emptyList()), 0.0);

        List<Point> segment = Arrays.asList(
                                new Point(45, 75),
                                new Point(46, 76),
                                new Point(45, 77),
                                new Point(44, 78),
                                new Point(45, 79));
        assertEquals("non-empty segment", 544428.8, SegmentUtil.distance(segment), 0.1);
    }


    public void testSimplify() throws Exception
    {
        Point p1 = new Point(39.95237, -75.16358);
        Point p2 = new Point(39.95170, -75.16369);  // 75
        Point p3 = new Point(39.95087, -75.16387);  // 93
        Point p4 = new Point(39.95008, -75.16401);  // 88
        Point p5 = new Point(39.94931, -75.16419);  // 87
        Point p6 = new Point(39.94819, -75.16451);  // 126
        Point p7 = new Point(39.94697, -75.16476);  // 137
        Point p8 = new Point(39.94556, -75.16499);  // 157
        Point p9 = new Point(39.94467, -75.16526);  // 101

        List<Point> orig    = Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9);

        List<Point> exp100  = Arrays.asList(p1, p3, p5, p6, p7, p8, p9);
        List<Point> exp300  = Arrays.asList(p1, p5, p8);
        List<Point> exp1000 = Arrays.asList(p1);

        List<Point> res0 = SegmentUtil.simplify(orig, 0);
        assertNotSame("should not return original list", orig, res0);

        assertEquals("minDistance = 0",    orig,    SegmentUtil.simplify(orig, 0));
        assertEquals("minDistance = 100",  exp100,  SegmentUtil.simplify(orig, 100));
        assertEquals("minDistance = 300",  exp300,  SegmentUtil.simplify(orig, 300));
        assertEquals("minDistance = 1000", exp1000, SegmentUtil.simplify(orig, 1000));

        assertEquals("passed null",  Collections.emptyList(), SegmentUtil.simplify(null, 0));
        assertEquals("passed empty", Collections.emptyList(), SegmentUtil.simplify(Collections.<Point>emptyList(), 0));
    }


    public void testAlignSimple() throws Exception
    {
        // in this test, the "B" points vary by a few meters from the "A" points
        // so the segments should align perfectly

        Point p1a = new Point(39.95237, -75.16358);
        Point p1b = new Point(39.95239, -75.16356);

        Point p2a = new Point(39.95170, -75.16369);
        Point p2b = new Point(39.95172, -75.16371);

        Point p3a = new Point(39.95087, -75.16387);
        Point p3b = new Point(39.95084, -75.16382);

        Point p4a = new Point(39.95008, -75.16401);
        Point p4b = new Point(39.95007, -75.16400);

        Point p5a = new Point(39.94931, -75.16419);
        Point p5b = new Point(39.94935, -75.16416);

        Point p6a = new Point(39.94819, -75.16451);
        Point p6b = new Point(39.94814, -75.16452);

        Point p7a = new Point(39.94697, -75.16476);
        Point p7b = new Point(39.94699, -75.16475);

        Point p8a = new Point(39.94556, -75.16499);
        Point p8b = new Point(39.94558, -75.16492);

        Point p9a = new Point(39.94467, -75.16526);
        Point p9b = new Point(39.94466, -75.16522);

        List<Point> segmentA = Arrays.asList(p1a, p2a, p3a, p4a, p5a, p6a, p7a, p8a, p9a);
        List<Point> segmentB = Arrays.asList(p1b, p2b, p3b, p4b, p5b, p6b, p7b, p8b, p9b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 10, 100);

        assertEquals("matched all points", segmentA.size(), result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]", segmentA.get(ii), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]", segmentB.get(ii), result.get(ii)[1]);
        }
    }


    public void testFirstSegmentShorter() throws Exception
    {
        // in this test, the "B" segment has excess points, which are ignored

        Point p1a = new Point(39.95237, -75.16358);
        Point p1b = new Point(39.95239, -75.16356);

        Point p2a = new Point(39.95170, -75.16369);
        Point p2b = new Point(39.95172, -75.16371);

        Point p3a = new Point(39.95087, -75.16387);
        Point p3b = new Point(39.95084, -75.16382);

        Point px = new Point(39.94900, -75.16356);
        Point py = new Point(39.94800, -75.16356);
        Point pz = new Point(39.98700, -75.16356);

        List<Point> segmentA = Arrays.asList(p1a, p2a, p3a);
        List<Point> segmentB = Arrays.asList(p1b, p2b, p3b, px, py, pz);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 10, 100);

        assertEquals("matched all points", 3, result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]", segmentA.get(ii), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]", segmentB.get(ii), result.get(ii)[1]);
        }
    }


    public void testSecondSegmentShorter() throws Exception
    {
        // in this test, the "A" segment has excess points, which are ignored

        Point p1a = new Point(39.95237, -75.16358);
        Point p1b = new Point(39.95239, -75.16356);

        Point p2a = new Point(39.95170, -75.16369);
        Point p2b = new Point(39.95172, -75.16371);

        Point p3a = new Point(39.95087, -75.16387);
        Point p3b = new Point(39.95084, -75.16382);

        Point px = new Point(39.94900, -75.16356);
        Point py = new Point(39.94800, -75.16356);
        Point pz = new Point(39.98700, -75.16356);

        List<Point> segmentA = Arrays.asList(p1a, p2a, p3a, px, py, pz);
        List<Point> segmentB = Arrays.asList(p1b, p2b, p3b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 10, 100);

        assertEquals("matched all points", 3, result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]", segmentA.get(ii), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]", segmentB.get(ii), result.get(ii)[1]);
        }
    }


    public void testFindStart1() throws Exception
    {
        // in this test, the 1st "A" point corresponds to the 4th "B" point

        Point px = new Point(39.95600, -75.16356);
        Point py = new Point(39.95500, -75.16356);
        Point pz = new Point(39.95400, -75.16356);

        Point p1a = new Point(39.95237, -75.16358);
        Point p1b = new Point(39.95239, -75.16356);

        Point p2a = new Point(39.95170, -75.16369);
        Point p2b = new Point(39.95172, -75.16371);

        Point p3a = new Point(39.95087, -75.16387);
        Point p3b = new Point(39.95084, -75.16382);

        List<Point> segmentA = Arrays.asList(p1a, p2a, p3a);
        List<Point> segmentB = Arrays.asList(px, py, pz, p1b, p2b, p3b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 10, 100);

        assertEquals("matched all points", 3, result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]", segmentA.get(ii), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]", segmentB.get(ii + 3), result.get(ii)[1]);
        }
    }


    public void testFindStart2() throws Exception
    {
        // in this test, the 4th "A" point corresponds to the 1st "B" point

        Point px = new Point(39.95600, -75.16356);
        Point py = new Point(39.95500, -75.16356);
        Point pz = new Point(39.95400, -75.16356);

        Point p1a = new Point(39.95237, -75.16358);
        Point p1b = new Point(39.95239, -75.16356);

        Point p2a = new Point(39.95170, -75.16369);
        Point p2b = new Point(39.95172, -75.16371);

        Point p3a = new Point(39.95087, -75.16387);
        Point p3b = new Point(39.95084, -75.16382);

        List<Point> segmentA = Arrays.asList(px, py, pz, p1a, p2a, p3a);
        List<Point> segmentB = Arrays.asList(p1b, p2b, p3b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 10, 100);

        assertEquals("matched all points", 3, result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]", segmentA.get(ii + 3), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]", segmentB.get(ii), result.get(ii)[1]);
        }
    }


    public void testMinimumIncrement() throws Exception
    {
        // in this test every "A" point has a friend that's less than the
        // minimum incremental distance, so should be skipped

        Point p1a  = new Point(39.95237, -75.16358);
        Point p1ax = new Point(39.95200, -75.16358);
        Point p1b  = new Point(39.95239, -75.16356);

        Point p2a  = new Point(39.95170, -75.16369);
        Point p2ax = new Point(39.95140, -75.16369);
        Point p2b  = new Point(39.95172, -75.16371);

        Point p3a  = new Point(39.95087, -75.16387);
        Point p3ax = new Point(39.95047, -75.16387);
        Point p3b  = new Point(39.95084, -75.16382);

        Point p4a  = new Point(39.95008, -75.16401);
        Point p4ax = new Point(39.94988, -75.16401);
        Point p4b  = new Point(39.95007, -75.16400);

        Point p5a  = new Point(39.94931, -75.16419);
        Point p5ax = new Point(39.94911, -75.16419);
        Point p5b  = new Point(39.94935, -75.16416);

        Point p6a = new Point(39.94819, -75.16451);
        Point p6b = new Point(39.94814, -75.16452);

        List<Point> segmentA = Arrays.asList(p1a, p1ax, p2a, p2ax, p3a, p3ax, p4a, p4ax, p5a, p5ax, p6a);
        List<Point> segmentB = Arrays.asList(p1b,       p2b,       p3b,       p4b,       p5b,       p6b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 50, 100);

        assertEquals("matched all points", segmentB.size(), result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii * 2 + "]", segmentA.get(ii * 2), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]",     segmentB.get(ii),     result.get(ii)[1]);
        }
    }


    public void testClosestPartner() throws Exception
    {
        // in this test there are two "B" points for each "A", and one of them
        // is closer than the other

        Point p1a  = new Point(39.95237, -75.16358);
        Point p1b  = new Point(39.95239, -75.16356);

        Point p2a  = new Point(39.95170, -75.16369);
        Point p2bx = new Point(39.95200, -75.16358);
        Point p2b  = new Point(39.95172, -75.16371);

        Point p3a  = new Point(39.95087, -75.16387);
        Point p3bx = new Point(39.95140, -75.16369);
        Point p3b  = new Point(39.95084, -75.16382);

        Point p4a  = new Point(39.95008, -75.16401);
        Point p4bx = new Point(39.95047, -75.16387);
        Point p4b  = new Point(39.95007, -75.16400);

        Point p5a  = new Point(39.94931, -75.16419);
        Point p5bx = new Point(39.94988, -75.16401);
        Point p5b  = new Point(39.94935, -75.16416);

        Point p6a = new Point(39.94819, -75.16451);
        Point p6bx = new Point(39.94911, -75.16419);
        Point p6b = new Point(39.94814, -75.16452);

        List<Point> segmentA = Arrays.asList(p1a,       p2a,       p3a,       p4a,       p5a,       p6a);
        List<Point> segmentB = Arrays.asList(p1b, p2bx, p2b, p3bx, p3b, p4bx, p4b, p5bx, p5b, p6bx, p6b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 50, 100);

        assertEquals("matched all points", segmentA.size(), result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]",     segmentA.get(ii), result.get(ii)[0]);
            assertEquals("segmentB[" + ii * 2 + "]", segmentB.get(ii * 2),     result.get(ii)[1]);
        }
    }


    public void testDivergenceSimple() throws Exception
    {
        // P4 A/B diverge by less than 100 meters, P5 by more, and P6 rejoin but should
        // be ignored

        Point p1a = new Point(39.95237, -75.16358);
        Point p1b = new Point(39.95239, -75.16356);

        Point p2a = new Point(39.95170, -75.16369);
        Point p2b = new Point(39.95172, -75.16371);

        Point p3a = new Point(39.95087, -75.16387);
        Point p3b = new Point(39.95084, -75.16382);

        Point p4a = new Point(39.95008, -75.16401);
        Point p4b = new Point(39.95007, -75.16350);

        Point p5a = new Point(39.95008, -75.16475);
        Point p5b = new Point(39.95007, -75.16310);

        Point p6a = new Point(39.94819, -75.16451);
        Point p6b = new Point(39.94814, -75.16452);

        List<Point> segmentA = Arrays.asList(p1a, p2a, p3a, p4a, p5a, p6a);
        List<Point> segmentB = Arrays.asList(p1b, p2b, p3b, p4b, p5b, p6b);

        List<Point[]> result = SegmentUtil.align(segmentA, segmentB, 10, 100);

        assertEquals("matched all points", 4, result.size());
        for (int ii = 0 ; ii < result.size() ; ii++)
        {
            assertEquals("segmentA[" + ii + "]", segmentA.get(ii), result.get(ii)[0]);
            assertEquals("segmentB[" + ii + "]", segmentB.get(ii), result.get(ii)[1]);
        }
    }
}
