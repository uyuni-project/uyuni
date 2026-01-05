/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.access.AccessGroup;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseHandlerTestCase extends RhnBaseTestCase {
    /*
     * admin - Org Admin
     * regular - retgular user
     * adminKey/regularKey - session keys for respective users
     */

    protected User admin;
    protected User regular;
    protected User satAdmin;
    protected String adminKey;
    protected String regularKey;
    protected String satAdminKey;
    private boolean committed;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        committed = false;

        admin = new UserTestUtils.UserBuilder()
                .userName(TestStatics.TEST_ADMIN_USER)
                .orgAdmin(true)
                .build();
        regular = new UserTestUtils.UserBuilder().orgId(admin.getOrg().getId()).build();
        satAdmin = UserTestUtils.createUser(TestStatics.TEST_SAT_USER, admin.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        UserFactory.save(satAdmin);

        assertTrue(admin.hasRole(RoleFactory.ORG_ADMIN));
        assertFalse(regular.hasRole(RoleFactory.ORG_ADMIN));
        assertTrue(satAdmin.hasRole(RoleFactory.SAT_ADMIN));

        //setup session keys
        adminKey = XmlRpcTestUtils.getSessionKey(admin);
        regularKey = XmlRpcTestUtils.getSessionKey(regular);
        satAdminKey = XmlRpcTestUtils.getSessionKey(satAdmin);

        //make sure the test org has the channel admin role
        Org org = admin.getOrg();
        org.addRole(RoleFactory.CHANNEL_ADMIN);
        org.addRole(RoleFactory.SYSTEM_GROUP_ADMIN);

        // Setup configuration for kickstart tests (mock cobbler etc.)
        KickstartDataTest.setupTestConfiguration(admin);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // If at some point we created a user and committed the transaction, we need
        // clean up our mess
        if (committed) {
            UserFactory.deleteUser(regular.getId());
            UserFactory.deleteUser(satAdmin.getId());
            OrgFactory.deleteOrg(admin.getOrg().getId(), admin);
            commitAndCloseSession();
        }
        committed = false;
    }

    // If we have to commit in mid-test, set up the next transaction correctly
    protected void commitHappened() {
        committed = true;
    }

    protected void addAccessGroup(User user, AccessGroup group) {
        user.addToGroup(group);
    }
}
