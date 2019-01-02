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

import java.sql.*;
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
     *  Executes a query and returns the results, ensuring that the created statement
     *  and resultset are closed.
     *
     *  @param  args    Parameters for the query. May be empty.
     *
     *  @return A list of maps, where each entry in the list represents a row from the
     *          results, and the keys in the map represent the column names.
     */
    public static List<Map<String,Object>> executeQuery(Connection cxt, String sql, Object... args)
    throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rslt = null;
        try
        {
            stmt = prepare(cxt, sql, args);
            rslt = stmt.executeQuery();
            return retrieve(rslt);
        }
        finally
        {
            closeQuietly(stmt);
            closeQuietly(rslt);
        }
    }


    /**
     *  Executes an update and returns the results, ensuring that the created statement is closed.
     *
     *  @param  args    Parameters for the query. May be empty.
     *
     *  @return The number of rows updated by this statement.
     */
    public static int executeUpdate(Connection cxt, String sql, Object... args)
    throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = prepare(cxt, sql, args);
            return stmt.executeUpdate();
        }
        finally
        {
            closeQuietly(stmt);
        }
    }


    /**
     *  Creates a <code>PreparedStatement</code> from the provided connection.
     *
     *  @param  args    Parameters for the query. May be empty.
     */
    public static PreparedStatement prepare(Connection cxt, String sql, Object... args)
    throws SQLException
    {
        PreparedStatement stmt = cxt.prepareStatement(sql);
        for (int ii = 0 ; ii < args.length ; ii++)
        {
            stmt.setObject(ii + 1, args[ii]);
        }
        return stmt;
    }


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
