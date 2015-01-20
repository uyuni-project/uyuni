/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.channel;

import com.redhat.rhn.FaultException;

/**
 * InvalidGPGFingerprintException
 * @version $Rev$
 */
public class InvalidGPGFingerprintException extends FaultException {

    /**
     * Constructor
     */
    public InvalidGPGFingerprintException() {
        super(1209, "invalidGpgFingerprint", "The GPG fingerprint " +
                "must be of form 'CA20 8686 2BD6 9DFC 65F6 ECC4 2191 80CD DB42 A60E'");
    }

    /**
     * Constructor
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    public InvalidGPGFingerprintException(Throwable cause) {
        super(1209, "invalidGpgFingerprint", "The GPG fingerprint must be of " +
                "form 'CA20 8686 2BD6 9DFC 65F6 ECC4 2191 80CD DB42 A60E'", cause);
    }
}
