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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionalActionManagerTest {

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
    public void testFailedTransactionalResultWithChangesSchedulesSnapshotRefresh() {
        Action action = new Action();
        ActionType actionType = new ActionType();
        actionType.setLabel(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        action.setActionType(actionType);

        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        AtomicInteger snapshotRefreshes = new AtomicInteger();

        TransactionalActionManager.TransactionalResult result =
                TransactionalActionManager.handleTransactionalResult(
                        history,
                        action,
                        history.getMinionServerId(),
                        history.getActionId(),
                        JsonParser.parseString("""
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
                        });

        assertTrue(result.isFailed());
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(20L, history.getSnapshotRefreshActionId());
        assertEquals(1, snapshotRefreshes.get());
        assertFalse(ProgressStatus.SCHEDULED.equals(history.getAfterRebootStatus()));
    }

    @Test
    public void testFailedTransactionalResultWithoutChangesDoesNotScheduleSnapshotRefresh() {
        Action action = new Action();
        ActionType actionType = new ActionType();
        actionType.setLabel(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        action.setActionType(actionType);

        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        AtomicInteger snapshotRefreshes = new AtomicInteger();

        TransactionalActionManager.TransactionalResult result =
                TransactionalActionManager.handleTransactionalResult(
                        history,
                        action,
                        history.getMinionServerId(),
                        history.getActionId(),
                        JsonParser.parseString("""
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
                        });

        assertTrue(result.isFailed());
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(0, snapshotRefreshes.get());
        assertFalse(ProgressStatus.SCHEDULED.equals(history.getAfterRebootStatus()));
    }

    @Test
    public void testFailedTransactionalSnapshotReconciliationDoesNotResumePostState() {
        Action action = new Action();
        ActionType actionType = new ActionType();
        actionType.setLabel(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        action.setActionType(actionType);

        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalApplyFailed("failed prerequisite");
        AtomicInteger resumptions = new AtomicInteger();

        TransactionalActionManager.reconcileSnapshotRefreshAction(
                history, action, true,
                (actionId, minionServerId) -> resumptions.incrementAndGet());

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
                false);

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
                true);

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
                true);

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
                false);

        assertEquals(1, calls.size());
        Map<String, Object> payload = calls.keySet().iterator().next().getPayload();
        assertEquals("state.apply", payload.get("fun"));
        assertEquals(List.of("direct_call"), payload.get("module_executors"));
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
                true);

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
}
