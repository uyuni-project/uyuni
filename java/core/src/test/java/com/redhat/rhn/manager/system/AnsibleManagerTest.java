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

package com.redhat.rhn.manager.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.salt.custom.AnsiblePlaybookSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.utils.Xor;

import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(JUnit5Mockery.class)
class AnsibleManagerTest extends BaseTestCaseWithUser {

    private SaltApi saltApi;
    private AnsibleManager ansibleManager;

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @BeforeEach
    public void setUp() throws Exception {
        saltApi = context.mock(SaltApi.class);
        ansibleManager = new AnsibleManager(saltApi);
    }

    @Test
    void testSaveAndLookupAnsiblePath() {
        MinionServer minion = createAnsibleControlNode(getTestUser());
        AnsiblePath path = ansibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", getTestUser());

        TestUtils.flushSession();
        TestUtils.evict(path);
        assertEquals(path, AnsibleManager.lookupAnsiblePathById(path.getId(), getTestUser()).get());
    }

    @Test
    void testSaveAndLookupAnsiblePathNoPerms() {
        User chuck = UserTestUtils.createUser(this);

        MinionServer minion = createAnsibleControlNode(getTestUser());
        Long minionId = minion.getId();

        assertThrows(LookupException.class,
            () -> ansibleManager.createAnsiblePath("inventory", minionId, "/tmp/test", chuck));

        // now save with allowed user
        AnsiblePath path = ansibleManager.createAnsiblePath("inventory", minionId, "/tmp/test", getTestUser());

        long pathId = path.getId();
        assertThrows(LookupException.class,
            () -> AnsibleManager.lookupAnsiblePathById(pathId, chuck));

        assertThrows(LookupException.class, () -> AnsibleManager.listAnsiblePaths(minionId, chuck));
    }

    @Test
    void testUpdateNonExistingAnsiblePath() {
        assertThrows(LookupException.class, () -> ansibleManager.updateAnsiblePath(-12345, "/tmp/test", getTestUser()));
    }

    @Test
    void testUpdateAnsiblePath() {
        MinionServer minion = createAnsibleControlNode(getTestUser());
        AnsiblePath path = ansibleManager.createAnsiblePath("inventory", minion.getId(), "/tmp/test", getTestUser());
        path = ansibleManager.updateAnsiblePath(path.getId(), "/tmp/test-updated", getTestUser());
        TestUtils.flushSession();
        TestUtils.evict(path);
        AnsiblePath updated = AnsibleManager.lookupAnsiblePathById(path.getId(), getTestUser()).get();
        assertEquals(path, updated);
    }

    @Test
    void testCreateAnsiblePathNormalSystem() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(getTestUser());
        Long minionId = minion.getId();

