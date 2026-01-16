/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.sshservice;

import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionFactoryTest;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.taskomatic.task.checkin.SystemSummary;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapperTest;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.TestSystemQuery;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * SSHServiceWorkerTest
 */
public class SSHServiceWorkerTest extends JMockBaseTestCaseWithUser {

    private Logger logger = LogManager.getLogger(SSHMinionBootstrapperTest.class);

    private MinionServer minion;
    private SystemSummary sshPushSystemMock;
    private SaltSSHService saltSSHServiceMock;
    private SystemInfo sampleSystemInfo;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        sshPushSystemMock = mock(SystemSummary.class);
        saltSSHServiceMock = mock(SaltSSHService.class);
        minion = MinionServerFactoryTest.createTestMinionServer(user);
        // Create script action directory
        File scriptDir = new File(SUMA_STATE_FILES_ROOT_PATH + "/" + SCRIPTS_DIR);
        if (!scriptDir.exists()) {
            scriptDir.mkdirs();
        }
        String jsonResult = TestUtils.readAll(TestUtils.findTestData("minion.startup.applied.state.response.json"));
        sampleSystemInfo = Json.GSON.fromJson(jsonResult, SystemInfo.class);
    }

    /**
     * Tests that the worker updates uptime of the minion and cleans its pending
     * reboot action after a successful uptime value retrieval.
     * Future reboot actions, on the other hand, should remain untouched.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testUptimeUpdatedAfterReboot() throws Exception {
        minion.setLastBoot(1L); // last boot is long time in the past
        Action action = createRebootAction(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusPickedUp, 5L);
        ActionFactory.save(action);
        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Boolean> ping(String minionId) {
                return Optional.of(true);
            }
        };

        SSHServiceWorker worker = successWorker(new TestSystemQuery(), saltApi);

        context().checking(new Expectations() {{

            oneOf(sshPushSystemMock).getId();
            will(returnValue(minion.getId()));

            oneOf(sshPushSystemMock).isRebooting();
            will(returnValue(false));

            Map<String, Result<SystemInfo>> systemInfoMap = new HashMap<>();
            systemInfoMap.put(minion.getMinionId(),  new Result<>(Xor.right(sampleSystemInfo)));
            allowing(saltSSHServiceMock).callSyncSSH(with(any(LocalCall.class)),
                    with(any(MinionList.class)));
            will(returnValue(systemInfoMap));
        }});

        worker.setParentQueue(mockQueue());
        worker.run();

        assertTrue(serverAction.isStatusCompleted());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(Long.valueOf(0L), serverAction.getResultCode());
        assertEquals("Reboot completed.", serverAction.getResultMsg());
        assertTrue(minion.getLastBoot() > 1L);
    }

    /**
     * Tests that the worker will update uptime of the minion and will clean its pending
     * reboot action and a 'picked up' reboot action from past after a successful uptime
     * value retrieval.
     *
     * Moreover it verifies that
     *
     *  Tests that the worker will clean
     * Tests that an attempt to execute action that has been already completed will not
     * invoke any salt calls and that the state of the action doesn't change.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testOldRebootActionsAreCleanedUp() throws Exception {
        minion.setLastBoot(1L); // last boot is long time in the past
        // very old reboot action
        Action oldAction = createRebootAction(new Date(1L));
        ServerAction oldServerAction = createChildServerAction(oldAction, ServerAction::setStatusPickedUp, 5L);
        ActionFactory.save(oldAction);

        // very new reboot action, shouldn't be cleaned
        Action futureAction = createRebootAction(
                Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
        ServerAction futureServerAction = createChildServerAction(futureAction, ServerAction::setStatusQueued, 5L);
        ActionFactory.save(futureAction);

        // action to be picked up
        Action upcomingAction = createRebootAction(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        ServerAction upcomingServerAction = createChildServerAction(upcomingAction, ServerAction::setStatusPickedUp,
                5L);
        ActionFactory.save(upcomingAction);

        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Boolean> ping(String minionId) {
                return Optional.of(true);
            }
        };
        SSHServiceWorker worker = successWorker(new TestSystemQuery(), saltApi);

        context().checking(new Expectations() {{

            oneOf(sshPushSystemMock).getId();
            will(returnValue(minion.getId()));

            oneOf(sshPushSystemMock).isRebooting();
            will(returnValue(false));

            Map<String, Result<SystemInfo>> systemInfoMap = new HashMap<>();
            systemInfoMap.put(minion.getMinionId(),  new Result<>(Xor.right(sampleSystemInfo)));
            allowing(saltSSHServiceMock).callSyncSSH(with(any(LocalCall.class)),
                    with(any(MinionList.class)));
            will(returnValue(systemInfoMap));
        }});

        worker.setParentQueue(mockQueue());
        worker.run();

        // assertions
        assertRebootCompleted(upcomingServerAction);
        assertRebootCompleted(oldServerAction);
        assertTrue(futureServerAction.isStatusQueued());
        assertEquals(Long.valueOf(5L), futureServerAction.getRemainingTries());
        assertNull(futureServerAction.getResultCode());
        assertTrue(minion.getLastBoot() > 1L);

        // The worker commits, so mark the session for cleanup
        commitHappened();
    }

    @Test
    public void testSystemIsRebooting() throws Exception {
        // action to be picked up
        Action upcomingAction = createRebootAction(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        ServerAction upcomingServerAction = createChildServerAction(upcomingAction, ServerAction::setStatusPickedUp,
                5L);
        ActionFactory.save(upcomingAction);

        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Boolean> ping(String minionId) {
                return Optional.of(true);
            }

            @Override
            public Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) {
                Map<String, Result<Map<String, String>>> result = new HashMap<>();
                Map<String, String> values = new HashMap<>();
                values.put("ssh_extra_filerefs", "salt://foobar");
                values.put("next_action_id", "123");
                values.put("next_chunk", "actionchain_2.sls");
                result.put(minion.getMinionId(), Result.success(values));
                return result;
            }
        };

        SSHServiceWorker worker = successWorker(new TestSystemQuery(), saltApi);

        context().checking(new Expectations() {{
            oneOf(saltSSHServiceMock).cleanPendingActionChainAsync(with(any(MinionServer.class)));

            oneOf(sshPushSystemMock).getId();
            will(returnValue(minion.getId()));

            oneOf(sshPushSystemMock).isRebooting();
            will(returnValue(true));

            Map<String, Result<SystemInfo>> systemInfoMap = new HashMap<>();
            systemInfoMap.put(minion.getMinionId(),  new Result<>(Xor.right(sampleSystemInfo)));
            allowing(saltSSHServiceMock).callSyncSSH(with(any(LocalCall.class)),
                    with(any(MinionList.class)));
            will(returnValue(systemInfoMap));
        }});

        worker.setParentQueue(mockQueue());
        worker.run();

        assertRebootCompleted(upcomingServerAction);
    }

    private void assertRebootCompleted(ServerAction serverAction) {
        assertTrue(serverAction.isStatusCompleted());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(Long.valueOf(0L), serverAction.getResultCode());
        assertEquals("Reboot completed.", serverAction.getResultMsg());
    }

    private Action createRebootAction(Date earliestAction) {
        Action action = ActionFactory.createAction(ActionFactory.TYPE_REBOOT);
        action.setOrg(user.getOrg());
        action.setEarliestAction(earliestAction);
        return action;
    }

    private ServerAction createChildServerAction(Action action, Consumer<ServerAction> statusSetter,
                                                 long remainingTries) {
        ServerAction serverAction = ActionFactoryTest.createServerAction(minion, action);
        statusSetter.accept(serverAction);
        serverAction.setRemainingTries(remainingTries);
        action.setServerActions(Collections.singleton(serverAction));
        return serverAction;
    }

    private TaskQueue mockQueue() {
        TaskQueue mockQueue = mock(TaskQueue.class);
        context().checking(new Expectations() {{
            oneOf(mockQueue).workerStarting();
            oneOf(mockQueue).workerDone();
        }});
        return mockQueue;
    }

    private SSHServiceWorker successWorker(SystemQuery systemQuery, SaltApi saltApi) {
        SaltUtils saltUtils = new SaltUtils(systemQuery, saltApi);
        SaltKeyUtils saltKeyUtils = new SaltKeyUtils(saltApi);
        return new SSHServiceWorker(
                logger,
                sshPushSystemMock,
                saltApi,
                saltSSHServiceMock,
                new SaltServerActionService(saltApi, saltUtils, saltKeyUtils),
                saltUtils
        );
    }
}
