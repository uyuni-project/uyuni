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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.test.TestEntity;
import com.redhat.rhn.domain.test.TestFactory;
import com.redhat.rhn.domain.test.TestInterface;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.FlushModeType;

/**
 * This test class is NOT intended to test Hibernate itself.
 * Its purpose is to validate that our assumptions and historical usage patterns of Hibernate
 * (as implemented in our project utilities, factories, etc.) remain consistent with our expectations.
 * In practice, will come handy for example when upgrading Hibernate to a new version, this class can help detect
 * changes in behavior that could impact our code.
 */
public class HibernateTest extends HibernateBaseTest {


    // ========== READ OPERATIONS ==========

    /**
     * Using getSession() for direct Hibernate queries
     * - Useful for complex queries not in factory
     */
    @Test
    public void testDirectSessionQuery() {
        String key = "duplicate";

        Session session = HibernateFactory.getSession();
        List<TestInterface> results = session
                .createQuery("FROM TestEntity WHERE foobar = :foobar", TestInterface.class)
                .setParameter("foobar", key)
                .getResultList();

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(x -> key.equals(x.getFoobar())));
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

    // ========== PERSISTENCE OPERATIONS ==========

    /**
     * Testing Hibernate's persist method
     * - Ensures entity is tracked and changes are auto-detected
     * - Verifies caching behavior within session
     */
    @Test
    public void testHibernatePersist() {
        String key = "dummy_testHibernatePersist";
        TestInterface e = new TestEntity();

        // transaction starts
        HibernateFactory.getSession().persist(e);
        e.setFoobar(key);
        e.setPin(0);
        // transaction ends, and the row for foobar field is updated in the database

        // verify the database
        TestInterface recordA = TestFactory.lookupByFoobar(key);
        assertEquals(key, recordA.getFoobar());

        // check caching - lookup should always return the same object
        TestInterface recordB = TestFactory.lookupByFoobar(key);
        assertEquals(recordA, recordB);
        assertEquals(recordA, e);

        // as object E, recordA and recordB are all references to the same object in the session's persistence
        // context, when changing the pin in recordA, hibernate will automatically check for updates
        recordA.setPin(123);
        assertEquals(123, TestFactory.lookupByFoobar(key).getPin());
    }

    /**
     * Testing Hibernate's merge method
     * - Ensures detached entity can be merged into session
     * - Verifies that changes made after merge are not auto-detected
     */
    @Test
    public void testHibernateMerge() {
        String keyDetached = "key_testHibernateMerge_detached";
        String keyManaged = "key_testHibernateMerge_managed";
        TestInterface e = new TestEntity();

        // transaction starts
        TestInterface managed = HibernateFactory.getSession().merge(e);
        e.setFoobar(keyDetached);
        // transaction ends but the row for foobar field is NOT updated in the database
        // (changes were made AFTER merging)
        // E is then detached/transient, while object managed is now monitored by hibernate

        // verify E isn't tracked
        assertFalse(HibernateFactory.getSession().contains(e));
        assertNull(e.getId());
        assertNull(TestFactory.lookupByFoobar(keyDetached));

        // verify managed is tracked
        managed.setFoobar(keyManaged);
        assertTrue(HibernateFactory.getSession().contains(managed));
        assertNotNull(managed.getId());
        assertNotNull(TestFactory.lookupByFoobar(keyManaged));
    }

    /**
     * Testing Hibernate's detach method
     * - Ensures entity is removed from session tracking
     * - Verifies that changes made after detach are not persisted
     */
    @Test
    public void testHibernateDetach() {
        String keyBefore = "dummy_testHibernateDetach_before";
        String keyAfter = "dummy_testHibernateDetach_after";
        TestInterface e = new TestEntity();

        // creating and persisting the object
        HibernateFactory.getSession().persist(e);
        e.setFoobar(keyBefore);
        assertNotNull(TestFactory.lookupByFoobar(keyBefore));

        // after detaching the persisted object, hibernate will no longer track its changes
        HibernateFactory.getSession().detach(e);
        e.setFoobar(keyAfter);
        assertNull(TestFactory.lookupByFoobar(keyAfter));
    }

    /**
     * Reloading an entity to refresh from database
     * - Use reload() to flush pending changes, evict from cache, and re-read from DB
     * - NOTE: reload() FLUSHES changes before reloading, so modified values are persisted
     * - Useful to ensure entity is refreshed from DB after flush
     */
    @Test
    public void testReloadEntity() {
        String key = "reloadTest";

        // Create and save
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar(key);
        entity.setPin(100);
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Find the entity
        TestInterface loaded = TestFactory.lookupByFoobar(key);
        assertEquals(100, loaded.getPin().intValue());
        
        // Modify in memory
        loaded.setPin(200);
        assertEquals(200, loaded.getPin().intValue(), "In-memory value should be 200");

        // Reload flushes changes first, then evicts and re-reads from DB
        // So it will persist the modified value (200) and reload it
        TestInterface reloaded = HibernateFactory.reload(loaded);
        assertEquals(200, reloaded.getPin().intValue(), "Reload flushes changes, so modified value is persisted");
        
        // Verify it was actually persisted to DB
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
        TestInterface fromDb = TestFactory.lookupByFoobar(key);
        assertEquals(200, fromDb.getPin().intValue(), "Value should be persisted in DB");
    }

