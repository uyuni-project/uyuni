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

package com.redhat.rhn.frontend.xmlrpc.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ansible.PlaybookAction;
import com.redhat.rhn.domain.action.ansible.PlaybookActionDetails;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.utils.Xor;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@ExtendWith(JUnit5Mockery.class)
class AnsibleHandlerTest extends BaseHandlerTestCase {

    private AnsibleHandler handler;

    private static TaskomaticApi taskomaticApi;

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private SaltApi saltApi;

    @BeforeEach
    public void setUp() throws Exception {
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ActionChainManager.setTaskomaticApi(getTaskomaticApi());

        saltApi = context.mock(SaltApi.class);
        AnsibleManager manager = new AnsibleManager(saltApi);
        handler = new AnsibleHandler(manager);
    }

    @Test
    void testSchedulePlaybook() {
        MinionServer controlNode = createAnsibleControlNode(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = handler.schedulePlaybook(admin, "/path/to/my-playbook.yml", "/path/to/hosts",
                controlNode.getId().intValue(), scheduleDate, null, false);
        assertNotNull(actionId);

        DataResult<ScheduledAction> schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, schedule.get(0).getId());

        // Look up the action and verify the details
        PlaybookAction action = (PlaybookAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertInstanceOf(PlaybookAction.class, action);
        assertEquals(scheduleDate, action.getEarliestAction());

        PlaybookActionDetails details = action.getDetails();
        assertNotNull(details);
        assertEquals("/path/to/my-playbook.yml", details.getPlaybookPath());
        assertEquals("/path/to/hosts", details.getInventoryPath());
        assertFalse(details.isTestMode());
    }

    @Test
    void testSchedulePlaybookTestMode() {
        MinionServer controlNode = createAnsibleControlNode(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = handler.schedulePlaybook(admin, "/path/to/my-playbook.yml", null,
                controlNode.getId().intValue(), scheduleDate, null, true,
                Map.of(AnsibleHandler.ANSIBLE_FLUSH_CACHE, true, AnsibleHandler.ANSIBLE_EXTRA_VARS, "{test: 123}"));
        assertNotNull(actionId);

        DataResult<ScheduledAction> schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, schedule.get(0).getId());

        // Look up the action and verify the details
        PlaybookAction action = (PlaybookAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertInstanceOf(PlaybookAction.class, action);
        assertEquals(scheduleDate, action.getEarliestAction());

        PlaybookActionDetails details = action.getDetails();
        assertNotNull(details);
        assertEquals("/path/to/my-playbook.yml", details.getPlaybookPath());
        assertEquals("{test: 123}", details.getExtraVarsContents());
        assertNull(details.getInventoryPath());
        assertTrue(details.isTestMode());
        assertTrue(details.isFlushCache());
    }

    @Test
    void testCreateAndGetAnsiblePath() {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/test"
                ));

        AnsiblePath playbookPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "playbook",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/playbooks"
                ));

        TestUtils.assertContains(
                handler.listAnsiblePaths(admin, controlNode.getId().intValue()),
                inventoryPath
        );
        TestUtils.assertContains(
                handler.listAnsiblePaths(admin, controlNode.getId().intValue()),
                playbookPath
        );

        assertEquals(inventoryPath, handler.lookupAnsiblePathById(admin, inventoryPath.getId().intValue()));
        assertEquals(playbookPath, handler.lookupAnsiblePathById(admin, playbookPath.getId().intValue()));
    }

    @Test
    void testCreateInvalidAnsiblePath() {
        assertThrows(ValidationException.class, () -> handler.createAnsiblePath(admin, Map.of()));
    }

    @Test
    void testUpdateAnsiblePath() {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/test"
                ));

        handler.updateAnsiblePath(admin, inventoryPath.getId().intValue(), Map.of("path", "/tmp/new-location"));
        assertEquals("/tmp/new-location",
                handler.lookupAnsiblePathById(admin, inventoryPath.getId().intValue()).getPath().toString());
    }

    @Test
    void testUpdateInvalidAnsiblePath() {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/test"
                ));

        int pathId = inventoryPath.getId().intValue();
        Map<String, Object> props = Map.of("my-path", "/tmp/new-location");
        assertThrows(ValidationException.class, () -> handler.updateAnsiblePath(admin, pathId, props));
    }

    @Test
    void testRemoveAnsiblePath() {
        MinionServer controlNode = createAnsibleControlNode(admin);

        AnsiblePath inventoryPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "inventory",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/ansible/test"
                ));

        int result = handler.removeAnsiblePath(admin, inventoryPath.getId().intValue());
        assertEquals(1, result);

        int pathId = inventoryPath.getId().intValue();
        assertThrows(EntityNotExistsFaultException.class, () -> handler.lookupAnsiblePathById(admin, pathId));
    }

    @Test
    void testRemoveInvalidAnsiblePath() {
        assertThrows(EntityNotExistsFaultException.class, () -> handler.lookupAnsiblePathById(admin, -1234));
    }

    @Test
    void testFetchPlaybookContentsInvalidPath() {
        createAnsibleControlNode(admin);

        assertThrows(EntityNotExistsFaultException.class, () -> handler.fetchPlaybookContents(admin, -123, "tmp/123"));
    }

    @Test
    void testFetchPlaybookContentsInvalidRelPath() {
        MinionServer controlNode = createAnsibleControlNode(admin);
        AnsiblePath playbookPath = handler.createAnsiblePath(
                admin,
                Map.of(
                        "type", "playbook",
                        "server_id", controlNode.getId().intValue(),
                        "path", "/etc/playbooks"
                ));

        int pathId = playbookPath.getId().intValue();
        assertThrows(InvalidArgsException.class,
            () -> handler.fetchPlaybookContents(admin, pathId, "/tmp/123"));
    }

    @Test
    void testFetchPlaybookContents() {
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

    private MinionServer createAnsibleControlNode(User user) {
        return ServerTestUtils.createAnsibleControlNode(user, saltApi, context);
    }

    private TaskomaticApi getTaskomaticApi() {
        if (taskomaticApi != null) {
            return taskomaticApi;
        }

        taskomaticApi = context.mock(TaskomaticApi.class);
        context.checking(new Expectations() {{
            try {
                allowing(taskomaticApi).scheduleActionExecution(with(any(Action.class)));
            }
            catch (TaskomaticApiException e) {
                throw new IllegalStateException("Unable to mock taskomatic api", e);
            }
        }});

        return taskomaticApi;
    }
}
