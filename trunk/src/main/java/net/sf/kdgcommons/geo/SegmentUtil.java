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
import java.util.Iterator;
import java.util.List;

/**
 *  Functions that work with segments (aka poly-lines): lists of points.
 */
public class SegmentUtil
{
    /**
     *  Computes and sums the Pythagorean distance between the points in a segment.
     */
    public static double distance(List<Point> segment)
    {
        if ((segment == null) || segment.isEmpty())
            return 0;

        Iterator<Point> itx = segment.iterator();
        Point prev = itx.next();
        double sum = 0;
        while (itx.hasNext())
        {
            Point cur = itx.next();
            sum += DistanceUtil.pythagorean(prev, cur);
            prev = cur;
        }

        return sum;
    }


    /**
     *  Simplifies a segment by removing points that are less than a specified distance apart
     *  (using Pythagorean distance). The returned segment is a newly-created mutable list.
     */
    public static List<Point> simplify(List<Point> segment, double minDistanceMeters)
    {
        if ((segment == null) || segment.isEmpty())
            return new ArrayList<Point>();

        List<Point> result = new ArrayList<Point>(segment.size());

        Iterator<Point> itx = segment.iterator();
        Point prev = itx.next();
        result.add(prev);
        while (itx.hasNext())
        {
            Point cur = itx.next();
            if (DistanceUtil.pythagorean(prev, cur) > minDistanceMeters)
            {
                result.add(cur);
                prev = cur;
            }
        }

        return result;
    }
}
