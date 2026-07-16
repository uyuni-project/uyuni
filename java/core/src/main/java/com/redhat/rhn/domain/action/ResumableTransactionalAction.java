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
package com.redhat.rhn.domain.action;

import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.action.TransactionalActionManager;
import com.suse.manager.webui.services.TransactionalUpdateCalls;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contract for transactional actions that continue with another Salt call
 * after their prerequisite phase.
 */
public interface ResumableTransactionalAction {

    /**
     * Determine whether a Salt result is from the prerequisite phase of this action.
     *
     * @param function Salt function used for the action
     * @return true when the result is from the prerequisite phase
     */
    default boolean isTransactionalPrerequisiteResult(Optional<Xor<String[], String>> function) {
        return TransactionalUpdateCalls.isApplyFunction(function);
    }

    /**
     * Handle a transactional prerequisite result.
     *
     * @param serverAction server action receiving the result
     * @param jsonResult Salt result
     */
    default void handleTransactionalPrerequisiteResult(ServerAction serverAction, JsonElement jsonResult) {
        serverAction.getServer().asMinionServer().ifPresent(minionServer ->
                serverAction.setResultMsg(TransactionalActionManager.handlePrerequisiteResult(
                        minionServer.getId(),
                        serverAction.getParentAction().getId(),
                        jsonResult,
                        serverAction.isStatusFailed())));
    }

    /**
     * Get the state that continues this action after its prerequisite phase.
     *
     * @return state name to apply
     */
    String getPostPrerequisiteState();

    /**
     * Build the Salt calls that continue this action after its prerequisite phase.
     *
     * @param minionSummaries minions for which the action is being resumed
     * @return Salt calls grouped by their target minions
     */
    default Map<LocalCall<?>, List<MinionSummary>> getPostPrerequisiteSaltCalls(
            List<MinionSummary> minionSummaries) {
        return Map.of(
                State.apply(
                        List.of(getPostPrerequisiteState()),
                        Optional.empty(),
                        Optional.of(true),
                        Optional.empty()),
                minionSummaries);
    }
}
