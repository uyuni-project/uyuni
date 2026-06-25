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
package com.redhat.rhn.manager.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * ServerGroupManagerTest
 */
public class ServerGroupManagerTest extends BaseTestCaseWithUser {
    private static final String NAME = "Foo1";
    private static final String DESCRIPTION = "Test Foo1";
    public static final String TEST_DIFF_USER = "testDiffUser";

    private ServerGroupManager manager;

    @BeforeEach
    public void setUp() throws Exception {
        manager = new ServerGroupManager(new TestSaltApi());
    }

    @Test
    public void testCreate() {
        try {
            manager.create(getTestUser(), NAME, DESCRIPTION);
            String msg = "Unprivileged user creates a servergroup." +
                            "Only a user with Sys Group Admin privilege " +
                            " should be able to create/remove a system group.";
            fail(msg);
        }
        catch (Exception e) {
            //Great... No privilege won't let you create a server group.
        }

        getTestUser().addToGroup(AccessGroupFactory.SYSTEM_GROUP_ADMIN);
        ServerGroup sg = manager.create(getTestUser(), NAME, DESCRIPTION);
        assertNotNull(sg);
        assertEquals(NAME, sg.getName());
        assertEquals(DESCRIPTION, sg.getDescription());
    }

    @Test
    public void testAccess() {
        getTestUser().addToGroup(AccessGroupFactory.SYSTEM_GROUP_ADMIN);
        ManagedServerGroup sg = manager.create(getTestUser(), NAME, DESCRIPTION);
        assertTrue(manager.canAccess(getTestUser(), sg));

        User newUser = UserTestUtils.createUser(TEST_DIFF_USER, getTestUser().getOrg().getId());
        assertFalse(manager.canAccess(newUser, sg));
        List admins = new ArrayList<>();
        admins.add(newUser);
        manager.associateAdmins(sg, admins, getTestUser());
        assertTrue(manager.canAccess(newUser, sg));

        manager.dissociateAdmins(sg, admins, getTestUser());
        assertFalse(manager.canAccess(newUser, sg));

        User orgAdmin = UserTestUtils.createUser(TEST_DIFF_USER, getTestUser().getOrg().getId());
        orgAdmin.addPermanentRole(RoleFactory.ORG_ADMIN);
        assertTrue(manager.canAccess(orgAdmin, sg));
    }

    @Test
    public void testRemove() {
        getTestUser().addToGroup(AccessGroupFactory.SYSTEM_GROUP_ADMIN);
        ManagedServerGroup sg = manager.create(getTestUser(), NAME, DESCRIPTION);
        sg = TestUtils.reload(sg);
        User newUser = UserTestUtils.createUser(TEST_DIFF_USER, getTestUser().getOrg().getId());
        try {
            manager.remove(newUser, sg);
            fail("Permission error. Can't remove if you don't have access");
        }
        catch (Exception e) {
            //passed
        }

        List admins = new ArrayList<>();
        admins.add(newUser);
        manager.associateAdmins(sg, admins, getTestUser());
        try {
            manager.remove(newUser, sg);
            fail("Permission error. Can't remove if you are not Sys Group Admin");
        }
        catch (Exception e) {
            //passed
        }

        manager.dissociateAdmins(sg, admins, getTestUser());
        getTestUser().addToGroup(AccessGroupFactory.SYSTEM_GROUP_ADMIN);
        try {
            manager.remove(newUser, sg);
            fail("Permission error. Can't remove if you don't have access");
        }
        catch (Exception e) {
            //passed
        }

        manager.remove(getTestUser(), sg);
        try {
            manager.lookup(sg.getId(), getTestUser());
            fail("Group Not Found Exception not thrown");
        }
        catch (Exception e) {
            //Group Not FOund exception thrown
        }

    }

    @Test
    public void testListNoAssociatedAdmins() {
        getTestUser().addToGroup(AccessGroupFactory.SYSTEM_GROUP_ADMIN);
        ServerGroup sg = manager.create(getTestUser(), NAME, DESCRIPTION);
        TestUtils.flushAndEvict(sg);
        try {
            manager.listNoAdminGroups(getTestUser());
            fail("ORG ADmin permission needed for this!");
        }
        catch (Exception e) {
          //passed
        }
        getTestUser().addPermanentRole(RoleFactory.ORG_ADMIN);
        Collection<ServerGroup> groups = manager.listNoAdminGroups(getTestUser());

        int initSize = groups.size();
        ServerGroup sg1 = ServerGroupFactory.create(NAME + "ALPHA", DESCRIPTION,
                getTestUser().getOrg());
        TestUtils.flushAndEvict(sg1);

        Collection<ServerGroup> groups1 = manager.listNoAdminGroups(getTestUser());
        assertEquals(initSize + 1, groups1.size());
        groups.add(sg1);
        assertEquals(new HashSet<>(groups), new HashSet<>(groups1));

    }

    @Test
    public void testAddRemoveAdmins() {
        getTestUser().addToGroup(AccessGroupFactory.SYSTEM_GROUP_ADMIN);
        ManagedServerGroup sg = manager.create(getTestUser(), NAME, DESCRIPTION);
        User newUser = UserTestUtils.createUser(TEST_DIFF_USER, getTestUser().getOrg().getId());
        List admins = new ArrayList<>();
        admins.add(newUser);
        manager.associateAdmins(sg, admins, getTestUser());

        Set expected = new HashSet<>(admins);
        expected.add(getTestUser());
        assertEquals(expected, sg.getAssociatedAdminsFor(getTestUser()));

        User orgAdmin = UserTestUtils.createUser(TEST_DIFF_USER, getTestUser().getOrg().getId());
        orgAdmin.addPermanentRole(RoleFactory.ORG_ADMIN);
        List admins1 = new ArrayList<>();
        admins1.add(orgAdmin);
        manager.associateAdmins(sg, admins1, getTestUser());
        //even though we asked the
        //Manager to associate an org admin
        // we expect that sg.getAssociatedAdminsFor(user)
        // to give us only the  associated admins (No orgAdmin admins).
        assertEquals(expected, sg.getAssociatedAdminsFor(getTestUser()));

        manager.dissociateAdmins(sg, admins, getTestUser());
        expected.removeAll(admins);
        assertEquals(expected, sg.getAssociatedAdminsFor(getTestUser()));

        manager.dissociateAdmins(sg, admins1, getTestUser());
        assertEquals(expected, sg.getAssociatedAdminsFor(getTestUser()));
    }
}
