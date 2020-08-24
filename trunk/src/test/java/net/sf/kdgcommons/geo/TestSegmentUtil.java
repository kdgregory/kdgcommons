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
        Point p1 = new Point(39.95237313052457,-75.16358214083702);
        Point p2 = new Point(39.95170387355903,-75.1636974783742);  // 75
        Point p3 = new Point(39.95087794725416,-75.16387676482816); // 93
        Point p4 = new Point(39.9500847988068,-75.16401850478087);  // 88
        Point p5 = new Point(39.94931118735456,-75.16419709380426); // 87
        Point p6 = new Point(39.94819763489783,-75.16451189922911); // 126
        Point p7 = new Point(39.9469767093315,-75.16476658678573);  // 137
        Point p8 = new Point(39.94556761759691,-75.16499357774308); // 157
        Point p9 = new Point(39.94467629798356,-75.16526082455496); // 101

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
}
