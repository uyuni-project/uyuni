/**
 * Copyright (c) 2021 SUSE LLC
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
import com.redhat.rhn.domain.server.MinionServer;

/**
 * XMLRPC Fault thrown when minion does not respond
 */
public class MinionNotRespondingFaultException extends FaultException {

    private static final int CODE = 1071;
    private static final String LABEL = "minionNotResponding";

    /**
     * Constructor
     */
    public MinionNotRespondingFaultException() {
        super(CODE, LABEL, "Minion not responding");
    }

    /**
     * Constructor
     * @param minion the affected minion
     */
    public MinionNotRespondingFaultException(MinionServer minion) {
        super(CODE, LABEL,
                String.format("Minion id '%d', minionId '%s' not responding", minion.getId(), minion.getMinionId()));
    }
}
