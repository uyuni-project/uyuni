/**
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

import com.redhat.rhn.GlobalInstanceHolder;
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
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.calls.LocalCall;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class AnsibleHandlerTest extends BaseHandlerTestCase {

    private AnsibleHandler handler;

    private static TaskomaticApi taskomaticApi;
    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private SaltApi originalSaltApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CONTEXT.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ActionChainManager.setTaskomaticApi(getTaskomaticApi());
        handler = new AnsibleHandler();
        originalSaltApi = AnsibleManager.getSaltApi();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        AnsibleManager.setSaltApi(originalSaltApi);
    }

    public void testSchedulePlaybook() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = handler.schedulePlaybook(admin, "/path/to/myplaybook.yml", "/path/to/hosts",
                controlNode.getId().intValue(), scheduleDate);
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
    }

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

    public void testCreateInvalidAnsiblePath() {
        try {
            handler.createAnsiblePath(admin, Map.of());
            fail("An exception shold have been thrown");
        }
        catch (ValidationException e) {
            // expected
        }
    }

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
        assertEquals("/tmp/new-location", handler.lookupAnsiblePathById(admin, inventoryPath.getId().intValue()).getPath().toString());
    }

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

    public void testRemoveInvalidAnsiblePath() throws Exception {
        try {
            handler.lookupAnsiblePathById(admin, -1234);
            fail("An exception shold have been thrown");
        }
        catch (EntityNotExistsFaultException e) {
            //expected
        }
    }

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

    public void testFetchPlaybookContents() throws Exception {
        MinionServer controlNode = createAnsibleControlNode(admin);
        AnsiblePath playbookPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "playbook",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/playbooks"
                ));

        SaltApi saltApi = CONTEXT.mock(SaltApi.class);
        CONTEXT.checking(new Expectations() {{
            allowing(saltApi).callSync(with(any(LocalCall.class)), with(controlNode.getMinionId()));
            will(returnValue(Optional.of("playbook-content")));
        }});
        AnsibleManager.setSaltApi(saltApi);
        assertEquals(
                "playbook-content",
                handler.fetchPlaybookContents(admin, playbookPath.getId().intValue(), "tmp/123"));
    }

    private MinionServer createAnsibleControlNode(User user) throws Exception {
        SystemEntitlementManager entitlementManager = GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        ServerArch a = ServerFactory.lookupServerArchByName("x86_64");
        server.setServerArch(a);
        TestUtils.saveAndFlush(server);
        entitlementManager.addEntitlementToServer(server, EntitlementManager.ANSIBLE_CONTROL_NODE);
        return server;
    }

    private TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = CONTEXT.mock(TaskomaticApi.class);
            CONTEXT.checking(new Expectations() {
                {
                    allowing(taskomaticApi)
                            .scheduleActionExecution(with(any(Action.class)));
                }
            });
        }

        return taskomaticApi;
    }
}
