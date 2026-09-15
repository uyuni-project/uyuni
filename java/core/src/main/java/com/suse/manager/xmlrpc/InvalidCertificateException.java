/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * The provided certificate is not valid
 */
public class InvalidCertificateException extends FaultException {

    /**
     * Constructor
     */
    public InvalidCertificateException() {
        super(12000 , "invalidCertificate" , "Invalid certificate provided");
    }

    /**
     * Constructor
     *
     * @param message exception message
     */
    public InvalidCertificateException(String message) {
        super(12000 , "invalidCertificate" , message);
    }

    /**
     * Constructor
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    public InvalidCertificateException(Throwable cause) {
        super(12000 , "invalidCertificate" , "Invalid certificate provided", cause);
    }
}
