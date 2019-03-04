/**
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
package com.redhat.rhn.taskomatic.task.sshpush.test;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.taskomatic.task.sshpush.SSHPushSystem;
import com.redhat.rhn.taskomatic.task.sshpush.SSHPushWorkerSalt;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.reactor.messaging.test.JobReturnEventMessageActionTest;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.utils.test.SSHMinionBootstrapperTest;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_QUEUED;
import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;

import com.redhat.rhn.common.hibernate.HibernateFactory;

/**
 * SSHPushWorkerSaltTest
 */
public class SSHPushWorkerSaltTest extends JMockBaseTestCaseWithUser {

    private Logger logger = Logger.getLogger(SSHMinionBootstrapperTest.class);

    private SSHPushWorkerSalt worker;
    private MinionServer minion;
    private SaltService saltServiceMock;
    private SSHPushSystem sshPushSystemMock;
    private SaltSSHService saltSSHServiceMock;
    private SystemInfo sampleSystemInfo;
    

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        saltServiceMock = mock(SaltService.class);
        sshPushSystemMock = mock(SSHPushSystem.class);
        saltSSHServiceMock = mock(SaltSSHService.class);
        worker = new SSHPushWorkerSalt(logger, sshPushSystemMock, saltServiceMock,
                saltSSHServiceMock, SaltServerActionService.INSTANCE);
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
    public void testUptimeUpdatedAfterReboot() throws Exception {
        minion.setLastBoot(1L); // last boot is long time in the past
        Action action = createRebootAction(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        ServerAction serverAction = createChildServerAction(action,
                ActionFactory.STATUS_PICKED_UP, 5L);
        ActionFactory.save(action);

        worker = successWorker();
        mockSyncCallResult();
       
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

        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
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
    public void testOldRebootActionsAreCleanedUp() throws Exception {
        minion.setLastBoot(1L); // last boot is long time in the past
        // very old reboot action
        Action oldAction = createRebootAction(new Date(1L));
        ServerAction oldServerAction = createChildServerAction(oldAction,
                ActionFactory.STATUS_PICKED_UP, 5L);
        ActionFactory.save(oldAction);

        // very new reboot action, shouldn't be cleaned
        Action futureAction = createRebootAction(
                Date.from(Instant.now().plus(5, ChronoUnit.DAYS)));
        ServerAction futureServerAction = createChildServerAction(futureAction,
                ActionFactory.STATUS_QUEUED, 5L);
        ActionFactory.save(futureAction);

        // action to be picked up
        Action upcomingAction = createRebootAction(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        ServerAction upcomingServerAction = createChildServerAction(upcomingAction,
                ActionFactory.STATUS_PICKED_UP, 5L);
        ActionFactory.save(upcomingAction);

        worker = successWorker();
        mockSyncCallResult();

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
        assertEquals(STATUS_QUEUED, futureServerAction.getStatus());
        assertEquals(Long.valueOf(5L), futureServerAction.getRemainingTries());
        assertNull(futureServerAction.getResultCode());
        assertTrue(minion.getLastBoot() > 1L);

        // explicitly remove any row created by the worker, as it commits
        OrgFactory.deleteOrg(user.getOrg().getId(), user);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    public void testSystemIsRebooting() throws Exception {
        // action to be picked up
        Action upcomingAction = createRebootAction(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)));
        ServerAction upcomingServerAction = createChildServerAction(upcomingAction,
                ActionFactory.STATUS_PICKED_UP, 5L);
        ActionFactory.save(upcomingAction);

        worker = successWorker();

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(MinionList.class)));

            Map<String, Result<Map<String, String>>> result = new HashMap<>();
            Map<String, String> values = new HashMap<>();
            values.put("ssh_extra_filerefs", "salt://foobar");
            values.put("next_action_id", "123");
            values.put("next_chunk", "actionchain_2.sls");
            result.put(minion.getMinionId(), Result.success(values));
            will(returnValue(result));

            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(returnValue(Optional.of(true)));

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
        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
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

    private ServerAction createChildServerAction(Action action, ActionStatus status,
            long remainingTries) throws Exception {
        return createChildServerAction(minion, action, status, remainingTries);
    }

    private ServerAction createChildServerAction(MinionServer minoin, Action action, ActionStatus status,
                                                 long remainingTries) throws Exception {
        ServerAction serverAction = ActionFactoryTest.createServerAction(minion, action);
        serverAction.setStatus(status);
        serverAction.setRemainingTries(remainingTries);
        action.setServerActions(Collections.singleton(serverAction));
        return serverAction;
    }

    private void mockSyncCallResult() {
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(returnValue(Optional.of(Boolean.TRUE)));
        }});
    }

    private TaskQueue mockQueue() {
        TaskQueue mockQueue = mock(TaskQueue.class);
        context().checking(new Expectations() {{
            oneOf(mockQueue).workerStarting();
            oneOf(mockQueue).workerDone();
        }});
        return mockQueue;
    }

    private SSHPushWorkerSalt successWorker() {
        SaltUtils saltUtils = new SaltUtils() {
            @Override
            public boolean shouldRefreshPackageList(String function,
                    Optional<JsonElement> callResult) {
                return false;
            }

            @Override
            public void updateServerAction(ServerAction serverAction, long retcode,
                    boolean success, String jid, JsonElement jsonResult, String function) {
                serverAction.setStatus(STATUS_COMPLETED);
            }
        };
        return new SSHPushWorkerSalt(logger, sshPushSystemMock, saltServiceMock, saltSSHServiceMock, SaltServerActionService.INSTANCE);
    }
}
