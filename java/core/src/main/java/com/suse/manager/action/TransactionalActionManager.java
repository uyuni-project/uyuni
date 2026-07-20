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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.TransactionalAction;
import com.redhat.rhn.domain.action.TransactionalFlow;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressEntry;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistoryId;

import com.suse.manager.reactor.messaging.ResumeTransactionalActionEventMessage;
import com.suse.manager.webui.services.TransactionalUpdateCalls;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manager for multi-step transactional actions.
 */
public class TransactionalActionManager {

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
                   AND history.postStatus = :pendingStatus
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
        if (!(action instanceof TransactionalAction transactionalAction) ||
                !TransactionalFlow.APPLY_THEN_COMPLETE.equals(transactionalAction.getTransactionalFlow())) {
            return false;
        }

        return findTransactionalActionHistory(minionServerId, action.getId())
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
        boolean transactionalApply = action instanceof TransactionalAction transactionalAction &&
                TransactionalFlow.APPLY_THEN_COMPLETE.equals(transactionalAction.getTransactionalFlow());
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
     * Return the transactional action when the given action result is from transactional-update apply.
     *
     * @param action action receiving the result
     * @param function Salt function used for the action
     * @return transactional action when the result is from a transactional phase
     */
    public static Optional<TransactionalAction> getTransactionalAction(
            Action action, Optional<Xor<String[], String>> function) {
        if (action instanceof TransactionalAction transactionalAction &&
                TransactionalUpdateCalls.isApplyFunction(function)) {
            return Optional.of(transactionalAction);
        }
        return Optional.empty();
    }

    /**
     * Check whether a transactional action needs additional Salt states after reboot.
     *
     * @param action action to check
     * @return true when the action needs another Salt state after reboot
     */
    public static boolean needsAdditionalStatesAfterReboot(TransactionalAction action) {
        return action.getAfterRebootState().isPresent();
    }

    /**
     * Return the transactional action when it needs additional Salt states after reboot.
     *
     * @param action action to check
     * @return transactional action with after-reboot state
     */
    public static Optional<TransactionalAction> getAfterRebootAction(Action action) {
        if (action instanceof TransactionalAction transactionalAction &&
                needsAdditionalStatesAfterReboot(transactionalAction)) {
            return Optional.of(transactionalAction);
        }
        return Optional.empty();
    }

    /**
     * Handle a transactional Salt result according to the action flow.
     *
     * @param transactionalAction transactional action receiving the result
     * @param minionServerId minion server id
     * @param actionId action id
     * @param jsonResult Salt state result
     * @param failed whether the transactional step failed
     * @return result message for the action
     */
    public static String handleTransactionalResult(
            TransactionalAction transactionalAction,
            Long minionServerId,
            Long actionId,
            JsonElement jsonResult,
            boolean failed) {
        return switch (transactionalAction.getTransactionalFlow()) {
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
            return "Prerequisite states already satisfied. Action continuation scheduled.";
        }
        else {
            recordPrerequisitesApplied(minionServerId, actionId, true);
            return "Prerequisite states applied. A system reboot is required before continuing the action.";
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
     * Record that the continuation step was scheduled.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordContinuationScheduled(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordContinuationScheduled();
    }

    /**
     * Record that scheduling the continuation step failed.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordContinuationFailed(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordContinuationFailed();
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
}
