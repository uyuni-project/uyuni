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
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressStatus;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistoryId;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.ResumeTransactionalActionEventMessage;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.services.TransactionalUpdateCalls;
import com.suse.manager.webui.utils.salt.LocalCallWithExecutors;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.Set;

/**
 * Manager for multi-step transactional actions.
 */
public class TransactionalActionManager {

    private static final Logger LOG = LogManager.getLogger(TransactionalActionManager.class);

    private static final List<String> DIRECT_CALL_EXECUTOR = List.of("direct_call");

    private static final Set<String> CONFIGURABLE_CUSTOM_STATES = Set.of(
            "custom",
            "custom_groups",
            "custom_org",
            "recurring",
            SaltParameters.REMOTE_COMMANDS);

    private static final Map<String, String> PREREQUISITE_STATE_BY_STATE = Map.of(
            ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE, SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ);

    private static final Set<String> TRANSACTIONAL_STATES = Set.of(
            ApplyStatesEventMessage.CERTIFICATE,
            ApplyStatesEventMessage.CHANNELS,
            ApplyStatesEventMessage.DISTUPGRADE,
            ApplyStatesEventMessage.DISTUPGRADE_SLES16,
            ApplyStatesEventMessage.PACKAGES,
            SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ,
            SaltParameters.PACKAGES_PATCHDOWNLOAD,
            SaltParameters.PACKAGES_PATCHINSTALL,
            SaltParameters.PACKAGES_PKGDOWNLOAD,
            SaltParameters.PACKAGES_PKGINSTALL,
            SaltParameters.PACKAGES_PKGLOCK,
            SaltParameters.PACKAGES_PKGREMOVE,
            SaltParameters.PACKAGES_PKGUPDATE,
            "update-salt",
            "uptodate",
            "util.mgr_switch_to_venv_minion");

    private TransactionalActionManager() {
    }

