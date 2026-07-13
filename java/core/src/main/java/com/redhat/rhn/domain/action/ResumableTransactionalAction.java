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

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

/**
 * Contract for transactional actions that continue with another Salt call
 * after their prerequisite phase.
 */
public interface ResumableTransactionalAction {

    /**
     * Determine whether a successful Salt result completes this action.
     *
     * @param serverAction server action receiving the result
     * @param jsonResult Salt result
     * @return true when this is the final result of the action
     */
    boolean isFinalResult(ServerAction serverAction, JsonElement jsonResult);

    /**
     * Build the Salt calls that continue this action after its prerequisite phase.
     *
     * @param minionSummaries minions for which the action is being resumed
     * @return Salt calls grouped by their target minions
     */
    Map<LocalCall<?>, List<MinionSummary>> getPostPrerequisiteSaltCalls(
            List<MinionSummary> minionSummaries);
}
