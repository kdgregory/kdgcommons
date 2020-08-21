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
 *  Represents a point on a sphere, using latitude and longitude.
 *  <p>
 *  Instances are immutable.
 */
public class Point
implements Comparable<Point>
{
    private final double lat;
    private final double lon;


    /**
     *  @param lat  Latitude, ranging from -90 (south) to +90 (north).
     *  @param lon  Longitude, ranging from -180 (west) to +180 (east).
     */
    public Point(double lat, double lon)
    {
        if ((lat < -90.0) || (lat > 90.0))
            throw new IllegalArgumentException("invalid latitude: " + lat);

        if ((lon < -180.0) || (lon > 180.0))
            throw new IllegalArgumentException("invalid longitude: " + lon);

        this.lat = lat;
        this.lon = lon;
    }


    public double getLat()
    {
        return lat;
    }


    public double getLon()
    {
        return lon;
    }


    @Override
    public int hashCode()
    {
        return Double.hashCode(lat) * 31 + Double.hashCode(lon);
    }


    /**
     *  Two points are equal if latitude and longitude are equal. Any subclass that
     *  adds additional fields must ensure that its implementation is reflexive.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof Point)
        {
            Point that = (Point)obj;
            return this.lat == that.lat
                && this.lon == that.lon;
        }
        return false;
    }


    /**
     *  String representation of a Point is "(lat, lon)".
     */
    @Override
    public String toString()
    {
        return "(" + lat + "," + lon + ")";
    }


    /**
     *  Instances are comparable, for compatibility with {@link TimestampedPoint}. One
     *  instance is larger than another if (1) it has a greater latitude, or (2) has
     *  the same latitude and a greater longitude (ie, ordered to the northeast).
     */
    public int compareTo(Point that)
    {
        return (this.lat > that.lat) ? 1
             : (this.lat < that.lat) ? -1
             : (this.lon > that.lon) ? 1
             : (this.lon < that.lon) ? -1
             : 0;
    }
}
