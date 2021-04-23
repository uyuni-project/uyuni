/**
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.manager.system.test;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.nio.file.Path;

public class AnsibleManagerTest extends BaseTestCaseWithUser {

    /**
     * Test saving and looking up {@link AnsiblePath} by given user
     *
     * @throws Exception
     */
    public void testSaveAndLookupAnsiblePath() throws Exception {
        MinionServer minion = createAnsibleControlNode(user);
        AnsiblePath path = new InventoryPath(minion);
        path.setPath(Path.of("/tmp/test1"));
        path = AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().evict( path);
        assertEquals(path, AnsibleManager.lookupAnsiblePathById(path.getId(), user).get());
    }

    /**
     * Test saving, listing and looking up {@link AnsiblePath} by an unauthorized user
     *
     * @throws Exception
     */
    public void testSaveAndLookupAnsiblePathNoPerms() throws Exception {
        User chuck = UserTestUtils.findNewUser("testUser", "testOrg" + this.getClass().getSimpleName());

        MinionServer minion = createAnsibleControlNode(user);
        AnsiblePath path = new InventoryPath(minion);
        path.setPath(Path.of("/tmp/test1"));

        try {
            AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", chuck);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }

        // now save with allowed user
        path = AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", user);

        try {
            AnsibleManager.lookupAnsiblePathById(path.getId(), chuck);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }

        try {
            AnsibleManager.listAnsiblePaths(minion.getId(), chuck);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Test updating non existing ansible path
     */
    public void testUpdateNonExistingAnsiblePath() {
        try {
            AnsibleManager.updateAnsiblePath(-12345, "/tmp/test", user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Test updating an existing ansible path
     *
     * @throws Exception
     */
    public void testUpdateAnsiblePath() throws Exception {
        MinionServer minion = createAnsibleControlNode(user);
        AnsiblePath path = AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", user);
        path = AnsibleManager.updateAnsiblePath(path.getId(), "/tmp/test-updated", user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().evict(path);
        AnsiblePath updated = AnsibleManager.lookupAnsiblePathById(path.getId(), user).get();
        assertEquals(path, updated);
    }

    public void testCreateAnsiblePathNormalSystem() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        try {
            AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Tests creating an {@link AnsiblePath} with a relative path. This is forbidden.
     *
     * @throws Exception
     */
    public void testSaveAnsibleRelativePath() throws Exception {
        MinionServer minion = createAnsibleControlNode(user);
        try {
            AnsibleManager.createAnsiblePath("inventory", minion.getId(), "relative/path", user);
            fail("An exception should have been thrown.");
        }
        catch (ValidatorException e) {
            // expected
        }
    }

    /**
     * Tests fetching non existing playbook path
     */
    public void testFetchPlaybookInvalidPath() {
        try {
            AnsibleManager.fetchPlaybookContents(-1234, "path/to/playbook", user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Tests fetching playbook path using an absolute path
     */
    public void testFetchPlaybookAbsolutePath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath path = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", user);

        try {
            AnsibleManager.fetchPlaybookContents(path.getId(), "/absolute", user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    private static MinionServer createAnsibleControlNode(User user) throws Exception {
        SystemEntitlementManager entitlementManager = GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        ServerArch a = ServerFactory.lookupServerArchByName("x86_64");
        server.setServerArch(a);
        TestUtils.saveAndFlush(server);
        entitlementManager.addEntitlementToServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE);
        return server;
    }
}
