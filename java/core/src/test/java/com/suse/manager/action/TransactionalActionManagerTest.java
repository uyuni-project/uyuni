/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressStatus;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TransactionalActionManagerTest {

    private static final BiConsumer<MinionSummary, FormulaTransactionalPlan> NO_OP_HISTORY_UPDATER =
            (minion, plan) -> { };

    @Test
    public void testPrerequisiteProgressEntriesAreReturnedInExecutionOrder() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(false, true);
        history.recordAfterRebootScheduled();

        Action action = new Action();
        ActionType actionType = new ActionType();
        actionType.setLabel(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        action.setActionType(actionType);

        List<TransactionalActionManager.ProgressEntry> entries =
                TransactionalActionManager.getProgressEntries(action, history);

        assertEquals(3, entries.size());
        assertEquals("prerequisites", entries.get(0).getStepKey());
        assertEquals("completed", entries.get(0).getStatusKey());
        assertTrue(entries.get(0).isTimestamped());
        assertEquals("reboot", entries.get(1).getStepKey());
        assertEquals("notNeeded", entries.get(1).getStatusKey());
        assertFalse(entries.get(1).isTimestamped());
        assertEquals("execution", entries.get(2).getStepKey());
        assertEquals("scheduled", entries.get(2).getStatusKey());
        assertTrue(entries.get(2).isTimestamped());
    }

    @Test
    public void testTransactionalApplyProgressEntriesUseApplyFlowSteps() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(true, false);

        List<TransactionalActionManager.ProgressEntry> entries =
                TransactionalActionManager.getProgressEntries(new Action(), history);

        assertEquals(3, entries.size());
        assertEquals("apply", entries.get(0).getStepKey());
        assertEquals("completed", entries.get(0).getStatusKey());
        assertEquals("applyReboot", entries.get(1).getStepKey());
        assertEquals("pending", entries.get(1).getStatusKey());
        assertEquals("finalization", entries.get(2).getStepKey());
        assertEquals("pending", entries.get(2).getStatusKey());
    }

    @Test
    public void testPrerequisiteResultIsReturnedWhenStored() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied("salt result");

        assertEquals(Optional.of("salt result"), TransactionalActionManager.getPrerequisiteResult(history));
        history.recordTransactionalStateApplied(" ");
        assertTrue(TransactionalActionManager.getPrerequisiteResult(history).isEmpty());
    }

    @Test
    public void testHighstateHasPostTransactionalState() {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of());
        action.setDetails(details);

        assertTrue(TransactionalActionManager.hasPostTransactionalState(action));
    }

    @Test
    public void testHighstateProgressUsesPrerequisiteFlowSteps() {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of());
        action.setDetails(details);

        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied("transactional highstate result");
        history.recordSnapshotReconciliation(false, true);

        List<TransactionalActionManager.ProgressEntry> entries =
                TransactionalActionManager.getProgressEntries(action, history);

        assertEquals(3, entries.size());
        assertEquals("prerequisites", entries.get(0).getStepKey());
        assertEquals("reboot", entries.get(1).getStepKey());
        assertEquals("execution", entries.get(2).getStepKey());
    }

    @Test
    public void testHighstateContinuationUsesDirectStateApply() {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of());
        details.setPillarsMap(Optional.of(Map.of("example", "value")));
        details.setTest(true);
        action.setDetails(details);

        MinionSummary transactionalMinion =
                new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of()).orElseThrow();

        assertEquals(1, calls.size());

        LocalCall<?> call = calls.keySet().iterator().next();
        assertEquals("state.apply", call.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));

        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion"), kwargs.get("mods"));
        assertEquals(Map.of("example", "value"), kwargs.get("pillar"));
        assertEquals(true, kwargs.get("test"));
        assertEquals(List.of(transactionalMinion), calls.get(call));
    }

    @Test
    public void testApplyStatesFormulasContinuationPreservesPillarAndTestMode() {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of("custom-before", "formulas", "custom-after"));
        details.setPillarsMap(Optional.of(Map.of("example", "value")));
        details.setTest(true);
        details.setUseTransactionalUpdate(false);
        action.setDetails(details);
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of("locale")).orElseThrow();

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("custom-before", "locale", "custom-after"), kwargs.get("mods"));
        assertEquals(Map.of("example", "value"), kwargs.get("pillar"));
        assertEquals(true, kwargs.get("test"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
    }

    @Test
    public void testHardwareRefreshContinuationKeepsEmptyPillarAndTestMode() {
        ApplyStatesAction action = new ApplyStatesAction();
        ActionType actionType = new ActionType();
        actionType.setLabel(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        action.setActionType(actionType);
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of(SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ));
        details.setPillarsMap(Optional.of(Map.of("example", "value")));
        details.setTest(true);
        action.setDetails(details);
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of()).orElseThrow();

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of(ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE), kwargs.get("mods"));
        assertFalse(kwargs.containsKey("pillar"));
        assertFalse(kwargs.containsKey("test"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
    }

    @Test
    public void testHighstateContinuationWithEmptyFrozenFormulasIsUnchanged() {
        ApplyStatesAction action = highstateAction();
        MinionSummary transactionalMinion = transactionalMinion(2L, "transactional");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of()).orElseThrow();

        assertEquals(1, calls.size());
        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion"), kwargs.get("mods"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
        assertEquals(List.of(transactionalMinion), calls.get(call));
    }

    @Test
    public void testHighstateContinuationIncludesFrozenFormulaWithDirectCall() {
        ApplyStatesAction action = highstateAction();
        MinionSummary transactionalMinion = transactionalMinion(2L, "transactional");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of("locale")).orElseThrow();

        assertEquals(1, calls.size());
        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion",
                "locale"), kwargs.get("mods"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
    }

    @Test
    public void testHighstateContinuationGroupsMinionsWithSameFrozenFormulas() {
        ApplyStatesAction action = highstateAction();
        MinionSummary firstMinion = transactionalMinion(1L, "first");
        MinionSummary secondMinion = transactionalMinion(2L, "second");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(firstMinion, secondMinion), ignored -> List.of("locale")).orElseThrow();

        assertEquals(1, calls.size());
        assertEquals(List.of(firstMinion, secondMinion), calls.values().iterator().next());
    }

    @Test
    public void testHighstateContinuationUsesSeparateCallsForDifferentFrozenFormulas() {
        ApplyStatesAction action = highstateAction();
        MinionSummary localeMinion = transactionalMinion(1L, "locale-minion");
        MinionSummary bindMinion = transactionalMinion(2L, "bind-minion");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action,
                        List.of(localeMinion, bindMinion),
                        minion -> minion.getServerId().equals(localeMinion.getServerId()) ?
                                List.of("locale") : List.of("bind")).orElseThrow();

        assertEquals(2, calls.size());
        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion",
                "locale"), statesForMinion(calls, localeMinion));
        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion",
                "bind"), statesForMinion(calls, bindMinion));
    }

    @Test
    public void testHighstateContinuationUsesFrozenFormulasOnly() {
        ApplyStatesAction action = highstateAction();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        AtomicInteger providerCalls = new AtomicInteger();

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action,
                        List.of(transactionalMinion),
                        ignored -> {
                            providerCalls.incrementAndGet();
                            return List.of("locale");
                        }).orElseThrow();

        assertEquals(1, providerCalls.get());
        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion",
                "locale"), statesForMinion(calls, transactionalMinion));
    }

    @Test
    public void testFrozenFormulaAloneCountsAsPostTransactionalWork() {
        Action action = new Action();
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.setPostTransactionalFormulaList(List.of("locale"));

        assertTrue(TransactionalActionManager.hasPostTransactionalState(action, history));
    }

    @Test
    public void testContinuationCanContainOnlyFrozenFormula() {
        Action action = new Action();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of("locale")).orElseThrow();

        assertEquals(1, calls.size());
        LocalCall<?> call = calls.keySet().iterator().next();
        assertEquals("state.apply", call.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
        assertEquals(List.of("locale"), ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
        assertEquals(List.of(transactionalMinion), calls.get(call));
    }

    @Test
    public void testFrozenFormulaOrderIsPreservedInContinuation() {
        ApplyStatesAction action = highstateAction();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of("locale", "bind")).orElseThrow();

        assertEquals(List.of(
                "ansible",
                "services.docker",
                "services.kiwi-image-server",
                "services.salt-minion",
                "locale",
                "bind"), statesForMinion(calls, transactionalMinion));
    }

    @Test
    public void testScheduledPostTransactionalContinuationSuccessCompletesProgress() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(false, true);
        history.recordAfterRebootScheduled();

        boolean updated = TransactionalActionManager.recordPostTransactionalContinuationResult(
                Optional.of(history), false);

        assertTrue(updated);
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertNull(history.getRebootAt());
        assertEquals(ProgressStatus.COMPLETED, history.getAfterRebootStatus());
        assertTrue(history.getAfterRebootStatusAt().getTime() >= history.getPrerequisiteAt().getTime());
    }

    @Test
    public void testScheduledPostTransactionalContinuationFailureFailsProgress() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(false, true);
        history.recordAfterRebootScheduled();

        boolean updated = TransactionalActionManager.recordPostTransactionalContinuationResult(
                Optional.of(history), true);

        assertTrue(updated);
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertNull(history.getRebootAt());
        assertEquals(ProgressStatus.FAILED, history.getAfterRebootStatus());
        assertTrue(history.getAfterRebootStatusAt().getTime() >= history.getPrerequisiteAt().getTime());
    }

    @Test
    public void testStaleNoRebootContinuationResultDoesNotRestorePendingRebootStatus() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordAfterRebootScheduled();

        boolean updated = TransactionalActionManager.recordPostTransactionalContinuationResult(
                Optional.of(history), false);

        assertTrue(updated);
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertNull(history.getRebootAt());
        assertEquals(ProgressStatus.COMPLETED, history.getAfterRebootStatus());
    }

    @Test
    public void testRebootContinuationResultPreservesCompletedRebootStatus() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(true, true);
        history.recordAfterRebootScheduled();

        boolean updated = TransactionalActionManager.recordPostTransactionalContinuationResult(
                Optional.of(history), false);

        assertTrue(updated);
        assertEquals(ProgressStatus.COMPLETED, history.getRebootStatus());
        assertEquals(ProgressStatus.COMPLETED, history.getAfterRebootStatus());
    }

    @Test
    public void testStateApplyWithoutTransactionalHistoryDoesNotUpdateProgress() {
        boolean updated = TransactionalActionManager.recordPostTransactionalContinuationResult(
                Optional.empty(), false);

        assertFalse(updated);
    }

    @Test
    public void testFailedTransactionalResultWithChangesSchedulesSnapshotRefreshWithoutContinuation() {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of());
        action.setDetails(details);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        AtomicInteger snapshotRefreshes = new AtomicInteger();
        AtomicInteger resumptions = new AtomicInteger();

        TransactionalActionManager.TransactionalResult result = TransactionalActionManager.handleTransactionalResult(
                history,
                action,
                history.getMinionServerId(),
                history.getActionId(),
                stateResult("""
                        {
                          "file_|-marker_|-/usr/share/marker_|-managed": {
                            "result": false,
                            "changes": {"diff": "New file"}
                          }
                        }
                        """),
                true,
                (scheduledAction, minionServerId) -> {
                    snapshotRefreshes.incrementAndGet();
                    return Optional.of(20L);
                },
                (actionId, minionServerId) -> resumptions.incrementAndGet());

        assertTrue(result.isFailed());
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(20L, history.getSnapshotRefreshActionId());
        assertEquals(1, snapshotRefreshes.get());
        assertEquals(0, resumptions.get());
        assertFalse(ProgressStatus.SCHEDULED.equals(history.getAfterRebootStatus()));
    }

    @Test
    public void testFailedTransactionalResultWithoutChangesDoesNotScheduleSnapshotRefresh() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        AtomicInteger snapshotRefreshes = new AtomicInteger();
        AtomicInteger resumptions = new AtomicInteger();

        TransactionalActionManager.TransactionalResult result = TransactionalActionManager.handleTransactionalResult(
                history,
                new Action(),
                history.getMinionServerId(),
                history.getActionId(),
                stateResult("""
                        {
                          "cmd_|-noop_|-true_|-run": {
                            "result": false,
                            "changes": {}
                          }
                        }
                        """),
                true,
                (scheduledAction, minionServerId) -> {
                    snapshotRefreshes.incrementAndGet();
                    return Optional.of(20L);
                },
                (actionId, minionServerId) -> resumptions.incrementAndGet());

        assertTrue(result.isFailed());
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getAfterRebootStatus());
        assertEquals(0, snapshotRefreshes.get());
        assertEquals(0, resumptions.get());
    }

    @Test
    public void testSuccessfulTransactionalResultWithFrozenFormulaWaitsForContinuation() {
        ApplyStatesAction action = applyStatesAction(List.of("formulas"), true);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.setPostTransactionalFormulaList(List.of("locale"));
        AtomicInteger snapshotRefreshes = new AtomicInteger();
        AtomicInteger resumptions = new AtomicInteger();

        TransactionalActionManager.TransactionalResult result = TransactionalActionManager.handleTransactionalResult(
                history,
                action,
                history.getMinionServerId(),
                history.getActionId(),
                stateResult("""
                        {
                          "cmd_|-noop_|-true_|-run": {
                            "result": true,
                            "changes": {}
                          }
                        }
                        """),
                false,
                (scheduledAction, minionServerId) -> {
                    snapshotRefreshes.incrementAndGet();
                    return Optional.of(20L);
                },
                (actionId, minionServerId) -> resumptions.incrementAndGet());

        assertFalse(result.isFailed());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertTrue(TransactionalActionManager.getPrerequisiteResult(history).isPresent());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.PENDING, history.getAfterRebootStatus());
        assertEquals(0, snapshotRefreshes.get());
        assertEquals(1, resumptions.get());
    }

    @Test
    public void testSuccessfulTransactionalResultWithoutPostTransactionalWorkFinalizesImmediately() {
        ApplyStatesAction action = applyStatesAction(List.of("formulas"), false);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.setPostTransactionalFormulaList(List.of());
        AtomicInteger snapshotRefreshes = new AtomicInteger();
        AtomicInteger resumptions = new AtomicInteger();

        TransactionalActionManager.TransactionalResult result = TransactionalActionManager.handleTransactionalResult(
                history,
                action,
                history.getMinionServerId(),
                history.getActionId(),
                stateResult("""
                        {
                          "cmd_|-noop_|-true_|-run": {
                            "result": true,
                            "changes": {}
                          }
                        }
                        """),
                false,
                (scheduledAction, minionServerId) -> {
                    snapshotRefreshes.incrementAndGet();
                    return Optional.of(20L);
                },
                (actionId, minionServerId) -> resumptions.incrementAndGet());

        assertFalse(result.isFailed());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.COMPLETED, history.getAfterRebootStatus());
        assertEquals(0, snapshotRefreshes.get());
        assertEquals(0, resumptions.get());
    }

    @Test
    public void testFailedTransactionalSnapshotReconciliationDoesNotResumePostState() {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(List.of());
        action.setDetails(details);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalApplyFailed("failed prerequisite");
        AtomicInteger resumptions = new AtomicInteger();

        TransactionalActionManager.reconcileSnapshotRefreshAction(
                history, action, true, (actionId, minionServerId) -> resumptions.incrementAndGet());

        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertTrue(history.isRebootRequired());
        assertEquals(ProgressStatus.PENDING, history.getRebootStatus());
        assertEquals(ProgressStatus.FAILED, history.getAfterRebootStatus());
        assertEquals(0, resumptions.get());
    }

    @Test
    public void testHardwareProfileUpdateUsesTransactionalPrerequisiteState() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE, Optional.empty());

        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of(SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ),
                ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
    }

    @Test
    public void testUnknownStateExecutesWithDirectCall() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                "unknown.state", Optional.empty());

        assertEquals("state.apply", call.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
    }

    @Test
    public void testPatchInstallUsesTransactionalUpdate() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                SaltParameters.PACKAGES_PATCHINSTALL, Optional.empty());

        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of(SaltParameters.PACKAGES_PATCHINSTALL),
                ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
    }

    @Test
    public void testAddApplyCallsUsesTransactionalUpdateForTransactionalMinionsOnly() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addApplyCalls(
                calls,
                List.of(SaltParameters.PACKAGES_PKGREMOVE),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "transactional_update.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testAddApplyCallsExecutesUnmappedStateWithDirectCallForTransactionalMinions() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addApplyCalls(
                calls,
                List.of(ApplyStatesEventMessage.SYSTEM_INFO),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        !entry.getKey().getPayload().containsKey("module_executors") &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        List.of("direct_call").equals(entry.getKey().getPayload().get("module_executors")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testAddApplyCallsPreservesEmptyTargetLists() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();

        TransactionalActionManager.addApplyCalls(
                calls,
                List.of(SaltParameters.PACKAGES_PKGINSTALL),
                Optional.empty(),
                List.of());

        assertEquals(1, calls.size());
        assertTrue(calls.values().iterator().next().isEmpty());
    }

    @Test
    public void testPrepareSaltCallsExecutesWithDirectCallForTransactionalMinions() {
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.prepareSaltCallsForTransactionalMinions(Map.of(
                        call, List.of(regularMinion, transactionalMinion)));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().getPayload().containsKey("module_executors") &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> List.of("direct_call").equals(entry.getKey().getPayload().get("module_executors")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testPrepareSaltCallsPreservesEmptyTargetLists() {
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(SaltParameters.PACKAGES_PKGINSTALL), Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.prepareSaltCallsForTransactionalMinions(Map.of(call, List.of()));

        assertEquals(1, calls.size());
        assertTrue(calls.containsKey(call));
        assertTrue(calls.get(call).isEmpty());
    }

    @Test
    public void testPrepareSaltCallExecutesWithDirectCallForTransactionalMinion() {
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        LocalCall<?> preparedCall = TransactionalActionManager.prepareSaltCallForTransactionalMinions(
                call, List.of(transactionalMinion));

        assertEquals("state.apply", preparedCall.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), preparedCall.getPayload().get("module_executors"));
    }

    @Test
    public void testPrepareSaltCallUsesTransactionalUpdateForMappedState() {
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.CHANNELS),
                Optional.of(Map.of("key", "value")),
                Optional.of(true),
                Optional.empty());

        LocalCall<?> preparedCall = TransactionalActionManager.prepareSaltCallForTransactionalMinions(
                call, List.of(transactionalMinion));

        assertEquals("transactional_update.apply", preparedCall.getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) preparedCall.getPayload().get("kwarg");
        assertEquals(List.of(ApplyStatesEventMessage.CHANNELS), kwargs.get("mods"));
        assertEquals(Map.of("key", "value"), kwargs.get("pillar"));
        assertEquals(true, kwargs.get("queue"));
    }

    @Test
    public void testPrepareSaltCallExecutesMixedStatesWithDirectCall() {
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.CHANNELS, ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        LocalCall<?> preparedCall = TransactionalActionManager.prepareSaltCallForTransactionalMinions(
                call, List.of(transactionalMinion));

        assertEquals("state.apply", preparedCall.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), preparedCall.getPayload().get("module_executors"));
    }

    @Test
    public void testApplyStatesActionUsesTransactionalUpdateForMappedState() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of(SaltParameters.PACKAGES_PKGLOCK),
                Optional.empty(),
                Optional.of(true),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion),
                false,
                1L);

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "transactional_update.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testTransactionalStateMappingTakesPrecedenceWhenTransactionalUpdateRequested() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion =
                new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of(ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                1L);

        assertEquals(1, calls.size());
        LocalCall<?> transactionalCall = calls.keySet().iterator().next();
        assertEquals("transactional_update.apply", transactionalCall.getPayload().get("fun"));

        Map<?, ?> kwargs = (Map<?, ?>) transactionalCall.getPayload().get("kwarg");
        assertEquals(List.of(SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ), kwargs.get("mods"));
    }

    @Test
    public void testCustomStatesUseTransactionalUpdateWhenRequested() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom"),
                Optional.empty(),
                Optional.of(true),
                Optional.of(true),
                List.of(regularMinion, transactionalMinion),
                true,
                1L);

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));
        Optional<LocalCall<?>> transactionalCall = calls.entrySet().stream()
                .filter(entry -> entry.getValue().equals(List.of(transactionalMinion)))
                .map(Map.Entry::getKey)
                .findFirst();
        assertTrue(transactionalCall.isPresent());
        assertEquals("transactional_update.apply", transactionalCall.get().getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) transactionalCall.get().getPayload().get("kwarg");
        assertEquals(List.of("custom"), kwargs.get("mods"));
        assertEquals(true, kwargs.get("queue"));
        assertEquals(true, kwargs.get("test"));
    }

    @Test
    public void testCustomStatesUseDirectStateApplyWhenTransactionalUpdateNotRequested() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                false,
                1L);

        assertEquals(1, calls.size());
        Map<String, Object> payload = calls.keySet().iterator().next().getPayload();
        assertEquals("state.apply", payload.get("fun"));
        assertEquals(List.of("direct_call"), payload.get("module_executors"));
    }

    @Test
    public void testFormulasStateApplyRunsNormallyOnTraditionalMinions() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        AtomicInteger planCalls = new AtomicInteger();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(regularMinion),
                true,
                ignored -> {
                    planCalls.incrementAndGet();
                    return emptyFormulaPlan();
                },
                NO_OP_HISTORY_UPDATER);

        assertEquals(1, calls.size());
        LocalCall<?> call = callForMinion(calls, regularMinion);
        assertEquals("state.apply", call.getPayload().get("fun"));
        assertEquals(List.of("formulas"), ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
        assertFalse(call.getPayload().containsKey("module_executors"));
        assertEquals(0, planCalls.get());
    }

    @Test
    public void testFormulasStateApplyUsesTransactionalWrapperForTransactionalFormulas() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("bind"),
                List.of(),
                List.of(),
                List.of());
        Map<MinionSummary, FormulaTransactionalPlan> persistedPlans = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> plan,
                persistedPlans::put);

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertEquals(Map.of("transactional_formulas", List.of("bind")), kwargs.get("pillar"));
        assertFalse(kwargs.containsKey("exclude"));
        assertTrue(plan == persistedPlans.get(transactionalMinion));
    }

    @Test
    public void testFormulasStateApplyFreezesLiveOnlyFormulaForContinuation() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of("live-formula"),
                List.of(),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertFalse(kwargs.containsKey("pillar"));
        assertFalse(kwargs.containsKey("exclude"));
        assertEquals(List.of("live-formula"), persistedFormulas.get(transactionalMinion));
    }

    @Test
    public void testFormulasStateApplyUsesExcludeAndFreezeForTransactionalThenLiveFormula() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_timezone_setting", "mgr_language_settings"),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertEquals(Map.of("transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(List.of(
                Map.of("id", "mgr_timezone_setting"),
                Map.of("id", "mgr_language_settings")), kwargs.get("exclude"));
        assertEquals(List.of("locale"), persistedFormulas.get(transactionalMinion));
    }

    @Test
    public void testFormulasStateApplyPassesUnsupportedFormulasToWrapper() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of(),
                List.of(),
                List.of("unsupported-formula"));
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertEquals(Map.of("transactional_unsupported_formulas", List.of("unsupported-formula")),
                kwargs.get("pillar"));
        assertTrue(persistedFormulas.get(transactionalMinion).isEmpty());
    }

    @Test
    public void testFormulasStateApplyGroupsMinionsWithEqualPlans() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary firstMinion = transactionalMinion(1L, "first");
        MinionSummary secondMinion = transactionalMinion(2L, "second");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("bind"),
                List.of(),
                List.of(),
                List.of());

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(firstMinion, secondMinion),
                true,
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        assertEquals(1, calls.size());
        assertEquals(List.of(firstMinion, secondMinion), calls.values().iterator().next());
    }

    @Test
    public void testFormulasStateApplySeparatesMinionsWithDifferentPlans() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary bindMinion = transactionalMinion(1L, "bind");
        MinionSummary localeMinion = transactionalMinion(2L, "locale");
        FormulaTransactionalPlan bindPlan = new FormulaTransactionalPlan(
                List.of("bind"),
                List.of(),
                List.of(),
                List.of());
        FormulaTransactionalPlan localePlan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_language_settings"),
                List.of());

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(bindMinion, localeMinion),
                true,
                minion -> minion.getServerId().equals(bindMinion.getServerId()) ? bindPlan : localePlan,
                NO_OP_HISTORY_UPDATER);

        assertEquals(2, calls.size());
        assertTrue(calls.values().stream().anyMatch(minions -> minions.equals(List.of(bindMinion))));
        assertTrue(calls.values().stream().anyMatch(minions -> minions.equals(List.of(localeMinion))));
    }

    @Test
    public void testFormulasStateApplyPlanProviderIsCalledOncePerTransactionalMinionAndReused() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary firstMinion = transactionalMinion(1L, "first");
        MinionSummary secondMinion = transactionalMinion(2L, "second");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_language_settings"),
                List.of());
        AtomicInteger planCalls = new AtomicInteger();
        List<FormulaTransactionalPlan> persistedPlans = new ArrayList<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(firstMinion, secondMinion),
                true,
                ignored -> {
                    planCalls.incrementAndGet();
                    return plan;
                },
                (minion, groupPlan) -> persistedPlans.add(groupPlan));

        assertEquals(2, planCalls.get());
        assertEquals(2, persistedPlans.size());
        assertTrue(persistedPlans.stream().allMatch(persistedPlan -> plan == persistedPlan));
    }

    @Test
    public void testFormulasStateApplyWithoutTransactionalUpdateRunsOnlyFormulaWrapperFirst() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        AtomicInteger planCalls = new AtomicInteger();
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("bind"),
                List.of(),
                List.of(),
                List.of());

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                false,
                ignored -> {
                    planCalls.incrementAndGet();
                    return plan;
                },
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of("formulas_transactional"), ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
        assertEquals(1, planCalls.get());
        assertTrue(persistedFormulas.get(transactionalMinion).isEmpty());
    }

    @Test
    public void testFormulasStateApplyWithoutTransactionalUpdateFreezesLiveFormulaForContinuation() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of("live-formula"),
                List.of(),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                false,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertFalse(kwargs.containsKey("pillar"));
        assertEquals(List.of("live-formula"), persistedFormulas.get(transactionalMinion));
    }

    @Test
    public void testFormulasStateApplyWithTransactionalUpdateRunsOtherStatesTransactionally() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_language_settings"),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom-before", "formulas", "custom-after"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of("custom-before", "formulas_transactional", "custom-after"), kwargs.get("mods"));
        assertEquals(Map.of("transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(List.of(Map.of("id", "mgr_language_settings")), kwargs.get("exclude"));
        assertEquals(List.of("locale"), persistedFormulas.get(transactionalMinion));
    }

    @Test
    public void testFormulasStateApplyWithoutTransactionalUpdateDefersOtherStatesToContinuation() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_language_settings"),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom-before", "formulas", "custom-after"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                false,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertEquals(Map.of("transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(List.of(Map.of("id", "mgr_language_settings")), kwargs.get("exclude"));
        assertEquals(List.of("locale"), persistedFormulas.get(transactionalMinion));

        ApplyStatesAction action = applyStatesAction(List.of("custom-before", "formulas", "custom-after"), false);
        Map<LocalCall<?>, List<MinionSummary>> continuationCalls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> persistedFormulas.get(transactionalMinion))
                        .orElseThrow();

        LocalCall<?> continuationCall = callForMinion(continuationCalls, transactionalMinion);
        assertEquals("state.apply", continuationCall.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), continuationCall.getPayload().get("module_executors"));
        assertEquals(List.of("custom-before", "locale", "custom-after"),
                ((Map<?, ?>) continuationCall.getPayload().get("kwarg")).get("mods"));
    }

    @Test
    public void testFormulasStateApplyWithTransactionalUpdateContinuesOnlyFrozenFormulas() {
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        ApplyStatesAction action = applyStatesAction(List.of("custom-before", "formulas", "custom-after"), true);

        Map<LocalCall<?>, List<MinionSummary>> continuationCalls =
                TransactionalActionManager.getAfterRebootSaltCalls(
                        action, List.of(transactionalMinion), ignored -> List.of("locale")).orElseThrow();

        LocalCall<?> continuationCall = callForMinion(continuationCalls, transactionalMinion);
        assertEquals(List.of("locale"), ((Map<?, ?>) continuationCall.getPayload().get("kwarg")).get("mods"));
        assertEquals(List.of("direct_call"), continuationCall.getPayload().get("module_executors"));
    }

    @Test
    public void testFormulasStateApplyWithoutTransactionalUpdateAndUnsupportedPlanHasDeferredWorkButNoFrozenFormula() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of(),
                List.of(),
                List.of("unsupported-formula"));
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom-before", "formulas", "custom-after"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                false,
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("formulas_transactional"), kwargs.get("mods"));
        assertEquals(Map.of("transactional_unsupported_formulas", List.of("unsupported-formula")),
                kwargs.get("pillar"));
        assertTrue(persistedFormulas.get(transactionalMinion).isEmpty());

        ApplyStatesAction action = applyStatesAction(List.of("custom-before", "formulas", "custom-after"), false);
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.setPostTransactionalFormulaList(persistedFormulas.get(transactionalMinion));
        assertTrue(TransactionalActionManager.hasPostTransactionalState(action, history));
    }

    @Test
    public void testFormulasStateApplyWithoutTransactionalUpdateAndNoOtherStateHasNoContinuationForTransactionalPlan() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.setPostTransactionalFormulaList(List.of());

        assertFalse(TransactionalActionManager.hasPostTransactionalState(applyStatesAction(List.of("formulas"), false),
                history));
    }

    @Test
    public void testActionsWithoutFormulasRemainDirectCallWhenTransactionalUpdateIsFalse() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        AtomicInteger planCalls = new AtomicInteger();
        AtomicInteger historyUpdaterCalls = new AtomicInteger();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                false,
                ignored -> {
                    planCalls.incrementAndGet();
                    return emptyFormulaPlan();
                },
                (minion, plan) -> historyUpdaterCalls.incrementAndGet());

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        assertEquals("state.apply", call.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
        assertEquals(List.of("custom"), ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
        assertEquals(0, planCalls.get());
        assertEquals(0, historyUpdaterCalls.get());
    }

    @Test
    public void testActionsWithoutFormulasRemainTransactionalWhenTransactionalUpdateIsTrue() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        AtomicInteger planCalls = new AtomicInteger();
        AtomicInteger historyUpdaterCalls = new AtomicInteger();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> {
                    planCalls.incrementAndGet();
                    return emptyFormulaPlan();
                },
                (minion, plan) -> historyUpdaterCalls.incrementAndGet());

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of("custom"), ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
        assertEquals(0, planCalls.get());
        assertEquals(0, historyUpdaterCalls.get());
    }

    @Test
    public void testFormulasStateApplyRecalculatesReservedPillarKeys() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        Map<String, Object> originalPillar = new HashMap<>(Map.of(
                "example", "value",
                "transactional_formulas", List.of("stale-formula"),
                "transactional_unsupported_formulas", List.of("stale-unsupported")));
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("formulas"),
                Optional.of(originalPillar),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = callForMinion(calls, transactionalMinion);
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(Map.of(
                "example", "value",
                "transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(Map.of(
                "example", "value",
                "transactional_formulas", List.of("stale-formula"),
                "transactional_unsupported_formulas", List.of("stale-unsupported")), originalPillar);
    }

    @Test
    public void testHighstateUsesTransactionalUpdateByDefaultOnTransactionalSystems() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        AtomicInteger planCalls = new AtomicInteger();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion),
                false,
                ignored -> {
                    planCalls.incrementAndGet();
                    return emptyFormulaPlan();
                },
                NO_OP_HISTORY_UPDATER);

        assertEquals(2, calls.size());
        assertEquals(1, planCalls.get());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));

        Optional<LocalCall<?>> transactionalCall = calls.entrySet().stream()
                .filter(entry -> entry.getValue().equals(List.of(transactionalMinion)))
                .map(Map.Entry::getKey)
                .findFirst();

        assertTrue(transactionalCall.isPresent());
        assertEquals("transactional_update.apply", transactionalCall.get().getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) transactionalCall.get().getPayload().get("kwarg");
        assertFalse(kwargs.containsKey("mods"));
    }

    @Test
    public void testHighstateUsesTransactionalUpdateWhenRequested() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion),
                true,
                ignored -> emptyFormulaPlan(),
                NO_OP_HISTORY_UPDATER);

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));
        Optional<LocalCall<?>> transactionalCall = calls.entrySet().stream()
                .filter(entry -> entry.getValue().equals(List.of(transactionalMinion)))
                .map(Map.Entry::getKey)
                .findFirst();
        assertTrue(transactionalCall.isPresent());
        assertEquals("transactional_update.apply", transactionalCall.get().getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) transactionalCall.get().getPayload().get("kwarg");
        assertFalse(kwargs.containsKey("mods"));
    }

    @Test
    public void testHighstateTransactionalPlanWithoutFormulasUsesEmptyTransactionalCall() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = emptyFormulaPlan();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        assertEquals(1, calls.size());
        LocalCall<?> call = calls.keySet().iterator().next();
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertFalse(kwargs.containsKey("mods"));
        assertFalse(kwargs.containsKey("pillar"));
        assertFalse(kwargs.containsKey("exclude"));
        assertEquals(List.of(transactionalMinion), calls.get(call));
    }

    @Test
    public void testHighstateTransactionalPlanIncludesLocaleAndExcludes() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_timezone_setting", "mgr_kb_settings", "mgr_language_settings"),
                List.of());

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(Map.of("transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(List.of(
                Map.of("id", "mgr_timezone_setting"),
                Map.of("id", "mgr_kb_settings"),
                Map.of("id", "mgr_language_settings")), kwargs.get("exclude"));
    }

    @Test
    public void testHighstateTransactionalPlanIncludesUnsupportedFormulas() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of(),
                List.of(),
                List.of("bind", "dhcpd"));

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(Map.of("transactional_unsupported_formulas", List.of("bind", "dhcpd")),
                kwargs.get("pillar"));
    }

    @Test
    public void testHighstateTransactionalPlansGroupMinionsWithEqualPlans() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary firstMinion = transactionalMinion(1L, "first");
        MinionSummary secondMinion = transactionalMinion(2L, "second");
        FormulaTransactionalPlan plan = emptyFormulaPlan();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(firstMinion, secondMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        assertEquals(1, calls.size());
        assertEquals(List.of(firstMinion, secondMinion), calls.values().iterator().next());
    }

    @Test
    public void testHighstateTransactionalPlansUseSeparateCallsForDifferentPlans() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary localeMinion = transactionalMinion(1L, "locale-minion");
        MinionSummary unsupportedMinion = transactionalMinion(2L, "unsupported-minion");
        FormulaTransactionalPlan localePlan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of("mgr_timezone_setting"),
                List.of());
        FormulaTransactionalPlan unsupportedPlan = new FormulaTransactionalPlan(
                List.of(),
                List.of(),
                List.of(),
                List.of("bind"));

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(localeMinion, unsupportedMinion),
                minion -> minion.getServerId().equals(localeMinion.getServerId()) ? localePlan : unsupportedPlan,
                NO_OP_HISTORY_UPDATER);

        assertEquals(2, calls.size());
        assertTrue(calls.values().stream().anyMatch(minions -> minions.equals(List.of(localeMinion))));
        assertTrue(calls.values().stream().anyMatch(minions -> minions.equals(List.of(unsupportedMinion))));
    }

    @Test
    public void testHighstateTransactionalPlanMergesPillarWithoutMutatingOriginal() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        Map<String, Object> originalPillar = new HashMap<>(Map.of("example", "value"));
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.of(originalPillar),
                Optional.of(true),
                Optional.of(false),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(Map.of(
                "example", "value",
                "transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(Map.of("example", "value"), originalPillar);
        assertEquals(true, kwargs.get("queue"));
        assertEquals(false, kwargs.get("test"));
    }

    @Test
    public void testHighstateTransactionalPlanDropsStaleTransactionalFormulasWhenPlanEmpty() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        Map<String, Object> originalPillar = new HashMap<>(Map.of("transactional_formulas", List.of("bind")));
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = emptyFormulaPlan();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.of(originalPillar),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        Map<?, ?> pillar = (Map<?, ?>) kwargs.get("pillar");
        assertFalse(pillar.containsKey("transactional_formulas"));
    }

    @Test
    public void testHighstateTransactionalPlanReplacesStaleTransactionalUnsupportedFormulas() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        Map<String, Object> originalPillar =
                new HashMap<>(Map.of("transactional_unsupported_formulas", List.of("bind")));
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.of(originalPillar),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(Map.of("transactional_formulas", List.of("locale")), kwargs.get("pillar"));
    }

    @Test
    public void testHighstateTransactionalPlanPreservesOtherPillarEntriesWhileSanitizingReservedKeys() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        Map<String, Object> originalPillar = new HashMap<>(Map.of(
                "example", "value",
                "transactional_formulas", List.of("bind"),
                "transactional_unsupported_formulas", List.of("dhcpd")));
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.of(originalPillar),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                NO_OP_HISTORY_UPDATER);

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(Map.of(
                "example", "value",
                "transactional_formulas", List.of("locale")), kwargs.get("pillar"));
        assertEquals(Map.of(
                "example", "value",
                "transactional_formulas", List.of("bind"),
                "transactional_unsupported_formulas", List.of("dhcpd")), originalPillar);
    }

    @Test
    public void testNonHighstateApplyStatesDoNotUseFormulaPlans() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom"),
                Optional.of(Map.of("example", "value")),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                1L);

        assertEquals(1, calls.size());
        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertEquals(List.of("custom"), kwargs.get("mods"));
        assertEquals(Map.of("example", "value"), kwargs.get("pillar"));
        assertFalse(kwargs.containsKey("exclude"));
    }

    @Test
    public void testHighstateTransactionalPersistsPlanFormulasToActionHistory() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        assertEquals(List.of("locale"), persistedFormulas.get(transactionalMinion));
    }

    @Test
    public void testHighstateTransactionalPersistsSamePlanToAllMinionsSharingIt() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary firstMinion = transactionalMinion(1L, "first");
        MinionSummary secondMinion = transactionalMinion(2L, "second");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(firstMinion, secondMinion),
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        assertEquals(List.of("locale"), persistedFormulas.get(firstMinion));
        assertEquals(List.of("locale"), persistedFormulas.get(secondMinion));
    }

    @Test
    public void testHighstateTransactionalPersistsDifferentFormulasPerMinionPlan() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary localeMinion = transactionalMinion(1L, "locale-minion");
        MinionSummary bindMinion = transactionalMinion(2L, "bind-minion");
        FormulaTransactionalPlan localePlan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());
        FormulaTransactionalPlan bindPlan = new FormulaTransactionalPlan(
                List.of("bind"),
                List.of(),
                List.of(),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(localeMinion, bindMinion),
                minion -> minion.getServerId().equals(localeMinion.getServerId()) ? localePlan : bindPlan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        assertEquals(List.of("locale"), persistedFormulas.get(localeMinion));
        assertTrue(persistedFormulas.get(bindMinion).isEmpty());
    }

    @Test
    public void testHighstateTransactionalNeverPersistsUnsupportedFormulas() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of(),
                List.of(),
                List.of("bind"));
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        assertTrue(persistedFormulas.get(transactionalMinion).isEmpty());
    }

    @Test
    public void testHighstateTransactionalEmptyPlanOverwritesStaleHistoryValue() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = emptyFormulaPlan();
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.setPostTransactionalFormulaList(List.of("locale"));

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                (minion, groupPlan) -> history.setPostTransactionalFormulaList(groupPlan.postTransactionalFormulas()));

        assertTrue(history.getPostTransactionalFormulaList().isEmpty());
    }

    @Test
    public void testNonHighstateApplyStatesDoNotPersistFormulaHistory() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        AtomicInteger historyUpdaterCalls = new AtomicInteger();

        TransactionalActionManager.addOptionalTransactionalApplyCalls(
                calls,
                List.of("custom"),
                Optional.of(Map.of("example", "value")),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                true,
                ignored -> emptyFormulaPlan(),
                (minion, groupPlan) -> historyUpdaterCalls.incrementAndGet());

        assertEquals(0, historyUpdaterCalls.get());
    }

    @Test
    public void testHighstateTransactionalPlanProviderIsCalledOnceAndReused() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("locale"),
                List.of("locale"),
                List.of(),
                List.of());
        AtomicInteger planCalls = new AtomicInteger();
        List<FormulaTransactionalPlan> persistedPlans = new ArrayList<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> {
                    planCalls.incrementAndGet();
                    return plan;
                },
                (minion, groupPlan) -> persistedPlans.add(groupPlan));

        assertEquals(1, planCalls.get());
        assertEquals(1, persistedPlans.size());
        assertTrue(plan == persistedPlans.get(0));
    }

    @Test
    public void testLiveOnlyFormulaIsAbsentFromFirstPassAndFrozenForContinuation() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of(),
                List.of("live-formula"),
                List.of(),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        LocalCall<?> call = calls.keySet().iterator().next();
        Map<?, ?> kwargs = (Map<?, ?>) call.getPayload().get("kwarg");
        assertFalse(kwargs.containsKey("pillar"));
        assertFalse(kwargs.containsKey("exclude"));
        assertEquals(List.of("live-formula"), persistedFormulas.get(transactionalMinion));
    }

    @Test
    public void testHighstateTransactionalPersistsOnlyPostTransactionalFormulas() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary transactionalMinion = transactionalMinion(1L, "transactional");
        FormulaTransactionalPlan plan = new FormulaTransactionalPlan(
                List.of("bind", "locale"),
                List.of("locale"),
                List.of("mgr_language_settings"),
                List.of());
        Map<MinionSummary, List<String>> persistedFormulas = new HashMap<>();

        TransactionalActionManager.addTransactionalHighstateCalls(
                calls,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(transactionalMinion),
                ignored -> plan,
                (minion, groupPlan) -> persistedFormulas.put(minion, groupPlan.postTransactionalFormulas()));

        assertEquals(List.of("locale"), persistedFormulas.get(transactionalMinion));
    }

    private static FormulaTransactionalPlan emptyFormulaPlan() {
        return new FormulaTransactionalPlan(List.of(), List.of(), List.of(), List.of());
    }

    private static ApplyStatesAction highstateAction() {
        return applyStatesAction(List.of(), false);
    }

    private static Action hardwareRefreshAction() {
        Action action = new Action();
        ActionType actionType = new ActionType();
        actionType.setLabel(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        action.setActionType(actionType);
        return action;
    }

    private static ApplyStatesAction applyStatesAction(List<String> states, boolean useTransactionalUpdate) {
        ApplyStatesAction action = new ApplyStatesAction();
        ApplyStatesActionDetails details = new ApplyStatesActionDetails();
        details.setMods(states);
        details.setUseTransactionalUpdate(useTransactionalUpdate);
        action.setDetails(details);
        return action;
    }

    private static MinionSummary transactionalMinion(Long id, String name) {
        return new MinionSummary(id, name, null, null, null, "SLES", true);
    }

    private static LocalCall<?> callForMinion(Map<LocalCall<?>, List<MinionSummary>> calls, MinionSummary minion) {
        return calls.entrySet().stream()
                .filter(entry -> entry.getValue().contains(minion))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    private static List<?> statesForMinion(Map<LocalCall<?>, List<MinionSummary>> calls, MinionSummary minion) {
        LocalCall<?> call = callForMinion(calls, minion);
        return (List<?>) ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods");
    }

    private static JsonElement stateResult(String result) {
        return JsonParser.parseString(result);
    }
}
