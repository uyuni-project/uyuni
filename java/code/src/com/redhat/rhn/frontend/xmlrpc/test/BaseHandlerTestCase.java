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
package com.redhat.rhn.frontend.xmlrpc.test;

import static com.suse.manager.webui.services.SaltConstants.SALT_CONFIG_STATES_DIR;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.SaltStateGeneratorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;

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
    protected Path tmpSaltRoot;
    private boolean committed;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        committed = false;

        admin = UserTestUtils.findNewUser("adminUser", "testOrg" + this.getClass().getSimpleName(), true);
        regular = UserTestUtils.createUser("testUser", admin.getOrg().getId());
        satAdmin = UserTestUtils.createUser("satUser", admin.getOrg().getId());
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

        tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot.toAbsolutePath());
        Files.createDirectory(tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR));
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

    protected void addRole(User user, Role role) {
        user.getOrg().addRole(role);
        user.addPermanentRole(role);
    }
}
