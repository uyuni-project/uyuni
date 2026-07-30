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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.partitioningBy;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.HardwareRefreshAction;
import com.redhat.rhn.domain.action.TransactionalFlow;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressEntry;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistoryId;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ResumeTransactionalActionEventMessage;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.services.TransactionalUpdateCalls;
import com.suse.manager.webui.utils.salt.LocalCallWithExecutors;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Manager for multi-step transactional actions.
 */
public class TransactionalActionManager {

    private static final List<String> DIRECT_CALL_EXECUTOR = List.of("direct_call");

    private static final Set<String> CONFIGURABLE_CUSTOM_STATES = Set.of(
            "custom",
            "custom_groups",
            "custom_org",
            "recurring",
            SaltParameters.REMOTE_COMMANDS);

    private TransactionalActionManager() {
    }

    /**
     * Find transactional actions waiting for reboot for a minion.
     *
     * @param minionServerId minion server id
     * @return transactional actions waiting for reboot
     */
    public static List<MinionTransactionalActionHistory> findPendingRebootActions(Long minionServerId) {
        if (minionServerId == null) {
            return emptyList();
        }

        return HibernateFactory.getSession().createQuery("""
                FROM MinionTransactionalActionHistory history
                 WHERE history.minionServerId = :minionServerId
                   AND history.rebootRequired = true
                   AND history.rebootStatus = :pendingStatus
                   AND history.afterRebootStatus = :pendingStatus
                 ORDER BY history.created ASC, history.actionId ASC
                """, MinionTransactionalActionHistory.class)
                .setParameter("minionServerId", minionServerId)
                .setParameter("pendingStatus", MinionTransactionalActionHistory.ProgressStatus.PENDING)
                .getResultList();
    }

