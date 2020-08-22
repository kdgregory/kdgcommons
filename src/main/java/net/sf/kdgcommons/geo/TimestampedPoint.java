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
 *  An extension of {@link Point} that adds a timestamp.
 */
public class TimestampedPoint
extends Point
{
    private long timestamp;


    public TimestampedPoint(long timestamp, double lat, double lon)
    {
        super(lat, lon);

        if (timestamp < 0)
            throw new IllegalArgumentException("invalid timestamp: " + timestamp);
        this.timestamp = timestamp;
    }


    public long getTimestamp()
    {
        return timestamp;
    }


    @Override
    public int hashCode()
    {
        return Long.hashCode(timestamp) * 31 + super.hashCode();
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof TimestampedPoint)
        {
            TimestampedPoint that = (TimestampedPoint)obj;
            return this.timestamp == that.timestamp
                && super.equals(obj);
        }

        return super.equals(obj);
    }


    /**
     *  String representation is "(timestamp: lat, lon)".
     */
    @Override
    public String toString()
    {
        return "(" + getTimestamp() + ": " + getLat() + "," + getLon() + ")";
    }


    /**
     *  Instances are primarily compared based on timestamp, with fallback to
     *  <code>Point.compareTo()</code>. This method takes a <code>Point</code>
     *  because Java only allows one interface parameterization; this means
     *  that you can sort arbitrary mixtures of points and timestamped points.
     */
    @Override
    public int compareTo(Point that)
    {
        if (that instanceof TimestampedPoint)
        {
            long thisTimestamp = getTimestamp();
            long thatTimestamp = ((TimestampedPoint)that).getTimestamp();
            return (thisTimestamp > thatTimestamp) ? 1
                 : (thisTimestamp < thatTimestamp) ? -1
                 : super.compareTo(that);
        }
        else
        {
            return super.compareTo(that);
        }
    }
}
