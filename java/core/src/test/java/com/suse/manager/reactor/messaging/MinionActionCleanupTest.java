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
package com.suse.manager.reactor.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionFactoryTest;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.ServerActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.reflect.TypeToken;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for {@link JobReturnEventMessageAction}.
 */
public class MinionActionCleanupTest extends JMockBaseTestCaseWithUser {

    private enum HistoryScenario {
        PREREQUISITE_PENDING,
        PREREQUISITE_FAILED,
        REBOOT_NOT_NEEDED,
        REBOOT_COMPLETED,
        CONTINUATION_SCHEDULED,
        WAITING_FOR_REBOOT
    }

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Test the processing of packages.profileupdate job return event.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testMinionActionCleanup() throws Exception {
        // Prepare test objects: minion servers, products and action
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setMinionId("minion1");
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setMinionId("minion2");

        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(minion1.getId(), minion2.getId()),
                Collections.singletonList(ApplyStatesEventMessage.PACKAGES),
                Date.from(Instant.now().minus(6, ChronoUnit.MINUTES)));
        action.addServerAction(ActionFactoryTest.createServerAction(minion1, action));
        action.addServerAction(ActionFactoryTest.createServerAction(minion2, action));

        Map<String, Result<List<SaltUtil.RunningInfo>>> running = new HashMap<>();
        running.put(minion1.getMinionId(), new Result<>(Xor.right(Collections.emptyList())));
        running.put(minion2.getMinionId(), new Result<>(Xor.right(Collections.emptyList())));

        SaltService saltServiceMock = mock(SaltService.class);

        context().checking(new Expectations() { {
            allowing(saltServiceMock).running(with(any(MinionList.class)));
            will(returnValue(running));
            never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
            never(saltServiceMock).listJob(with(any(String.class)));
        } });

