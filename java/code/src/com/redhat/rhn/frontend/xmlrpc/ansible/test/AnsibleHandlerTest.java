/*
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.ansible.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.PlaybookAction;
import com.redhat.rhn.domain.action.salt.PlaybookActionDetails;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.utils.Xor;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@ExtendWith(JUnit5Mockery.class)
public class AnsibleHandlerTest extends BaseHandlerTestCase {

    private AnsibleHandler handler;

    private static TaskomaticApi taskomaticApi;

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private SaltApi saltApi;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ActionChainManager.setTaskomaticApi(getTaskomaticApi());

        saltApi = context.mock(SaltApi.class);
        AnsibleManager manager = new AnsibleManager(saltApi);
        handler = new AnsibleHandler(manager);
    }

    @Test
    public void testSchedulePlaybook() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = handler.schedulePlaybook(admin, "/path/to/myplaybook.yml", "/path/to/hosts",
                controlNode.getId().intValue(), scheduleDate, null, false);
        assertNotNull(actionId);

        DataResult schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, ((ScheduledAction) schedule.get(0)).getId());

        // Look up the action and verify the details
        PlaybookAction action = (PlaybookAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertEquals(ActionFactory.TYPE_PLAYBOOK, action.getActionType());
        assertEquals(scheduleDate, action.getEarliestAction());

        PlaybookActionDetails details = action.getDetails();
        assertNotNull(details);
        assertEquals("/path/to/myplaybook.yml", details.getPlaybookPath());
        assertEquals("/path/to/hosts", details.getInventoryPath());
        assertFalse(details.isTestMode());
    }

    @Test
    public void testSchedulePlaybookTestMode() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = handler.schedulePlaybook(admin, "/path/to/myplaybook.yml", null, controlNode.getId().intValue(),
                scheduleDate, null, true, Collections.singletonMap(AnsibleHandler.ANSIBLE_FLUSH_CACHE, true));
        assertNotNull(actionId);

        DataResult schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, ((ScheduledAction) schedule.get(0)).getId());

        // Look up the action and verify the details
        PlaybookAction action = (PlaybookAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertEquals(ActionFactory.TYPE_PLAYBOOK, action.getActionType());
        assertEquals(scheduleDate, action.getEarliestAction());

        PlaybookActionDetails details = action.getDetails();
        assertNotNull(details);
        assertEquals("/path/to/myplaybook.yml", details.getPlaybookPath());
        assertNull(details.getInventoryPath());
        assertTrue(details.isTestMode());
        assertTrue(details.isFlushCache());
    }

    @Test
    public void testCreateAndGetAnsiblePath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/hosts"
                ));

        AnsiblePath playbookPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "playbook",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/playbooks"
                ));

        assertContains(
                handler.listAnsiblePaths(admin, controlNode.getId().intValue()),
                inventoryPath
        );
        assertContains(
                handler.listAnsiblePaths(admin, controlNode.getId().intValue()),
                playbookPath
        );

        assertEquals(inventoryPath, handler.lookupAnsiblePathById(admin, inventoryPath.getId().intValue()));
        assertEquals(playbookPath, handler.lookupAnsiblePathById(admin, playbookPath.getId().intValue()));
    }

    @Test
    public void testCreateInvalidAnsiblePath() {
        try {
            handler.createAnsiblePath(admin, Map.of());
            fail("An exception shold have been thrown");
        }
        catch (ValidationException e) {
            // expected
        }
    }

    @Test
    public void testUpdateAnsiblePath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/hosts"
                ));

        handler.updateAnsiblePath(admin, inventoryPath.getId().intValue(), Map.of("path", "/tmp/new-location"));
        assertEquals("/tmp/new-location",
                handler.lookupAnsiblePathById(admin, inventoryPath.getId().intValue()).getPath().toString());
    }

    @Test
    public void testUpdateInvalidAnsiblePath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/hosts"
                ));

        try {
            handler.updateAnsiblePath(admin, inventoryPath.getId().intValue(), Map.of("my-path", "/tmp/new-location"));
            fail("An exception shold have been thrown");
        }
        catch (ValidationException e) {
            // expected
        }
    }

    @Test
    public void testRemoveAnsiblePath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/hosts"
                ));

        int result = handler.removeAnsiblePath(admin, inventoryPath.getId().intValue());
        assertEquals(1, result);
        try {
            handler.lookupAnsiblePathById(admin, inventoryPath.getId().intValue());
            fail("An exception shold have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            //expected
        }
    }

    @Test
    public void testRemoveInvalidAnsiblePath() throws Exception {
        try {
            handler.lookupAnsiblePathById(admin, -1234);
            fail("An exception shold have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            //expected
        }
    }

    @Test
    public void testFetchPlaybookContentsInvalidPath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        try {
            handler.fetchPlaybookContents(admin, -123, "tmp/123");
            fail("An exception shold have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            // expected
        }
    }

    @Test
    public void testFetchPlaybookContentsInvalidRelPath() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        AnsiblePath playbookPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "playbook",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/playbooks"
                ));
        try {
            // try with absolute path
            handler.fetchPlaybookContents(admin, playbookPath.getId().intValue(), "/tmp/123");
            fail("An exception shold have been thrown");
        }
        catch (InvalidArgsException e) {
            // expected
        }
    }

    @Test
    public void testFetchPlaybookContents() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        AnsiblePath playbookPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "playbook",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/playbooks"
                ));

        context.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of(Xor.right("playbook-content"))));
        }});
        assertEquals(
                "playbook-content",
                handler.fetchPlaybookContents(admin, playbookPath.getId().intValue(), "tmp/123"));
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

    private TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = context.mock(TaskomaticApi.class);
            context.checking(new Expectations() {
                {
                    allowing(taskomaticApi)
                            .scheduleActionExecution(with(any(Action.class)));
                }
            });
        }

        return taskomaticApi;
    }
}
