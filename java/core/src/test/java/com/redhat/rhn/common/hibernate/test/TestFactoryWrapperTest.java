/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.common.hibernate.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.test.TestEntity;
import com.redhat.rhn.domain.test.TestFactory;
import com.redhat.rhn.domain.test.TestInterface;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

public class TestFactoryWrapperTest extends RhnBaseTestCase {
    private static Logger log = LogManager.getLogger(TestFactoryWrapperTest.class);

    @Override
    @BeforeEach
    public void setUp() {
        TestFactory.createSessionFactory();
    }

    @Test
    public void testLookupReturnNull() {
        TestInterface obj = TestFactory.lookupByFoobar("NOTFOUND");
        assertNull(obj);
    }

    @Test
    public void testLookup() {
        TestInterface obj = TestFactory.lookupByFoobar("Blarg");
        assertEquals("Blarg", obj.getFoobar());
        assertNull(obj.getPin());
        assertEquals(1, (long) obj.getId());
    }

    @Test
    public void testInsert() {
        final String testInsert = "testInsert";
        assertNull(TestFactory.lookupByFoobar(testInsert));

        //
        TestInterface record = TestFactory.createTest();
        record.setFoobar(testInsert);
        TestFactory.save(record);
        assertTrue(record.getId() != 0L);
    }

    @Test
    public void testUpdate() {

        TestInterface obj = TestFactory.createTest();
        obj.setFoobar("update_Multi_test");
        obj.setPin(12345);
        TestFactory.save(obj);
        TestInterface result = TestFactory.lookupByFoobar("update_Multi_test");
        assertEquals("update_Multi_test", result.getFoobar());

        result.setFoobar("After_multi_change");
        result.setPin(54321);
        TestFactory.save(result);
        TestInterface updated = TestFactory.lookupByFoobar("After_multi_change");
        assertEquals("After_multi_change", updated.getFoobar());
        assertEquals(54321, updated.getPin().intValue());
    }

    @Test
    public void testUpdateAfterCommit() {
        TestInterface obj = TestFactory.createTest();
        obj.setFoobar("update_test");
        TestFactory.save(obj);
        // Make sure we make it here without exception
        assertTrue(true);
    }

    @Test
    public void testLookupMultipleObjects() {
        List<TestInterface> allTests = TestFactory.lookupAll();
        assertFalse(allTests.isEmpty());
    }

    @Test
    public void testUpdateToNullValue() {
        TestInterface obj = TestFactory.createTest();
        obj.setFoobar("update_test3");
        obj.setTestColumn("AAA");
        TestFactory.save(obj);
        TestInterface result = TestFactory.lookupByFoobar("update_test3");
        assertEquals("update_test3", result.getFoobar());

        result.setFoobar("After_change3");
        // This is the critical part where we set a value
        // that once had a value to a NULL value
        result.setTestColumn(null);
        TestFactory.save(result);
        result = TestFactory.lookupByFoobar("After_change3");
        assertNull(result.getTestColumn());
    }

    @Test
    public void testLotsOfTransactions() {

        for (int i = 0; i < 20; i++) {
            SelectMode m = ModeFactory.getMode("test_queries", "get_test_users");
            m.execute(new HashMap<>());
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();
        }
    }

    @Test
    public void testHandleNonUniqueResultException() {
        assertThrows(HibernateRuntimeException.class, () -> {
            TestFactory.lookupByFoobar("duplicate");
        });

    }

    @BeforeAll
    public static void oneTimeSetup() {
        TestFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                
                // Always clean up and recreate to ensure schema is current
                forceQuery(connection, "drop table if exists persist_test cascade");
                forceQuery(connection, "drop sequence if exists persist_sequence");
                
                statement.execute("create sequence persist_sequence");
                statement.execute("""
                        create table persist_test(
                                foobar VarChar(32),
                                test_column VarChar(5),
                                pin    numeric,
                                hidden VarChar(32),
                                id     numeric constraint persist_test_pk primary key,
                                created timestamp with time zone,
                                modified timestamp with time zone
                                )""");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('Blarg', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('duplicate', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('duplicate', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, hidden, id) " +
                        "values ('duplicate', 'xxxxx', nextval('persist_sequence'))");

                connection.commit();
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @AfterAll
    public static void oneTimeTeardown() {
        TestFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                // Couldn't select 1, so the table didn't exist, create it
                forceQuery(connection, "drop sequence persist_sequence");
                forceQuery(connection, "drop table persist_test");
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    private static void forceQuery(Connection c, String query) {
        try {
            Statement stmt = c.createStatement();
            stmt.execute(query);
        }
        catch (SQLException se) {
            log.warn("Failed to execute query {}: {}", query, se.toString());
        }
    }
}
