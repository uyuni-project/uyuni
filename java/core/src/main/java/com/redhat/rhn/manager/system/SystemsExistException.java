/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.manager.system;

import java.util.List;

/**
 * Exception signalizing existence of systems with given IDs.
 */
public class SystemsExistException extends RuntimeException {

    private List<Long> systemIds;

    /**
     * Standard constructor
     * @param systemIdsIn the system ids
     */
    public SystemsExistException(List<Long> systemIdsIn) {
        this.systemIds = systemIdsIn;
    }

    /**
     * Gets the systemIds.
     *
     * @return systemIds
     */
    public List<Long> getSystemIds() {
        return systemIds;
    }

}
