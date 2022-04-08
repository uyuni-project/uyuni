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
package com.redhat.rhn.manager.rhnset.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RhnSetDeclTest - Simple set of Unit tests that exercise
 * the SQL syntax of the queries used by RhnSetDeclTest.  Not
 * a full logic test because this only inserts fake -1 IDs
 * but it at least verifies that the SQL is runable and will filter
 * out bad IDs placed in the set.
 *
 */
public class RhnSetDeclTest extends RhnBaseTestCase {

    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetUnownedSystems() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);
        Server s = ServerFactoryTest.createTestServer(user, true);
        testBadAndGoodIds(RhnSetDecl.SYSTEMS_AFFECTED, s.getId());
    }

    private void testBadAndGoodIds(RhnSetDecl declIn, Long goodId) throws Exception {
        RhnSet set = declIn.get(user);
        Long badId = (long) -1;
        set.addElement(goodId);
        set.addElement(badId);
        assertTrue(set.contains(goodId));
        assertTrue(set.contains(badId));
        RhnSetManager.store(set);
        set = declIn.get(user);
        assertTrue(set.contains(goodId));
        assertFalse(set.contains(badId));

    }

}
