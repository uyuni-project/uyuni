/**
 * Copyright (c) 2017 SUSE LLC
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
 * JSON representation of multiple server ids with a list of states to be applied.
 */
public class JSONServerApplyHighstate {

    /** Server id */
    private List<Long> ids;

    /** The earliest execution date */
    private Optional<LocalDateTime> earliest = Optional.empty();

    /** The action chain to which to add */
    private Optional<String> actionChain = Optional.empty();

    /**
     * @return the server id
     */
    public List<Long> getIds() {
        return ids;
    }

    /**
     * @return the date of earliest execution
     */
    public Optional<LocalDateTime> getEarliest() {
        return earliest;
    }

    /**
     * @return the action chain to which to add the action
     */
    public Optional<String> getActionChain() {
        return actionChain;
    }
}
