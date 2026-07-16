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
import com.redhat.rhn.domain.action.ResumableTransactionalAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistoryId;

import com.suse.manager.reactor.messaging.ResumeTransactionalActionEventMessage;
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
     * Return the resumable transactional action when the given action result is a prerequisite result.
     *
     * @param action action receiving the result
     * @param function Salt function used for the action
     * @return resumable action when the result is from its transactional prerequisite phase
     */
    public static Optional<ResumableTransactionalAction> getTransactionalPrerequisiteAction(
            Action action, Optional<Xor<String[], String>> function) {
        if (action instanceof ResumableTransactionalAction resumableAction &&
                resumableAction.isTransactionalPrerequisiteResult(function)) {
            return Optional.of(resumableAction);
        }
        return Optional.empty();
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
     * Record that the post-prerequisite step was scheduled.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordPostScheduled(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordPostScheduled();
    }

    /**
     * Record that scheduling the post-prerequisite step failed.
     *
     * @param minionServerId minion server id
     * @param actionId action id
     */
    public static void recordPostFailed(Long minionServerId, Long actionId) {
        lookupOrCreateActionHistory(minionServerId, actionId).recordPostFailed();
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
