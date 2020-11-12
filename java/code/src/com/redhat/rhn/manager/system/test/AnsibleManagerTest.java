/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.salt.custom.AnsiblePlaybookSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.utils.Xor;

import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(JUnit5Mockery.class)
public class AnsibleManagerTest extends BaseTestCaseWithUser {

    private SaltApi saltApi;
    private AnsibleManager ansibleManager;

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        saltApi = context.mock(SaltApi.class);
        ansibleManager = new AnsibleManager(saltApi);
    }

    /**
     * Test saving and looking up {@link AnsiblePath} by given user
     *
     * @throws Exception
     */
    @Test
    public void testSaveAndLookupAnsiblePath() throws Exception {
        MinionServer minion = createAnsibleControlNode(user);
        AnsiblePath path = new InventoryPath(minion);
        path.setPath(Path.of("/tmp/test1"));
        path = AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().evict(path);
        assertEquals(path, AnsibleManager.lookupAnsiblePathById(path.getId(), user).get());
    }

    /**
     * Test saving, listing and looking up {@link AnsiblePath} by an unauthorized user
     *
     * @throws Exception
     */
    @Test
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
    @Test
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
    @Test
    public void testUpdateAnsiblePath() throws Exception {
        MinionServer minion = createAnsibleControlNode(user);
        AnsiblePath path = AnsibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", user);
        path = AnsibleManager.updateAnsiblePath(path.getId(), "/tmp/test-updated", user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().evict(path);
        AnsiblePath updated = AnsibleManager.lookupAnsiblePathById(path.getId(), user).get();
        assertEquals(path, updated);
    }

    @Test
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
    @Test
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
    @Test
    public void testFetchPlaybookInvalidPath() {
        try {
            ansibleManager.fetchPlaybookContents(-1234, "path/to/playbook", user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Tests fetching playbook path using an absolute path
     */
    @Test
    public void testFetchPlaybookAbsolutePath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath path = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", user);

        try {
            ansibleManager.fetchPlaybookContents(path.getId(), "/absolute", user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests fetching playbook contents
     */
    @Test
    public void testFetchPlaybook() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath path = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", user);

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right("suchplaybookwow"))));
        }});

        assertEquals(
                Optional.of("suchplaybookwow"),
                ansibleManager.fetchPlaybookContents(path.getId(), "site.yml", user));
    }

    /**
     * Tests fetching playbook contents
     */
    @Test
    public void testFetchPlaybookSaltNoResult() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath path = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", user);

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.left(false))));
        }});

        try {
            ansibleManager.fetchPlaybookContents(path.getId(), "site.yml", user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalStateException e) {
            assertEquals("no result", e.getMessage());
        }
    }

    /**
     * Test scheduling playbook with an empty path
     *
     * @throws Exception
     */
    @Test
    public void testSchedulePlaybookBlankPath() throws Exception {
        MinionServer minion = createAnsibleControlNode(user);
        try {
            AnsibleManager.schedulePlaybook("   ", "/etc/ansible/hosts", minion.getId(), false, false, new Date(),
                    Optional.empty(), user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Test scheduling playbook on an non-existing minion
     *
     * @throws Exception
     */
    @Test
    public void testSchedulePlaybookNonexistingMinion() throws Exception {
        try {
            AnsibleManager.schedulePlaybook("/test/site.yml", "/etc/ansible/hosts", -1234, false, false, new Date(),
                    Optional.empty(), user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Test discover playbooks
     *
     * @throws Exception
     */
    @Test
    public void testDiscoverPlaybooks() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath playbookPath = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/tmp/test", user);

        Map<String, Map<String, AnsiblePlaybookSlsResult>> expected = Map.of("/tmp/test", Map.of("site.yml",
                new AnsiblePlaybookSlsResult("/tmp/test/site.yml", "/tmp/test/hosts")));

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right(expected))));
        }});

        Optional<Map<String, Map<String, AnsiblePlaybookSlsResult>>> result =
                ansibleManager.discoverPlaybooks(playbookPath.getId(), user);
        assertEquals(Optional.of(expected), result);
    }

    /**
     * Test discover playbooks
     *
     * @throws Exception
     */
    @Test
    public void testDiscoverPlaybooksSaltError() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath playbookPath = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/tmp/test", user);

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.left("error"))));
        }});

        try {
            ansibleManager.discoverPlaybooks(playbookPath.getId(), user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalStateException e) {
            assertEquals("error", e.getMessage());
        }
    }

    /**
     * Test discover playbooks in an non-existing path
     *
     * @throws Exception
     */
    @Test
    public void testDiscoverPlaybooksNonExistingPath() throws Exception {
        try {
            ansibleManager.discoverPlaybooks(-1234, user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Test discover playbooks in an inventory path
     *
     * @throws Exception
     */
    @Test
    public void testDiscoverPlaybooksInInventory() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath inventoryPath = AnsibleManager.createAnsiblePath(
                "inventory", controlNode.getId(), "/tmp/test/hosts", user);
        try {
            ansibleManager.discoverPlaybooks(inventoryPath.getId(), user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Test introspect inventory
     */
    @Test
    public void testIntrospectInventory() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath inventoryPath = AnsibleManager.createAnsiblePath(
                "inventory", controlNode.getId(), "/tmp/test/hosts", user);

        Map<String, Map<String, Map<String, List<String>>>> expected =
                Map.of("minion",  Map.of("all", Map.of("children", List.of("host1", "host2"))));

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right(expected))));
        }});

        Optional<Map<String, Map<String, Object>>> result =
                ansibleManager.introspectInventory(inventoryPath.getId(), user);
        assertEquals(Optional.of(expected), result);
    }

    /**
     * Test introspect inventory
     */
    @Test
    public void testIntrospectInventorySaltError() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath inventoryPath = AnsibleManager.createAnsiblePath(
                "inventory", controlNode.getId(), "/tmp/test/hosts", user);

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.left("error desc"))));
        }});

        try {
            ansibleManager.introspectInventory(inventoryPath.getId(), user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalStateException e) {
            assertEquals("error desc", e.getMessage());
        }
    }

    /**
     * Test introspecting inventory in an non-existing path
     */
    @Test
    public void testIntrospectInventoryNonExistingPath() {
        try {
            ansibleManager.introspectInventory(-1234, user);
            fail("An exception should have been thrown.");
        }
        catch (LookupException e) {
            // expected
        }
    }

    /**
     * Test discover playbooks in an inventory path
     *
     * @throws Exception
     */
    @Test
    public void testIntrospectInventoryInPlaybook() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(user);
        AnsiblePath playbookPath = AnsibleManager.createAnsiblePath("playbook", controlNode.getId(), "/tmp/test", user);
        try {
            ansibleManager.introspectInventory(playbookPath.getId(), user);
            fail("An exception should have been thrown.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    private MinionServer createAnsibleControlNode(User user) throws Exception {
        VirtManager virtManager = new VirtManagerSalt(saltApi);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager groupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager entitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, groupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, groupManager)
        );

        context.checking(new Expectations() {{
            allowing(saltApi).refreshPillar(with(any(MinionList.class)));
        }});

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        ServerArch a = ServerFactory.lookupServerArchByName("x86_64");
        server.setServerArch(a);
        TestUtils.saveAndFlush(server);
        entitlementManager.addEntitlementToServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE);
        return server;
    }
}
