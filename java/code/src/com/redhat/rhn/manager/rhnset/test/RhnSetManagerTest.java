/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.rhnset.test;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * RhnManagerTest
 * @version $Rev$
 */
public class RhnSetManagerTest extends RhnBaseTestCase {

    /** user id to be used when creating RhnSet tests */
    private Long userId = null;
    private TestSetCleanup cleanup;

    private static final String TEST_USER_NAME = "automated_test_user_jesusr";
    private static final String TEST_ORG_NAME = "automated_test_org_jesusr";

    protected void setUp() throws Exception {
        super.setUp();
        userId = UserTestUtils.createUser(TEST_USER_NAME, TEST_ORG_NAME);
        cleanup = new TestSetCleanup();
    }

    protected void tearDown() throws Exception {
        userId = null;
        cleanup = null;
        super.tearDown();
    }
    /**
     * Looks for an RhnSet for a non-existent user.
     */
    public void testGetByLabelInvalidUser() {
        RhnSet set = RhnSetManager.findByLabel(10L, "foo", cleanup);
        assertNull(set);
    }

    /**
     * Creates an RhnSet then verifies that it was stored in the db
     * by trying to fetch it again.
     * @throws Exception something bad happened
     */
    public void testCreateDeleteRhnSet() throws Exception {
        String label = "test_rhn_set_label";

        RhnSet set = RhnSetManager.createSet(userId, label, cleanup);
        set.addElement(1234L, 0L);
        assertNotNull(set);
        RhnSetManager.store(set);
        assertEquals(1, cleanup.callbacks);
        RhnSet foundSet = RhnSetManager.findByLabel(userId, label, cleanup);
        assertNotNull(foundSet);
        assertEquals(1, foundSet.getElements().size());

        // get rid of it.
        RhnSetManager.deleteByLabel(userId, label);
        assertNull(RhnSetManager.findByLabel(userId, label, cleanup));
        assertEquals(1, cleanup.callbacks);
    }

    /**
     * Creates an RhnSet, then Deletes verifies it was deleted.
     */
    public void testCreateDeleteMultipleRhnSet() {
        RhnSet set = RhnSetManager.createSet(userId,
                        "test_rhn_set_label_delete", cleanup);
        // need to add an element to make this work.
        set.addElement(1121L, 11L);
        set.addElement(1111L, 12L);
        set.addElement(1111L, null);
        assertNotNull(set);
        assertEquals(3, set.getElements().size());

        // store a new set in the DB.
        RhnSetManager.store(set);
        assertEquals(1, cleanup.callbacks);

        // let's try to find it, we should.
        RhnSet set1 = RhnSetManager.findByLabel(userId,
                        "test_rhn_set_label_delete", cleanup);
        assertNotNull(set1);
        assertEquals(3, set1.getElements().size());

        // let's delete the above set from the DB.
        RhnSetManager.deleteByLabel(userId,
                        "test_rhn_set_label_delete");

        // let's try to find it again, we better
        // not find anything.
        RhnSet set2 = RhnSetManager.findByLabel(userId,
                        "test_rhn_set_label_delete", cleanup);
        assertNull(set2);
        assertEquals(1, cleanup.callbacks);
    }

    /**
     * Tests the remove method of RhnSetManager
     */
    public void testCreateRemoveRhnSet() {
        RhnSet set = RhnSetManager.createSet(userId,
                        "test_rhn_set_label_remove", cleanup);
        set.addElement(42L, 10L);
        set.addElement(423L, 324L);
        assertNotNull(set);
        assertEquals(2, set.getElements().size());

        // store the new set in the DB.
        RhnSetManager.store(set);
        assertEquals(1, cleanup.callbacks);

        RhnSet set1 = RhnSetManager.findByLabel(userId,
                        "test_rhn_set_label_remove", cleanup);
        assertNotNull(set1);
        assertEquals(2, set.getElements().size());

        RhnSetManager.remove(set1);

        // let's try to find it again, we better
        // not find anything.
        RhnSet set2 = RhnSetManager.findByLabel(userId,
                        "test_rhn_set_label_remove", cleanup);
        assertNull(set2);
        assertEquals(1, cleanup.callbacks);
    }

    /**
     * Testing the store method of RhnSetManager
     * @throws Exception something bad happened
     */
    public void testStore() throws Exception {
        String label = "test_rhn_set_label_store";

        //Stores Set with null second element
        RhnSet set = RhnSetManager.createSet(userId, label, cleanup);
        set.addElement(Long.valueOf(31));
        set.addElement(Long.valueOf(464));
        RhnSetManager.store(set);
        assertEquals(1, cleanup.callbacks);

        //Deletes the previous and stores a new set
        //with one of the same elements
        RhnSet set2 = RhnSetManager.createSet(userId, label, cleanup);
        set2.addElement(Long.valueOf(57));
        set2.addElement(Long.valueOf(464)); //same as above
        RhnSetManager.store(set2);
        assertEquals(2, cleanup.callbacks);

        //Deletes the previous and stores a new set
        //with non-null second element
        RhnSet set3 = RhnSetManager.createSet(userId, label, cleanup);
        set3.addElement(31L, 11L);
        set3.addElement(464L, 236L);
        RhnSetManager.store(set3);
        assertEquals(3, cleanup.callbacks);

        //Deletes the previous and stores a new set
        //with one of the same elements
        RhnSet set4 = RhnSetManager.createSet(userId, label, cleanup);
        set4.addElement(46L, 87L);
        set4.addElement(31L, 11L); //same as above
        RhnSetManager.store(set4);
        assertEquals(4, cleanup.callbacks);

        //Attempts to store a set with two rows having the
        //same first element or same second element
        RhnSet set5 = RhnSetManager.createSet(userId, label, cleanup);
        set5.addElement(75L, 87L);
        set5.addElement(75L, 11L);
        set5.addElement(36L, 11L);
        RhnSetManager.store(set5);
        assertEquals(5, cleanup.callbacks);

        set = RhnSetManager.findByLabel(userId, label, cleanup);
        assertEquals(3, set.size());
        assertEquals(5, cleanup.callbacks);
    }

    public void testStoreElement3() throws Exception {
        String label = "test_rhn_set_store_element_3";

        // Tests storing something in element 3
        RhnSet set = RhnSetManager.createSet(userId, label, cleanup);
        set.addElement(11L, 22L, 33L);
        RhnSetManager.store(set);

        set = RhnSetManager.findByLabel(userId, label, cleanup);
        assertEquals(1, set.size());

        RhnSetElement element = set.getElements().iterator().next();
        assertEquals(Long.valueOf(11), element.getElement());
        assertEquals(Long.valueOf(22), element.getElementTwo());
        assertEquals(Long.valueOf(33), element.getElementThree());
    }

    public static final class TestSetCleanup extends SetCleanup {
        private int callbacks = 0;

        public TestSetCleanup() {
            super("test", "test");
        }

        protected int cleanup(RhnSet set) {
            return callbacks++;
        }
    }
}
