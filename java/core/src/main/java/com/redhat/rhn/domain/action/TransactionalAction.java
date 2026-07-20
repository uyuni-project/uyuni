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

import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contract for actions that need special handling on transactional systems.
 */
public interface TransactionalAction {

    /**
     * @return transactional flow used by this action
     */
    TransactionalFlow getTransactionalFlow();

    /**
     * Get the state that continues this action after reboot.
     *
     * @return state name to apply
     */
    default Optional<String> getAfterRebootState() {
        return Optional.empty();
    }

    /**
     * Build the Salt calls that continue this action after reboot.
     *
     * @param minionSummaries minions for which the action is being resumed
     * @return Salt calls grouped by their target minions
     */
    default Map<LocalCall<?>, List<MinionSummary>> getAfterRebootSaltCalls(
            List<MinionSummary> minionSummaries) {
        return Map.of(
                State.apply(
                        List.of(getAfterRebootState().orElseThrow()),
                        Optional.empty(),
                        Optional.of(true),
                        Optional.empty()),
                minionSummaries);
    }
}
