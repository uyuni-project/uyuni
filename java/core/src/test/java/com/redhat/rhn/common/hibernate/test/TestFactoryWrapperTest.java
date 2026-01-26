/*
 * Copyright (c) 2026 SUSE LLC
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
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.test.TestEntity;
import com.redhat.rhn.domain.test.TestFactory;
import com.redhat.rhn.domain.test.TestInterface;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TestFactoryWrapperTest extends HibernateBaseTest {

    /**
     * Null handling in lookups
     * - Lookups return null when entity not found
     * - Don't throw exceptions for missing data
     */
    @Test
    public void testLookupReturnNull() {
        TestInterface obj = TestFactory.lookupByFoobar("NOTFOUND");
        assertNull(obj, "Should return null for non-existent entity");
    }

    /**
     * Handle NonUniqueResultException in lookups
     * - Throw HibernateRuntimeException wrapping the original exception
     */
    @Test
    public void testHandleNonUniqueResultException() {
        assertThrows(HibernateRuntimeException.class, () -> {
            TestFactory.lookupByFoobar("duplicate");
        });
    }

    /**
     * Test that lookupByFoobar returns the correct record
     */
    @Test
    public void testLookup() {
        TestInterface obj = TestFactory.lookupByFoobar("Blarg");
        assertEquals("Blarg", obj.getFoobar());
        assertNull(obj.getPin());
        assertEquals(1, (long) obj.getId());
    }

    /**
     * Test that lookupAll returns multiple records
     */
    @Test
    public void testLookupMultipleObjects() {
        List<TestInterface> allTests = TestFactory.lookupAll();
        assertFalse(allTests.isEmpty());
    }

    /**
     * Test inserting a new entity
     * - Use factory method to instantiate
     * - Save via factory
     */
    @Test
    public void testInsert() {
        final String testInsert = "testInsert";
        assertNull(TestFactory.lookupByFoobar(testInsert));

        // insert
        TestInterface record = TestFactory.createTest();
        record.setFoobar(testInsert);
        TestFactory.save(record);

        // verify
        assertTrue(record.getId() != 0L);
        assertTrue(HibernateFactory.getSession().contains(record));
    }

    /**
     * Test reloading an object
     */
    @Test
    public void testReload() {
        final String testReload = "testReload";
        assertNull(TestFactory.lookupByFoobar(testReload));

        // insert the record
        TestInterface record = TestFactory.createTest();
        record.setFoobar(testReload);
        TestFactory.save(record);
        Long id = record.getId();

        // evict and confirm record is detached
        TestUtils.flushAndEvict(record);
        assertFalse(HibernateFactory.getSession().contains(record));
        assertNotNull(id);

        // verify reload retrieves the same record but on a different instance
        TestInterface recordReloaded = TestUtils.reload(record);
        assertTrue(HibernateFactory.getSession().contains(recordReloaded));
        assertEquals(id, recordReloaded.getId());
        assertTrue(record != recordReloaded);
    }

    /**
     * Test updating a managed object
     */
    @Test
    public void testUpdateManaged() {
        String key = "updateTestDetached";

        //
        TestInterface obj = TestFactory.createTest();
        obj.setFoobar(key);
        obj.setPin(123);
        obj.setTestColumn("orig");
        TestFactory.save(obj);

        // verify lookup returns the same instance
        TestInterface result = TestFactory.lookupByFoobar(key);
        assertEquals(obj, result);

        // update fields on the managed instance - should persist
        result.setPin(null);
        result.setTestColumn("upd");

        // verify changes were persisted
        TestInterface updated = TestFactory.lookupByFoobar(key);
        assertNull(updated.getPin());
        assertEquals("upd", updated.getTestColumn());
    }

    @Test
    public void testUpdateDetached() {
        String key = "updateTestManaged";

        //
        TestInterface obj = TestFactory.createTest();
        obj.setFoobar(key);
        obj.setPin(123);
        obj.setTestColumn("orig");
        TestFactory.save(obj);

        // verify lookup returns the same instance
        TestUtils.flushAndEvict(obj);
        assertFalse(HibernateFactory.getSession().contains(obj));

        // re-attach the instance
        TestInterface managed = TestFactory.save(obj);
        assertTrue(HibernateFactory.getSession().contains(managed));

        // update fields on the managed instance - should persist
        managed.setPin(null);
        managed.setTestColumn("upd");

        // verify changes were persisted
        TestInterface updated = TestFactory.lookupByFoobar(key);
        assertNull(updated.getPin());
        assertEquals("upd", updated.getTestColumn());
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
            TestEntity managed = (TestEntity) TestFactory.save(entity);
            toDelete.add(managed);
        }

        //
        assertEquals(5, HibernateFactory.delete(toDelete, TestEntity.class));

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
     * Test creating a parent-child relationship
     * - Cascade persist from parent to child
     * - Verify bidirectional relationship
     *
     * Rule of thumb for these 1-N relationships:
     * - update both sides of the relationship in memory
     * - only persist the owner side _IF_ cascade is set
     * (owner side is the many-to-one side / has the @JoinColumn)
     */
    @Test
    public void testSuccessCreateRelationship() {
        String parentKey = "parent";
        String childKey = "child";

        TestInterface parent = TestFactory.createTest();
        parent.setFoobar(parentKey);

        TestInterface child = TestFactory.createTest();
        child.setFoobar(childKey);

        child.setParent((TestEntity) parent);
        parent.getChildren().add((TestEntity) child);

        // persisting the parent is enough to cascade to child
        //
        TestFactory.save(parent);

        // Verify relationship is bidirectional
        TestInterface fetchedChild = TestFactory.lookupByFoobar(childKey);
        assertEquals(parent.getId(), fetchedChild.getParent().getId());

        TestInterface fetchedParent = TestFactory.lookupByFoobar(parentKey);
        assertEquals(1, fetchedParent.getChildren().size());
        assertEquals(child.getId(), fetchedParent.getChildren().get(0).getId());
    }

    /**
     * Test creating a parent-child relationship without cascading
     */
    @Test
    public void testFailCreateRelationshipWhenNotCascading() {
        String parentKey = "parentFail";
        String childKey = "childFail";

        TestInterface parent = TestFactory.createTest();
        parent.setFoobar(parentKey);

        TestInterface child = TestFactory.createTest();
        child.setFoobar(childKey);

        child.setParent((TestEntity) parent);
        parent.getChildren().add((TestEntity) child);

        // persisting the parent is enough to cascade to child
        TestFactory.save(child);

        // Assert the issues with the relationship
        assertThrows(IllegalStateException.class, () -> {
            TestFactory.lookupByFoobar(childKey);
        });
        assertNull(parent.getId());
        assertThrows(IllegalStateException.class, () -> {
            TestFactory.lookupByFoobar(parentKey);
        });
    }

    /**
     * Test updating parent-child relationships
     * - Remove one child and add a new child
     * - Verify changes persisted correctly
     */
    @Test
    public void testUpdateRelationship() {
        TestInterface vito = TestFactory.lookupByFoobar("vito");
        TestInterface sonny = TestFactory.lookupByFoobar("sonny");
        assertEquals(3, vito.getChildren().size());
        Set<TestInterface> vitoChildren = Set.copyOf(vito.getChildren());
        assertTrue(vitoChildren.contains(sonny));


        // remove sunny from vito's children
        vito.getChildren().remove(sonny);
        sonny.setParent(null);

        // create a new child and add to vito
        TestEntity connie = new TestEntity();
        connie.setFoobar("connie");
        connie.setParent((TestEntity) vito);
        vito.getChildren().add(connie);

        //
        HibernateFactory.getSession().flush();
        TestFactory.closeSession();
        assertFalse(HibernateFactory.getSession().contains(vito));
        assertFalse(HibernateFactory.getSession().contains(connie));

        // verify changes persisted
        TestInterface vitoReloaded = TestFactory.lookupByFoobar("vito");
        assertEquals(3, vitoReloaded.getChildren().size());
        TestInterface connieReloaded = TestFactory.lookupByFoobar("connie");
        Set<TestInterface> vitoChildrenReloaded = Set.copyOf(vitoReloaded.getChildren());
        assertFalse(vitoChildrenReloaded.contains(sonny));
        assertTrue(vitoChildrenReloaded.contains(connieReloaded));
    }
}
