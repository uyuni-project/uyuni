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
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.TestUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;

import java.util.Date;

public class AnsibleHandlerTest extends BaseHandlerTestCase {

    private AnsibleHandler handler;

    private static TaskomaticApi taskomaticApi;
    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CONTEXT.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ActionChainManager.setTaskomaticApi(getTaskomaticApi());
        handler = new AnsibleHandler();
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
