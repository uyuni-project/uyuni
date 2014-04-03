/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.RhnRuntimeException;

/**
 * Represents an error while working with SUSE products.
 */
public class ProductSyncManagerException extends RhnRuntimeException {

    /**
     * Message constructor.
     * @param message exception message
     */
    public ProductSyncManagerException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param message exception message
     * @param cause the cause (which is saved for later retrieval by the
     * Throwable.getCause() method). (A null value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     */
    public ProductSyncManagerException(String message, Throwable cause) {
        super(message, cause);
        // begin member variable initialization
    }
}