    /**
     * Find transactional progress for a specific action on a minion.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     * @return transactional progress, when tracked
     */
    public static Optional<MinionTransactionalActionHistory> findTransactionalActionHistory(
            Long minionServerId, Long actionId) {
        if (minionServerId == null || actionId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(HibernateFactory.getSession().find(
                MinionTransactionalActionHistory.class,
                new MinionTransactionalActionHistoryId(minionServerId, actionId)));
    }

    /**
     * Check whether an action is a transactional apply action waiting for reboot.
     *
     * @param action action to check
     * @param minionServerId minion server id
     * @return true when the action applied transactional states and is waiting for reboot
     */
    public static boolean isTransactionalApplyWaitingForReboot(Action action, Long minionServerId) {
        return findDefinitionByAction(action)
                .filter(definition -> TransactionalFlow.APPLY_THEN_COMPLETE.equals(definition.flow()))
                .flatMap(definition -> findTransactionalActionHistory(minionServerId, action.getId()))
                .map(MinionTransactionalActionHistory::isWaitingForReboot)
                .orElse(false);
    }

    /**
     * Get transactional progress entries using the flow declared by the action.
     *
     * @param action action owning the transactional history
     * @param history transactional history
     * @return progress entries in execution order
     */
    public static List<ProgressEntry> getProgressEntries(Action action, MinionTransactionalActionHistory history) {
        boolean transactionalApply = findDefinitionByAction(action)
                .map(definition -> TransactionalFlow.APPLY_THEN_COMPLETE.equals(definition.flow()))
                .orElse(false);
        return history.getProgressEntries(transactionalApply);
    }

    /**
     * Resume transactional actions that were waiting for a real reboot.
     *
     * @param minion minion that reported startup
     * @param bootTime boot time reported by the minion, in seconds since epoch
     */
    public static void resumePendingRebootActionsIfNeeded(MinionServer minion, Optional<Long> bootTime) {
        List<MinionTransactionalActionHistory> pendingActions = findPendingRebootActions(minion.getId());

        if (pendingActions.isEmpty()) {
            return;
        }

        pendingActions.stream()
                .filter(action -> bootTime
                        .map(time -> action.canContinueAfter(time * 1000L))
                        .orElse(true))
                .map(MinionTransactionalActionHistory::getActionId)
                .forEach(actionId -> MessageQueue.publish(new ResumeTransactionalActionEventMessage(
                        actionId, minion.getId())));
    }

    /**
     * Build the Salt call for a state on transactional systems.
     *
     * @param state state to apply
     * @param pillar Salt pillar parameters
     * @return Salt call
     */
    public static LocalCall<Map<String, State.ApplyResult>> getTransactionalSaltCall(
            String state, Optional<Map<String, Object>> pillar) {
        Optional<TransactionalStateDefinition> definition = findDefinitionByState(state);
        return definition.isPresent() ?
                getTransactionalSaltCall(definition.get(), pillar) :
                withDirectCallExecutor(State.apply(List.of(state), pillar));
    }

    /**
     * Add Salt calls for minions, using transactional-update apply for registered transactional states.
     *
     * @param calls destination map
     * @param states states to apply
     * @param pillar Salt pillar parameters
     * @param minionSummaries target minions
     */
    public static void addApplyCalls(
            Map<? super LocalCall<Map<String, State.ApplyResult>>, List<MinionSummary>> calls,
            List<String> states,
            Optional<Map<String, Object>> pillar,
            List<MinionSummary> minionSummaries) {
        Map<Boolean, List<MinionSummary>> minionsByTransactionalUpdate = minionSummaries.stream()
                .collect(partitioningBy(MinionSummary::isTransactionalUpdate));

        addCall(calls, State.apply(states, pillar), minionsByTransactionalUpdate.get(false));

        Optional<TransactionalStateDefinition> definition = findDefinitionByStates(states);
        LocalCall<Map<String, State.ApplyResult>> transactionalCall = definition.isPresent() ?
                getTransactionalSaltCall(definition.get(), pillar) :
                withDirectCallExecutor(State.apply(states, pillar));
        addCall(calls, transactionalCall, minionsByTransactionalUpdate.get(true));
    }

    /**
     * Prepare Salt calls for transactional minions.
     *
     * <p>Transactional states are applied explicitly through {@code transactional_update.*}. Other calls targeting
     * transactional minions use the direct executor so they run against the live system.</p>
     *
     * @param calls Salt calls mapped to target minions
     * @return Salt calls with transactional minions split into direct-call targets when needed
     */
    public static Map<LocalCall<?>, List<MinionSummary>> prepareSaltCallsForTransactionalMinions(
            Map<LocalCall<?>, List<MinionSummary>> calls) {
        Map<LocalCall<?>, List<MinionSummary>> result = new HashMap<>();

        calls.forEach((call, minions) -> {
            Map<Boolean, List<MinionSummary>> minionsByTransactionalUpdate = minions.stream()
                    .collect(partitioningBy(MinionSummary::isTransactionalUpdate));

            addAnyCall(result, call, minionsByTransactionalUpdate.get(false));

            LocalCall<?> transactionalCall = prepareSaltCallForTransactionalMinions(
                    call, minionsByTransactionalUpdate.get(true));
            addAnyCall(result, transactionalCall, minionsByTransactionalUpdate.get(true));
        });

        return result;
    }

    /**
     * Prepare a Salt call for a transactional target.
     *
     * @param call Salt call
     * @param minions target minions
     * @return call using direct executor when at least one target is transactional and the call is not explicit
     * transactional-update
     */
    public static LocalCall<?> prepareSaltCallForTransactionalMinions(
            LocalCall<?> call, List<MinionSummary> minions) {
        if (minions.stream().anyMatch(MinionSummary::isTransactionalUpdate) &&
                shouldExecuteWithDirectCall(call)) {
            return withDirectCallExecutor(call);
        }
        return call;
    }

    /**
     * Add Salt calls for configurable custom states.
     *
     * <p>When {@code java.salt_custom_states_use_transactional_update} is enabled, transactional systems use
     * {@code transactional_update.apply}. Otherwise they use {@code state.apply} through the direct executor.</p>
     *
     * @param calls destination map
     * @param states states to apply
     * @param pillar Salt pillar parameters
     * @param queue optional queue flag
     * @param test optional test flag
     * @param minionSummaries target minions
     */
    public static void addCustomStateApplyCalls(
            Map<? super LocalCall<Map<String, State.ApplyResult>>, List<MinionSummary>> calls,
            List<String> states,
            Optional<Map<String, Object>> pillar,
            Optional<Boolean> queue,
            Optional<Boolean> test,
            List<MinionSummary> minionSummaries) {
        Map<Boolean, List<MinionSummary>> minionsByTransactionalUpdate = minionSummaries.stream()
                .collect(partitioningBy(MinionSummary::isTransactionalUpdate));
        LocalCall<Map<String, State.ApplyResult>> stateApply = State.apply(states, pillar, queue, test);

        if (!shouldUseTransactionalUpdateForCustomStates(states)) {
            addCall(calls, stateApply, minionsByTransactionalUpdate.get(false));
            addCall(calls, withDirectCallExecutor(stateApply), minionsByTransactionalUpdate.get(true));
            return;
        }

        addCall(calls, stateApply, minionsByTransactionalUpdate.get(false));
        addCall(calls, TransactionalUpdateCalls.apply(states, pillar, queue, test),
                minionsByTransactionalUpdate.get(true));
    }

    /**
     * Extract applied states from Salt function arguments.
     *
     * @param functionArgs Salt function arguments
     * @return applied states
     */
    public static Optional<List<String>> getStatesFromFunctionArgs(Object functionArgs) {
        if (!(functionArgs instanceof List<?> args)) {
            return Optional.empty();
        }

        return args.stream()
                .map(TransactionalActionManager::getStatesFromFunctionArg)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    /**
     * Extract applied states from a Salt local call payload.
     *
     * @param call Salt local call
     * @return applied states
     */
    public static Optional<List<String>> getStatesFromCall(LocalCall<?> call) {
        return getStatesFromFunctionArg(call.getPayload().get("kwarg"))
                .or(() -> getStatesFromFunctionArg(call.getPayload().get("arg")));
    }

    /**
     * Check whether a transactional action needs additional Salt states after reboot.
     *
     * @param action action receiving the result
     * @param function Salt function used for the action
     * @param states states applied by Salt
     * @return true when the action needs another Salt state after reboot
     */
    public static boolean needsAdditionalStatesAfterReboot(
            Action action, Optional<Xor<String[], String>> function, Optional<List<String>> states) {
        return getTransactionalDefinition(action, function, states)
                .map(TransactionalActionManager::needsAdditionalStatesAfterReboot)
                .orElse(false);
    }

    /**
     * Build Salt calls that continue this action after reboot.
     *
     * @param action action to continue
     * @param minionSummaries target minions
     * @return after-reboot Salt calls
     */
    public static Optional<Map<LocalCall<?>, List<MinionSummary>>> getAfterRebootSaltCalls(
            Action action, List<MinionSummary> minionSummaries) {
        return getAfterRebootDefinition(action)
                .map(definition -> Map.of(
                        withDirectCallExecutor(State.apply(
                                List.of(definition.afterRebootState().orElseThrow()),
                                Optional.empty(),
                                Optional.of(true),
                                Optional.empty())),
                        minionSummaries));
    }

    /**
     * Check whether an action has an after-reboot Salt state.
     *
     * @param action action to check
     * @return true when an after-reboot state exists
     */
    public static boolean hasAfterRebootState(Action action) {
        return getAfterRebootDefinition(action).isPresent();
    }

    /**
     * Handle a transactional Salt result according to the action flow.
     *
     * @param action action receiving the result
     * @param function Salt function used for the action
     * @param states states applied by Salt
     * @param minionServerId minion server id
     * @param actionId action id
     * @param jsonResult Salt state result
     * @param failed whether the transactional step failed
     * @return result message for the action
     */
    public static Optional<String> handleTransactionalResult(
            Action action,
            Optional<Xor<String[], String>> function,
            Optional<List<String>> states,
            Long minionServerId,
            Long actionId,
            JsonElement jsonResult,
            boolean failed) {
        return getTransactionalDefinition(action, function, states)
                .map(definition -> handleTransactionalResult(
                        definition, minionServerId, actionId, jsonResult, failed));
    }

    /**
     * Check whether the given result belongs to a transactional action.
     *
     * @param action action receiving the result
     * @param function Salt function used for the action
     * @param states states applied by Salt
     * @return true when the result is from a transactional phase
     */
    public static boolean isTransactionalResult(
            Action action, Optional<Xor<String[], String>> function, Optional<List<String>> states) {
        return getTransactionalDefinition(action, function, states).isPresent();
    }

    private static Optional<TransactionalStateDefinition> getTransactionalDefinition(
            Action action, Optional<Xor<String[], String>> function, Optional<List<String>> states) {
        if (!TransactionalUpdateCalls.isApplyFunction(function)) {
            return Optional.empty();
        }

        return states.flatMap(TransactionalActionManager::findDefinitionByStates)
                .or(() -> findDefinitionByAction(action));
    }

    private static boolean needsAdditionalStatesAfterReboot(TransactionalStateDefinition definition) {
        return definition.afterRebootState().isPresent();
    }

    private static Optional<TransactionalStateDefinition> getAfterRebootDefinition(Action action) {
        return findDefinitionByAction(action)
                .filter(TransactionalActionManager::needsAdditionalStatesAfterReboot);
    }

    /**
     * Handle a transactional Salt result according to the state definition.
     *
     * @param definition transactional state definition receiving the result
     * @param minionServerId minion server id
     * @param actionId action id
     * @param jsonResult Salt state result
     * @param failed whether the transactional step failed
     * @return result message for the action
     */
    private static String handleTransactionalResult(
            TransactionalStateDefinition definition,
            Long minionServerId,
            Long actionId,
            JsonElement jsonResult,
            boolean failed) {
        return switch (definition.flow()) {
            case PREREQUISITE_THEN_STATE -> handlePrerequisiteResult(minionServerId, actionId, jsonResult, failed);
            case APPLY_THEN_COMPLETE -> handleApplyResult(minionServerId, actionId, jsonResult, failed);
        };
    }

    /**
     * Handle the result of a transactional prerequisite step.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     * @param jsonResult Salt state result
     * @param failed whether the prerequisite step failed
     * @return result message for the action
     */
    public static String handlePrerequisiteResult(
            Long minionServerId, Long actionId, JsonElement jsonResult, boolean failed) {
        if (failed) {
            recordPrerequisitesFailed(minionServerId, actionId);
            return "Failed to apply prerequisite states.";
        }
        else if (!hasChanges(jsonResult)) {
            recordPrerequisitesApplied(minionServerId, actionId, false);
            MessageQueue.publish(new ResumeTransactionalActionEventMessage(actionId, minionServerId));
            return "Prerequisite states already satisfied. After-reboot state requested.";
        }
        else {
            recordPrerequisitesApplied(minionServerId, actionId, true);
            return "Prerequisite states applied. A system reboot is required before applying the after-reboot state.";
        }
    }

    /**
     * Handle the result of a transactional apply step.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     * @param jsonResult Salt state result
     * @param failed whether the transactional apply step failed
     * @return result message for the action
     */
    public static String handleApplyResult(
            Long minionServerId, Long actionId, JsonElement jsonResult, boolean failed) {
        if (failed) {
            recordTransactionalApplyFailed(minionServerId, actionId);
            return "Failed to apply transactional states.";
        }
        else if (!hasChanges(jsonResult)) {
            recordTransactionalApplyCompleted(minionServerId, actionId, false);
            return "Transactional states already satisfied. No reboot is required.";
        }
        else {
            recordTransactionalApplyCompleted(minionServerId, actionId, true);
            return "Transactional states applied. A system reboot is required to complete the action.";
        }
    }

    /**
     * Record that transactional prerequisites were applied for an action.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     * @param rebootRequired whether reboot is required before continuing
     */
    public static void recordPrerequisitesApplied(Long minionServerId, Long actionId, boolean rebootRequired) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordPrerequisitesApplied(rebootRequired);
    }

    /**
     * Record that transactional prerequisites failed for an action.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordPrerequisitesFailed(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordPrerequisitesFailed();
    }

    /**
     * Record that the after-reboot state was scheduled.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordAfterRebootScheduled(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordAfterRebootScheduled();
    }

    /**
     * Record that scheduling the after-reboot state failed.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordAfterRebootFailed(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordAfterRebootFailed();
    }

    /**
     * Record that transactional states were applied for an action.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     * @param rebootRequired whether reboot is required to complete the action
     */
    public static void recordTransactionalApplyCompleted(Long minionServerId, Long actionId, boolean rebootRequired) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordTransactionalApplyCompleted(rebootRequired);
    }

