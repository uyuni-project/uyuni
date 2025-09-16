/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * BaseActionJson represents a schedulable action.
 */
public class ScheduledRequestJson {

    /** The earliest execution date */
    private Optional<LocalDateTime> earliest = Optional.empty();

    /** The action chain to which to add */
    private Optional<String> actionChain = Optional.empty();

    /**
     * @return the earliest
     */
    public Optional<LocalDateTime> getEarliest() {
        return earliest;
    }

    /**
     * @param earliestIn The earliest to set.
     */
    public void setEarliest(Optional<LocalDateTime> earliestIn) {
        earliest = earliestIn;
    }

    /**
     * @return actionChain to get
     */
    public Optional<String> getActionChain() {
        return actionChain;
    }

    /**
     * @param actionChainIn The actionChain to set.
     */
    public void setActionChain(Optional<String> actionChainIn) {
        actionChain = actionChainIn;
    }
}
