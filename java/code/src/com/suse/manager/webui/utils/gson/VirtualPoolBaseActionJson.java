/**
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
import java.util.List;
import java.util.Optional;

/**
 *
 * VirtualPoolBaseAction represents the generic pool action request body structure.
 */
public class VirtualPoolBaseActionJson {
    private List<String> poolNames;
    private LocalDateTime earliest;
    private Optional<String> actionChain = Optional.empty();

    /**
     * @return the names of the pools to action on
     */
    public List<String> getPoolNames() {
        return poolNames;
    }

    /**
     * @param poolNamesIn The poolNames to set.
     */
    public void setPoolNames(List<String> poolNamesIn) {
        poolNames = poolNamesIn;
    }

    /**
     * @return the earliest
     */
    public LocalDateTime getEarliest() {
        return earliest;
    }

    /**
     * @param earliestIn The earliest to set.
     */
    public void setEarliest(LocalDateTime earliestIn) {
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
