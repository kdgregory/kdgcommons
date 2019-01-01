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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.test.ExceptionMock;
import net.sf.kdgcommons.test.SelfMock;
import net.sf.kdgcommons.test.SimpleMock;


/**
 *  Tests for the JDBC utilities. These tests use internally-defined faux
 *  objects rather than relying on a real database (such as Hypersonic).
 */
public class TestJDBCUtil extends TestCase
{

    public TestJDBCUtil(String testName)
    {
        super(testName);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private static class ResultSetMock
    extends SelfMock<ResultSet>
    {
        private String[] columnNames;

        public int getMetaDataInvocationCount;
        public int nextInvocationCount;

        private Iterator<? extends Map<Object,Object>> rowItx;
        private Map<Object,Object> currentRow;

        public ResultSetMock(String[] columnNames, List<? extends Map<Object,Object>> data)
        {
            super(ResultSet.class);
            this.columnNames = columnNames;
            this.rowItx = data.iterator();
        }

        @SuppressWarnings("unused")
        public ResultSetMetaData getMetaData()
        {
            getMetaDataInvocationCount++;
            return new ResultSetMetaDataMock(columnNames).getInstance();
        }

        @SuppressWarnings("unused")
        public boolean next()
        {
            nextInvocationCount++;
            if (rowItx.hasNext())
            {
                currentRow = rowItx.next();
                return true;
            }
            else
            {
                return false;
            }
        }

        @SuppressWarnings("unused")
        public Object getObject(int idx)
        {
            return currentRow.get(columnNames[idx - 1]);
        }
    }


    private static class ResultSetMetaDataMock
    extends SelfMock<ResultSetMetaData>
    {
        private String[] columnNames;

        public ResultSetMetaDataMock(String[] columnNames)
        {
            super(ResultSetMetaData.class);
            this.columnNames = columnNames;
        }

        @SuppressWarnings("unused")
        public int getColumnCount()
        {
            return columnNames.length;
        }

        @SuppressWarnings("unused")
        public String getColumnName(int idx)
        {
            return columnNames[idx - 1];
        }
    }


//----------------------------------------------------------------------------
//  Test cases
//----------------------------------------------------------------------------

    public void testRetrieve() throws Exception
    {
        final List<? extends Map<Object,Object>> data = Arrays.asList(
            CollectionUtil.asMap("foo", "123",  "argle", Integer.valueOf(123)),
            CollectionUtil.asMap("foo", "baz",  "argle", Integer.valueOf(456)),
            CollectionUtil.asMap("foo", null,   "argle", Integer.valueOf(789))
        );

        ResultSetMock mock = new ResultSetMock(new String[] { "foo", "argle" }, data);
        List<Map<String,Object>> result = JDBCUtil.retrieve(mock.getInstance());

        assertEquals("result equivalent to source data",    data,   result);
        assertEquals("getMetaData() invocation count",      1,      mock.getMetaDataInvocationCount);
        assertEquals("next() invocation count",             4,      mock.nextInvocationCount);

    }


    public void testCloseQuietly()
    throws Exception
    {
        SimpleMock cxtMock = new SimpleMock();
        Connection cxt = cxtMock.getInstance(Connection.class);
        JDBCUtil.closeQuietly(cxt);
        cxtMock.assertCallCount(1);
        cxtMock.assertCall(0, "close");

        SimpleMock stmtMock = new SimpleMock();
        Statement stmt = stmtMock.getInstance(Statement.class);
        JDBCUtil.closeQuietly(stmt);
        stmtMock.assertCallCount(1);
        stmtMock.assertCall(0, "close");

        SimpleMock rsltMock = new SimpleMock();
        ResultSet rslt = rsltMock.getInstance(ResultSet.class);
        JDBCUtil.closeQuietly(rslt);
        rsltMock.assertCallCount(1);
        rsltMock.assertCall(0, "close");
    }


    public void testCloseQuietlyWhenNull()
    throws Exception
    {
        // compiler needs us to specify the type of the argument to pick the
        // correct method ... we could just cast a null, but creating vars
        // lets us follow the same code pattern as the other two tests

        Connection cxt = null;
        JDBCUtil.closeQuietly(cxt);

        Statement stmt = null;
        JDBCUtil.closeQuietly(stmt);

        ResultSet rslt = null;
        JDBCUtil.closeQuietly(rslt);
    }


    public void testCloseQuietlyWithException()
    throws Exception
    {
        ExceptionMock mock = new ExceptionMock(SQLException.class);

        Connection cxt = mock.getInstance(Connection.class);
        JDBCUtil.closeQuietly(cxt);

        Statement stmt = mock.getInstance(Statement.class);
        JDBCUtil.closeQuietly(stmt);

        ResultSet rslt = mock.getInstance(ResultSet.class);
        JDBCUtil.closeQuietly(rslt);
    }
}
