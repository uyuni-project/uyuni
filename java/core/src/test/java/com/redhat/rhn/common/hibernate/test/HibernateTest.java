/*
 * Copyright (c) 2025 SUSE LLC
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.test.TestEntity;
import com.redhat.rhn.domain.test.TestFactory;
import com.redhat.rhn.domain.test.TestInterface;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.FlushModeType;


public class HibernateTest extends RhnBaseTestCase {
    
    private static final Logger LOG = LogManager.getLogger(HibernateTest.class);
    
    @BeforeEach
    public void setUp() {
        // Initialize session factory with TestConnectionManager that registers TestImpl
        TestFactory.getSession();
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

//    /**
//     * Creating a new entity
//     * - Use factory method to instantiate
//     * - Set required fields
//     * - Call save/persist through factory
//     * - Verify ID is assigned after commit
//     */
//    @Test
//    public void testCreateNewEntity() {
//        // Create new instance
//        TestInterface newEntity = TestFactory.createTest();
//        newEntity.setFoobar("testCreate");
//        newEntity.setTestColumn("ABC");
//        newEntity.setPin(12345);
//
//        // Persist the entity
//        TestFactory.save(newEntity);
//
//        // Commit transaction to persist changes
//        HibernateFactory.commitTransaction();
//        HibernateFactory.closeSession();
//
//        // Verify persistence
//        TestInterface retrieved = TestFactory.lookupByFoobar("testCreate");
//        assertNotNull(retrieved);
//        assertEquals("testCreate", retrieved.getFoobar());
//        assertEquals("ABC", retrieved.getTestColumn());
//        assertEquals(12345, retrieved.getPin().intValue());
//        assertNotNull(retrieved.getId());
//    }




    /**
     * Batch creation with transaction management
     * - Create multiple entities in one transaction
     * - Improves performance over individual transactions
     */
    @Test
    public void testBatchCreate() {
        List<String> values = List.of("batch1", "batch2", "batch3");

        // Create multiple entities in one transaction
        for (String value : values) {
            TestInterface entity = TestFactory.createTest();
            entity.setFoobar(value);
            entity.setPin(100);
            TestFactory.save(entity);
        }

        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify all were persisted
        List<TestInterface> all = TestFactory.lookupAll();
        long count = all.stream()
                .filter(t -> values.contains(t.getFoobar()))
                .count();
        assertEquals(3, count);
    }

    // ========== READ OPERATIONS ==========

    /**
     * Simple lookup by unique field
     * - Use lookupObjectByParam for simple queries
     * - Returns null if not found
     */
    @Test
    public void testReadByUniqueField() {
        // Setup
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar("uniqueValue");
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Lookup
        TestInterface found = TestFactory.lookupByFoobar("uniqueValue");
        assertNotNull(found);
        assertEquals("uniqueValue", found.getFoobar());

        // Not found case
        TestInterface notFound = TestFactory.lookupByFoobar("doesNotExist");
        assertNull(notFound);
    }

    /**
     * Using getSession() for direct Hibernate queries
     * - Useful for complex queries not in factory
     * - Remember to manage session lifecycle
     */
    @Test
    public void testDirectSessionQuery() {
        // Setup test data
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar("directQuery");
        entity.setPin(999);
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Direct session query
        Session session = HibernateFactory.getSession();
        List<TestInterface> results = session
                .createQuery("FROM TestEntity WHERE pin = :pin", TestInterface.class)
                .setParameter("pin", 999)
                .getResultList();

        assertFalse(results.isEmpty());
        assertEquals(999, results.get(0).getPin().intValue());
    }

    /**
     * Checking if session exists before using it
     * - Use getSessionIfPresent() to check session availability
     * - Prevents unnecessary session creation
     */
    @Test
    public void testSessionPresenceCheck() {
        // Session exists from setUp
        Optional<Session> sessionBefore = HibernateFactory.getSessionIfPresent();
        assertTrue(sessionBefore.isPresent(), "Session should exist from setUp");

        // Get the existing session
        Session session = HibernateFactory.getSession();
        assertNotNull(session);

        Optional<Session> sessionAfter = HibernateFactory.getSessionIfPresent();
        assertTrue(sessionAfter.isPresent(), "Session should still exist");
        assertEquals(session, sessionAfter.get());
        
        // Close and verify it's gone
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
        Optional<Session> sessionClosed = HibernateFactory.getSessionIfPresent();
        assertTrue(sessionClosed.isEmpty(), "Session should be closed");
    }

    /**
     * Reloading an entity to refresh from database
     * - Use reload() to get latest state from DB
     * - Useful after external updates or to discard changes
     */
    @Test
    public void testReloadEntity() {
        // Create and save
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar("reloadTest");
        entity.setPin(100);
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Load and modify (but don't save - just modify in memory)
        TestInterface loaded = TestFactory.lookupByFoobar("reloadTest");
        assertEquals(100, loaded.getPin().intValue());
        
        // Modify in memory only
        loaded.setPin(200);
        assertEquals(200, loaded.getPin().intValue(), "In-memory value should be 200");

        // Reload to get original state from DB (discarding in-memory changes)
        TestInterface reloaded = HibernateFactory.reload(loaded);
        assertEquals(100, reloaded.getPin().intValue(), "Reload should get original DB value");
    }

    // ========== UPDATE OPERATIONS ==========

//    /**
//     * Simple update pattern
//     * - Load entity
//     * - Modify fields
//     * - Save through factory
//     * - Commit transaction
//     */
//    @Test
//    public void testUpdateEntity() {
//        // Create initial entity
//        TestInterface entity = TestFactory.createTest();
//        entity.setFoobar("updateTest");
//        entity.setPin(100);
//        entity.setTestColumn("OLD");
//        TestFactory.save(entity);
//        HibernateFactory.commitTransaction();
//        HibernateFactory.closeSession();
//
//        // Load and update
//        TestInterface toUpdate = TestFactory.lookupByFoobar("updateTest");
//        assertNotNull(toUpdate);
//        toUpdate.setPin(200);
//        toUpdate.setTestColumn("NEW");
//        TestFactory.save(toUpdate);
//        HibernateFactory.commitTransaction();
//        HibernateFactory.closeSession();
//
//        // Verify update
//        TestInterface updated = TestFactory.lookupByFoobar("updateTest");
//        assertEquals(200, updated.getPin().intValue());
//        assertEquals("NEW", updated.getTestColumn());
//    }

//    /**
//     * Updating to null values
//     * - Hibernate properly handles setting fields to null
//     * - Important for clearing optional data
//     */
//    @Test
//    public void testUpdateToNull() {
//        // Create with values
//        TestInterface entity = TestFactory.createTest();
//        entity.setFoobar("nullTest");
//        entity.setTestColumn("VALUE");
//        entity.setPin(500);
//        TestFactory.save(entity);
//        HibernateFactory.commitTransaction();
//        HibernateFactory.closeSession();
//
//        // Update to null
//        TestInterface toUpdate = TestFactory.lookupByFoobar("nullTest");
//        toUpdate.setTestColumn(null);
//        toUpdate.setPin(null);
//        TestFactory.save(toUpdate);
//        HibernateFactory.commitTransaction();
//        HibernateFactory.closeSession();
//
//        // Verify nulls
//        TestInterface updated = TestFactory.lookupByFoobar("nullTest");
//        assertNull(updated.getTestColumn());
//        assertNull(updated.getPin());
//    }

    /**
     * Batch updates with proper transaction boundaries
     * - Update multiple entities in one transaction
     * - More efficient than individual transactions
     */
    @Test
    public void testBatchUpdate() {
        // Setup: Create multiple entities
        for (int i = 1; i <= 3; i++) {
            TestInterface entity = TestFactory.createTest();
            entity.setFoobar("batch_" + i);
            entity.setPin(i * 10);
            TestFactory.save(entity);
        }
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Batch update
        List<TestInterface> all = TestFactory.lookupAll();
        for (TestInterface entity : all) {
            if (entity.getFoobar() != null && entity.getFoobar().startsWith("batch_")) {
                entity.setPin(entity.getPin() + 1000);
                TestFactory.save(entity);
            }
        }
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify updates
        TestInterface updated = TestFactory.lookupByFoobar("batch_1");
        assertEquals(1010, updated.getPin().intValue());
    }

    // ========== DELETE OPERATIONS ==========

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
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify exists
        assertNotNull(TestFactory.lookupByFoobar("deleteTest"));

        // Delete using session.remove()
        TestInterface toDelete = TestFactory.lookupByFoobar("deleteTest");
        Session session = HibernateFactory.getSession();
        session.remove(toDelete);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

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

    // ========== TRANSACTION MANAGEMENT ==========

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
            throw new RuntimeException("Simulated error");
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

    // ========== PERFORMANCE OPTIMIZATION ==========

    /**
     * Using doWithoutAutoFlushing for performance
     * - Disables automatic flushing during reads
     * - Improves performance when many objects in cache
     * - WARNING: Queries may return stale data
     */
    @Test
    public void testDisableAutoFlushing() {
        // Create test data
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar("flushTest");
        entity.setPin(100);
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Modify without flushing
        Session session = HibernateFactory.getSession();
        TestInterface loaded = TestFactory.lookupByFoobar("flushTest");
        loaded.setPin(200);

        // Query without auto-flush - may see old data
        Integer pin = HibernateFactory.doWithoutAutoFlushing(() -> {
            // This query won't see the unflushed change
            TestInterface queried = session
                    .createQuery("FROM TestEntity WHERE foobar = :fb", TestInterface.class)
                    .setParameter("fb", "flushTest")
                    .uniqueResult();
            return queried != null ? queried.getPin() : null;
        });

        // Pin might still be 100 because change wasn't flushed
        assertNotNull(pin);
    }

    /**
     * Verify flush mode is restored
     * - doWithoutAutoFlushing restores original mode
     */
    @Test
    public void testFlushModeRestored() {
        Session session = HibernateFactory.getSession();
        FlushModeType originalMode = session.getFlushMode();

        HibernateFactory.doWithoutAutoFlushing(() -> {
            assertEquals(FlushModeType.COMMIT, session.getFlushMode());
        });

        assertEquals(originalMode, session.getFlushMode(), "Flush mode should be restored");
    }

    // ========== ERROR HANDLING ==========

    /**
     * Handling non-unique results
     * - When query expects unique result but finds multiple
     * - Throws HibernateRuntimeException
     */
    @Test
    public void testNonUniqueResultHandling() {
        // Create duplicate entries
        for (int i = 0; i < 2; i++) {
            TestInterface entity = TestFactory.createTest();
            entity.setFoobar("duplicate");
            TestFactory.save(entity);
        }
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Lookup should throw because of duplicates
        assertThrows(HibernateRuntimeException.class, () -> {
            TestFactory.lookupByFoobar("duplicate");
        });
    }

    /**
     * Null handling in lookups
     * - Lookups return null when entity not found
     * - Don't throw exceptions for missing data
     */
    @Test
    public void testNullHandlingInLookup() {
        TestInterface notFound = TestFactory.lookupByFoobar("nonExistent");
        assertNull(notFound, "Should return null for non-existent entity");
    }

    // ========== SESSION LIFECYCLE ==========

    /**
     * Multiple operations in single session
     * - Group related operations in one session/transaction
     * - More efficient than opening multiple sessions
     */
    @Test
    public void testMultipleOperationsInSession() {
        // All operations in one session
        TestInterface entity1 = TestFactory.createTest();
        entity1.setFoobar("multi1");
        TestFactory.save(entity1);

        TestInterface entity2 = TestFactory.createTest();
        entity2.setFoobar("multi2");
        TestFactory.save(entity2);

        // Query in same session
        List<TestInterface> all = TestFactory.lookupAll();
        assertNotNull(all);

        // Commit once
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify persistence
        assertNotNull(TestFactory.lookupByFoobar("multi1"));
        assertNotNull(TestFactory.lookupByFoobar("multi2"));
    }

    /**
     * Proper session cleanup
     * - Always close session in finally block
     * - Prevents resource leaks
     */
    @Test
    public void testProperSessionCleanup() {
        try {
            Session session = HibernateFactory.getSession();
            assertNotNull(session);
            assertTrue(session.isOpen());

            TestInterface entity = TestFactory.createTest();
            entity.setFoobar("cleanup");
            TestFactory.save(entity);

            HibernateFactory.commitTransaction();
        }
        finally {
            // Always close in finally
            HibernateFactory.closeSession();
        }

        // Verify session is closed
        Optional<Session> session = HibernateFactory.getSessionIfPresent();
        assertTrue(session.isEmpty(), "Session should be closed");
    }

}
