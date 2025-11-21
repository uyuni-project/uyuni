/*
 * Copyright (c) 2016 SUSE LLC
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
 * Operation not supported.
 */
public class UnsupportedOperationException extends FaultException {

    /**
     * Constructor
     */
    public UnsupportedOperationException() {
        super(10003, "unsupportedOperation", "unsupported_operation");
    }

    /**
     * Constructor
     *
     * @param message exception message
     */
    public UnsupportedOperationException(String message) {
        super(10003, "unsupportedOperation", message);
    }

    /**
     * Constructor
     *
     * @param cause the cause
     */
    public UnsupportedOperationException(Throwable cause) {
        super(10003, "unsupportedOperation", "unsupported_operation", cause);
    }
}
