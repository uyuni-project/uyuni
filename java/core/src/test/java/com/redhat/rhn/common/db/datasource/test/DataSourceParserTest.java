/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.MapColumnNotFoundException;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.ModeNotFoundException;
import com.redhat.rhn.common.db.datasource.ParameterValueNotFoundException;
import com.redhat.rhn.common.db.datasource.ParsedMode;
import com.redhat.rhn.common.db.datasource.ParsedQuery;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.common.util.manifestfactory.ManifestFactoryLookupException;
import com.redhat.rhn.frontend.dto.VisibleSystems;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataSourceParserTest extends RhnBaseTestCase {

    private static final String TEST_QUERIES = "test_queries";
    private String dbUser;

    public DataSourceParserTest() {
        dbUser = Config.get().getString(ConfigDefaults.DB_USER);
    }

    @Test
    public void testGetModes() {
        SelectMode m = ModeFactory.getMode("System_queries", "ssm_remote_commandable");
        assertNotNull(m);
    }

    @Test
    public void testGetModesNoFile() {
        try {
            ModeFactory.getMode("Garbage", "ssm_remote_commandable");
            fail("Should have received an exception");
        }
        catch (ManifestFactoryLookupException e) {
            // Expected this exception, Garbage isn't a valid file.
        }
    }

    @Test
    public void testGetModesNoMode() {
        try {
            ModeFactory.getMode(TEST_QUERIES, "Garbage");
            fail("Should have received an exception");
        }
        catch (ModeNotFoundException e) {
            // Expected this exception, Garbage isn't a valid file.
        }
    }

    @Test
    public void testExternalElaborator() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES,
            "user_tables_external_elaborator_pg");
        assertNotNull(m);

        DataResult<Map<String, Object>> dr = m.execute(new HashMap<>());
        assertNotNull(dr);

        Iterator<Map<String, Object>> i = dr.iterator();
        int pos = 0;
        while (i.hasNext()) {
            Map<String, Object> hm = i.next();
            String name = (String)hm.get("username");

            if (name.toLowerCase().equals(dbUser)) {
                dr = dr.subList(pos, pos + 1);
            }
            pos++;
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_name", dbUser);
        dr.elaborate(parameters);
        assertNotNull(dr);

        i = dr.iterator();
        while (i.hasNext()) {
            Map<String, Object> hm = i.next();
            Map<String, Object> elab = (Map<String, Object>)hm.get("external_elaborator_pg");
            assertTrue(((Long)elab.get("table_count")).intValue() > 0);
        }
    }

    @Test
    public void testRunQuery() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "user_tables_pg");
        assertNotNull(m);

        DataResult<Map<String, Object>> dr = m.execute(new HashMap<>());
        assertNotNull(dr);

        Iterator<Map<String, Object>> i = dr.iterator();
        int pos = 0;
        while (i.hasNext()) {
            Map<String, Object> hm = i.next();
            String name = (String)hm.get("username");

            if (name.toLowerCase().equals(dbUser)) {
                dr = dr.subList(pos, pos + 1);
            }
            pos++;
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_name", dbUser);
        dr.elaborate(parameters);
        assertNotNull(dr);

        i = dr.iterator();
        while (i.hasNext()) {
            Map<String, Object> hm = i.next();
            Map<String, Object> elab = (Map<String, Object>)hm.get("table_elaborator_pg");
            assertTrue(((Long)elab.get("table_count")).intValue() > 0);
        }
    }

    private boolean shouldSkip(ParsedMode m) {
        /* Don't do plans for queries that use system tables or for
         * dummy queries.
         */
        return (m != null && m.getParsedQuery() != null &&
                (m.getName().equals("tablespace_overview") ||
                 m.getParsedQuery().getSqlStatement().trim().startsWith("--")));
    }

    @Test
    public void testPrepareAll() {
        HibernateFactory.getSession().doWork(connection -> {
            PreparedStatement ps = null;
            try {
                Collection<?> fileSet = ModeFactory.getKeys();
                for (Object valueIn : fileSet) {
                    String file = (String) valueIn;

                    for (Object oIn : ModeFactory.getFileKeys(file).values()) {
                        ParsedMode m = (ParsedMode) oIn;

                        if (shouldSkip(m)) {
                            continue;
                        }
                        ParsedQuery pq = m.getParsedQuery();
                        if (pq != null) {
                            String query = pq.getSqlStatement();

                            // HACK: some of the queries actually have %s in them.
                            // So, replace all %s with :rbb so that the explain plan
                            // can be generated.
                            query = query.replace("%s", ":rbb");

                            ps = connection.prepareStatement(query);
                        }
                    }
                }
            }
            finally {
                if (connection != null) {
                    connection.commit();
                }
                HibernateHelper.cleanupDB(ps);
            }
        });
    }

    private void runTestQuery(String queryName, String elabName) {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, queryName);
        assertNotNull(m);

        DataResult<Row> dr = m.execute(new HashMap<>());
        assertNotNull(dr);

        // Pick the first three tables, just so that we aren't elaborating
        // all of the tables.
        dr = dr.subList(0, 3);

        dr.elaborate(new HashMap<>());
        assertNotNull(dr);
        assertEquals(3, dr.size());

        for (Row hm : dr) {
            List<Map<String, Object>> elab = (List<Map<String, Object>>) hm.get(elabName);
            assertFalse(elab.isEmpty());
            for (Map<String, Object> curr : elab) {
                assertTrue(((Number) curr.get("column_id")).intValue() > 0);
                assertNotNull(curr.get("column_name"));
                assertNotNull(curr.get("table_name"));
            }
        }
    }

    @Test
    public void testPercentS() {
        runTestQuery("all_tables_pg", "elaborator0");
    }

    @Test
    public void testBrokenDriving() {
        try {
            runTestQuery("broken_driving_pg", "elaborator0");
            fail("Should have thrown an exception");
        }
        catch (MapColumnNotFoundException e) {
            assertEquals("Column, id, not found in driving query results",
                         e.getMessage());
        }
    }

    @Test
    public void testBrokenElaborator() {
        try {
            runTestQuery("broken_elaborator_pg", "elaborator0");
            fail("Should have thrown an exception");
        }
        catch (MapColumnNotFoundException e) {
            assertEquals("Column, id, not found in elaborator results",
                         e.getMessage());
        }
    }

    @Test
    public void testAlias() {
        runTestQuery("all_tables_with_alias_pg", "details_pg");
    }

    @Test
    public void testExtraParams() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "all_tables_pg");
        assertNotNull(m);

        Map<String, Object> params = new HashMap<>();
        params.put("foo", "bar");
        DataResult<Map<String, Object>> dr = m.execute(params);
        assertNotNull(dr);
    }

    @Test
    public void testDrivingParams() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "user_tables_for_user_pg");
        assertNotNull(m);

        Map<String, Object> hm = new HashMap<>();
        hm.put("username", dbUser);
        DataResult<Map<String, Object>> dr = m.execute(hm);
        assertNotNull(dr);
        assertFalse(dr.isEmpty());
    }

    @Test
    public void testNullParam() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "user_tables_for_user_pg");
        assertNotNull(m);

        try {
            m.execute(new HashMap<>());
            fail("Should have received an exception");
        }
        catch (ParameterValueNotFoundException e) {
            assertTrue(e.getMessage().startsWith(
                        "Parameter 'username' not given for query:"));
        }
    }

    @Test
    public void testExternalQuery() {
        SelectMode m = ModeFactory.getMode("System_queries", "visible_to_uid");
        Map<String, Object> params = new HashMap<>();
        params.put("formvar_uid", 12345L);
        DataResult<VisibleSystems> dr = m.execute(params);
        assertEquals(m, dr.getMode());
    }

    @Test
    public void testSpecifiedClass() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "withClass_pg");
        String clazz = m.getClassString();
        assertEquals("com.redhat.rhn.common.db.datasource.test.TableData", clazz);
    }

    @Test
    public void testSpecifiedClassExecute() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "withClass_pg");
        String clazz = m.getClassString();
        assertEquals("com.redhat.rhn.common.db.datasource.test.TableData", clazz);
        DataResult<TableData> dr = m.execute(new HashMap<>());
        assertNotNull(dr);
        assertFalse(dr.isEmpty());
        Iterator<TableData> i = dr.iterator();
        TableData first = i.next();
        assertTrue(first.getTableName().toLowerCase().startsWith("rhn"));
    }

    @Test
    public void testClassElaborateList() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "withClass_pg");
        String clazz = m.getClassString();
        assertEquals("com.redhat.rhn.common.db.datasource.test.TableData", clazz);
        DataResult<TableData> dr = m.execute(new HashMap<>());
        assertNotNull(dr);
        assertFalse(dr.isEmpty());
        dr = dr.subList(0, 1);
        dr.elaborate(new HashMap<>());

        Iterator<TableData> i = dr.iterator();
        TableData first = i.next();
        assertTrue(first.getTableName().toLowerCase().startsWith("rhn"));
        assertFalse(first.getColumnName().isEmpty());
        assertFalse(first.getColumnId().isEmpty());
    }

    @Test
    public void testSpecifiedClassElaborate() {
        SelectMode m = ModeFactory.getMode(TEST_QUERIES, "user_class_pg");
        String clazz = m.getClassString();
        assertEquals("com.redhat.rhn.common.db.datasource.test.UserData", clazz);
        Map<String, Object> hm = new HashMap<>();
        hm.put("username", dbUser);
        DataResult<UserData> dr = m.execute(hm);
        assertNotNull(dr);
        assertFalse(dr.isEmpty());

        dr.elaborate(hm);

        Iterator<UserData> i = dr.iterator();
        UserData first = i.next();
        assertNotNull(first.getUsername());
        assertTrue(first.getTableCount().intValue() > 0);
    }
}
