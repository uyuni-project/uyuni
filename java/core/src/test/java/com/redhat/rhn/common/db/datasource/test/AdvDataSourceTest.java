/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.db.datasource.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.ObjectCreateWrapperException;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AdvDataSourceTest extends RhnBaseTestCase {

    private static final String TEST_QUERIES = "test_queries";

    private static Logger log = LogManager.getLogger(AdvDataSourceTest.class);
    private final Random random = new Random();

    private void lookup(String foobar, int id, int size) {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "find_in_table");
        Map<String, Object> params = new HashMap<>();
        params.put("foobar", foobar);
        params.put("id", id);
        DataResult<AdvDataSourceDto> dr = m.execute(params);
        assertEquals(size, dr.size());
        if (size > 0) {
            assertEquals(foobar, dr.get(0).getFoobar());
            assertEquals(Long.valueOf(id), dr.get(0).getId());
        }
    }

    private void insert(String foobar, int id) {
        WriteMode m = ModeFactory.getWriteMode(TEST_QUERIES, "insert_into_table");
        Map<String, Object> params = new HashMap<>();
        params.put("foobar", foobar);
        params.put("id", id);
        params.put("test_column", "test-" + TestUtils.randomString());
        params.put("pin", random.nextInt(100));
        int res = m.executeUpdate(params);
        assertEquals(1, res);
    }

    @Test
    public void testMaxRows() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "withClass_pg");
        try {
            m.setMaxRows(-10);
            fail("setMaxRows should NOT allow negative numbers.");
        }
        catch (IllegalArgumentException e) {
            // expected.
        }
        m.setMaxRows(10);
        Map<String, Object> params = null;
        DataResult<TableData> dr = m.execute(params);
        assertNotNull(dr);
        assertEquals(10, dr.size());
    }

    /**
     * Test for ModeFactory.getMode methods
     */
    @Test
    public void testModes() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "withClass_pg");
        Map<String, Object> params = null;
        DataResult<TableData> dr = m.execute(params);
        assertNotNull(dr);
        assertTrue(dr.size() > 1);
        Object obj = dr.iterator().next();
        /* The withClass query in test_queries should have a class defined. We don't
         * really care what it is as long as it isn't a Map.
         */
        assertFalse(obj instanceof Map);

        //Try over-riding and getting a Map back
        SelectMode m2 = ModeFactory.getMode(TEST_QUERIES,
                "withClass_pg", Map.class);
        dr = m2.execute(params);
        assertNotNull(dr);
        assertTrue(dr.size() > 1);
        obj = dr.iterator().next();
        //make sure we got some sort of a Map back
        assertEquals(Row.class, obj.getClass());

        //Try over-riding with something incompatible
        SelectMode m3 = ModeFactory.getMode(TEST_QUERIES,
                "withClass_pg", Set.class);
        try {
            m3.execute(params);
            fail();
        }
        catch (ObjectCreateWrapperException e) {
            //success!
        }

        //Make sure our selectMode object was a copy and not the one cached
        SelectMode m2a = ModeFactory.getMode(TEST_QUERIES, "withClass_pg");
        assertNotEquals("java.util.Set", m2a.getClassString());
        assertNotEquals("java.util.Map", m2a.getClassString());

        //finally, make sure that by default our DataResult objects contain Maps
        SelectMode m4 = ModeFactory.getMode(TEST_QUERIES, "all_tables_pg");
        dr = m4.execute(params);
        assertNotNull(dr);
        assertTrue(dr.size() > 1);
        obj = dr.iterator().next();
        assertEquals(Row.class, obj.getClass());
    }

    @Test
    public void testInsert() {
        insert("insert_test", 3);
        TestUtils.flushAndClearSession();
        lookup("insert_test", 3, 1);
    }

    @Test
    public void testDelete() {
        // Take nothing for granted, make sure the data is there.
        insert("Blarg", 1);
        WriteMode m = ModeFactory.getWriteMode(TEST_QUERIES, "delete_from_table");
        Map<String, Object> params = new HashMap<>();
        params.put("foobar", "Blarg");
        assertEquals(1, m.executeUpdate(params));
        // Close our Session so we test to make sure it
        // actually deleted.
        TestUtils.flushAndClearSession();
        lookup("Blarg", 1, 0);
    }

    @Test
    public void testUpdate() {
        insert("update_test", 4);

        WriteMode m = ModeFactory.getWriteMode(TEST_QUERIES, "update_in_table");
        Map<String, Object> params = new HashMap<>();
        params.put("foobar", "after_update");
        params.put("id", 4);
        int res = m.executeUpdate(params);
        assertEquals(1, res);
        // Close our Session so we test to make sure it
        // actually updated.
        TestUtils.flushAndClearSession();
        lookup("after_update", 4, 1);
    }

    /** This test makes sure we can call "execute" multiple times
     * and re-use the existing internal transaction within the CommitableMode
     */
    @Test
    public void testUpdateMultiple() {
        insert("update_multi_test", 5);

        WriteMode m = ModeFactory.getWriteMode(TEST_QUERIES, "update_in_table");
        Map<String, Object> params = new HashMap<>();
        params.put("foobar", "after_update_multi");
        params.put("id", 5);
        m.executeUpdate(params);
        m = ModeFactory.getWriteMode(TEST_QUERIES, "update_in_table");
        // Call it 5 times to make sure we can
        // execute it multipletimes.
        for (int i = 0; i < 5; i++) {
            int res = m.executeUpdate(params);
            assertEquals(1, res);
        }
        lookup("after_update_multi", 5, 1);
    }

    @Test
    public void testGetCallable() {
        CallableMode m = ModeFactory.getCallableMode(TEST_QUERIES,
                                        "stored_procedure_jdbc_format");
        assertNotNull(m);
    }

    @Test
    public void testCollectionCreate() {
        List<String> ll = new LinkedList<>();
        for (int i = 0; i < 13; i++) {
            ll.add("i" + i);
        }
        DataResult<String> dr = new DataResult<>(ll);
        assertEquals(13, dr.size());
        assertEquals(1, dr.getStart());
        assertEquals(13, dr.getEnd());

    }

    @Test
    public void testStoredProcedureJDBC() {
        CallableMode m = ModeFactory.getCallableMode(TEST_QUERIES,
                                        "stored_procedure_jdbc_format");
        Map<String, Object> inParams = new HashMap<>();
        Map<String, Integer> outParams = new HashMap<>();
        inParams.put("label", "noarch");
        outParams.put("arch", Types.NUMERIC);
        Map<String, Object> row = m.execute(inParams, outParams);
        assertNotNull(row);
        assertEquals(100, ((Long)row.get("arch")).intValue());

    }

    @Test
    public void testInClause() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "select_in");
        List<Integer> params = new ArrayList<>();
        params.add(1);
        params.add(2);
        params.add(3);
        DataResult<Map<String, Object>> result = m.execute(params);
        assertNotNull(result);
        assertNotEmpty(result);
    }

    @Test
    public void testStressedElaboration() {
        int startId = 1000;
        int endId = startId + 1500;

        for (int i = startId; i < endId; i++) {
            insert("foobar" + TestUtils.randomString(), i);
        }
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "find_all_in_table");
        DataResult<AdvDataSourceDto> dr = m.execute(Collections.emptyMap());
        dr.elaborate();
        for (AdvDataSourceDto row : dr) {
            assertNotNull(row.getTestColumn());
            assertNotNull(row.getPin());
            assertNotNull(row.getFoobar());
        }
    }

    @Test
    public void testMaxRowsWithElaboration() {
        int startId = 1000;
        int endId = startId + 50;

        for (int i = startId; i < endId; i++) {
            insert("foobar" + TestUtils.randomString(), i);
        }
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "find_all_in_table");
        m.setMaxRows(10);
        DataResult<AdvDataSourceDto> dr = m.execute(Collections.emptyMap());
        assertEquals(10, dr.size());
        dr.elaborate();
        assertTrue(dr.size() <= 10);
        for (AdvDataSourceDto row : dr) {
            assertNotNull(row.getTestColumn());
            assertNotNull(row.getPin());
            assertNotNull(row.getFoobar());
        }
    }

    @Test
    public void testSelectInWithParams() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "select_in_withparams");
        List<Integer> inclause = new ArrayList<>();
        inclause.add(500);
        inclause.add(1);
        Map<String, Object> params = new HashMap<>();
        params.put("name", "jesusr");

        DataResult<Map<String, Object>> dr = m.execute(params, inclause);
        assertNotNull(dr);
    }


    @Override
    @BeforeEach
    public void setUp() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = connection.createStatement();
            try {
                statement.execute("create table if not exists adv_datasource " +
                        "( " +
                        "  foobar VarChar," +
                        "  test_column VarChar," +
                        "  pin    numeric, " +
                        "  id     numeric" +
                        "         constraint adv_datasource_pk primary key" +
                        ");"
                        );
                connection.commit();
            }
            catch (SQLException se) {
                log.warn("Failed to create table adv_datasource: {}", se.toString());
                connection.rollback();
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @Override
    @AfterEach
    public void tearDown() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                forceQuery(connection, "drop table adv_datasource");
                connection.commit();
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    private static void forceQuery(Connection c, String query) {
        try (Statement stmt = c.createStatement()) {
            stmt.execute(query);
        }
        catch (SQLException se) {
            log.warn("Failed to execute query {}: {}", query, se.toString());
        }
    }
}
