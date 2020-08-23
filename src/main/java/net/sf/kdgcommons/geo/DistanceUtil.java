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


/**
 *  Utilities to calculate distance between two points on a sphere, nominally
 *  Earth. Provides Pythagorean distance (with corrected longitude length) for
 *  short measurements (up to a few kilometers), and Great Circle distance for
 *  longer measurements.
 */
public class DistanceUtil
{
    /**
     *  The base length of a degree in meters, used for Pythagorean diestance
     *  calculations. This is the length of a degree of latitude at 45 N/S as
     *  given by https://en.wikipedia.org/wiki/Latitude
     */
    public final static double EARTH_DEGREE_LENGTH = 111132;


    /**
     *  The mean Earth radius, from https://en.wikipedia.org/wiki/Great-circle_distance.
     */
    public final static double EARTH_RADIUS = 6371000;


    /**
     *  Corrects the length of a degree of longitude based on the measured latitude
     *  (specified in degrees, not radians).
     */
    public static double correctedLongitude(double baseDegreeLength, double atLatitude)
    {
        return baseDegreeLength * Math.cos(Math.toRadians(atLatitude));
    }


    /**
     *  Calculates the Pythagorean distance between two points, with arbitrary
     *  base degree length.
     */
    public static double pythagorean(double lat1, double lon1, double lat2, double lon2, double baseDegreeLength)
    {
        double dLat = baseDegreeLength * (lat2 - lat1);
        double dLon = correctedLongitude(baseDegreeLength * (lon2 - lon1), lat1);
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }


    /**
     *  Calculates the Pythagorean distance between two points on Earth.
     */
    public static double pythagorean(double lat1, double lon1, double lat2, double lon2)
    {
        return pythagorean(lat1, lon1, lat2, lon2, EARTH_DEGREE_LENGTH);
    }


    /**
     *  Calculates the Pythagorean distance between two points on Earth.
     */
    public static double pythagorean(Point p1, Point p2)
    {
        return pythagorean(p1.getLat(), p1.getLon(), p2.getLat(), p2.getLon());
    }


    /**
     *  Calculates the Great Circle distance between two points, with arbitrary
     *  sphere radius.
     */
    public static double greatCircle(double lat1, double lon1, double lat2, double lon2, double radius)
    {
        double lat1R = Math.toRadians(lat1);
        double lat2R = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2) - Math.toRadians(lon1);
        double n1 = Math.cos(lat2R) * Math.sin(dLon);
        double n2 = Math.cos(lat1R) * Math.sin(lat2R) - Math.sin(lat1R) * Math.cos(lat2R) * Math.cos(dLon);
        double d  = Math.sin(lat1R) * Math.sin(lat2R) + Math.cos(lat1R) * Math.cos(lat2R) * Math.cos(dLon);
        double sigma = Math.atan(Math.sqrt(n1 * n1 + n2 * n2) / d);
        return sigma * radius;
    }


    /**
     *  Calculates the Great Circle distance between two points on the Earth.
     */
    public static double greatCircle(double lat1, double lon1, double lat2, double lon2)
    {
        return greatCircle(lat1, lon1, lat2, lon2, EARTH_RADIUS);
    }


    /**
     *  Calculates the Great Circle distance between two points on the Earth.
     */
    public static double greatCircle(Point p1, Point p2)
    {
        return greatCircle(p1.getLat(), p1.getLon(), p2.getLat(), p2.getLon());
    }
}