    // ========== UPDATE OPERATIONS ==========

    /**
     * Update pattern using managed entity through lookup
     * - Create and persist entity
     * - Close session to detach
     * - Load entity
     * - Modify fields
     */
    @Test
    public void testUpdateManaged() {
        String key = "updateTestManaged";

        TestInterface entity = TestFactory.createTest();
        entity.setFoobar(key);
        entity.setPin(100);
        entity.setTestColumn("orig");
        HibernateFactory.getSession().persist(entity);
        HibernateFactory.commitTransaction();

        // Ensure entity is detached
        HibernateFactory.closeSession();
        assertFalse(HibernateFactory.getSession().contains(entity));

        // Load and update
        TestInterface managed = TestFactory.lookupByFoobar(key);
        assertNotNull(managed);
        managed.setPin(null);
        managed.setTestColumn("upd");

        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify update
        TestInterface updated = TestFactory.lookupByFoobar(key);
        assertNull(updated.getPin());
        assertEquals("upd", updated.getTestColumn());
    }

    /**
     * Update pattern using managed entity through lookup
     * - Create and persist entity
     * - Close session to detach
     * - Load entity
     * - Modify fields
     */
    @Test
    public void testUpdateDetached() {
        String key = "updateTestDetached";

        TestInterface entity = TestFactory.createTest();
        entity.setFoobar(key);
        entity.setPin(100);
        entity.setTestColumn("orig");
        HibernateFactory.getSession().persist(entity);
        HibernateFactory.commitTransaction();

        // Ensure entity is detached
        HibernateFactory.closeSession();
        assertFalse(HibernateFactory.getSession().contains(entity));

        // Get a managed instance via merge and update
        TestInterface managed = HibernateFactory.getSession().merge(entity);
        assertNotNull(managed);
        managed.setPin(null);
        managed.setTestColumn("upd");

        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify update
        TestInterface updated = TestFactory.lookupByFoobar(key);
        assertNull(updated.getPin());
        assertEquals("upd", updated.getTestColumn());
    }

    // ========== DELETE OPERATIONS ==========

    /**
     * Deleting a single entity (use HibernateFactory.delete() for collections)
     * - Load the entity first
     * - Commit to persist deletion
     */
    @Test
    public void testDeleteSingleEntity() {
        String key = "deleteTest";

        // Create entity
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar(key);
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify exists
        assertNotNull(TestFactory.lookupByFoobar(key));

        // Delete using session.remove()
        TestInterface toDelete = TestFactory.lookupByFoobar(key);
        Session session = HibernateFactory.getSession();
        session.remove(toDelete);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Verify deleted
        assertNull(TestFactory.lookupByFoobar(key));
    }

    /**
     * Batch delete using HibernateFactory.delete()
     * - More efficient than individual deletes
     * - Uses criteria API internally
     */
    @Test
    public void testBatchDelete() {
        String keyPrefix = "delete_batch_";

        // Create test entities and collect the managed instances
        List<TestEntity> toDelete = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TestEntity entity = (TestEntity) TestFactory.createTest();
            entity.setFoobar(keyPrefix + i);
            // Collect the managed entities
            TestEntity managed = (TestEntity) TestFactory.save(entity);
            toDelete.add(managed);
        }

        //
        assertEquals(5, HibernateFactory.delete(toDelete, TestEntity.class));

        // Verify deletions
        for (int i = 1; i <= 5; i++) {
            assertNull(TestFactory.lookupByFoobar(keyPrefix + i));
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
        HibernateFactory.getSession();
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
     * Cancel in-memory changes by rolling back transaction
     * - Create and save an entity with original values
     * - Modify the entity but don't want to keep changes
     * - Rollback transaction to discard changes
     * - Verify database still has original values
     */
    @Test
    public void testCancelChangesByRollback() {
        // Create entity with original values
        TestInterface entity = TestFactory.createTest();
        entity.setFoobar("original");
        entity.setPin(100);
        entity.setTestColumn("ABC");
        TestFactory.save(entity);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();

        // Retrieve and verify original values
        TestInterface retrieved = TestFactory.lookupByFoobar("original");
        assertNotNull(retrieved);
        assertEquals("original", retrieved.getFoobar());
        assertEquals(100, retrieved.getPin().intValue());
        assertEquals("ABC", retrieved.getTestColumn());

        // Modify in memory (change your mind scenario)
        retrieved.setPin(999);
        retrieved.setTestColumn("XYZ");
        
        // Verify in-memory changes
        assertEquals(999, retrieved.getPin().intValue());
        assertEquals("XYZ", retrieved.getTestColumn());

        // Rollback
        HibernateFactory.rollbackTransaction();
        HibernateFactory.closeSession();

        // Verify database still has original values
        TestInterface fromDb = TestFactory.lookupByFoobar("original");
        assertNotNull(fromDb);
        assertEquals(100, fromDb.getPin().intValue(), "Database should have original pin value");
        assertEquals("ABC", fromDb.getTestColumn(), "Database should have original test column value");
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

    // ========== SESSION LIFECYCLE ==========

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