        assertThrows(LookupException.class,
            () -> ansibleManager.createAnsiblePath("inventory", minionId, "/tmp/test", getTestUser()));
    }

    @Test
    void testSaveAnsibleRelativePath() {
        MinionServer minion = createAnsibleControlNode(getTestUser());
        Long minionId = minion.getId();

        assertThrows(ValidatorException.class,
            () -> ansibleManager.createAnsiblePath("inventory", minionId, "relative/path", getTestUser()));
    }

    @Test
    void testFetchPlaybookInvalidPath() {
        assertThrows(LookupException.class,
            () -> ansibleManager.fetchPlaybookContents(-1234, "path/to/playbook",
                    getTestUser()));
    }

    @Test
    void testFetchPlaybookAbsolutePath() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath path =
                ansibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", getTestUser());
        Long pathId = path.getId();

        assertThrows(IllegalArgumentException.class,
            () -> ansibleManager.fetchPlaybookContents(pathId, "/absolute", getTestUser()));
    }

    @Test
    void testFetchPlaybook() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath path =
                ansibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", getTestUser());

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right("such-playbook-wow"))));
        }});

        assertEquals(
                Optional.of("such-playbook-wow"),
                ansibleManager.fetchPlaybookContents(path.getId(), "site.yml", getTestUser()));
    }

    @Test
    void testFetchPlaybookSaltNoResult() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath path =
                ansibleManager.createAnsiblePath("playbook", controlNode.getId(), "/root/playbooks", getTestUser());

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.left(false))));
        }});

        Long pathId = path.getId();

        var ex = assertThrows(IllegalStateException.class,
            () -> ansibleManager.fetchPlaybookContents(pathId, "site.yml", getTestUser()));
        assertEquals("no result", ex.getMessage());
    }

    @Test
    void testSchedulePlaybookBlankPath() {
        MinionServer minion = createAnsibleControlNode(getTestUser());

        Long minionId = minion.getId();
        Optional<String> actionChainLabel = Optional.empty();
        Date earliestDate = new Date();

        assertThrows(IllegalArgumentException.class, () -> AnsibleManager.schedulePlaybook(
            "   ",
            "/etc/ansible/hosts",
            minionId,
            false,
            false,
            "",
            earliestDate,
            actionChainLabel,
                getTestUser()
        ));
    }


    @Test
    void testSchedulePlaybookNonexistingMinion() {
        Optional<String> actionChainLabel = Optional.empty();
        Date earliestDate = new Date();

        assertThrows(LookupException.class, () -> AnsibleManager.schedulePlaybook(
            "/test/site.yml",
            "/etc/ansible/hosts",
            -1234,
            false,
            false,
            "",
            earliestDate,
            actionChainLabel,
                getTestUser()
        ));
    }

    @Test
    void testDiscoverPlaybooks() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath playbookPath =
                ansibleManager.createAnsiblePath("playbook", controlNode.getId(), "/tmp/test", getTestUser());

        var expected = Map.of(
            "/tmp/test", Map.of("site.yml", new AnsiblePlaybookSlsResult("/tmp/test/site.yml", "/tmp/test/hosts"))
        );

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right(expected))));
        }});

        var result = ansibleManager.discoverPlaybooks(playbookPath.getId(), getTestUser());
        assertEquals(Optional.of(expected), result);
    }

    @Test
    void testDiscoverPlaybooksSaltError() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath playbookPath =
                ansibleManager.createAnsiblePath("playbook", controlNode.getId(), "/tmp/test", getTestUser());

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.left("error"))));
        }});


        Long pathId = playbookPath.getId();
        var ex = assertThrows(IllegalStateException.class,
                () -> ansibleManager.discoverPlaybooks(pathId, getTestUser()));
        assertEquals("error", ex.getMessage());
    }

    @Test
    void testDiscoverPlaybooksNonExistingPath() {
        assertThrows(LookupException.class, () -> ansibleManager.discoverPlaybooks(-1234, getTestUser()));
    }

    @Test
    void testDiscoverPlaybooksInInventory() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath inventoryPath = ansibleManager.createAnsiblePath(
                "inventory", controlNode.getId(), "/tmp/test/hosts", getTestUser());

        Long pathId = inventoryPath.getId();
        assertThrows(IllegalArgumentException.class, () -> ansibleManager.discoverPlaybooks(pathId, getTestUser()));
    }

    @Test
    void testIntrospectInventory() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath inventoryPath = ansibleManager.createAnsiblePath(
                "inventory", controlNode.getId(), "/tmp/test/hosts", getTestUser());

        var expected = Map.of(
            "minion",  Map.of("all", Map.of("children", List.of("host1", "host2")))
        );

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right(expected))));
        }});

        var result = ansibleManager.introspectInventory(inventoryPath.getId(), getTestUser());
        assertEquals(Optional.of(expected), result);
    }

    @Test
    void testIntrospectInventorySaltError() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath inventoryPath = ansibleManager.createAnsiblePath(
                "inventory", controlNode.getId(), "/tmp/test/hosts", getTestUser());

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.left("error desc"))));
        }});

        Long pathId = inventoryPath.getId();
        var ex = assertThrows(IllegalStateException.class,
            () -> ansibleManager.introspectInventory(pathId, getTestUser()));
        assertEquals("error desc", ex.getMessage());
    }

    @Test
    void testIntrospectInventoryNonExistingPath() {
        assertThrows(LookupException.class, () -> ansibleManager.introspectInventory(-1234, getTestUser()));
    }

    /**
     * Test discover playbooks in an inventory path
     *
     * @throws Exception
     */
    @Test
    void testIntrospectInventoryInPlaybook() {
        MinionServer controlNode = createAnsibleControlNode(getTestUser());
        AnsiblePath playbookPath =
                ansibleManager.createAnsiblePath("playbook", controlNode.getId(), "/tmp/test", getTestUser());

        Long pathId = playbookPath.getId();
        assertThrows(IllegalArgumentException.class, () -> ansibleManager.introspectInventory(pathId, getTestUser()));
    }

    private MinionServer createAnsibleControlNode(User user) {
        return ServerTestUtils.createAnsibleControlNode(user, saltApi, context);
    }
}