    /**
     * Find transactional actions waiting for reboot for a minion.
     *
     * @param minionServerId minion server id
     * @param bootTime boot time reported by the minion, in seconds since epoch
     * @return transactional actions waiting for reboot
     */
    private static List<MinionTransactionalActionHistory> findPendingRebootActions(
            Long minionServerId, Optional<Long> bootTime) {
        if (minionServerId == null) {
            return emptyList();
        }

        if (bootTime.isPresent()) {
            Date bootDate = bootTime.map(time -> new Date(time * 1000L)).orElseThrow();
            return HibernateFactory.getSession().createQuery("""
                    FROM MinionTransactionalActionHistory history
                     WHERE history.minionServerId = :minionServerId
                       AND history.rebootRequired = true
                       AND history.rebootStatus = :pendingStatus
                       AND history.afterRebootStatus = :pendingStatus
                       AND history.prerequisiteAt < :bootTime
                    """, MinionTransactionalActionHistory.class)
                    .setParameter("minionServerId", minionServerId)
                    .setParameter("pendingStatus", MinionTransactionalActionHistory.ProgressStatus.PENDING)
                    .setParameter("bootTime", bootDate)
                    .getResultList();
        }

        return HibernateFactory.getSession().createQuery("""
                FROM MinionTransactionalActionHistory history
                 WHERE history.minionServerId = :minionServerId
                   AND history.rebootRequired = true
                   AND history.rebootStatus = :pendingStatus
                   AND history.afterRebootStatus = :pendingStatus
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
     * Get transactional progress entries using the flow declared by the action.
     *
     * @param action action owning the transactional history
     * @param history transactional history
     * @return progress entries in execution order
     */
    public static List<ProgressEntry> getProgressEntries(Action action, MinionTransactionalActionHistory history) {
        boolean transactionalApply = !hasAfterRebootState(action);
        return List.of(
                new ProgressEntry(transactionalApply ? "apply" : "prerequisites",
                        statusKey(history.getPrerequisiteStatus()), history.getPrerequisiteAt(),
                        isTimestamped(history.getPrerequisiteStatus())),
                new ProgressEntry(transactionalApply ? "applyReboot" : "reboot",
                        statusKey(history.getRebootStatus()), history.getRebootAt(),
                        isTimestamped(history.getRebootStatus())),
                new ProgressEntry(transactionalApply ? "finalization" : "execution",
                        statusKey(history.getAfterRebootStatus()), history.getAfterRebootStatusAt(),
                        isTimestamped(history.getAfterRebootStatus()))
        );
    }

    /**
     * Get the Salt result produced by the prerequisite state, when the action has one.
     *
     * @param history transactional history
     * @return prerequisite Salt result
     */
    public static Optional<String> getPrerequisiteResult(MinionTransactionalActionHistory history) {
        return Optional.ofNullable(history.getPrerequisiteResult())
                .filter(result -> !result.isBlank());
    }

    /**
     * Resume transactional actions that were waiting for a real reboot.
     *
     * @param minion minion that reported startup
     * @param bootTime boot time reported by the minion, in seconds since epoch
     */
    public static void resumePendingRebootActionsIfNeeded(MinionServer minion, Optional<Long> bootTime) {
        List<MinionTransactionalActionHistory> pendingActions = findPendingRebootActions(minion.getId(), bootTime);

        if (pendingActions.isEmpty()) {
            return;
        }

        pendingActions.stream()
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
        return getTransactionalStateToApply(state)
                .map(stateToApply -> TransactionalUpdateCalls.apply(List.of(stateToApply), pillar))
                .orElseGet(() -> withDirectCallExecutor(State.apply(List.of(state), pillar)));
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
        if (minionSummaries.isEmpty()) {
            calls.put(State.apply(states, pillar), minionSummaries);
            return;
        }

        Map<Boolean, List<MinionSummary>> minionsByTransactionalUpdate = minionSummaries.stream()
                .collect(partitioningBy(MinionSummary::isTransactionalUpdate));

        addCall(calls, State.apply(states, pillar), minionsByTransactionalUpdate.get(false));

        LocalCall<Map<String, State.ApplyResult>> transactionalCall = findSingleTransactionalStateToApply(states)
                .map(stateToApply -> TransactionalUpdateCalls.apply(List.of(stateToApply), pillar))
                .orElseGet(() -> withDirectCallExecutor(State.apply(states, pillar)));
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
            if (minions.isEmpty()) {
                result.put(call, minions);
                return;
            }

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
            Optional<String> stateToApply = getTransactionalStateToApply(call);
            if (stateToApply.isPresent()) {
                return TransactionalUpdateCalls.apply(List.of(stateToApply.get()),
                        getPillarFromCall(call),
                        getBooleanFromCall(call, "queue"),
                        getBooleanFromCall(call, "test"));
            }
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

        if (states.isEmpty()) {
            addCall(calls, stateApply, minionsByTransactionalUpdate.get(false));
            addCall(calls, TransactionalUpdateCalls.apply(states, pillar, queue, test),
                    minionsByTransactionalUpdate.get(true));
            return;
        }

        if (!shouldUseTransactionalUpdateForCustomStates(states)) {
            Optional<String> stateToApply = findSingleTransactionalStateToApply(states);
            if (stateToApply.isPresent()) {
                addCall(calls, stateApply, minionsByTransactionalUpdate.get(false));
                addCall(calls, TransactionalUpdateCalls.apply(List.of(stateToApply.get()), pillar, queue, test),
                        minionsByTransactionalUpdate.get(true));
                return;
            }

            addCall(calls, stateApply, minionsByTransactionalUpdate.get(false));
            addCall(calls, withDirectCallExecutor(stateApply), minionsByTransactionalUpdate.get(true));
            return;
        }

        addCall(calls, stateApply, minionsByTransactionalUpdate.get(false));
        addCall(calls, TransactionalUpdateCalls.apply(states, pillar, queue, test),
                minionsByTransactionalUpdate.get(true));
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
        return getAfterRebootState(action)
                .map(afterRebootState -> Map.of(
                        withDirectCallExecutor(State.apply(
                                List.of(afterRebootState),
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
        return getAfterRebootState(action).isPresent();
    }

    /**
     * Handle a transactional Salt result.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     * @param jsonResult Salt state result
     * @param failed whether the transactional step failed
     * @return processing result for the action
     */
    public static TransactionalResult handleTransactionalResult(
            Long minionServerId, Long actionId, JsonElement jsonResult, boolean failed) {
        MinionTransactionalActionHistory history = lookupOrCreateActionHistory(minionServerId, actionId);
        Action action = ActionFactory.lookupById(actionId);
        return handleTransactionalResult(history, action, minionServerId, actionId, jsonResult, failed,
                TransactionalActionManager::scheduleSnapshotRefresh);
    }

    static TransactionalResult handleTransactionalResult(
            MinionTransactionalActionHistory history,
            Action action,
            Long minionServerId,
            Long actionId,
            JsonElement jsonResult,
            boolean failed,
            BiFunction<Action, Long, Optional<Long>> snapshotRefreshScheduler) {
        boolean hasAfterRebootState = action != null && hasAfterRebootState(action);
        String formattedResult = formatResult(jsonResult);
        String prerequisiteResult = hasAfterRebootState ? formattedResult : null;
        boolean hasChanges = hasChanges(jsonResult);

        if (failed) {
            history.recordTransactionalApplyFailed(prerequisiteResult);
            if (hasChanges) {
                Optional<Long> refreshActionId = snapshotRefreshScheduler.apply(action, minionServerId);
                if (refreshActionId.isEmpty()) {
                    history.recordSnapshotRefreshFailed();
                    return TransactionalResult.failed(
                            "Failed to apply transactional states. Unable to schedule snapshot refresh.");
                }
                history.recordSnapshotRefreshAction(refreshActionId.get());
            }
            return TransactionalResult.failed(formattedResult == null || formattedResult.isBlank() ?
                    "Failed to apply transactional states." :
                    formattedResult);
        }

        history.recordTransactionalStateApplied(prerequisiteResult);
        if (!hasAfterRebootState && !hasChanges) {
            history.recordSnapshotReconciliation(false, false);
            return TransactionalResult.success("Transactional states applied. No changes reported.");
        }

        Optional<Long> refreshActionId = snapshotRefreshScheduler.apply(action, minionServerId);
        if (refreshActionId.isEmpty()) {
            history.recordSnapshotRefreshFailed();
            return TransactionalResult.failed("Transactional states applied. Unable to schedule snapshot refresh.");
        }
        history.recordSnapshotRefreshAction(refreshActionId.get());
        return TransactionalResult.success("Transactional states applied. Snapshot refresh requested.");
    }

    private static String formatResult(JsonElement jsonResult) {
        return jsonResult == null || jsonResult.isJsonNull() ? null : Json.PRETTY_GSON.toJson(jsonResult);
    }

    private static Optional<String> getAfterRebootState(Action action) {
        return ActionTypeEnum.of(action.getActionType())
                .filter(ActionTypeEnum.TYPE_HARDWARE_REFRESH_LIST::equals)
                .map(type -> ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE);
    }

    private static String statusKey(MinionTransactionalActionHistory.ProgressStatus status) {
        return switch (status) {
            case COMPLETED -> "completed";
            case FAILED -> "failed";
            case NOT_NEEDED -> "notNeeded";
            case PENDING -> "pending";
            case SCHEDULED -> "scheduled";
        };
    }

    private static boolean isTimestamped(MinionTransactionalActionHistory.ProgressStatus status) {
        return switch (status) {
            case COMPLETED, FAILED, SCHEDULED -> true;
            case NOT_NEEDED, PENDING -> false;
        };
    }

    /**
     * Reconcile the transactional action associated with a completed snapshot refresh.
     *
     * @param minionServerId minion server id
     * @param snapshotRefreshActionId completed snapshot refresh action id
     * @param rebootRequired whether snapshot information indicates that reboot is required
     *
     * <p>Action-specific reboot attribution is a best-effort heuristic. Snapshot information can tell whether a
     * reboot is currently pending after a transactional action, but it cannot prove that this specific action caused
     * the pending reboot when the minion already had an unactivated transaction.</p>
     */
    public static void reconcileSnapshotRefreshAction(
            Long minionServerId, Long snapshotRefreshActionId, boolean rebootRequired) {
        findTransactionalActionHistoryBySnapshotRefreshAction(minionServerId, snapshotRefreshActionId)
                .ifPresent(history -> {
                    Action action = ActionFactory.lookupById(history.getActionId());
                    reconcileSnapshotRefreshAction(history, action, rebootRequired,
                            (actionId, serverId) -> MessageQueue.publish(
                                    new ResumeTransactionalActionEventMessage(actionId, serverId)));
                });
    }

    static void reconcileSnapshotRefreshAction(
            MinionTransactionalActionHistory history,
            Action action,
            boolean rebootRequired,
            BiConsumer<Long, Long> resumePublisher) {
        boolean hasAfterRebootState = action != null && hasAfterRebootState(action);

        if (MinionTransactionalActionHistory.ProgressStatus.FAILED.equals(history.getPrerequisiteStatus())) {
            history.recordFailedSnapshotReconciliation(rebootRequired, hasAfterRebootState);
            return;
        }

        history.recordSnapshotReconciliation(rebootRequired, hasAfterRebootState);
        if (!rebootRequired && hasAfterRebootState) {
            resumePublisher.accept(history.getActionId(), history.getMinionServerId());
        }
    }

    /**
     * Find transactional action history associated with a snapshot refresh action.
     *
     * @param minionServerId minion server id
     * @param snapshotRefreshActionId snapshot refresh action id
     * @return transactional action history, if one is associated
     */
    private static Optional<MinionTransactionalActionHistory> findTransactionalActionHistoryBySnapshotRefreshAction(
            Long minionServerId, Long snapshotRefreshActionId) {
        if (minionServerId == null || snapshotRefreshActionId == null) {
            return Optional.empty();
        }

        return HibernateFactory.getSession().createQuery("""
                FROM MinionTransactionalActionHistory history
                 WHERE history.minionServerId = :minionServerId
                   AND history.snapshotRefreshActionId = :snapshotRefreshActionId
                """, MinionTransactionalActionHistory.class)
                .setParameter("minionServerId", minionServerId)
                .setParameter("snapshotRefreshActionId", snapshotRefreshActionId)
                .uniqueResultOptional();
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
                .anyMatch(changes -> changes != null &&
                        (!changes.isJsonObject() || !changes.getAsJsonObject().entrySet().isEmpty()));
    }

    private static Optional<Long> scheduleSnapshotRefresh(Action action, Long minionServerId) {
        Optional<MinionServer> minion = MinionServerFactory.lookupById(minionServerId);
        if (action == null || minion.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(ActionManager.scheduleSnapshotRefreshAction(
                    action.getSchedulerUser(), minion.get(), new Date()).getId());
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule snapshot refresh for transactional action {} on minion {}",
                    action.getId(), minionServerId, e);
            return Optional.empty();
        }
    }

    private static Optional<String> findSingleTransactionalStateToApply(List<String> states) {
        return states.size() == 1 ? getTransactionalStateToApply(states.get(0)) : Optional.empty();
    }

    private static boolean shouldUseTransactionalUpdateForCustomStates(List<String> states) {
        return ConfigDefaults.get().isSaltCustomStatesUseTransactionalUpdate() &&
                isConfigurableCustomState(states);
    }

    private static boolean isConfigurableCustomState(List<String> states) {
        return !states.isEmpty() &&
                CONFIGURABLE_CUSTOM_STATES.containsAll(states);
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

    private static Optional<List<String>> getStatesFromFunctionArgs(Object functionArgs) {
        if (!(functionArgs instanceof List<?> args)) {
            return Optional.empty();
        }

        return args.stream()
                .map(TransactionalActionManager::getStatesFromFunctionArg)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static Optional<List<String>> getStatesFromCall(LocalCall<?> call) {
        return getStatesFromFunctionArg(call.getPayload().get("kwarg"))
                .or(() -> getStatesFromFunctionArg(call.getPayload().get("arg")));
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

    private static Optional<String> getTransactionalStateToApply(LocalCall<?> call) {
        if (!"state.apply".equals(call.getPayload().get("fun"))) {
            return Optional.empty();
        }

        return getStatesFromCall(call).flatMap(TransactionalActionManager::findSingleTransactionalStateToApply);
    }

    private static Optional<String> getTransactionalStateToApply(String state) {
        if (PREREQUISITE_STATE_BY_STATE.containsKey(state)) {
            return Optional.of(PREREQUISITE_STATE_BY_STATE.get(state));
        }
        if (!TRANSACTIONAL_STATES.contains(state)) {
            return Optional.empty();
        }
        return Optional.of(state);
    }

    private static Optional<Map<String, Object>> getPillarFromCall(LocalCall<?> call) {
        Object kwarg = call.getPayload().get("kwarg");
        if (kwarg instanceof Map<?, ?> kwargs && kwargs.get("pillar") instanceof Map<?, ?> pillar) {
            Map<String, Object> result = new HashMap<>();
            pillar.forEach((key, value) -> {
                if (key instanceof String stringKey) {
                    result.put(stringKey, value);
                }
            });
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private static Optional<Boolean> getBooleanFromCall(LocalCall<?> call, String key) {
        Object kwarg = call.getPayload().get("kwarg");
        if (kwarg instanceof Map<?, ?> kwargs && kwargs.get(key) instanceof Boolean value) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private static <T> LocalCall<T> withDirectCallExecutor(LocalCall<T> call) {
        return new LocalCallWithExecutors<>(call, DIRECT_CALL_EXECUTOR, Map.of());
    }

    /**
     * Transactional progress entry for action detail pages.
     */
    public static class ProgressEntry {
        private final String stepKey;
        private final String statusKey;
        private final Date date;
        private final boolean timestamped;

        /**
         * @param stepKeyIn progress step message key suffix
         * @param statusKeyIn progress status message key suffix
         * @param dateIn when the step reached the current status
         * @param timestampedIn whether the status should display a timestamp
         */
        public ProgressEntry(String stepKeyIn, String statusKeyIn, Date dateIn, boolean timestampedIn) {
            stepKey = stepKeyIn;
            statusKey = statusKeyIn;
            date = dateIn;
            timestamped = timestampedIn && dateIn != null;
        }

        public String getStepKey() {
            return stepKey;
        }

        public String getStatusKey() {
            return statusKey;
        }

        public Date getDate() {
            return date;
        }

        public boolean isTimestamped() {
            return timestamped;
        }
    }

    /**
     * Result of processing a transactional Salt result.
     */
    public static class TransactionalResult {
        private final String message;
        private final boolean failed;

        private TransactionalResult(String messageIn, boolean failedIn) {
            message = messageIn;
            failed = failedIn;
        }

        /**
         * @param message result message
         * @return successful transactional result
         */
        public static TransactionalResult success(String message) {
            return new TransactionalResult(message, false);
        }

        /**
         * @param message result message
         * @return failed transactional result
         */
        public static TransactionalResult failed(String message) {
            return new TransactionalResult(message, true);
        }

        /**
         * @return result message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return true if processing failed
         */
        public boolean isFailed() {
            return failed;
        }
    }
}
