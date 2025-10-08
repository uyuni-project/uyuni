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
package com.redhat.rhn.domain.rhnpackage.profile.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.rhnpackage.profile.ProfileFactory;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileType;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * ProfileTypeTest
 */
public class ProfileTypeTest extends RhnBaseTestCase {

    /**
     * Test Equals method
     * @throws Exception something bad happened
     */
    @Test
    public void testEquals() throws Exception {
        ProfileType ptype1 = lookupByLabel("normal");
        ProfileType ptype2 = lookupByLabel("normal");
        ProfileType ptype3 = lookupByLabel("sync_profile");

        assertNotNull(ptype1);
        assertNotNull(ptype2);
        assertNotNull(ptype3);
        assertEquals(ptype1, ptype2);
        assertNotEquals(ptype1, ptype3);
        ptype2 = null;
        assertNotEquals(ptype1, ptype2);
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
        ProfileType r1 = lookupByLabel("normal");
        ProfileType r2 = lookupByLabel("normal");
        assertEquals(r2.getName(), r1.getName());
        assertEquals(r2.getLabel(), r1.getLabel());
    }

    /**
     * Helper method to get a ProfileType by label
     * @param label the label
     * @return Returns the ProfileType corresponding to label
     */
    public static ProfileType lookupByLabel(String label) {
        return ProfileFactory.lookupByLabel(label);
    }
}