        SaltUtils saltUtils = new SaltUtils(saltServiceMock, saltServiceMock);
        MinionActionUtils minionActionUtils = new MinionActionUtils(saltServiceMock, saltUtils);
        minionActionUtils.cleanupMinionActions();
    }

    @Test
    public void testExpiredPickedUpActionWithoutHistoryIsFailed() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerAction serverAction = createOldPickedUpAction(minion);
        Long serverId = serverAction.getServerId();
        Long actionId = serverAction.getParentAction().getId();
        assertTrue(serverAction.getParentAction().getEarliestAction().toInstant()
                .isBefore(Instant.now().minus(1, ChronoUnit.HOURS)));

        assertEquals(1, ActionFactory.pendingMinionServerActions().stream()
                .filter(candidate -> candidate.getServerId().equals(serverId) &&
                        candidate.getParentAction().getId().equals(actionId))
                .count());

        runCleanup(minion);

        serverAction = reloadServerAction(minion, actionId);
        assertEquals(ActionFactory.STATUS_FAILED, serverAction.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = HistoryScenario.class, names = "WAITING_FOR_REBOOT", mode = EnumSource.Mode.EXCLUDE)
    public void testNonWaitingTransactionalHistoryDoesNotPreventFailure(HistoryScenario scenario) throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerAction serverAction = createOldPickedUpAction(minion);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(
                minion.getId(), serverAction.getParentAction().getId());
        configureHistory(history, scenario);
        HibernateFactory.getSession().persist(history);
        TestUtils.flushSession();

        runCleanup(minion);

        serverAction = reloadServerAction(minion, serverAction.getParentAction().getId());
        assertEquals(ActionFactory.STATUS_FAILED, serverAction.getStatus());
    }

    @Test
    public void testWaitingForRebootIsNotFailed() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerAction serverAction = createOldPickedUpAction(minion);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(
                minion.getId(), serverAction.getParentAction().getId());
        configureHistory(history, HistoryScenario.WAITING_FOR_REBOOT);
        HibernateFactory.getSession().persist(history);
        TestUtils.flushSession();

        runCleanup(minion);

        serverAction = reloadServerAction(minion, serverAction.getParentAction().getId());
        assertEquals(ActionFactory.STATUS_PICKED_UP, serverAction.getStatus());
    }

    @Test
    public void testOnlyNonWaitingMinionOfMultiMinionActionIsFailed() throws Exception {
        MinionServer waitingMinion = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer eligibleMinion = MinionServerFactoryTest.createTestMinionServer(user);
        waitingMinion.setMinionId("cleanup-waiting-" + waitingMinion.getId());
        eligibleMinion.setMinionId("cleanup-eligible-" + eligibleMinion.getId());
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                List.of(waitingMinion.getId(), eligibleMinion.getId()),
                Collections.singletonList(ApplyStatesEventMessage.PACKAGES),
                Date.from(Instant.now().minus(2, ChronoUnit.HOURS)));
        ServerAction waitingAction = action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServerId().equals(waitingMinion.getId()))
                .findFirst().orElseThrow();
        ServerAction eligibleAction = action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServerId().equals(eligibleMinion.getId()))
                .findFirst().orElseThrow();
        waitingAction.setStatusPickedUp();
        eligibleAction.setStatusPickedUp();
        ServerActionFactory.save(waitingAction);
        ServerActionFactory.save(eligibleAction);

        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(
                waitingMinion.getId(), action.getId());
        configureHistory(history, HistoryScenario.WAITING_FOR_REBOOT);
        HibernateFactory.getSession().persist(history);
        TestUtils.flushSession();

        runCleanup(waitingMinion, eligibleMinion);

        waitingAction = reloadServerAction(waitingMinion, action.getId());
        assertEquals(ActionFactory.STATUS_PICKED_UP, waitingAction.getStatus());
        eligibleAction = reloadServerAction(eligibleMinion, action.getId());
        assertEquals(ActionFactory.STATUS_FAILED, eligibleAction.getStatus());
    }

    @Test
    public void testHistoryForAnotherMinionDoesNotPreventFailure() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer otherMinion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerAction serverAction = createOldPickedUpAction(minion);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(
                otherMinion.getId(), serverAction.getParentAction().getId());
        configureHistory(history, HistoryScenario.WAITING_FOR_REBOOT);
        HibernateFactory.getSession().persist(history);
        TestUtils.flushSession();

        runCleanup(minion);

        serverAction = reloadServerAction(minion, serverAction.getParentAction().getId());
        assertEquals(ActionFactory.STATUS_FAILED, serverAction.getStatus());
    }

    @Test
    public void testHistoryForAnotherActionDoesNotPreventFailure() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerAction serverAction = createOldPickedUpAction(minion);
        ServerAction otherAction = createOldPickedUpAction(minion);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(
                minion.getId(), otherAction.getParentAction().getId());
        configureHistory(history, HistoryScenario.WAITING_FOR_REBOOT);
        HibernateFactory.getSession().persist(history);
        TestUtils.flushSession();

        runCleanup(minion);

        serverAction = reloadServerAction(minion, serverAction.getParentAction().getId());
        assertEquals(ActionFactory.STATUS_FAILED, serverAction.getStatus());
        otherAction = reloadServerAction(minion, otherAction.getParentAction().getId());
        assertEquals(ActionFactory.STATUS_PICKED_UP, otherAction.getStatus());
    }

    @Test
    public void testActionInsideCleanupTimeoutIsNotFailed() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ServerAction serverAction = createPickedUpAction(minion,
                Date.from(Instant.now().minus(1, ChronoUnit.HOURS).plusSeconds(5)));

        runCleanup(minion);

        serverAction = reloadServerAction(minion, serverAction.getParentAction().getId());
        assertEquals(ActionFactory.STATUS_PICKED_UP, serverAction.getStatus());
    }

    private ServerAction createOldPickedUpAction(MinionServer minion) {
        return createPickedUpAction(minion, Date.from(Instant.now().minus(2, ChronoUnit.HOURS)));
    }

    private ServerAction createPickedUpAction(MinionServer minion, Date earliestAction) {
        minion.setMinionId("cleanup-" + minion.getId());
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Collections.singletonList(minion.getId()),
                Collections.singletonList(ApplyStatesEventMessage.PACKAGES),
                earliestAction);
        ServerAction serverAction = action.getServerActions().stream()
                .filter(candidate -> candidate.getServerId().equals(minion.getId()))
                .findFirst().orElseThrow();
        serverAction.setStatusPickedUp();
        ServerActionFactory.save(serverAction);
        TestUtils.flushSession();
        return serverAction;
    }

    private void configureHistory(MinionTransactionalActionHistory history, HistoryScenario scenario) {
        switch (scenario) {
            case PREREQUISITE_PENDING -> {
                // A newly created history is already pending.
            }
            case PREREQUISITE_FAILED -> history.recordTransactionalApplyFailed();
            case REBOOT_NOT_NEEDED -> {
                history.recordTransactionalStateApplied();
                history.recordSnapshotReconciliation(false, true);
            }
            case REBOOT_COMPLETED -> {
                history.recordTransactionalStateApplied();
                history.recordSnapshotReconciliation(true, false);
                history.recordTransactionalApplyFinalized();
            }
            case CONTINUATION_SCHEDULED -> {
                history.recordTransactionalStateApplied();
                history.recordSnapshotReconciliation(true, true);
                history.recordAfterRebootScheduled();
            }
            case WAITING_FOR_REBOOT -> {
                history.recordTransactionalStateApplied();
                history.recordSnapshotReconciliation(true, true);
            }
            default -> throw new IllegalArgumentException("Unhandled history scenario: " + scenario);
        }
    }

    private void runCleanup(MinionServer... minions) {
        Map<String, Result<List<SaltUtil.RunningInfo>>> running = new HashMap<>();
        Arrays.stream(minions).forEach(minion ->
                running.put(minion.getMinionId(), new Result<>(Xor.right(Collections.emptyList()))));

        SaltService saltServiceMock = mock(SaltService.class);
        context().checking(new Expectations() { {
            allowing(saltServiceMock).running(with(any(MinionList.class)));
            will(returnValue(running));
            never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
            never(saltServiceMock).listJob(with(any(String.class)));
        } });

        SaltUtils saltUtils = new SaltUtils(saltServiceMock, saltServiceMock);
        new MinionActionUtils(saltServiceMock, saltUtils).cleanupMinionActions();
    }

    private ServerAction reloadServerAction(MinionServer minion, Long actionId) {
        TestUtils.flushSession();
        TestUtils.clearSession();
        return ServerActionFactory.listServerActionsForServer(minion).stream()
                .filter(serverAction -> serverAction.getParentAction().getId().equals(actionId))
                .findFirst().orElseThrow();
    }

    private Jobs.Info listJob(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Jobs.Info> jsonParser = new JsonParser<>(new TypeToken<>() {
        });
        return jsonParser.parse(eventString);
    }

    private Jobs.Info listJob(String filename, String minion1Id, List<String> actions1,
                              String minion2Id, List<String> actions2) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\\$minion_1", minion1Id)
                .replaceAll("\\$minion_2", minion2Id)
                .replaceAll("\\$action_1_1", actions1.get(0))
                .replaceAll("\\$action_1_2", actions1.get(1))
                .replaceAll("\\$action_2_1", actions2.get(0))
                .replaceAll("\\$action_2_2", actions2.get(1));

        JsonParser<Jobs.Info> jsonParser = new JsonParser<>(new TypeToken<>() { });
        return jsonParser.parse(eventString);
    }

    private Map<String, Jobs.ListJobsEntry> jobsByMetadata(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Map<String, Jobs.ListJobsEntry>> jsonParser = new JsonParser<>(new TypeToken<>() { });
        return jsonParser.parse(eventString);
    }

    @Test
    public void testMinionActionChainCleanupAllCompleted() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion2.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        SaltService saltServiceMock = mock(SaltService.class);

        ActionManager.setTaskomaticApi(taskomaticMock);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        ActionChainFactory.setTaskomaticApi(taskomaticMock);


        Date earliest = Date.from(ZonedDateTime.now()
                .minus(2, ChronoUnit.HOURS)
                .toInstant());

        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.getOrCreateActionChain(label, user);

        Set<Action> applyStates = ActionChainManager
                .scheduleApplyStates(user, Arrays.asList(minion1.getId(), minion2.getId()),
                        Optional.of(false), earliest, actionChain);
        assertEquals(2, applyStates.size());

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", 10L, "#!/bin/csh\necho hello");
        Set<Action> scriptRun = ActionChainManager.scheduleScriptRuns(
                user, Arrays.asList(minion1.getId(), minion2.getId()), "Run script test", sad, earliest, actionChain);
        assertEquals(2, scriptRun.size());

        TestUtils.flushSession();

        context().checking(new Expectations() {
            {
                allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
                allowing(taskomaticMock).scheduleActionChainExecution(with(any(ActionChain.class)));

                allowing(saltServiceMock).jobsByMetadata(
                        with(any(Object.class)), with(any(LocalDateTime.class)), with(any(LocalDateTime.class)));
                will(returnValue(Optional.of(jobsByMetadata("jobs.list_jobs.actionchains.json", 0))));

                mockListJob();
                mockListJob();
                mockListJob();
                mockListJob();
            }

            private void mockListJob() {
                never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
                never(saltServiceMock).listJob(with(any(String.class)));
            }
        });

        ActionChainFactory.schedule(actionChain, earliest);

        ActionChainFactory.delete(actionChain);
    }
}
