/*
 * Copyright (c) 2022 SUSE LLC
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
 * XML-RPC Fault thrown when generating SSL Certificates
 */
public class SSLCertFaultException extends FaultException {
    private static final int CODE = 10005;
    private static final String LABEL = "sslCertFault";

    /**
     * Constructor
     */
    public SSLCertFaultException() {
        super(CODE, LABEL, "SSL Certificate fault");
    }

    /**
     * Constructor
     * @param message the error message
     */
    public SSLCertFaultException(String message) {
        super(CODE, LABEL, String.format("SSL Certificate fault: %s", message));
    }
}
