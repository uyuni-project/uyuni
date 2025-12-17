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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Test suite generic Hibernate Factory and expected behaviors.
 * Uses TestEntity as reference entity.
 */
public class TestFactoryTest extends RhnBaseTestCase {

    private static final Logger LOG = LogManager.getLogger(TestFactoryTest.class);

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

    /**
     * Creating a new entity
     * - Use factory method to instantiate
     * - Set required fields
     * - Save via factory
     */
    @Test
    public void testInsert() {
        final String testInsert = "testInsert";

        // Assert no record matches foobar key
        assertNull(TestFactory.lookupByFoobar(testInsert));

        // Create new instance
        TestInterface newEntity = TestFactory.createTest();
        newEntity.setFoobar(testInsert);
        newEntity.setTestColumn("ABC");
        newEntity.setPin(12345);
        assertNull(newEntity.getId());

        // Persist the entity
        TestFactory.save(newEntity);

        // Assert persistence
        TestInterface retrieved = TestFactory.lookupByFoobar(testInsert);
        assertNotNull(retrieved);
        assertEquals(testInsert, retrieved.getFoobar());
        assertEquals("ABC", retrieved.getTestColumn());
        assertEquals(12345, retrieved.getPin().intValue());
        assertNotNull(retrieved.getId());
    }

    /**
     * Updating an entity
     * - Load entity
     * - Modify fields
     * - Save through factory
     */
    @Test
    public void testUpdate() {
        final String foobarKey = "Blarg";

        // Get existing entity
        TestInterface original = TestFactory.lookupByFoobar(foobarKey);
        assertNotNull(original);
        assertNull(original.getPin());

        // Update entity pin
        original.setPin(200);
        TestFactory.save(original);

        // Verify update
        TestInterface updated = TestFactory.lookupByFoobar(foobarKey);
        assertEquals(200, updated.getPin().intValue());
        assertEquals(original.getPin(), updated.getPin());

        // rollback changes and test setting values to null / clearing optional data
        original.setPin(null);
        TestFactory.save(original);
        TestInterface updatedAgain = TestFactory.lookupByFoobar(foobarKey);
        assertNull(updatedAgain.getPin());
        assertEquals(original.getPin(), updatedAgain.getPin());
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

    /**
     * Handling non-unique results
     * - When query expects unique result but finds multiple
     * - Throws HibernateRuntimeException
     */
    @Test
    public void testNonUniqueResultHandling() {
        assertThrows(HibernateRuntimeException.class, () -> {
            TestFactory.lookupByFoobar("duplicate");
        });

    }

    /**
     * Deleting a single entity
     * - Load the entity first
     * - Use HibernateFactory.delete() for collections
     * - Commit to persist deletion
     */
    @Test
    public void testDeleteSingleEntity() {
        // Create entity
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar("deleteTest");
        TestFactory.save(entity);

        // Verify exists
        assertNotNull(TestFactory.lookupByFoobar("deleteTest"));

        // Delete using session.remove()
        TestInterface toDelete = TestFactory.lookupByFoobar("deleteTest");
        Session session = HibernateFactory.getSession();
        session.remove(toDelete);

        // Verify deleted
        assertNull(TestFactory.lookupByFoobar("deleteTest"));
    }

    /**
     * Batch delete using HibernateFactory.delete()
     * - More efficient than individual deletes
     * - Uses criteria API internally
     */
    @Test
    public void testBatchDelete() {
        // Create test entities
        List<TestEntity> toDelete = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TestEntity entity = (TestEntity) TestFactory.createTest();
            entity.setFoobar("delete_batch_" + i);
            TestFactory.save(entity);
            toDelete.add(entity);
        }
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Batch delete
        int deletedCount = HibernateFactory.delete(toDelete, TestEntity.class);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        assertEquals(5, deletedCount);

        // Verify deletions
        for (int i = 1; i <= 5; i++) {
            assertNull(TestFactory.lookupByFoobar("delete_batch_" + i));
        }
    }

    /**
     * Deleting with empty collection
     * - HibernateFactory.delete() handles empty collections gracefully
     */
    @Test
    public void testDeleteEmptyCollection() {
        Collection<TestEntity> emptyList = new ArrayList<>();
        int deletedCount = HibernateFactory.delete(emptyList, TestEntity.class);
        assertEquals(0, deletedCount);
    }

    /**
     * Checking transaction state
     * - Use inTransaction() to check if transaction is active
     * - Important for conditional commit/rollback logic
     */
    @Test
    public void testTransactionStateCheck() {
        // New session starts a transaction automatically
        Session session = HibernateFactory.getSession();
        assertTrue(HibernateFactory.inTransaction(), "Transaction should be active");

        HibernateFactory.commitTransaction();
        assertFalse(HibernateFactory.inTransaction(), "Transaction should be committed");

        HibernateFactory.closeSession();
        assertFalse(HibernateFactory.inTransaction(), "No transaction after close");
    }

    /**
     * Rollback on error
     * - Always rollback on exceptions
     * - Use try-finally to ensure cleanup
     */
    @Test
    public void testRollbackOnError() {
        try {
            TestInterface entity = TestFactory.createTest();
            entity.setFoobar("rollbackTest");
            TestFactory.save(entity);

            // Simulate error condition
            throw new RuntimeException("Dummy simulated error");
        }
        catch (RuntimeException e) {
            // Rollback on error
            HibernateFactory.rollbackTransaction();
        }
        finally {
            HibernateFactory.closeSession();
        }

        // Verify rollback - entity should not exist
        assertNull(TestFactory.lookupByFoobar("rollbackTest"));
    }

    /**
     * Using rollbackTransactionAndCloseSession helper
     * - Convenient method for cleanup in error scenarios
     * - Handles both rollback and session close
     */
    @Test
    public void testRollbackAndCloseHelper() {
        boolean committed = false;
        try {
            TestInterface entity = TestFactory.createTest();
            entity.setFoobar("helperTest");
            TestFactory.save(entity);

            // Simulate error
            throw new RuntimeException("Error");
        }
        catch (RuntimeException e) {
            // Not committed
        }
        finally {
            HibernateFactory.rollbackTransactionAndCloseSession(committed);
        }

        assertNull(TestFactory.lookupByFoobar("helperTest"));
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
            LOG.warn("Failed to execute query {}: {}", query, se.toString());
        }
    }

   @AfterEach
   public void tearDown() {
       try {
           if (HibernateFactory.inTransaction()) {
               HibernateFactory.commitTransaction();
           }
       }
       catch (Exception e) {
           LOG.warn("Error committing transaction in tearDown", e);
       }
       finally {
           HibernateFactory.closeSession();
       }
   }
}
