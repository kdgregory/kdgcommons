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

package net.sf.kdgcommons.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *  A collection of utility methods for working at the JDBC level.
 */
public class JDBCUtil
{
    /**
     *  Iterates through the passed <code>ResultSet</code>, converting each row into a
     *  <code>Map</code>, where keys are the column names as retrieved from metadata,
     *  and values are the result of calling <code>getObject()</code>.
     *  <p>
     *  Caller is responsible for closing the <code>ResultSet</code>.
     */
    public static List<Map<String,Object>> retrieve(ResultSet rslt)
    throws SQLException
    {
        // we use a LinkedList because it has simpler memory allocation characteristics
        List<Map<String,Object>> results = new LinkedList<Map<String,Object>>();

        ResultSetMetaData meta = rslt.getMetaData();
        while (rslt.next())
        {
            Map<String,Object> row = new HashMap<String,Object>();
            for (int ii = 1 ; ii <= meta.getColumnCount() ; ii++)
            {
                row.put(meta.getColumnName(ii), rslt.getObject(ii));
            }
            results.add(row);
        }

        return results;
    }


    /**
     *  Closes the passed <code>Connection</code> ignoring exceptions. This is usually
     *  called in a <code>finally</code> block, and throwing an exception there would
     *  overwrite any exception thrown by the main code (and if there is an exception
     *  when closing, there probably was one there as well).
     */
    public static void closeQuietly(Connection cxt)
    {
        if (cxt != null)
        {
            try
            {
                cxt.close();
            }
            catch (SQLException ignored)
            { /* nothing here */ }
        }
    }


    /**
     *  Closes the passed <code>ResultSet</code> ignoring exceptions. This is usually
     *  called in a <code>finally</code> block, and throwing an exception there would
     *  overwrite any exception thrown by the main code (and if there is an exception
     *  when closing, there probably was one there as well).
     */
    public static void closeQuietly(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException ignored)
            { /* nothing here */ }
        }
    }


    /**
     *  Closes the passed <code>ResultSet</code> ignoring exceptions. This is usually
     *  called in a <code>finally</code> block, and throwing an exception there would
     *  overwrite any exception thrown by the main code (and if there is an exception
     *  when closing, there probably was one there as well).
     */
    public static void closeQuietly(ResultSet rslt)
    {
        if (rslt != null)
        {
            try
            {
                rslt.close();
            }
            catch (SQLException ignored)
            { /* nothing here */ }
        }
    }
}
