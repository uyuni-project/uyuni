/*
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

/**
 * XMLRPC Fault thrown on salt execution errors
 */
public class SaltFaultException extends FaultException {

    private static final int CODE = 1071;
    private static final String LABEL = "saltFault";

    /**
     * Constructor
     */
    public SaltFaultException() {
        super(CODE, LABEL, "Salt fault");
    }

    /**
     * Constructor
     * @param message the error message
     */
    public SaltFaultException(String message) {
        super(CODE, LABEL, String.format("Salt fault: %s", message));
    }
}
