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

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;

import java.util.List;

/**
 * Fault signalizing existence of systems with given IDs.
 */
public class SystemsExistFaultException extends FaultException {

    /**
     * Standard constructor
     *
     * @param systemIds the system ids
     */
    public SystemsExistFaultException(List<Long> systemIds) {
        super(1100, "systemsExist", "Existing system IDs: [" + systemIds.stream()
                .map(id -> id.toString())
                .reduce((id1, id2) -> id1 + "," + id2)
                .orElse("") + "]");
    }

}