    /**
     * Record that applying transactional states failed for an action.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordTransactionalApplyFailed(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordTransactionalApplyFailed();
    }

    /**
     * Record that a transactional apply action was completed after reboot.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordTransactionalApplyFinalized(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordTransactionalApplyFinalized();
    }

    private static MinionTransactionalActionHistory lookupOrCreateActionHistory(Long minionServerId, Long actionId) {
        if (minionServerId == null || actionId == null) {
            throw new IllegalArgumentException("minionServerId and actionId are required");
        }

        MinionTransactionalActionHistory history = HibernateFactory.getSession().find(
                MinionTransactionalActionHistory.class,
                new MinionTransactionalActionHistoryId(minionServerId, actionId));
        if (history == null) {
            history = MinionTransactionalActionHistory.create(minionServerId, actionId);
            HibernateFactory.getSession().persist(history);
        }
        return history;
    }

    private static boolean hasChanges(JsonElement jsonResult) {
        Map<String, State.ApplyResult> results = Json.GSON.fromJson(
                jsonResult,
                new TypeToken<Map<String, State.ApplyResult>>() { }.getType());

        return results != null && results.values().stream()
                .map(State.ApplyResult::getChanges)
                .filter(Objects::nonNull)
                .anyMatch(changes -> !changes.isJsonObject() ||
                        changes.getAsJsonObject().size() > 0);
    }

    private static Optional<TransactionalStateDefinition> findDefinitionByState(String state) {
        return TransactionalState.getByState(state)
                .map(TransactionalState::getDefinition);
    }

    private static Optional<TransactionalStateDefinition> findDefinitionByStates(List<String> states) {
        return states.stream()
                .map(TransactionalActionManager::findDefinitionByState)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static boolean shouldUseTransactionalUpdateForCustomStates(List<String> states) {
        return ConfigDefaults.get().isSaltCustomStatesUseTransactionalUpdate() &&
                !states.isEmpty() &&
                CONFIGURABLE_CUSTOM_STATES.containsAll(states);
    }

    private static Optional<TransactionalStateDefinition> findDefinitionByAction(Action action) {
        if (action instanceof HardwareRefreshAction) {
            return Optional.of(TransactionalState.HARDWARE_PROFILE_UPDATE.getDefinition());
        }
        else if (action instanceof PackageRemoveAction) {
            return Optional.of(TransactionalState.PACKAGES_PKGREMOVE.getDefinition());
        }
        else if (action instanceof ErrataAction) {
            return Optional.of(TransactionalState.PACKAGES_PATCHINSTALL.getDefinition());
        }
        else if (action instanceof PackageUpdateAction packageUpdateAction) {
            return Optional.of(packageUpdateAction.getDetails().isEmpty() ?
                    TransactionalState.PACKAGES_PKGUPDATE.getDefinition() :
                    TransactionalState.PACKAGES_PKGINSTALL.getDefinition());
        }

        return Optional.empty();
    }

    private static LocalCall<Map<String, State.ApplyResult>> getTransactionalSaltCall(
            TransactionalStateDefinition definition, Optional<Map<String, Object>> pillar) {
        return TransactionalUpdateCalls.apply(List.of(definition.transactionalState()), pillar);
    }

    private static Optional<List<String>> getStatesFromFunctionArg(Object arg) {
        if (arg instanceof Map<?, ?> argMap) {
            return getStatesFromFunctionArg(argMap.get("mods"));
        }
        else if (arg instanceof String state) {
            return Optional.of(List.of(state));
        }
        else if (arg instanceof Collection<?> states && states.stream().allMatch(String.class::isInstance)) {
            return Optional.of(states.stream()
                    .map(String.class::cast)
                    .toList());
        }
        else if (arg instanceof Object[] args) {
            return getStatesFromFunctionArgs(List.of(args));
        }

        return Optional.empty();
    }

    private static void addCall(
            Map<? super LocalCall<Map<String, State.ApplyResult>>, List<MinionSummary>> calls,
            LocalCall<Map<String, State.ApplyResult>> call,
            List<MinionSummary> minions) {
        if (!minions.isEmpty()) {
            calls.put(call, minions);
        }
    }

    private static void addAnyCall(
            Map<LocalCall<?>, List<MinionSummary>> calls,
            LocalCall<?> call,
            List<MinionSummary> minions) {
        if (!minions.isEmpty()) {
            calls.put(call, minions);
        }
    }

    private static boolean shouldExecuteWithDirectCall(LocalCall<?> call) {
        Map<String, Object> payload = call.getPayload();
        String function = (String) payload.get("fun");
        return function != null &&
                !function.startsWith("transactional_update.") &&
                !payload.containsKey("module_executors");
    }

    private static <T> LocalCall<T> withDirectCallExecutor(LocalCall<T> call) {
        return new LocalCallWithExecutors<>(call, DIRECT_CALL_EXECUTOR, Map.of());
    }

    private static TransactionalStateDefinition prerequisiteThenState(
            String prerequisiteState, String afterRebootState) {
        return new TransactionalStateDefinition(
                TransactionalFlow.PREREQUISITE_THEN_STATE,
                prerequisiteState,
                Optional.of(afterRebootState));
    }

    private static TransactionalStateDefinition applyThenComplete(String state) {
        return new TransactionalStateDefinition(
                TransactionalFlow.APPLY_THEN_COMPLETE,
                state,
                Optional.empty());
    }

    private record TransactionalStateDefinition(
            TransactionalFlow flow,
            String transactionalState,
            Optional<String> afterRebootState) {
    }

    private enum TransactionalState {
        HARDWARE_PROFILE_UPDATE(
                ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE,
                prerequisiteThenState(
                        SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ,
                        ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ),
        PACKAGES_PKGINSTALL(
                SaltParameters.PACKAGES_PKGINSTALL,
                applyThenComplete(SaltParameters.PACKAGES_PKGINSTALL)),
        PACKAGES_PKGUPDATE(
                SaltParameters.PACKAGES_PKGUPDATE,
                applyThenComplete(SaltParameters.PACKAGES_PKGUPDATE)),
        PACKAGES_PKGREMOVE(
                SaltParameters.PACKAGES_PKGREMOVE,
                applyThenComplete(SaltParameters.PACKAGES_PKGREMOVE)),
        PACKAGES_PATCHINSTALL(
                SaltParameters.PACKAGES_PATCHINSTALL,
                applyThenComplete(SaltParameters.PACKAGES_PATCHINSTALL));

        private final List<String> states;
        private final TransactionalStateDefinition definition;

        TransactionalState(String state, TransactionalStateDefinition definitionIn, String... aliases) {
            states = Stream.concat(Stream.of(state), Arrays.stream(aliases)).toList();
            definition = definitionIn;
        }

        private TransactionalStateDefinition getDefinition() {
            return definition;
        }

        private static Optional<TransactionalState> getByState(String state) {
            return Arrays.stream(values())
                    .filter(transactionalState -> transactionalState.states.contains(state))
                    .findFirst();
        }
    }
}
