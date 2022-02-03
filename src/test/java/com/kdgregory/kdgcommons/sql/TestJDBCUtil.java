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

package com.kdgregory.kdgcommons.sql;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.kdgcommons.collections.CollectionUtil;
import com.kdgregory.kdgcommons.test.ExceptionMock;
import com.kdgregory.kdgcommons.test.SelfMock;
import com.kdgregory.kdgcommons.test.SimpleMock;


/**
 *  Tests for the JDBC utilities. These tests use internally-defined faux
 *  objects rather than relying on a real database (such as Hypersonic).
 */
public class TestJDBCUtil
{

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private final static List<? extends Map<Object,Object>> SAMPLE_DATA = Arrays.asList(
        CollectionUtil.asMap("foo", "123",  "argle", Integer.valueOf(123)),
        CollectionUtil.asMap("foo", "baz",  "argle", Integer.valueOf(456)),
        CollectionUtil.asMap("foo", null,   "argle", Integer.valueOf(789))
    );

    private final static String[] SAMPLE_DATA_COLNAMES = new String[] { "foo", "argle" };


    private static class MockConnection
    extends SelfMock<Connection>
    {
        public boolean isOpen = true;
        public String lastPrepareSql;
        public MockPreparedStatement lastPrepareMock;

        public MockConnection()
        {
            super(Connection.class);
        }

        @SuppressWarnings("unused")
        public void close()
        {
            isOpen = false;
        }

        @SuppressWarnings("unused")
        public PreparedStatement prepareStatement(String sql)
        {
            lastPrepareSql = sql;
            lastPrepareMock = new MockPreparedStatement();
            return lastPrepareMock.getInstance();
        }
    }


    private static class MockPreparedStatement
    extends SelfMock<PreparedStatement>
    {
        // these can be changed if necessary
        public String[] queryColumnNames = SAMPLE_DATA_COLNAMES;
        public List<? extends Map<Object,Object>> queryData = SAMPLE_DATA;
        public int rowsUpdated = 1;

        // note: first element will always be null; objects are stored
        //       at the index specified in the call
        public ArrayList<Object> parameters = new ArrayList<Object>();
        public boolean isOpen = true;
        public MockResultSet lastResultMock;

        public MockPreparedStatement()
        {
            super(PreparedStatement.class);
        }

        @SuppressWarnings("unused")
        public void close()
        {
            isOpen = false;
        }

        @SuppressWarnings("unused")
        public int executeUpdate()
        {
            return rowsUpdated;
        }

        @SuppressWarnings("unused")
        public ResultSet executeQuery()
        {
            lastResultMock = new MockResultSet(queryColumnNames, queryData);
            return lastResultMock.getInstance();
        }

        @SuppressWarnings("unused")
        public void setObject(int idx, Object obj)
        {
            if (parameters.size() > idx)
                parameters.set(idx, obj);
            else
            {
                while (parameters.size() < idx)
                    parameters.add(null);
                parameters.add(obj);
            }
        }
    }


    private static class MockResultSet
    extends SelfMock<ResultSet>
    {
        private String[] columnNames;

        public int getMetaDataInvocationCount;
        public int nextInvocationCount;
        public boolean isOpen = true;

        private Iterator<? extends Map<Object,Object>> rowItx;
        private Map<Object,Object> currentRow;

        public MockResultSet(String[] columnNames, List<? extends Map<Object,Object>> data)
        {
            super(ResultSet.class);
            this.columnNames = columnNames;
            this.rowItx = data.iterator();
        }

        @SuppressWarnings("unused")
        public void close()
        {
            isOpen = false;
        }

        @SuppressWarnings("unused")
        public ResultSetMetaData getMetaData()
        {
            getMetaDataInvocationCount++;
            return new MockResultSetMetaData(columnNames).getInstance();
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


    private static class MockResultSetMetaData
    extends SelfMock<ResultSetMetaData>
    {
        private String[] columnNames;

        public MockResultSetMetaData(String[] columnNames)
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

    @Test
    public void testExecuteQuery() throws Exception
    {
        final String sql = "select * from foo where bar = ?";

        MockConnection cxtMock = new MockConnection();
        List<? extends Map<String,Object>> result = JDBCUtil.executeQuery(cxtMock.getInstance(), sql, "baz");

        assertEquals("SQL",                 sql,                        cxtMock.lastPrepareSql);
        assertEquals("parameters",          Arrays.asList(null, "baz"), cxtMock.lastPrepareMock.parameters);
        assertEquals("results",             SAMPLE_DATA,                result);
        assertTrue("connection still open",                             cxtMock.isOpen);
        assertFalse("statment not open",                                cxtMock.lastPrepareMock.isOpen);
        assertFalse("resultset not open",                               cxtMock.lastPrepareMock.lastResultMock.isOpen);
    }


    @Test
    public void testExecuteUpdate() throws Exception
    {
        final String sql = "insert into foo values(?, ?)";

        MockConnection cxtMock = new MockConnection();
        int count = JDBCUtil.executeUpdate(cxtMock.getInstance(), sql, "bar", "baz");

        assertEquals("SQL",                 sql,                                cxtMock.lastPrepareSql);
        assertEquals("parameters",          Arrays.asList(null, "bar", "baz"),  cxtMock.lastPrepareMock.parameters);
        assertEquals("results",             1,                                  count);
        assertTrue("connection still open",                                     cxtMock.isOpen);
        assertFalse("statment not open",                                        cxtMock.lastPrepareMock.isOpen);
    }


    @Test
    public void testPrepare() throws Exception
    {
        final String sql = "select * from foo where bar = ?";

        MockConnection cxtMock = new MockConnection();
        PreparedStatement stmt = JDBCUtil.prepare(cxtMock.getInstance(), sql, "baz");

        assertNotNull("created statement",                              stmt);
        assertEquals("SQL",                 sql,                        cxtMock.lastPrepareSql);
        assertEquals("parameters",          Arrays.asList(null, "baz"), cxtMock.lastPrepareMock.parameters);
        assertTrue("statement open",                                    cxtMock.lastPrepareMock.isOpen);
        assertTrue("connection still open",                             cxtMock.isOpen);
    }


    @Test
    public void testRetrieve() throws Exception
    {
        MockResultSet mock = new MockResultSet(SAMPLE_DATA_COLNAMES, SAMPLE_DATA);
        List<Map<String,Object>> result = JDBCUtil.retrieve(mock.getInstance());

        assertEquals("result equivalent to source data",    SAMPLE_DATA,   result);
        assertEquals("getMetaData() invocation count",      1,      mock.getMetaDataInvocationCount);
        assertEquals("next() invocation count",             4,      mock.nextInvocationCount);
        assertTrue("resultSet still open",                          mock.isOpen);
    }


    @Test
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


    @Test
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


    @Test
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
