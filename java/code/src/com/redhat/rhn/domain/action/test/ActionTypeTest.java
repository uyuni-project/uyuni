/*
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
package com.redhat.rhn.domain.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

/**
 * ActionTypeTest
 */
public class ActionTypeTest extends RhnBaseTestCase {

    /**
     * Test Equals
     * @throws Exception something bad happened
     */
    @Test
    public void testEquals() throws Exception {
        ActionType r1 = new ActionType();
        ActionType r2 = null;
        assertNotEquals(r1, r2);
        r2 = lookupByLabel("reboot.reboot");
        r1 = lookupByLabel("reboot.reboot");
        assertEquals(r1, r2);
        ActionType r3 = lookupByLabel("packages.update");
        assertNotEquals(r1, r3);
        assertEquals(r1, r1);
    }

    /**
     * Test findByLabel query
     * This method can be used to test the
     * second level cache in hibernate. Turn on sql output
     * in the hibernate.properties file and make sure that
     * we're not going to the db twice
     * @throws Exception something bad happened
     */
    @Test
    public void testFindByLabel() throws Exception {
        ActionType r1 = lookupByLabel("errata.update");
        ActionType r2 = lookupByLabel("errata.update");
        assertEquals(r2.getName(), r1.getName());
        assertEquals(r2.getLabel(), r1.getLabel());
    }

    /**
     * Helper method to get a ActionType by label
     * @param label the label
     * @return Returns the ActionType corresponding to label
     */
    private ActionType lookupByLabel(String label) {
        Session session = HibernateFactory.getSession();
        return (ActionType) session.getNamedQuery("ActionType.findByLabel")
                                .setString("label", label)
                                //Retrieve from cache if there
                                .setCacheable(true)
                                .uniqueResult();
    }
}
