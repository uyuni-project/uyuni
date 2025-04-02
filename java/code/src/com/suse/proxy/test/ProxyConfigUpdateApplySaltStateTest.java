/*
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

package com.suse.proxy.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.proxy.update.ProxyConfigUpdateApplySaltState;
import com.suse.proxy.update.ProxyConfigUpdateContext;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
/**
 * Tests for ProxyConfigUpdateApplySaltState
 */
@SuppressWarnings({"java:S3599", "java:S1171"})
public class ProxyConfigUpdateApplySaltStateTest extends BaseTestCaseWithUser {
    private final ProxyConfigUpdateApplySaltState handler = new ProxyConfigUpdateApplySaltState();
    private static TaskomaticApi taskomaticApi;


    @SuppressWarnings({"java:S1171"})
    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ActionManager.setTaskomaticApi(getTaskomaticApi());
    }

    /**
     * Test the handle method when the action is successfully scheduled
     */
    @Test
    public void testApplyStates() throws NoSuchFieldException, IllegalAccessException, TaskomaticApiException {
        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        handler.handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());

        assertNotNull(proxyConfigUpdateContext.getAction());
        Action scheduledAction = ActionManager.lookupAction(user, proxyConfigUpdateContext.getAction().getId());
        assertNotNull(scheduledAction);
        assertEquals(scheduledAction, proxyConfigUpdateContext.getAction());
    }

    /**
     * Helper method to create a common ProxyConfigUpdateContext
     *
     * @return the ProxyConfigUpdateContext
     */
    private ProxyConfigUpdateContext getProxyConfigUpdateContext() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(null, null, user);
        proxyConfigUpdateContext.setProxyConfigFiles(new HashMap<>());
        proxyConfigUpdateContext.setProxyMinion(minion);
        return proxyConfigUpdateContext;
    }

    private TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = context.mock(TaskomaticApi.class);
            context.checking(new Expectations() {
                {
                    allowing(taskomaticApi).scheduleActionExecution(with(any(Action.class)));
                }
            });
        }
        return taskomaticApi;
    }

}
